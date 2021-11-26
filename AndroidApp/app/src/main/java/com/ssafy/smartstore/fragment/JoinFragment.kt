package com.ssafy.smartstore.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ssafy.smartstore.activity.LoginActivity
import com.ssafy.smartstore.api.UserApi
import com.ssafy.smartstore.config.ApplicationClass
import com.ssafy.smartstore.databinding.FragmentJoinBinding
import com.ssafy.smartstore.dto.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.create

// 회원 가입 화면
private const val TAG = "JoinFragment_싸피"
class JoinFragment : Fragment(){
    private var checkedId = false
    lateinit var binding: FragmentJoinBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentJoinBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user_service = ApplicationClass.retrofit.create(UserApi::class.java)

        //id 중복 확인 버튼
        binding.btnConfirm.setOnClickListener {

            user_service.isUsedId(binding.editTextJoinID.text.toString()).enqueue(object : Callback<Boolean> {
                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                    if (response.isSuccessful) {
                        val check = response.body() as Boolean

                        if (!check) {
                            Toast.makeText(requireContext(), "사용가능한 아이디입니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "이미 가입된 아이디입니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                    Toast.makeText(requireContext(), t.toString(), Toast.LENGTH_SHORT).show()
                }

            })


        }

        // 회원가입 버튼
        binding.btnJoin.setOnClickListener {

            val id = binding.editTextJoinID.text.toString()
            val pwd = binding.editTextJoinPW.text.toString()
            val name = binding.editTextJoinName.text.toString()

            val user = User(id, name, pwd)

            user_service.insert(user).enqueue(object : Callback<Boolean> {
                override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                    if (response.isSuccessful) {
                        val result = response.body() as Boolean

                        if (result == true) {
                            Toast.makeText(requireContext(), "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                            val intent = Intent(requireContext(), LoginActivity::class.java)
                            startActivity(intent)
                            activity?.supportFragmentManager
                                ?.beginTransaction()
                                ?.remove(this@JoinFragment)
                                ?.commit()
                        }

                    }
                }

                override fun onFailure(call: Call<Boolean>, t: Throwable) {
                    Toast.makeText(requireContext(), t.toString(), Toast.LENGTH_SHORT).show()
                }

            })



        }
    }
}