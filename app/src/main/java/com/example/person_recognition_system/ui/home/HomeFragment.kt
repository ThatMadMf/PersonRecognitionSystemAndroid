package com.example.person_recognition_system.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.person_recognition_system.R
import com.example.person_recognition_system.databinding.FragmentHomeBinding
import com.example.person_recognition_system.ui.face_capture.FaceCapture

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val button = view.findViewById(R.id.start_face_recognition_button) as ImageButton

        button.setOnClickListener {
            activity!!.supportFragmentManager.beginTransaction().replace(
                R.id.nav_host_fragment_activity_main,
                FaceCapture(),
                R.id.face_capture.toString(),
            ).commit()

            Toast.makeText(activity, "click", 5).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}