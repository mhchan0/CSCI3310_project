/*
CSCI3310 Project
name : Chan Man Ho, Lee Yan Hin
SID : 1155144075, 1155144079
 */
package edu.cuhk.csci3310.photomapper

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class DetailActivity : AppCompatActivity() {

    private var lat : Double = 0.0
    private var lng : Double = 0.0
    private var photoPathStr : String = ""
    private var title : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {
            photoPathStr = intent.getStringExtra("photoPathStr")!!
            lat = intent.getDoubleExtra("lat", 0.0)
            lng = intent.getDoubleExtra("lng", 0.0)
            title = intent.getStringExtra("title")!!
        }
        else {
            photoPathStr = savedInstanceState.getString("photoPathStr")!!
            lat = savedInstanceState.getDouble("lat")
            lng = savedInstanceState.getDouble("lng")
            title = savedInstanceState.getString("title")!!
        }
        val editTitle : EditText = findViewById(R.id.title_write)
        editTitle.setText(title)

        val mRecyclerView : RecyclerView = findViewById(R.id.recyclerview)
        val mAdapter = DetailListAdapter(applicationContext, photoPathStr, lat, lng, this, title)
        mRecyclerView.adapter = mAdapter

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            val mLayoutManager = GridLayoutManager(applicationContext, 2)
            mRecyclerView.layoutManager = mLayoutManager
        }
        else if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val mLayoutManager = GridLayoutManager(applicationContext, 3)
            mRecyclerView.layoutManager = mLayoutManager
        }

    }

    override fun onPause() {
        super.onPause()
        val editTitle : EditText = findViewById(R.id.title_write)
        title = editTitle.text.toString()
        val path : File = applicationContext.filesDir
        val dir = File(path, "Data")
        if (!dir.exists()) {
            dir.mkdir()
        }

        val file = File(dir, "locations.json")
        if (file.exists()) {
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
                val length = locations.length()
                for (i in 0 until length) {
                    val location : JSONObject = locations.get(i) as JSONObject
                    val locationLat : Double = location.getDouble("Lat")
                    val locationLng : Double = location.getDouble("Lng")
                    val locationPhoto : String = location.getString("photo")
                    val locationTitle : String = location.getString("title")

                    if (lat == locationLat && lng == locationLng) {
                        location.put("title", title)
                    }
                }

                val fw = FileWriter(file)
                val bw = BufferedWriter(fw)
                bw.write(locations.toString())
                bw.close()
                fw.close()

            } catch(e : Exception) {
                Log.e("DetailActivity_onClose", e.message!!)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> {
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
                finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 45) {
            if ((grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
                && grantResults[2] == PackageManager.PERMISSION_GRANTED)
                || (grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[3] == PackageManager.PERMISSION_GRANTED)) {
                val dialogFragment = SelectPhotoFragment()
                dialogFragment.show(supportFragmentManager, "selectPhotoDialog")
            } else if ((grantResults[0] != PackageManager.PERMISSION_GRANTED
                && grantResults[1] != PackageManager.PERMISSION_GRANTED
                && grantResults[2] != PackageManager.PERMISSION_GRANTED)
                || (grantResults[0] != PackageManager.PERMISSION_GRANTED
                        && grantResults[3] != PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Please allow camera and gallery permissions", Toast.LENGTH_SHORT).show()
            } else if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Please allow camera permission", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please allow gallery permission", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                val mBitMap : Bitmap = data?.extras!!.get("data") as Bitmap

                savePhoto(mBitMap)
            }
            else if (requestCode == 2) {
                val selectedPhoto : Uri = data?.data as Uri
                val filePath = arrayOf(MediaStore.Images.Media.DATA)
                val mCursor : Cursor = contentResolver.query(selectedPhoto!!, filePath, null, null, null)!!
                mCursor.moveToFirst()
                val index : Int = mCursor.getColumnIndex(filePath[0])
                val photoPath : String = mCursor.getString(index)
                mCursor.close()
                val mBitmap : Bitmap = BitmapFactory.decodeFile(photoPath)

                savePhoto(mBitmap)
            }
        }
    }

    private fun savePhoto(mBitmap : Bitmap) {
        val path : File = applicationContext.filesDir

        val locationDir = File(path, "Data")
        if (!locationDir.exists()) {
            return
        }

        val locationFile = File(locationDir, "locations.json")
        if (!locationFile.exists()) {
            return
        }

        val dir = File(path, "Photo")
        if (!dir.exists()) {
            dir.mkdir()
        }

        try {
            val fr = FileReader(locationFile)
            val br = BufferedReader(fr)
            var str = ""
            var strLine = br.readLine()
            while (strLine != null) {
                str += strLine
                strLine = br.readLine()
            }
            br.close()
            fr.close()

            var updateLocations = JSONArray()

            val locations = JSONArray(str)
            for (i in 0 until locations.length()) {
                val location : JSONObject = locations.get(i) as JSONObject
                val locationLat : Double = location.getDouble("Lat")
                val locationLng : Double = location.getDouble("Lng")
                val locationTitle : String = location.getString("title")

                if (locationLat == lat && locationLng == lng) {
                    var updateLocation = JSONObject()
                    updateLocation.put("Lat", locationLat)
                    updateLocation.put("Lng", locationLng)
                    updateLocation.put("title", locationTitle)

                    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

                    val file = File(dir, "$timeStamp.png")
                    var out : OutputStream = FileOutputStream(file)
                    mBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                    out.close()

                    val locationPhoto : String = location.getString("photo")
                    val updateLocationPhoto = "$locationPhoto$timeStamp.png,"

                    photoPathStr = updateLocationPhoto

                    updateLocation.put("photo", updateLocationPhoto)
                    updateLocations.put(updateLocation)
                }
                else {
                    updateLocations.put(location)
                }
            }

            val fw = FileWriter(locationFile)
            val bw = BufferedWriter(fw)
            bw.write(updateLocations.toString())
            bw.close()
            fw.close()

        } catch (e : Exception) {
            Log.e("DetailActivity", e.message!!)
        }

    }

    override fun onResume() {
        super.onResume()
        val mRecyclerView : RecyclerView = findViewById(R.id.recyclerview)
        val mAdapter = DetailListAdapter(applicationContext, photoPathStr, lat, lng, this, title)
        mRecyclerView.adapter = mAdapter

        if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            val mLayoutManager = GridLayoutManager(applicationContext, 2)
            mRecyclerView.layoutManager = mLayoutManager
        }
        else if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val mLayoutManager = GridLayoutManager(applicationContext, 3)
            mRecyclerView.layoutManager = mLayoutManager
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putDouble("lat", lat)
        outState.putDouble("lng", lng)
        outState.putString("photoPathStr", photoPathStr)
        val editTitle : EditText = findViewById(R.id.title_write)
        outState.putString("title", editTitle.text.toString())
        super.onSaveInstanceState(outState)
    }



}