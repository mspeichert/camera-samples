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
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.android.camera.utils.GenericListAdapter
import com.example.android.camera2.basic.R
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

        val cameraManager =
                requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager

        val cameraId = findCamera(cameraManager)
        if(cameraId == null) {
            start_button.isEnabled = false
            return
        }
        start_button.setOnClickListener {
            Navigation.findNavController(requireActivity(), R.id.fragment_container)
                .navigate(SetupFragmentDirections.actionSelectorToCamera(
                        cameraId, ImageFormat.JPEG))
        }

//        view.apply {
//
//            val cameraManager =
//                    requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager
//
//            val cameraId = findCamera(cameraManager)
//
//            val button = view.findViewById<Button>(android.R.id.startBtn)
//
//            val layoutId = android.R.layout.simple_list_item_1
////            adapter = GenericListAdapter
//            adapter = GenericListAdapter(cameraList, itemLayoutId = layoutId) { view, item, _ ->
//                view.findViewById<TextView>(android.R.id.text1).text = item.title
//                view.setOnClickListener {
//                    Navigation.findNavController(requireActivity(), R.id.fragment_container)
//                            .navigate(SelectorFragmentDirections.actionSelectorToCamera(
//                                    item.cameraId, item.format))
//                }
//            }
//        }

    }

    companion object {

        /** Helper class used as a data holder for each selectable camera format item */
//        private data class FormatItem(val title: String, val cameraId: String, val format: Int)

        /** Helper function used to convert a lens orientation enum into a human-readable string */
//        private fun lensOrientationString(value: Int) = when(value) {
//            CameraCharacteristics.LENS_FACING_BACK -> "Back"
//            CameraCharacteristics.LENS_FACING_FRONT -> "Front"
//            CameraCharacteristics.LENS_FACING_EXTERNAL -> "External"
//            else -> "Unknown"
//        }

        /** Helper function used to list all compatible cameras and supported pixel formats */
        @SuppressLint("InlinedApi")
        private fun findCamera(cameraManager: CameraManager): String? {
//            var availableCamera: FormatItem? = null

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
            // Iterate over the list of cameras and return all the compatible ones
//            cameraIds.forEach { id ->
//                val characteristics = cameraManager.getCameraCharacteristics(id)
//                if(characteristics.get(CameraCharacteristics.LENS_FACING)!! === CameraCharacteristics.LENS_FACING_BACK) {
//                availableCamera = FormatItem("Back camera", id, ImageFormat.JPEG)
//            }
//                val orientation = lensOrientationString(
//                        characteristics.get(CameraCharacteristics.LENS_FACING)!!)
//
//                // Query the available capabilities and output formats
//                val capabilities = characteristics.get(
//                        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)!!
//                val outputFormats = characteristics.get(
//                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!.outputFormats
//
//                // All cameras *must* support JPEG output so we don't need to check characteristics
//                availableCameras.add(FormatItem(
//                        "$orientation JPEG ($id)", id, ImageFormat.JPEG))

//                // Return cameras that support RAW capability
//                if (capabilities.contains(
//                                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW) &&
//                        outputFormats.contains(ImageFormat.RAW_SENSOR)) {
//                    availableCameras.add(FormatItem(
//                            "$orientation RAW ($id)", id, ImageFormat.RAW_SENSOR))
//                }
//
//                // Return cameras that support JPEG DEPTH capability
//                if (capabilities.contains(
//                            CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT) &&
//                        outputFormats.contains(ImageFormat.DEPTH_JPEG)) {
//                    availableCameras.add(FormatItem(
//                            "$orientation DEPTH ($id)", id, ImageFormat.DEPTH_JPEG))
//                }
//            }

//            return availableCameras
        }
    }
}
