package com.i.workmanager

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.work.*
import com.bumptech.glide.Glide
import com.i.workmanager.workers.applyBlur
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val workManager = WorkManager.getInstance(application)
        requestPermissionsIfNecessary()
        Glide.with(this).asBitmap().load(BitmapFactory.decodeResource(this.resources, R.drawable.koi_fish)).into(slika)
        zamegliSliko.setOnClickListener {
            applyBlur(workManager, seekBar.progress)
            it.isEnabled = false
            progressBar.visibility = View.VISIBLE
        }
        workManager.getWorkInfosByTagLiveData("OUTPUT")
            .observe(this, Observer { workInfo ->
                if(workInfo.size>0)
                    if (workInfo[0] != null && workInfo[0].state.isFinished) {
                        val outputImageUri = workInfo[0].outputData.getString("KEY_IMAGE_URI")
                        if(!outputImageUri.isNullOrEmpty()){
                            zamegliSliko.isEnabled = true
                            progressBar.visibility = View.INVISIBLE
                            Glide.with(this).load(outputImageUri).into(slika)
                        }
                    }
            })
    }

    private val REQUEST_CODE_IMAGE = 100
    private val REQUEST_CODE_PERMISSIONS = 101

    private val KEY_PERMISSIONS_REQUEST_COUNT = "KEY_PERMISSIONS_REQUEST_COUNT"
    private val MAX_NUMBER_REQUEST_PERMISSIONS = 2

    private val permissions = Arrays.asList(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private var permissionRequestCount: Int = 0

    private fun requestPermissionsIfNecessary() {
        if (!checkAllPermissions()) {
            if (permissionRequestCount < MAX_NUMBER_REQUEST_PERMISSIONS) {
                permissionRequestCount += 1
                ActivityCompat.requestPermissions(
                    this,
                    permissions.toTypedArray(),
                    REQUEST_CODE_PERMISSIONS
                )
            } else {
                Toast.makeText(
                    this,
                    "Za delovanje aplikacije potrebujemo dovoljenje za dostop do shrambe",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /** Permission Checking  */
    private fun checkAllPermissions(): Boolean {
        var hasPermissions = true
        for (permission in permissions) {
            hasPermissions = hasPermissions and isPermissionGranted(permission)
        }
        return hasPermissions
    }

    private fun isPermissionGranted(permission: String) =
        ContextCompat.checkSelfPermission(this, permission) ==
                PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            requestPermissionsIfNecessary() // no-op if permissions are granted already.
        }
    }

}