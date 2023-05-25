/*
CSCI3310 Project
name : Chan Man Ho, Lee Yan Hin
SID : 1155144075, 1155144079
 */
package edu.cuhk.csci3310.photomapper

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import java.io.File

class PhotoListAdapter(context : Context, photoPathStr : String, lat : Double, lng : Double, title : String) :
    Adapter<PhotoListAdapter.PhotoViewHolder>() {
    private val mInflater : LayoutInflater = LayoutInflater.from(context)
    private val photoPathStr : String = photoPathStr
    private val photoPathList : List<String> = photoPathStr.split(",")
    private val lat : Double = lat
    private val lng : Double = lng
    private val title : String = title

    class PhotoViewHolder(itemView : View, adapter : PhotoListAdapter) : RecyclerView.ViewHolder(itemView) {
        val mAdapter : PhotoListAdapter = adapter
        val photoView : ImageView = itemView.findViewById<ImageView>(R.id.photo)
        val buttonPhotoView : Button = itemView.findViewById<Button>(R.id.photoButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : PhotoViewHolder {
        val mItemView : View = mInflater.inflate(R.layout.photolist_item, parent, false)
        return PhotoViewHolder(mItemView, this)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val mPhotoPath : String = photoPathList[position]
        if (mPhotoPath == "") {
            holder.photoView.visibility = View.GONE
            holder.buttonPhotoView.setOnClickListener { view : View ->
                var intent = Intent(view.context, DetailActivity::class.java)
                intent.putExtra("photoPathStr", photoPathStr)
                intent.putExtra("lat", lat)
                intent.putExtra("lng", lng)
                intent.putExtra("title", title)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                view.context.startActivity(intent)
            }
        }
        else {
            holder.buttonPhotoView.visibility = View.GONE

            val dir = File(mInflater.context.filesDir, "Photo")
            if (dir.exists()) {
                val file = File(dir, mPhotoPath)
                val mBitmap : Bitmap = BitmapFactory.decodeFile(file.absolutePath)
                holder.photoView.setImageBitmap(mBitmap)
            }

            holder.photoView.setOnClickListener { view : View ->
                var intent = Intent(view.context, DetailActivity::class.java)
                intent.putExtra("photoPathStr", photoPathStr)
                intent.putExtra("lat", lat)
                intent.putExtra("lng", lng)
                intent.putExtra("title", title)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                view.context.startActivity(intent)
            }
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