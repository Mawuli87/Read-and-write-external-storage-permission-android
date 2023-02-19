package com.yawomessie.myapplication

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File


class MainActivity : AppCompatActivity() {
    private lateinit var folderName: EditText
    private lateinit var createFolderBtn : Button

    private companion object {
        private const val STORAGE_PERMISSION_CODE = 100
        private const val TAG = "PERMISSION_TAG"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //init views
        folderName = findViewById(R.id.textEdtFolderName)
        createFolderBtn = findViewById(R.id.btnCreateFolder)

        createFolderBtn.setOnClickListener {
            if (checkPermission()){
                createFolder()
            }else {
                requestPermission()
            }
        }
    }

    private fun createFolder(){
        val folderName = folderName.text.toString().trim()
        val file = File(Environment.getExternalStorageDirectory().toString()+"/"+folderName)
        val folderCreated = file.mkdir()

        if (folderCreated){
           toast("Folder created ${file.absolutePath}")
        }else {
            toast("Folder not created")
        }
    }

    private fun requestPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            //android is 11 or above
           try {
               val intent = Intent()
               intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
               val uri = Uri.fromParts("package",this.packageName,null)
               intent.data = uri
               storageActivityResultLauncher.launch(intent)
           }catch (e:Exception){
               Log.e("TAG","RequestPermission")
               val intent = Intent()
              intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
               storageActivityResultLauncher.launch(intent)
           }
        }else {
            //android is below 10
            ActivityCompat.requestPermissions(this,
            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE)
        }
    }

    private val storageActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                 if (Environment.isExternalStorageManager()){
                     createFolder()
                 }else {
                     //manage external storage is denied
                     toast("Manage external storage permission denied")
                 }
            }else {
               //android is below 11
            }
        }


    private fun checkPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            //android 11 (R) or above
            Environment.isExternalStorageManager()

        }else {
       val write = ContextCompat.checkSelfPermission(this@MainActivity,android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            val read = ContextCompat.checkSelfPermission(this@MainActivity,android.Manifest.permission.READ_EXTERNAL_STORAGE)
            write == PackageManager.PERMISSION_GRANTED && read == PackageManager.PERMISSION_DENIED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty()){
                val write = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val read = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (write && read){
                   createFolder()
                }else {
                    toast("You should allow permission to be able to stream ")
                }
            }
        }
    }


    private fun toast(message:String){
        Toast.makeText(this,message,Toast.LENGTH_LONG).show()
    }
}