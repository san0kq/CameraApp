package com.example.cameraapp

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.squareup.picasso.Picasso
import java.io.File
import kotlin.reflect.typeOf

class PhotoActivity : AppCompatActivity() {

    companion object{
        private const val PERMISSION_CODE = 10
        private val PERMISSION = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    val imageFolderPath = externalMediaDirs.firstOrNull()?.absoluteFile.let {
        File(it, resources.getString(R.string.app_name)).apply {
            mkdir()
        }
    }

    private fun allPermissionGranted() = PERMISSION.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)




    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_CODE){
            if (allPermissionGranted()){
                showFirstImage()
            }else{
                Toast.makeText(this, "Permission error", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    }

    private fun showFirstImage() {
        if (imageFolderPath.exists() && imageFolderPath.isDirectory) {
            val files = imageFolderPath.listFiles()
            Log.d("print", "$files")


            if (files != null) {
                if (files.isNotEmpty()) {
                    // Получаем первый файл из списка
                    val firstImageFile = files[0]
                    Log.d("print", "$firstImageFile")
                    val imageView = findViewById<ImageView>(R.id.img)


                    // Отображаем изображение в ImageView
                    Picasso.get()
                        .load(firstImageFile)
                        .into(imageView)
                }
            }
        }
    }
}