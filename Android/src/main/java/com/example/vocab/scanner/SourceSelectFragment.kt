package com.example.vocab.scanner

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.vocab.R
import com.example.vocab.Tools
import com.example.vocab.databinding.FragmentSourceSelectBinding
import com.example.vocab.mediaUtils.Camera
import kotlinx.android.synthetic.main.activity_scanner.view.*

/**
 * First fragment that is shown when [ScannerActivity] is started
 * User choose between taking photo and choosing it from gallery
 */
class SourceSelectFragment : Fragment() {
    private lateinit var binding: FragmentSourceSelectBinding
    private lateinit var fragmentModel: ScannerModel

    /**
     * Helper function for getting real path of image
     */
    fun getRealPathFromURI(contentUri: Uri): String? {
        var res: String? = null
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor = requireContext().contentResolver.query(contentUri, proj, null, null, null)!!
        if (cursor.moveToFirst()) {
            val column_index =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            res = cursor.getString(column_index)
        }
        cursor.close()
        return res
    }

    private fun takePhoto(){
        Camera.takePicture(requireActivity()){ image, rotation ->
            image?.let {
                fragmentModel.imageBitMap = image
                fragmentModel.rotation = rotation

                if(fragmentModel.process == null) {
                    binding.root.findNavController().navigate(R.id.toSelectMethod)
                }
                else{
                    binding.root.findNavController().navigate(R.id.toSelectWord)
                }
            }
        }
    }

    private fun chooseFromGallery(){
        val galleryFinished = requireActivity().registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
            if(result.resultCode == Activity.RESULT_OK){
                result.data?.data?.let {
                    val image = BitmapFactory.decodeFile(getRealPathFromURI(it))
                    fragmentModel.imageBitMap = image

                    if(fragmentModel.process == null) {
                        binding.root.findNavController().navigate(R.id.toSelectMethod)
                    }
                    else{
                        binding.root.findNavController().navigate(R.id.toSelectWord)
                    }
                }
            }
        }

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryFinished.launch(intent)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSourceSelectBinding.inflate(inflater, container, false)
        val _fragmentModel: ScannerModel by activityViewModels()
        fragmentModel = _fragmentModel

        (activity as AppCompatActivity).supportActionBar?.title = resources.getString(R.string.vocabulary_scanner_choose_source)

        if(Tools.isVertical(resources)){
            binding.layoutSource.orientation = LinearLayout.VERTICAL
        }
        else{
            binding.layoutSource.orientation = LinearLayout.HORIZONTAL
        }

        binding.cameraSource.setOnClickListener {
            takePhoto()
        }

        binding.gallerySource.setOnClickListener {
            chooseFromGallery()
        }

        return binding.root
    }
}