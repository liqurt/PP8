package com.ssafy.smartstore.fragment

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.Preference
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.ssafy.smartstore.R
import com.ssafy.smartstore.activity.MainActivity
import com.ssafy.smartstore.adapter.NoticeAdapter
import com.ssafy.smartstore.adapter.OrderAdapter
import com.ssafy.smartstore.api.UserApi
import com.ssafy.smartstore.config.ApplicationClass
import com.ssafy.smartstore.databinding.FragmentMypageBinding
import com.ssafy.smartstore.dto.Grade
import com.ssafy.smartstore.dto.User
import com.ssafy.smartstore.response.LatestOrderResponse
import com.ssafy.smartstore.service.OrderService
import com.ssafy.smartstore.util.SharedPreferencesUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// MyPage 탭
private const val TAG = "MypageFragment_싸피"

class MypageFragment : Fragment() {
    private lateinit var orderAdapter: OrderAdapter
    private lateinit var mainActivity: MainActivity
    private lateinit var list: List<LatestOrderResponse>
    private lateinit var binding: FragmentMypageBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMypageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = getUserId()
        initData(id)
        initDrawerLayout(view)
    }

    private fun initDrawerLayout(view: View) {
        binding.myProfile.setOnClickListener {
            binding.myPageDrawer.openDrawer(GravityCompat.START)
        }

        // drawerHeader
        val navigationView = binding.navigationView
        val headerView = navigationView.getHeaderView(0)
        val navUsername = headerView.findViewById<View>(R.id.drawerHeaderUserName) as TextView
        navUsername.text = binding.textUserName.text

        val userId : String = getUserId()
        val userPw : String = getUserPW()

        // drawerLayout menu event
        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.item1 -> {
                    Snackbar.make(view, "내 프로필 미구현", Snackbar.LENGTH_SHORT).show()
                }
                R.id.item2 ->{
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(
                            R.id.frame_layout_main,
                            MyCommentFragment.newInstance(userId, userPw)
                        )
                        .addToBackStack(null)
                        .commit()
                }
                R.id.item3 ->{
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout_main, OrderStatusFragment.newInstance("userId", userId))
                        .addToBackStack(null)
                        .commit()
                }
                R.id.item4 ->{
                    mainActivity.showMemberShipDialog()
                }
                R.id.item5 ->{
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.frame_layout_main, WebViewFragment())
                        .addToBackStack(null)
                        .commit()
                }
                R.id.item6 ->{
                    mainActivity.openFragment(5)
                }
            }
            binding.myPageDrawer.closeDrawer(GravityCompat.START)
            false
        }
    }

    private fun initData(id: String) {
        val userLastOrderLiveData = OrderService().getLastMonthOrder(id)
        Log.d(TAG, "onViewCreated: ${userLastOrderLiveData.value}")
        userLastOrderLiveData.observe(
            viewLifecycleOwner,
            {
                list = it
                orderAdapter = OrderAdapter(mainActivity, list)
                orderAdapter.setItemClickListener(object : OrderAdapter.ItemClickListener {
                    override fun onClick(view: View, position: Int, orderid: Int) {
                        mainActivity.openFragment(2, "orderId", orderid)
                    }
                })
                binding.recyclerViewOrder.apply {
                    layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                    adapter = orderAdapter
                    //원래의 목록위치로 돌아오게함
                    adapter!!.stateRestorationPolicy =
                        RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
                }
            }
        )
        stampUpdate()
    }

    private fun getUserId(): String {
        val user = ApplicationClass.sharedPreferencesUtil.getUser()
        binding.textUserName.text = "${user.name}님,"
        return user.id
    }

    private fun getUserPW(): String {
        return ApplicationClass.sharedPreferencesUtil.getUser().pass
    }

    private fun stampUpdate() {
        val id = getUserId()
        val pass = getUserPW()
        val me = User(id, "", pass, 0, ArrayList())
        val service = ApplicationClass.retrofit.create(UserApi::class.java)

        service.getInfo(me).enqueue(object : Callback<HashMap<String, Any>> {
            override fun onResponse(
                call: Call<HashMap<String, Any>>,
                response: Response<HashMap<String, Any>>
            ) {
                val grade = Gson().fromJson(response.body()?.get("grade").toString(), Grade::class.java)
                binding.textUserLevel.text = grade.title + " " + grade.step + "단계"
                binding.textLevelRest.text = "다음 레벨까지 ${grade.to}잔 남았습니다."
                val max = when (grade.title) {
                    "씨앗" -> 10
                    "꽃" -> 15
                    "열매" -> 20
                    "커피콩" -> 25
                    "나무" -> 100
                    else -> 50
                }
                binding.proBarUserLevel.progress = (max - grade.to) * 100 / max
                val name = (grade.img).substring(0, grade.img.length - 4)
                val drawID = resources.getIdentifier(name, "drawable", activity?.packageName)
                val drawablers = resources.getDrawable(drawID)
                binding.imageLevel.setImageDrawable(drawablers)
                binding.textUserNextLevel.text = "${max - grade.to} / $max"
            }

            override fun onFailure(call: Call<HashMap<String, Any>>, t: Throwable) {
                Log.d(TAG, "onFailure: $t")
            }

        })
    }

}