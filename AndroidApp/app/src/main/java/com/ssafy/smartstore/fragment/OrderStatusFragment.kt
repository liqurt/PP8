package com.ssafy.smartstore.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.smartstore.activity.MainActivity
import com.ssafy.smartstore.adapter.LastOrderListAdapter
import com.ssafy.smartstore.databinding.FragmentOrderStatusBinding
import com.ssafy.smartstore.response.LastOrderResponse
import com.ssafy.smartstore.util.CommonUtils
import com.ssafy.smartstore.util.RetrofitUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import android.R
import androidx.lifecycle.MutableLiveData
import com.ssafy.smartstore.config.ApplicationClass
import java.util.*
import android.os.CountDownTimer


private const val TAG = "OrderStatusFragment_싸피"

class OrderStatusFragment : Fragment() {
    private lateinit var mainActivity: MainActivity
    private lateinit var binding: FragmentOrderStatusBinding
    private var userId: String = ""
    private lateinit var res: List<LastOrderResponse>

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity.hideBottomNav(true)
        arguments?.let {
            userId = it.getString("userId", "")

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOrderStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()


    }

    private fun initData() {
        val lastOrderRequest = RetrofitUtil.orderService.getLastOrder(userId)
        lastOrderRequest.enqueue(object : Callback<List<LastOrderResponse>> {
            override fun onResponse(
                call: Call<List<LastOrderResponse>>,
                response: Response<List<LastOrderResponse>>
            ) {
                res = response.body()!!
                if (response.code() == 200) {
                    if (res != null) {
                        showProgress(res)
                    }
//                    Toast.makeText(mainActivity, "$result", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "onResponse: $res")
                } else {
                    Log.d(TAG, " getLastOrder onResponse: Error Code ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<LastOrderResponse>>, t: Throwable) {
                Log.d(TAG, t.message ?: "마지막 주문 내역 받아오는 중 통신오류")
            }
        })
    }

    private fun showProgress(lo: List<LastOrderResponse>) {
        val orderedTime = lo[0].orderDate.time
        var curTime = (Date().time + 60 * 60 * 9 * 1000)
        var liveStep: MutableLiveData<Int> = MutableLiveData()

        if (CommonUtils.checkTime(orderedTime)) {
            binding.tvStatus.text = "진행중인 주문이 없습니다."
            binding.llOrderStatus.visibility = View.GONE

        } else {
            binding.tvStatus.text = "진행중인 주문"
            binding.llOrderStatus.visibility = View.VISIBLE
        }

        var step = ((curTime - orderedTime) / ApplicationClass.ORDER_COMPLETED_TIME).toInt() - 1

        val descriptionData = mutableListOf<String>("주문완료", "제조중", "제조 완료", "대기중", "픽업 완료")
        binding.stepView.apply {
            setSteps(descriptionData)
            go(step, true)
            done(false)
        }

        val CDT: CountDownTimer = object : CountDownTimer((1000 * 60 * 3).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                curTime = (Date().time + 60 * 60 * 9 * 1000)
                liveStep.value =
                    ((curTime - orderedTime) * 5 / ApplicationClass.ORDER_COMPLETED_TIME).toInt()
            }

            override fun onFinish() {}
        }

        CDT.start() //CountDownTimer 실행

        liveStep.observe(this, {
            Log.d(TAG, "showProgress: livestep called.... ${liveStep.value}")
            if (step < liveStep.value?.minus(0) ?: step) {
                step = liveStep.value!!
                binding.stepView.go(step, true);
            } else {
                binding.stepView.done(true);
            }

        })

        //CDT.cancel() // 타이머 종료

        val dateFormat = SimpleDateFormat("yyyy.MM.dd HH시 mm분 ss초")
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        binding.tvOrderedDate.text = dateFormat.format(lo[0].orderDate)

        var totalPrice = 0
        lo.forEach { totalPrice += it.totalPrice }
        binding.tvTotPrice.text = "${CommonUtils.makeComma(totalPrice)}"

        var lastOrderListAdapter = LastOrderListAdapter(mainActivity, lo)
        binding.rvOrderDetailList.apply {
            val linearLayoutManager = LinearLayoutManager(context)
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            layoutManager = linearLayoutManager
            adapter = lastOrderListAdapter
            //원래의 목록위치로 돌아오게함
            adapter!!.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity.hideBottomNav(false)
    }

    companion object {
        @JvmStatic
        fun newInstance(key: String, value: String) =
            OrderStatusFragment().apply {
                arguments = Bundle().apply {
                    putString(key, value)
                }
            }
    }

}