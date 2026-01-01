package com.alpan.lostfound.ui.list

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.alpan.lostfound.adapter.ReportAdapter
import com.alpan.lostfound.databinding.FragmentListBinding
import com.alpan.lostfound.model.Report
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects

class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        db.collection("reports").addSnapshotListener { value, _ ->
            value?.let {
                val data = it.toObjects<Report>()
                binding.recyclerView.adapter = ReportAdapter(data) { location ->
                    val uri = Uri.parse("geo:0,0?q=$location")
                    startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
