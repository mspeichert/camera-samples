package com.example.android.camera2.basic

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.absoluteValue

class Camera2Support(context: Context) : CameraSupport {

    private val cameraManager: CameraManager by lazy {
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    private val characteristics: CameraCharacteristics by lazy {
        cameraManager.getCameraCharacteristics(State.cameraId ?: "0")
    }

    private val cameraThread = HandlerThread("CameraThread").apply { start() }

    private val cameraHandler = Handler(cameraThread.looper)

    private lateinit var camera: CameraDevice

    private lateinit var session: CameraCaptureSession


    companion object Utils {

    }
    private suspend fun createCaptureSession(
            device: CameraDevice,
            targets: List<Surface>,
            handler: Handler? = null
    ): CameraCaptureSession = suspendCoroutine { cont ->

        device.createCaptureSession(targets, object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) = cont.resume(session)
            override fun onConfigureFailed(session: CameraCaptureSession) {
                val exc = RuntimeException("Camera ${device.id} session configuration failed")
                Log.e("Camera2Support", exc.message, exc)
                cont.resumeWithException(exc)
            }
        }, handler)
    }

    override suspend fun prepareCamera(activity: FragmentActivity, surface: Surface, imageReader: ImageReader): Size {

        val size = characteristics.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
                .getOutputSizes(State.imageFormat).reduce { acc, size ->
                    if ((acc.width - 640).absoluteValue < (size.width - 640).absoluteValue) acc
                    else size
                }

        camera = openCamera(activity, cameraManager, State.cameraId ?: "0", cameraHandler)

        val targets = listOf(surface, imageReader.surface)

        session = createCaptureSession(camera, targets, cameraHandler)

        val captureRequest = camera.createCaptureRequest(
                CameraDevice.TEMPLATE_PREVIEW).apply { addTarget(surface) }

        captureRequest.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
        captureRequest.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
//        captureRequest.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF);
//        captureRequest.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_OFF);
        captureRequest.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
        captureRequest.set(CaptureRequest.LENS_FOCUS_DISTANCE, 0.0f);
        captureRequest.set(CaptureRequest.SENSOR_EXPOSURE_TIME, (234324552 - 9516) / 2);
        captureRequest.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, 0);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            captureRequest.set(CaptureRequest.CONTROL_POST_RAW_SENSITIVITY_BOOST, 0)
        };
        captureRequest.set(CaptureRequest.SENSOR_SENSITIVITY, State.method.value.ISO)

        session.setRepeatingRequest(captureRequest.build(), null, cameraHandler)

        return size
    }

    @SuppressLint("MissingPermission")
    suspend fun openCamera(
            activity: FragmentActivity,
            manager: CameraManager,
            cameraId: String,
            handler: Handler? = null
    ): CameraDevice = suspendCancellableCoroutine { cont ->
        manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(device: CameraDevice) = cont.resume(device)

            override fun onDisconnected(device: CameraDevice) {
                Log.w("Camera2Support", "Camera $cameraId has been disconnected")
                activity.finish()
            }

            override fun onError(device: CameraDevice, error: Int) {
                val msg = when (error) {
                    ERROR_CAMERA_DEVICE -> "Fatal (device)"
                    ERROR_CAMERA_DISABLED -> "Device policy"
                    ERROR_CAMERA_IN_USE -> "Camera in use"
                    ERROR_CAMERA_SERVICE -> "Fatal (service)"
                    ERROR_MAX_CAMERAS_IN_USE -> "Maximum cameras in use"
                    else -> "Unknown"
                }
                val exc = RuntimeException("Camera $cameraId error: ($error) $msg")
                Log.e("Camera2Support", exc.message, exc)
                if (cont.isActive) cont.resumeWithException(exc)
            }
        }, handler)
    }

    override fun capture(imageReader: ImageReader, onCompleted: (ts: Long?) -> Unit) {
        val captureRequest = session.device.createCaptureRequest(
                CameraDevice.TEMPLATE_STILL_CAPTURE).apply { addTarget(imageReader.surface) }
        session.capture(captureRequest.build(), object : CameraCaptureSession.CaptureCallback() {
            override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult) {
                super.onCaptureCompleted(session, request, result)
                val resultTimestamp = result.get(CaptureResult.SENSOR_TIMESTAMP)
                Log.d("Camera2Support", "Capture result received: $resultTimestamp")
                onCompleted(resultTimestamp)
            }
        }, cameraHandler)
    }

    override fun stop() {
        try {
            camera.close()
        } catch (exc: Throwable) {
            Log.e("Camera2Stop", "Error closing camera", exc)
        }
    }

    override fun destroy() {
        cameraThread.quitSafely()
    }
}