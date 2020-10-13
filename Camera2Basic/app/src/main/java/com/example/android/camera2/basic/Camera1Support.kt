package com.example.android.camera2.basic

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.Camera
import android.hardware.camera2.CameraCharacteristics
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import androidx.fragment.app.FragmentActivity
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.absoluteValue

class Camera1Support : CameraSupport {

    val camera: Camera? by lazy {
        getCameraInstance()
    }

    private fun getCameraInstance(): Camera? {
        return try {
            Camera.open()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun prepareCamera(activity: FragmentActivity, surface: Surface) {
        camera?.setDisplayOrientation(90)
    }

    override fun applyPreview(holder: SurfaceHolder) {
        if (camera == null) return
        camera?.apply {
            try {
                parameters?.also { params ->
                    params?.set("flash-mode", Camera.Parameters.FLASH_MODE_TORCH)

                    val size = camera?.parameters?.supportedPictureSizes?.reduce { acc, size ->
                        if ((acc.width - 640).absoluteValue < (size.width - 640).absoluteValue) acc
                        else size
                    }
                    if (size != null) {
                        params?.setPictureSize(size.width, size.height)
                        params?.setPreviewSize(size.width, size.height)
                    }
                    params?.set("iso", "ISO${State.method.value.ISO}")
                    parameters = params
                }
                setPreviewDisplay(holder)
                startPreview()
            } catch (e: java.lang.Exception) {
                Log.d("Camera1Support", "Error setting camera preview: ${e.message}")
            }
        }
    }

    override fun reapplyPreview(holder: SurfaceHolder) {
        if (camera == null) return
        camera!!.stopPreview()
        applyPreview(holder)
    }

    override suspend fun capture(): Bitmap? = suspendCoroutine { cnt ->
        if (camera == null) cnt.resume(null)

        val pic2 = object : Camera.PictureCallback {
            override fun onPictureTaken(data: ByteArray?, camera: Camera?) {
                if (data == null) {
                    Log.e("s", "data null")
                }
                val bitmap: Bitmap = BitmapFactory.decodeByteArray(data, 0, data!!.size) // NULL err
                cnt.resume(bitmap)
            }
        }
        val pic = Camera.PictureCallback { data, _ ->
            val bitmap: Bitmap = BitmapFactory.decodeByteArray(data, 0, data.size) // NULL err
            cnt.resume(bitmap)
        }
        camera!!.takePicture(null, null, pic2)
    }

    override fun stop() {
        camera?.release()
    }

    override fun destroy() {}
}