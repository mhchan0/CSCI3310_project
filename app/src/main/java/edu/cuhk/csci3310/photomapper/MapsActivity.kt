/*
CSCI3310 Project
name : Chan Man Ho, Lee Yan Hin
SID : 1155144075, 1155144079
 */
package edu.cuhk.csci3310.photomapper

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.*
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import edu.cuhk.csci3310.photomapper.databinding.ActivityMapsBinding
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
class MapsActivity : AppCompatActivity(), OnMapReadyCallback, PositionFragment.OnInputListener{

    private var status : Int = 0
    // status = 0 : normal status
    // status = 1 : add location by gps
    // status = 2 : add location manually
    // status = 3 : remove location

    private var camLat : Double = 22.3
    private var camLng : Double = 114.1
    private var camZoom : Float = 11.0f

    private var isFragmentDisplay : Boolean = false
    private var isMessageDisplay : Boolean = false
    private var photoLat : Double = 0.0
    private var photoLng : Double = 0.0
    private var photo : String = ""
    private var title : String = ""

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private lateinit var mFusedLocationProviderClient : FusedLocationProviderClient

    override fun sendData(data: Int) {
        status = when(data) {
            1 -> {
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
                supportActionBar?.title = "PhotoMapper"
                status = 0

                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION), 44)
                0
            }
            2 -> {
                closePhoto()
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.title = "Add location"
                2
            }
            else -> {
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
                supportActionBar?.title = "PhotoMapper"
                status = 0
                0
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        if (savedInstanceState != null) {
            camLat = savedInstanceState.getDouble("camLat")
            camLng = savedInstanceState.getDouble("camLng")
            camZoom = savedInstanceState.getFloat("camZoom")
            status = savedInstanceState.getInt("status")
            if (status == 2) {
                closePhoto()
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.title = "Add location"
            }
            else if (status == 3) {
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
                supportActionBar?.title = "Remove location"
            }
            isFragmentDisplay = savedInstanceState.getBoolean("isFragmentDisplay")
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val cameraPos = LatLng(camLat, camLng)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cameraPos, camZoom))
        mMap.setOnMapClickListener{ point : LatLng ->
            if (status == 2) {
                addLocation(point.latitude, point.longitude)
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
                supportActionBar?.title = "PhotoMapper"
                status = 0
            }
            else if (isFragmentDisplay) {
                closePhoto()
            }
        }

        addLocationsMarker()

        mMap.setOnMarkerClickListener{marker : Marker ->
            if (status == 3) {
                closeMessage()
                val lat = marker.position.latitude
                val lng = marker.position.longitude
                var locationPhoto = ""
                var locationTitle = ""

                val path : File = applicationContext.filesDir
                val dir = File(path, "Data")
                val photoDir = File(path, "Photo")

                if (dir.exists()) {
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
                            var updateLocations = JSONArray()

                            for (i in 0 until locations.length()) {
                                val location : JSONObject = locations.get(i) as JSONObject
                                val locationLat : Double = location.getDouble("Lat")
                                val locationLng : Double = location.getDouble("Lng")
                                val photoPathStr : String = location.getString("photo")

                                if (locationLat == lat && locationLng == lng) {
                                    val photoArray = photoPathStr.split(",")
                                    for (photo in photoArray) {
                                        if (photoDir.exists() && photo != "") {
                                            val photoFile = File(photoDir, photo)
                                            if (photoFile.exists()) {
                                                photoFile.delete()
                                            }
                                        }
                                    }
                                }
                                else {
                                    updateLocations.put(location)
                                }
                            }

                            val fw = FileWriter(file)
                            val bw = BufferedWriter(fw)
                            bw.write(updateLocations.toString())
                            bw.close()
                            fw.close()

                            Toast.makeText(this, "Remove location successfully", Toast.LENGTH_SHORT).show()
                            marker.remove()

                        } catch(e : Exception) {
                            Log.e("MapsActivity", e.message!!)
                        }
                    }
                }

