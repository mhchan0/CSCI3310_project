/*
CSCI3310 Project
name : Chan Man Ho, Lee Yan Hin
SID : 1155144075, 1155144079
 */
package edu.cuhk.csci3310.photomapper

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import java.io.File

class DetailListAdapter(context : Context, photoPathStr : String, lat : Double, lng : Double, activity: Activity, title : String) :
    Adapter<DetailListAdapter.DetailViewHolder>() {
    private val mInflater : LayoutInflater = LayoutInflater.from(context)
    private val photoPathStr : String = photoPathStr
    private val photoPathList : List<String> = photoPathStr.split(",")
    private val lat : Double = lat
    private val lng : Double = lng
    private val title : String = title
    private val activity : Activity = activity

    class DetailViewHolder(itemView : View, adapter :DetailListAdapter) : RecyclerView.ViewHolder(itemView) {
        val mAdapter : DetailListAdapter = adapter
        val photoView : ImageView = itemView.findViewById<ImageView>(R.id.photo)
        val buttonPhotoView : Button = itemView.findViewById<Button>(R.id.photoButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : DetailViewHolder {
        val mItemView : View = mInflater.inflate(R.layout.photolist_item, parent, false)
        return DetailViewHolder(mItemView, this)
    }

    override fun onBindViewHolder(holder: DetailViewHolder, position: Int) {
        val mPhotoPath : String = photoPathList[position]
        if (mPhotoPath == "") {
            holder.photoView.visibility = View.GONE
            holder.buttonPhotoView.setOnClickListener { view : View ->

                ActivityCompat.requestPermissions(activity, arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.READ_MEDIA_IMAGES), 45)

            }
        }
        else {
            val dir = File(mInflater.context.filesDir, "Photo")
            if (dir.exists()) {
                val file = File(dir, mPhotoPath)
                val mBitmap : Bitmap = BitmapFactory.decodeFile(file.absolutePath)
                holder.photoView.setImageBitmap(mBitmap)
            }

            holder.buttonPhotoView.visibility = View.GONE
        }
    }

    override fun getItemCount() : Int {
        val num : Int = photoPathList.size
        return if (num > 0) {
            num
        } else {
            0
        }
    }

}