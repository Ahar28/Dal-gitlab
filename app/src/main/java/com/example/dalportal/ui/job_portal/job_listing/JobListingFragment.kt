package com.example.dalportal.ui.job_portal.job_listing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dalportal.R
import com.example.dalportal.databinding.FragmentJobListingBinding
import com.example.dalportal.model.JobListing
import com.google.firebase.firestore.FirebaseFirestore

class JobListingFragment : Fragment() {
    private var _binding: FragmentJobListingBinding? = null
    private val binding get() = _binding!!
    private lateinit var jobListingsFull: List<JobListing> // Full list of job postings
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentJobListingBinding.inflate(inflater, container, false)
        setupRecyclerView()
        loadJobPostings()
        setupSearchView()
// Setting up click listener for the Add Job button
        binding.addJobButton.setOnClickListener {
            findNavController().navigate(R.id.action_jobListingFragment_to_jobPostingFragment)
        }
        return binding.root
    }

//    private fun setupRecyclerView() {
//        binding.recyclerViewJobListings.layoutManager = LinearLayoutManager(context)
//        binding.recyclerViewJobListings.adapter = JobListingAdapter(mutableListOf(), requireContext())
//    }

    private fun setupRecyclerView() {
        binding.recyclerViewJobListings.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewJobListings.adapter =
            JobListingAdapter(mutableListOf(), requireContext()) { jobListing ->
                val bundle = Bundle().apply {
                    putParcelable("jobListing", jobListing)
                }
                findNavController().navigate(
                    R.id.action_jobListingFragment_to_jobDetailsFragment,
                    bundle
                )

            }
    }

    private fun loadJobPostings() {
        db.collection("jobPostings").get()
            .addOnSuccessListener { documents ->
                jobListingsFull = documents.map { doc ->
                    JobListing(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        location = doc.getString("location") ?: "",
                        pay = doc.getLong("pay")?.toInt() ?: 0,
                        positions = doc.getLong("positions")?.toInt() ?: 0,
                        requirements = doc.getString("requirements") ?: "",
                        type = doc.getString("type") ?: ""
                    )
                }
                (binding.recyclerViewJobListings.adapter as JobListingAdapter).updateData(
                    jobListingsFull
                )
            }
            .addOnFailureListener { e ->
                // Handle error, e.g., show a Toast
            }
    }

    private fun setupSearchView() {
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterJobPostings(newText.orEmpty())
                return true
            }
        })
    }

    private fun filterJobPostings(query: String) {
        val filteredList = jobListingsFull.filter {
            it.title.contains(query, ignoreCase = true)
        }
        (binding.recyclerViewJobListings.adapter as JobListingAdapter).updateData(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
