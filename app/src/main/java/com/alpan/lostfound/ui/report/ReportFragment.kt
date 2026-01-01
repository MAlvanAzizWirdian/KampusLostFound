package com.alpan.lostfound.ui.report

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.alpan.lostfound.databinding.FragmentReportBinding
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.firestore.FirebaseFirestore

class ReportFragment : Fragment() {

    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!

    private var imageUri: Uri? = null
    private val db = FirebaseFirestore.getInstance()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            binding.imgPreview.setImageURI(imageUri)
            binding.imgPreview.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
            pickImageLauncher.launch(intent)
        }

        binding.btnSubmit.setOnClickListener {
            imageUri?.let { uri ->
                uploadToCloudinary(uri)
            } ?: Toast.makeText(requireContext(), "Please select an image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadToCloudinary(uri: Uri) {
        MediaManager.get().upload(uri)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val imageUrl = resultData["secure_url"] as? String
                    imageUrl?.let {
                        saveToFirestore(it)
                    }
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    Toast.makeText(requireContext(), "Upload failed: ${error.description}", Toast.LENGTH_SHORT).show()
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).dispatch()
    }

    private fun saveToFirestore(imageUrl: String) {
        val data = hashMapOf(
            "title" to binding.edtTitle.text.toString(),
            "desc" to binding.edtDesc.text.toString(),
            "location" to binding.edtLocation.text.toString(),
            "imageUrl" to imageUrl
        )

        db.collection("reports").add(data)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Report submitted successfully", Toast.LENGTH_SHORT).show()
                activity?.supportFragmentManager?.popBackStack()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to submit report", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
