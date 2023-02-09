package com.example.vocab.mediaUtils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import com.example.vocab.databinding.ActivityCameraBinding
import com.example.vocab.teacherHome.TeacherActivity
import com.example.vocab.thirdParty.Translator
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Class for handling activity for taking photos
 */
class Camera: AppCompatActivity(){
    private lateinit var binding: ActivityCameraBinding
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraExecutor: ExecutorService

    private var rotationOfImage: Int? = null

    companion object {
        /**
         * Launches camera [activity] and pass taken photo via callback [onResult]
         */
        fun takePicture(activity: FragmentActivity, onResult: (Bitmap?, Int?) -> Unit) {
            val cameraFinished = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
                if(result.resultCode == Activity.RESULT_OK) {
                    val image = BitmapFactory.decodeFile(activity.filesDir.absolutePath + "/toRecognize.jpg")
                    val rotationOfImage = result.data?.getBundleExtra("rotation")?.get("rotation") as Int
                    onResult(image, rotationOfImage)
                }
                else{
                    onResult(null, null)
                }
            }

            val intent = Intent(activity, Camera::class.java)
            cameraFinished.launch(intent)
        }
    }

    /**
     * Determines rotation of taken picture
     */
    private class RotationOfImage(private val listener: (Int) -> Unit) : ImageAnalysis.Analyzer {
        @SuppressLint("UnsafeExperimentalUsageError")
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                listener(imageProxy.imageInfo.rotationDegrees)
            }
        }
    }


    /**
     * Takes photo and saves it to application directory, and passes information about image's rotation
     * via activity result
     */
    private fun takePhoto() {
        val photoFile = File(
            filesDir,
            "toRecognize.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val data = Intent()
                    data.putExtra("rotation", bundleOf("rotation" to rotationOfImage))
                    setResult(Activity.RESULT_OK, data)
                    finish()
                }
            }
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val viewFinder = binding.viewFinder

        cameraExecutor = Executors.newSingleThreadExecutor()

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.createSurfaceProvider())
                }

            imageCapture = ImageCapture.Builder().build()

            val imageAnalyzer = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, RotationOfImage { rotation ->
                        rotationOfImage = rotation
                    })
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()

                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture, imageAnalyzer)

            } catch(exc: Exception) {
                println("error")
            }

        }, ContextCompat.getMainExecutor(this))

        binding.cameraCaptureButton.setOnClickListener {
            takePhoto()
        }

        binding.cameraBackButton.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

}