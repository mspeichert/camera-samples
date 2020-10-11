package com.example.android.camera2.basic

import android.media.ImageReader
import android.util.Size
import android.view.Surface
import androidx.fragment.app.FragmentActivity

interface CameraSupport {
    suspend fun prepareCamera(activity: FragmentActivity, surface: Surface, imageReader: ImageReader): Size
    fun capture(imageReader: ImageReader, onCompleted: (ts: Long?) -> Unit)
    fun stop()
    fun destroy()
}