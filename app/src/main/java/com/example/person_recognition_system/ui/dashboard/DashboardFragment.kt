package com.example.person_recognition_system.ui.dashboard

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.person_recognition_system.MainActivity.Companion.token
import com.example.person_recognition_system.R
import com.example.person_recognition_system.databinding.FragmentDashboardBinding
import com.example.person_recognition_system.dtos.AuthorizationResponseDto
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException


class DashboardFragment : Fragment() {
    private lateinit var dashboardViewModel: DashboardViewModel
    private var _binding: FragmentDashboardBinding? = null
    private var authorized = false
    private var firstName = ""
    private var secondName = ""

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textDashboard
        dashboardViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })

        return root
    }

    override fun onResume() {
        super.onResume()

        getUserInfo()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getUserInfo() {
        val request = Request.Builder()
            .url("https://8b5f-194-146-190-152.ngrok.io/api/me")
//            .url("http://192.168.0.195:8000/api/me")
            .addHeader("Authorization", "Bearer $token")
            .addHeader("Accept", "*/*")
            .addHeader("Accept-Encoding", "gzip, deflate, br")
            .build()

        val client = OkHttpClient()
        val call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                throw e
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.code() == 200) {
                    val textDashboard = view!!.findViewById<TextView>(R.id.text_dashboard)
                    authorized = true

                    val dto = Gson().fromJson(
                        response.body()!!.string(),
                        AuthorizationResponseDto::class.java
                    )

                    textDashboard.text = "Authorized as ${dto.firstName} ${dto.lastName}"
                }
            }
        })
    }
}