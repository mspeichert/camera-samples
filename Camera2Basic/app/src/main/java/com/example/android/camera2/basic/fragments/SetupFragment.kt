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
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.navigation.Navigation
import com.example.android.camera2.basic.CMethod
import com.example.android.camera2.basic.KeyboardUtils
import com.example.android.camera2.basic.R
import com.example.android.camera2.basic.State
import com.example.android.camera2.basic.State.colors
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_setup.*

class SetupFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_setup, container, false)

    @SuppressLint("MissingPermission")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val tabs = view.findViewById<TabLayout>(R.id.tabs)
        val inputA = view.findViewById<EditText>(R.id.a)
        if(State.a != null) inputA.setText(State.a.toString())
        val inputB = view.findViewById<EditText>(R.id.b)
        if(State.b != null) inputB.setText(State.b.toString())
        val equation = view.findViewById<TextView>(R.id.equation)
        val iso = view.findViewById<TextView>(R.id.iso)
        equation.text = State.method.value.text
        iso.text = "ISO: ${State.method.value.ISO}"
        tabs.tabRippleColor = null


        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if(tab == null) return
                if(tab.text == getString(R.string.dnba)) State.method = CMethod.DNBA
                if(tab.text == getString(R.string.jaffe)) State.method = CMethod.Jaffe

                equation.text = State.method.value.text
                iso.text = "ISO: ${State.method.value.ISO}"
            }
            override fun onTabReselected(tab: TabLayout.Tab?)= Unit
            override fun onTabUnselected(tab: TabLayout.Tab?)= Unit
        })

        inputA.addTextChangedListener(object:TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                State.a = s.toString().toDouble()
                inputA.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#000000"))
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        inputB.addTextChangedListener(object:TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                State.b = s.toString().toDouble()
                inputB.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#000000"))
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        val cameraManager =
                requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager

        val cameraId = findCamera(cameraManager)
        if(cameraId == null) {
            start_button.isEnabled = false
            return
        }
        start_button.setOnClickListener {
            if(State.a == null) inputA.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FF0000"))
            if(State.b == null) inputB.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FF0000"))
            KeyboardUtils.hideKeyboardFrom(context, view)
            if(State.a == null || State.b == null) return@setOnClickListener
            Navigation.findNavController(requireActivity(), R.id.fragment_container)
                .navigate(SetupFragmentDirections.actionSelectorToCamera(
                        cameraId, ImageFormat.JPEG))
        }


    }

    companion object {
        /** Helper function used to list all compatible cameras and supported pixel formats */
        @SuppressLint("InlinedApi")
        private fun findCamera(cameraManager: CameraManager): String? {

            // Get list of all compatible cameras
            val cameraIds = cameraManager.cameraIdList.filter {
                val characteristics = cameraManager.getCameraCharacteristics(it)
                val capabilities = characteristics.get(
                        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
                capabilities?.contains(
                        CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE) ?: false
            }

            val backCamera = cameraIds.find { id ->
                cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING)!! == CameraCharacteristics.LENS_FACING_BACK
            }
            return backCamera
        }
    }
}
