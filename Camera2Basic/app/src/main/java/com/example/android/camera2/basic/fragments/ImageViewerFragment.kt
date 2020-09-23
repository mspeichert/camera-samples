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
import com.example.android.camera2.basic.displayDouble
import com.example.android.camera2.basic.getHue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ImageViewerFragment : Fragment() {
    private val navController: NavController by lazy {
        Navigation.findNavController(requireActivity(), R.id.fragment_container)
    }


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_image_viewer, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imgView = view.findViewById<ImageView>(R.id.image_display)
        view.findViewById<Button>(R.id.retake_button).setOnClickListener { navController.navigate(ImageViewerFragmentDirections.actionImageViewerFragmentToSetupFragment()) }

        lifecycleScope.launch(Dispatchers.IO) {
            val bitmap = State.photo
            val colors = State.colors
            val a = State.a
            val b = State.b
            if(bitmap !== null) {
                val matrix = Matrix()
                matrix.postRotate(90.0.toFloat())
                val rotatedBitmap = Bitmap.createBitmap(
                        bitmap, 0,0, bitmap.width, bitmap.height, matrix, false)
                imgView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imgView.adjustViewBounds = true
                imgView.setImageBitmap(rotatedBitmap)
            }

            val res = view.findViewById<TextView>(R.id.result)
            val tv = view.findViewById<TextView>(R.id.result_display)
            val tvrgb = view.findViewById<TextView>(R.id.rgb_display)
            if(colors !== null && a != null && b != null) {
                res.text = "${res.text} (${State.method.name})"
                tvrgb.text = "(R: ${colors.red} G: ${colors.green} B: ${colors.blue})"
                val measured = if(State.method.name == "DNBA") colors.green.toFloat() else getHue(colors)
                tv.text = "${State.a} ${State.method.value.text.replace("measured", displayDouble(measured))} ${State.b} = ${displayDouble(State.method.value.calculate(a,b,colors))}"
            } else {
                tv.text = "Error occured"
                tvrgb.text = "Please try again"
            }
        }
    }





    companion object {
        private val TAG = ImageViewerFragment::class.java.simpleName
    }
}
