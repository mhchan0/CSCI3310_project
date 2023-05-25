/*
CSCI3310 Project
name : Chan Man Ho, Lee Yan Hin
SID : 1155144075, 1155144079
 */
package edu.cuhk.csci3310.photomapper

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment

@Suppress("DEPRECATION")
class SelectPhotoFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var rootView : View = inflater.inflate(R.layout.fragment_selectphoto, container, false)

        val cameraButton = rootView.findViewById<Button>(R.id.cameraButton)
        val galleryButton = rootView.findViewById<Button>(R.id.galleryButton)

        cameraButton.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            activity?.startActivityForResult(intent, 1)
            dismiss()
        }

        galleryButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activity?.startActivityForResult(intent, 2)
            dismiss()
        }

        return rootView
    }
}