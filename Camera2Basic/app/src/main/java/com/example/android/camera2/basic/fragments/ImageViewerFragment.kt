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

import android.graphics.Bitmap
import android.graphics.Matrix
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.example.android.camera2.basic.R
import com.example.android.camera2.basic.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ImageViewerFragment : Fragment() {

    /** Default Bitmap decoding options */
//    private val bitmapOptions = BitmapFactory.Options().apply {
//        inJustDecodeBounds = false
//        // Keep Bitmaps at less than 1 MP
//        if (max(outHeight, outWidth) > DOWNSAMPLE_SIZE) {
//            val scaleFactorX = outWidth / DOWNSAMPLE_SIZE + 1
//            val scaleFactorY = outHeight / DOWNSAMPLE_SIZE + 1
//            inSampleSize = max(scaleFactorX, scaleFactorY)
//        }
//    }

    /** Bitmap transformation derived from passed arguments */
//    private val bitmapTransformation: Matrix by lazy { decodeExifOrientation(ORIENTATION_NORMAL) }

    /** Flag indicating that there is depth data available for this image */
//    private val isDepth: Boolean by lazy { args.depth }
//
//    /** Data backing our Bitmap viewpager */
//    private val bitmapList: MutableList<Bitmap> = mutableListOf()
//
//    private fun imageViewFactory() = ImageView(requireContext()).apply {
//        layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
//    }
    private val navController: NavController by lazy {
        Navigation.findNavController(requireActivity(), R.id.fragment_container)
    }


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_image_viewer, container, false)
//    ): View? = ViewPager2(requireContext()).apply {
//        // Populate the ViewPager and implement a cache of two media items
//        offscreenPageLimit = 2
//        adapter = GenericListAdapter(
//                bitmapList,
//                itemViewFactory = { imageViewFactory() }) { view, item, _ ->
//            view as ImageView
//            Glide.with(view).load(item).into(view)
//        }
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imgView = view.findViewById<ImageView>(R.id.image_display)
//        view as ViewPager2
        view.findViewById<Button>(R.id.retake_button).setOnClickListener { navController.navigate(ImageViewerFragmentDirections.actionImageViewerFragmentToSetupFragment()) }

        lifecycleScope.launch(Dispatchers.IO) {
            val bitmap = State.photo
            val colors = State.colors
            if(bitmap !== null) {
                val matrix = Matrix()
                matrix.postRotate(90.0.toFloat())
                val rotatedBitmap = Bitmap.createBitmap(
                        bitmap, 0,0, bitmap.width, bitmap.height, matrix, false)
                imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imgView.adjustViewBounds = true
                imgView.setImageBitmap(rotatedBitmap)
            }

            val tv = view.findViewById<TextView>(R.id.rgb_desc)
            if(colors !== null) {
                tv.text = "R:${colors.red} G:${colors.green} B:${colors.blue}"
            } else {
                tv.text = "Err occured"
            }
            // Load input image file
//            val inputBuffer = loadInputBuffer()
//
//            // Load the main JPEG image
//            addItemToViewPager(view, decodeBitmap(inputBuffer, 0, inputBuffer.size))
//
//            // If we have depth data attached, attempt to load it
//            if (isDepth) {
//                try {
//                    val depthStart = findNextJpegEndMarker(inputBuffer, 2)
//                    addItemToViewPager(view, decodeBitmap(
//                            inputBuffer, depthStart, inputBuffer.size - depthStart))
//
//                    val confidenceStart = findNextJpegEndMarker(inputBuffer, depthStart)
//                    addItemToViewPager(view, decodeBitmap(
//                            inputBuffer, confidenceStart, inputBuffer.size - confidenceStart))
//
//                } catch (exc: RuntimeException) {
//                    Log.e(TAG, "Invalid start marker for depth or confidence data")
//                }
//            }
        }
    }





    companion object {
        private val TAG = ImageViewerFragment::class.java.simpleName
    }
}
