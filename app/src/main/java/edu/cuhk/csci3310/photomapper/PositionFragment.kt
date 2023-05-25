/*
CSCI3310 Project
name : Chan Man Ho, Lee Yan Hin
SID : 1155144075, 1155144079
 */
package edu.cuhk.csci3310.photomapper

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment

class PositionFragment : DialogFragment() {

    interface  OnInputListener {
        fun sendData(data : Int)
    }

    lateinit var mOnInputListener: OnInputListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var rootView : View = inflater.inflate(R.layout.fragment_position, container, false)

        val gpsButton = rootView.findViewById<Button>(R.id.GPSButton)
        val manualButton = rootView.findViewById<Button>(R.id.manualButton)

        gpsButton.setOnClickListener {
            mOnInputListener.sendData(1)
            dismiss()
        }

        manualButton.setOnClickListener {
            mOnInputListener.sendData(2)
            (activity as MapsActivity).openMessage(0)
            dismiss()
        }

        return rootView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mOnInputListener = activity as OnInputListener
        } catch (e : Exception) {
            Log.e("PositionFragment", e.message!!)
        }
    }

}