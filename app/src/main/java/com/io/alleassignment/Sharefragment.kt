package com.io.alleassignment

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.io.alleassignment.databinding.FragmentShareBinding
import java.io.File

class ShareFragment : Fragment() {

    private lateinit var binding: FragmentShareBinding
    private lateinit var imageViewTop: ImageView
    lateinit var imageAdapter: ImageAdapter
    private lateinit var viewModel: ImageViewModel
    private val READ_EXTERNAL_STORAGE_PERMISSION = 1
    private lateinit var uri: Uri


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_share, container, false)
        imageViewTop = binding.imageViewTop
        viewModel = ViewModelProvider(requireActivity())[ImageViewModel::class.java]
        val horizontalLayoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )

        imageAdapter = ImageAdapter(requireContext())

        binding.horizontalRecyclerView.layoutManager = horizontalLayoutManager
        binding.horizontalRecyclerView.adapter = imageAdapter

        if (checkPermission()) {
            loadImageIntoImageView(getAllScreenshots()[0])
            viewModel.setSelectedImage(getAllScreenshots()[0])
            imageAdapter.setData(getAllScreenshots())
        } else {
            requestPermission()
        }

        viewModel.selectedImageUri.observe(viewLifecycleOwner) {
        }
        imageAdapter.setOnItemClickListener { position ->
            val selectedUri = imageAdapter.getItemAtPosition(position)
            loadImageIntoImageView(selectedUri)
            viewModel.setSelectedImage(selectedUri)
        }
        return binding.root
    }


    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
            READ_EXTERNAL_STORAGE_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
            }
        }
    }

    private fun getAllScreenshots(): List<Uri> {
        val picturesDirectory =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val screenshotsDirectory = File(picturesDirectory, "Screenshots")
        return screenshotsDirectory.listFiles { _, name ->
            name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")
        }?.map { Uri.fromFile(it) } ?: emptyList()
    }

    private fun loadImageIntoImageView(uri: Uri) {
        Glide.with(requireContext())
            .load(uri)
            .centerCrop()
            .into(imageViewTop)
        viewModel.setSelectedImage(uri)
    }
}
