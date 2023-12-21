package com.io.alleassignment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import com.google.mlkit.vision.text.TextRecognition
import com.io.alleassignment.databinding.FragmentInfoBinding
import com.google.mlkit.vision.common.InputImage

import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.InputStream


class InfoFragment : Fragment() {

    private lateinit var binding: FragmentInfoBinding
    private lateinit var viewModel: ImageViewModel
    private lateinit var uri: Uri

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_info, container, false)
        val rootView = binding.root
        viewModel = ViewModelProvider(requireActivity())[ImageViewModel::class.java]
        viewModel.selectedImageUri.observe(viewLifecycleOwner) {
            uri = it
        }

        setDescription()

        return rootView
    }


    private fun setDescription() {
        uri = viewModel.selectedImageUri.value!!
        val inputStream: InputStream? = requireActivity().contentResolver.openInputStream(uri)
        val recognizerOptions = TextRecognizerOptions.Builder().build()

        val recognizer = TextRecognition.getClient(recognizerOptions)
        val selectedBitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
        val image: InputImage = InputImage.fromBitmap(selectedBitmap, 0)
        recognizer.process(image).addOnSuccessListener { textResults ->
                val extractedText: String = textResults.text
                binding.descriptionTextView.text = extractedText
            }.addOnFailureListener { e ->
                e.printStackTrace()
            }

    }

    private fun addChip(chipText: String) {
        val chip = Chip(requireContext())
        chip.setChipBackgroundColorResource(R.color.yellow)
        chip.setPadding(16, 8, 16, 8)

        chip.text = chipText
        chip.isClickable = true
        chip.isCheckable = false
        binding.chipGroup.addView(chip)
    }
}
