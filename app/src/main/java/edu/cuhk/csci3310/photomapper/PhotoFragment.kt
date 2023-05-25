/*
CSCI3310 Project
name : Chan Man Ho, Lee Yan Hin
SID : 1155144075, 1155144079
 */
package edu.cuhk.csci3310.photomapper

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class PhotoFragment : Fragment() {

    private var lat : Double? = 0.0
    private var lng : Double? = 0.0
    private var photo : String? = ""
    private var title : String? = ""
    private lateinit var titleView : TextView

    fun newInstance(lat : Double, lng : Double, photo : String, title : String) : PhotoFragment {
        val fragment = PhotoFragment()
        var args = Bundle()
        args.putDouble("lat", lat)
        args.putDouble("lng", lng)
        args.putString("photo", photo)
        args.putString("title", title)
        fragment.arguments = args
        return fragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            lat = arguments?.getDouble("lat")
            lng = arguments?.getDouble("lng")
            photo = arguments?.getString("photo")
            title = arguments?.getString("title")
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        updateLocation()

        val view : View = inflater.inflate(R.layout.fragment_photo, container, false)

        titleView = view.findViewById(R.id.text_title)
        titleView.text = title
        view.setOnClickListener{ view : View ->
            var intent = Intent(view.context, DetailActivity::class.java)
            intent.putExtra("photoPathStr", photo)
            intent.putExtra("lat", lat)
            intent.putExtra("lng", lng)
            intent.putExtra("title", title)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            view.context.startActivity(intent)
        }

        val mRecyclerView : RecyclerView = view.findViewById(R.id.recyclerview_photo)
        val mAdapter = PhotoListAdapter(activity?.applicationContext!!, photo!!, lat!!, lng!!, title!!)
        mRecyclerView.adapter = mAdapter

        val mLayoutManager = LinearLayoutManager(activity?.applicationContext!!, LinearLayoutManager.HORIZONTAL, false)
        mRecyclerView.layoutManager = mLayoutManager

        return view
    }

    override fun onResume() {
        super.onResume()

        updateLocation()

        titleView.text = title
        val mRecyclerView : RecyclerView = view?.findViewById(R.id.recyclerview_photo)!!
        val mAdapter = PhotoListAdapter(activity?.applicationContext!!, photo!!, lat!!, lng!!, title!!)
        mRecyclerView.adapter = mAdapter

        val mLayoutManager = LinearLayoutManager(activity?.applicationContext!!, LinearLayoutManager.HORIZONTAL, false)
        mRecyclerView.layoutManager = mLayoutManager
    }

    fun updateLocation() {
        val dir = File(activity?.filesDir, "Data")
        if (!dir.exists()) {
            return
        }

        val file = File(dir, "locations.json")
        if (!file.exists()) {
            return
        }

        try {
            val fr = FileReader(file)
            val br = BufferedReader(fr)
            var str = ""
            var strLine = br.readLine()
            while (strLine != null) {
                str += strLine
                strLine = br.readLine()
            }
            br.close()
            fr.close()

            val locations = JSONArray(str)

            for (i in 0 until locations.length()) {
                val location : JSONObject = locations.get(i) as JSONObject
                val locationLat : Double = location.getDouble("Lat")
                val locationLng : Double = location.getDouble("Lng")

                if ((lat == locationLat) && (lng == locationLng)) {
                    title = location.getString("title")
                    photo = location.getString("photo")
                }
            }

        } catch (e : Exception) {
            Log.d("PhotoFragment", e.message!!)
        }

    }

}