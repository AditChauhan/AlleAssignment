package com.io.alleassignment

import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import com.bumptech.glide.request.RequestOptions
import com.io.alleassignment.databinding.FragmentDeleteBinding
import java.io.File
import java.security.MessageDigest


class DeleteFragment : Fragment() {

    private lateinit var binding: FragmentDeleteBinding
    private lateinit var uri: Uri
    private lateinit var viewModel: ImageViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_delete, container, false)
        viewModel = ViewModelProvider(requireActivity())[ImageViewModel::class.java]
        uri = viewModel.selectedImageUri.value!!
        viewModel.selectedImageUri.observe(viewLifecycleOwner) {
            uri = it
        }

        showDeleteConfirmationDialog(uri)
        loadImageIntoImageView(uri)
        return binding.root

    }

    private fun showDeleteConfirmationDialog(uri: Uri) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Delete Screenshot")
            .setMessage("Are you sure you want to delete this screenshot?")
            .setPositiveButton("Delete") { dialogInterface: DialogInterface, _: Int ->

                val file = File(uri.path!!)
                if (file.exists()) {
                    file.delete()
                }

                dialogInterface.dismiss()
            }
            .setNegativeButton("Cancel") { dialogInterface: DialogInterface, _: Int ->
                dialogInterface.dismiss()
            }
            .show()
    }

    private fun loadImageIntoImageView(uri: Uri) {
        Glide.with(requireContext())
            .load(uri)
            .centerCrop()
            .apply(
                RequestOptions().transform(
                    BlurTransformation(
                        requireContext(),
                        25
                    )
                )
            )

            .into(binding.imageViewTop)
        viewModel.setSelectedImage(uri)
    }

    class BlurTransformation(context: Context, private val radius: Int) : BitmapTransformation() {

        private val rs: RenderScript = RenderScript.create(context)

        override fun transform(
            pool: BitmapPool,
            toTransform: Bitmap,
            outWidth: Int,
            outHeight: Int
        ): Bitmap {
            return getBlurredBitmap(toTransform)
        }

        override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        }

        private fun getBlurredBitmap(bitmap: Bitmap): Bitmap {
            val inputBitmap = Bitmap.createBitmap(bitmap)
            val outputBitmap = Bitmap.createBitmap(
                inputBitmap.width,
                inputBitmap.height,
                Bitmap.Config.ARGB_8888
            )

            val blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
            val allocationInput = Allocation.createFromBitmap(rs, inputBitmap)
            val allocationOutput = Allocation.createFromBitmap(rs, outputBitmap)

            blurScript.setRadius(radius.toFloat())
            blurScript.setInput(allocationInput)
            blurScript.forEach(allocationOutput)

            allocationOutput.copyTo(outputBitmap)

            return outputBitmap
        }

    }
}
