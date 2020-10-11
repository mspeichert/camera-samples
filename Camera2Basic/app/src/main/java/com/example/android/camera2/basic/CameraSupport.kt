package com.example.android.camera2.basic

import android.graphics.Bitmap
import android.view.Surface
import android.view.SurfaceHolder
import androidx.fragment.app.FragmentActivity

interface CameraSupport {
    suspend fun prepareCamera(activity: FragmentActivity, surface: Surface)
    suspend fun capture(): Bitmap?
    fun applyPreview(holder: SurfaceHolder)
    fun reapplyPreview(holder: SurfaceHolder)
    fun stop()
    fun destroy()
}