/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.camera2.basic.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.media.ImageReader
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.android.camera2.basic.*
import kotlinx.android.synthetic.main.fragment_camera.*
import kotlinx.coroutines.*
import java.lang.Runnable
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeoutException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class CameraFragment : Fragment() {

    private val cameraSupport: CameraSupport by lazy {
        Camera2Support(requireContext().applicationContext)
    }

    private val navController: NavController by lazy {
        Navigation.findNavController(requireActivity(), R.id.fragment_container)
    }

    private lateinit var imageReader: ImageReader

    private val imageReaderThread = HandlerThread("imageReaderThread").apply { start() }

    private val imageReaderHandler = Handler(imageReaderThread.looper)

    private lateinit var viewFinder: SurfaceView

    private lateinit var overlay: View

    private lateinit var timer: CountDownTimer

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_camera, container, false)

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        overlay = view.findViewById(R.id.overlay)
        viewFinder = view.findViewById(R.id.view_finder)
        viewFinder.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceDestroyed(holder: SurfaceHolder) = Unit

            override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    height: Int) = Unit

            override fun surfaceCreated(holder: SurfaceHolder) {

//                val previewSize = getPreviewOutputSize(
//                        viewFinder.display, characteristics, SurfaceHolder::class.java)
                Log.d(TAG, "View finder size: ${viewFinder.width} x ${viewFinder.height}")
                view.post { initializeCamera() }
            }
        })
    }

    private fun initializeCamera() = lifecycleScope.launch(Dispatchers.Main) {
        if(State.cameraId == null) return@launch this.cancel()

        val size = cameraSupport.prepareCamera(requireActivity(), viewFinder.holder.surface, imageReader)

        imageReader = ImageReader.newInstance(size.width, size.height, State.imageFormat, IMAGE_BUFFER_SIZE)

        timer = object : CountDownTimer(5000, 100) {
            override fun onTick(millisUntilFinished: Long) {
                timer_text.setText(displayDouble(millisUntilFinished / 1000.toDouble()))
            }

            override fun onFinish() {
                var isProcessing = true
                var count = 0
                lifecycleScope.launch(Dispatchers.Main) {
                    while(isProcessing) {
                        if(count > 2) count = 0
                        else count++
                        timer_text.textSize = 36.toFloat()
                        timer_text.setText("Processing" + ".".repeat(count % 4))
                        delay(800)
                    }
                }

                lifecycleScope.launch(Dispatchers.IO) {
                    State.colors = Calculations.getRGB(takePhoto())
                    isProcessing = false
                    lifecycleScope.launch(Dispatchers.Main) {
                        navController.navigate(CameraFragmentDirections.actionCameraToJpegViewer())
                    }
                }
            }
        }
        timer.start()
    }

    private suspend fun takePhoto(): Bitmap = suspendCoroutine { cont ->

        @Suppress("ControlFlowWithEmptyBody")
        while (imageReader.acquireNextImage() != null) {}

        val imageQueue = ArrayBlockingQueue<Image>(IMAGE_BUFFER_SIZE)
        imageReader.setOnImageAvailableListener({ reader ->
            imageQueue.add(reader.acquireNextImage())
        }, imageReaderHandler)

        cameraSupport.capture(imageReader) {

            val exc = TimeoutException("Image dequeuing took too long")
            val timeoutRunnable = Runnable { cont.resumeWithException(exc) }
            imageReaderHandler.postDelayed(timeoutRunnable, IMAGE_CAPTURE_TIMEOUT_MILLIS)

            @Suppress("BlockingMethodInNonBlockingContext")
            lifecycleScope.launch(cont.context) {
                while (true) {
                    val image = imageQueue.take()
                    // Dequeue images while timestamps don't match
                    if (image.timestamp != it) continue

                    val planes = image.planes
                    val bs = planes[0].buffer.capacity()
                    val ba = ByteArray(bs)
                    planes[0].buffer.get(ba)

                    val bitmap: Bitmap = BitmapFactory.decodeByteArray(ba, 0, ba.size) // NULL err

                    imageReader.setOnImageAvailableListener(null, null)
                    imageReaderHandler.removeCallbacks(timeoutRunnable)

                    cont.resume(bitmap)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        cameraSupport.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraSupport.destroy()
        imageReaderThread.quitSafely()
    }

    companion object {
        private val TAG = CameraFragment::class.java.simpleName

        private const val IMAGE_BUFFER_SIZE: Int = 3

        private const val IMAGE_CAPTURE_TIMEOUT_MILLIS: Long = 5000
    }
}
