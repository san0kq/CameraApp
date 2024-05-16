package com.example.cameraapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var cameraExecutor: ExecutorService

    private lateinit var outputDir: File
    private var imageCapture: ImageCapture? = null


    private fun getOutputDir(): File{
        val mediaDir = externalMediaDirs.firstOrNull()?.absoluteFile.let {
            File(it, resources.getString(R.string.app_name)).apply {
                mkdir()
            }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir
        else filesDir
    }

    companion object{
        private  const val TAG = "CameraX"
        private const val FILE_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PERMISSION_CODE = 10
        private val PERMISSION = arrayOf(Manifest.permission.CAMERA)
    }

    private fun allPermissionGranted() = PERMISSION.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val photoIntent = Intent(this, PhotoActivity::class.java)
        val switchActivity = findViewById<SwitchCompat>(R.id.switch_act)
        switchActivity.setOnClickListener {
            startActivity(photoIntent)
        }


        if (allPermissionGranted()){
            startCamera()
        }else{
            ActivityCompat.requestPermissions(
                this,
                PERMISSION,
                PERMISSION_CODE
            )
        }

        val btnSave = findViewById<Button>(R.id.btn_save)
        btnSave.setOnClickListener {
            takePhoto()
        }

        outputDir = getOutputDir()
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_CODE){
            if (allPermissionGranted()){
                startCamera()
            }else{
                Toast.makeText(this, "Permission error", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(
            Runnable{
                val cameraProvider = cameraProviderFuture.get()
                val pvCamera = findViewById<PreviewView>(R.id.pv_camera)
                val preview = Preview.Builder().build()
                    .also {
                        it.setSurfaceProvider(pvCamera.surfaceProvider)
                    }
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                imageCapture = ImageCapture
                    .Builder()
                    .build()

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                }catch (e: Exception){
                    Log.e(TAG, "Bind error", e)
                }
        },
        ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture?:return

        val photoFile = File(outputDir,
        SimpleDateFormat(FILE_FORMAT, Locale.US)
            .format(System.currentTimeMillis()) + ".jpg")

        val outputOption = ImageCapture.OutputFileOptions
            .Builder(photoFile).build()

        imageCapture.takePicture(
            outputOption,
            ContextCompat.getMainExecutor(baseContext),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val uri = Uri.fromFile(photoFile)
                    val msg = "Photo : $uri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast
                        .makeText(
                            baseContext,
                            "Save error: ${exception.message}",
                            Toast.LENGTH_SHORT).show()
                }

            }
        )
    }
}