                supportActionBar?.setDisplayHomeAsUpEnabled(false)
                supportActionBar?.title = "PhotoMapper"
                status = 0
            }
            else {
                closeMessage()
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 15.0f))

                val lat = marker.position.latitude
                val lng = marker.position.longitude
                var locationPhoto = ""
                var locationTitle = ""

                photoLat = lat
                photoLng = lng

                val path : File = applicationContext.filesDir
                val dir = File(path, "Data")

                if (dir.exists()) {
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
                            for (i in 0 until locations.length()) {
                                val location : JSONObject = locations.get(i) as JSONObject
                                val locationLat : Double = location.getDouble("Lat")
                                val locationLng : Double = location.getDouble("Lng")

                                if (locationLat == lat && locationLng == lng) {
                                    locationTitle = location.getString("title")
                                    locationPhoto = location.getString("photo")
                                    break
                                }

                            }

                        } catch(e : Exception) {
                            Log.e("MapsActivity", e.message!!)
                        }
                    }
                }

                photo = locationPhoto
                title = locationTitle

                displayPhoto()
            }
            true
        }

        mMap.setOnCameraChangeListener { pos ->
            camLat = pos.target.latitude
            camLng = pos.target.longitude
            camZoom = pos.zoom
        }

    }

    fun showDialog(view: View) {
        val dialogFragment = PositionFragment()
        dialogFragment.show(supportFragmentManager, "positionDialog")
        //openMessage(0)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        closeMessage()
        when(item.itemId) {
            android.R.id.home -> {
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
                supportActionBar?.title = "PhotoMapper"
                status = 0
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        closeMessage()
        if (isFragmentDisplay) {
            closePhoto()
        }
        else if (status == 2 || status == 3) {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            supportActionBar?.title = "PhotoMapper"
            status = 0
        }
        else if (status == 0) {
            this.finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 44) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                getLocation()
            } else {
                Toast.makeText(this, "Please allow location permission", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun getLocation() {
        val mLocationManager : LocationManager= getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val mLocationListener =  LocationListener { location : Location ->
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0f, mLocationListener)

        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            mFusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null).addOnCompleteListener(this) { task ->
                val location : Location? = task.result
                if (location != null) {
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val list : List<Address>? = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    addLocation(list!![0].latitude, list!![0].longitude)
                }
            }

        }
        else {
            Toast.makeText(this, "Please turn on location", Toast.LENGTH_SHORT).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
    }

    private fun addLocation(lat : Double, lng: Double) {
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
                        val point = LatLng(lat, lng)
                        mMap.addMarker(MarkerOptions().position(point))
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15.0f))

                        photoLat = lat
                        photoLng = lng
                        photo = locationPhoto
                        title = locationTitle
                        displayPhoto()

                        return
                    }
                }
                var newLocation = JSONObject()
                newLocation.put("Lat", lat)
                newLocation.put("Lng", lng)
                newLocation.put("photo", "")
                val timestamp: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                newLocation.put("title", timestamp)
                locations.put(length, newLocation)
                val fw = FileWriter(file)
                val bw = BufferedWriter(fw)
                bw.write(locations.toString())
                bw.close()
                fw.close()

                val point = LatLng(lat, lng)
                mMap.addMarker(MarkerOptions().position(point))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15.0f))

                photoLat = lat
                photoLng = lng
                photo = ""
                title = timestamp
                displayPhoto()

            } catch(e : Exception) {
                Log.e("MapsActivity", e.message!!)
            }
        }
        else {
            try {
                val fw = FileWriter(file)
                val bw = BufferedWriter(fw)
                var location = JSONObject()
                location.put("Lat", lat)
                location.put("Lng", lng)
                location.put("photo", "")
                val timestamp: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
                location.put("title", timestamp)
                var locations = JSONArray()
                locations.put(0, location)
                bw.write(locations.toString())
                bw.close()
                fw.close()

                val point = LatLng(lat, lng)
                mMap.addMarker(MarkerOptions().position(point))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15.0f))

                photoLat = lat
                photoLng = lng
                photo = ""
                title = timestamp
                displayPhoto()
            } catch (e : Exception) {
                Log.e("MapsActivity", e.message!!)
            }
        }
    }

    private fun addLocationsMarker() {
        val path : File = applicationContext.filesDir
        val dir = File(path, "Data")

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
                val lat : Double = location.getDouble("Lat")
                val lng : Double = location.getDouble("Lng")

                val point = LatLng(lat, lng)
                mMap.addMarker(MarkerOptions().position(point))
            }

        } catch(e : Exception) {
            Log.e("MapsActivity", e.message!!)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putDouble("camLat", camLat)
        outState.putDouble("camLng", camLng)
        outState.putFloat("camZoom", camZoom)
        outState.putInt("status", status)
        outState.putBoolean("isFragmentDisplay", isFragmentDisplay)
        outState.putBoolean("isMessageDisplay", isMessageDisplay)
        super.onSaveInstanceState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        if (savedInstanceState.getBoolean("isMessageDisplay")){
            if (status == 3){
                openMessage(1)
            }else{
                openMessage(0)
            }

        }
    }

    private fun displayPhoto() {
        closeMessage()
        isFragmentDisplay = true
        val mFragmentManager : FragmentManager = supportFragmentManager
        val existFragment : PhotoFragment? = mFragmentManager.findFragmentById(R.id.photo) as PhotoFragment?

        val mFragment : PhotoFragment = PhotoFragment().newInstance(photoLat, photoLng, photo, title)
        val mFragmentTransaction : FragmentTransaction = mFragmentManager.beginTransaction()

        if (existFragment != null) {
            mFragmentTransaction.replace(R.id.photo, mFragment).commit()
        }
        else {
            mFragmentTransaction.add(R.id.photo, mFragment).commit()
        }

    }

    private fun closePhoto() {
        if (isFragmentDisplay) {
            val mFragmentManager : FragmentManager = supportFragmentManager
            val mFragment : PhotoFragment = mFragmentManager.findFragmentById(R.id.photo) as PhotoFragment
            if (mFragment != null) {
                val mFragmentTransaction : FragmentTransaction = mFragmentManager.beginTransaction()
                mFragmentTransaction.remove(mFragment).commit()
            }
        }

        isFragmentDisplay = false
    }

    fun openMessage(choice : Int) {
        //val mInflater : LayoutInflater = LayoutInflater.from(applicationContext)
        //val view : View = mInflater.inflate(R.layout.message, null)
        val messageView : TextView = findViewById(R.id.message)
        var text : String = if (choice == 0) {
            " Please click on a location to add marker."
        }else{
            " Please click on a marker to remove it."
        }
        if (choice != 0){
            closePhoto()
        }
        messageView.text = text


        messageView.visibility = View.VISIBLE
        isMessageDisplay = true
    }

    private fun closeMessage() {
        //val mInflater : LayoutInflater = LayoutInflater.from(applicationContext)
        //val view : View = mInflater.inflate(R.layout.message, null)
        val messageView : TextView = findViewById(R.id.message)
        messageView.visibility = View.INVISIBLE
        isMessageDisplay = false
    }


    fun activateRemove(view: View) {
        status = 3
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Remove location"
        openMessage(1)
    }

}