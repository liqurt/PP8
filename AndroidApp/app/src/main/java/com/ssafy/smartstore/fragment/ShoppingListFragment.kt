package com.ssafy.smartstore.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.ssafy.smartstore.R
import com.ssafy.smartstore.activity.MainActivity
import com.ssafy.smartstore.adapter.MyCouponAdapter
//import com.ssafy.smartstore.adapter.ShoppingListAdapter
import com.ssafy.smartstore.api.OrderApi
import com.ssafy.smartstore.api.UserApi
import com.ssafy.smartstore.config.ApplicationClass
import com.ssafy.smartstore.dto.*
import com.ssafy.smartstore.service.CouponService
import com.ssafy.smartstore.util.Cart
import com.ssafy.smartstore.util.CommonUtils
import com.ssafy.smartstore.util.RetrofitCallback
import com.ssafy.smartstore.util.SharedPreferencesUtil
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.HashMap

private const val TAG = "ShoppingListFragment_싸피"

//장바구니 Fragment
class ShoppingListFragment : Fragment() {
    private lateinit var shoppingListRecyclerView: RecyclerView
    private lateinit var shoppingListAdapter: ShoppingListAdapter
    private lateinit var myCouponAdapter: MyCouponAdapter
    private lateinit var mainActivity: MainActivity
    private lateinit var btnShop: Button
    private lateinit var btnTakeout: Button
    private lateinit var btnOrder: Button
    private lateinit var tvShoppingCount: TextView
    private lateinit var tvShoppingMoney: TextView
    private var isShopWithNFC: Boolean = true
    private var dialog: AlertDialog? = null

    private var shoppingList = Cart.items
    var nfcAdapter: NfcAdapter? = null
    var pIntent: PendingIntent? = null
    private var filters: Array<IntentFilter>? = null
    private var table: String = ""

    var totalCount = 0
    var totalMoney = 0

    private var usedCouponId: Int = -1

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity.hideBottomNav(true)

        nfcAdapter = NfcAdapter.getDefaultAdapter(mainActivity)

        val intent = Intent(mainActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        pIntent = PendingIntent.getActivity(mainActivity, 0, intent, 0)

        val tag_filter = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        filters = arrayOf(tag_filter)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter!!.disableForegroundDispatch(mainActivity)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_shopping_list, null)
        shoppingListRecyclerView = view.findViewById(R.id.recyclerViewShoppingList)
        btnShop = view.findViewById(R.id.btnShop)
        btnTakeout = view.findViewById(R.id.btnTakeout)
        btnOrder = view.findViewById(R.id.btnOrder)
        tvShoppingCount = view.findViewById(R.id.textShoppingCount)
        tvShoppingMoney = view.findViewById(R.id.textShoppingMoney)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        shoppingListAdapter = ShoppingListAdapter(requireContext(), shoppingList)
        shoppingListRecyclerView.apply {
            val linearLayoutManager = LinearLayoutManager(context)
            linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
            layoutManager = linearLayoutManager
            adapter = shoppingListAdapter
            //원래의 목록위치로 돌아오게함
            adapter!!.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }

        totalCount = 0
        totalMoney = 0

        for (i in Cart.items.indices) {
            totalCount += Cart.items[i].menuCnt
            totalMoney += Cart.items[i].menuPrice * Cart.items[i].menuCnt
        }

        tvShoppingCount.text = "총 ${totalCount}개"
        tvShoppingMoney.text = CommonUtils.makeComma(totalMoney)

        btnShop.setOnClickListener {
            btnShop.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.button_color)
            btnTakeout.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.button_non_color)
            isShopWithNFC = true
        }
        btnTakeout.setOnClickListener {
            btnTakeout.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.button_color)
            btnShop.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.button_non_color)
            isShopWithNFC = false
        }
        btnOrder.setOnClickListener {
            if(Cart.items.size == 0){
                Toast.makeText(mainActivity, "장바구니에 담긴 항목이 없습니다.", Toast.LENGTH_SHORT).show()
            } else {
                if (isShopWithNFC) showDialogForOrderInShop()
                else showDialogForOrderTakeoutOver200m()
            }
        }

        // 장바구니에 물건이 있을때
        if (totalCount != 0) {
            // 나의 쿠폰확인
            doIHaveACoupon()
        }
    }

    private fun doIHaveACoupon() {
        val user = ApplicationClass.sharedPreferencesUtil.getUser()
        CouponService().selectCouponByUserId(user.id, SelectCouponByUserIdCallback())
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity.hideBottomNav(false)
    }

    private fun showDialogForOrderInShop() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle("알림")
        builder.setMessage("Table NFC를 찍어주세요.\n")
        builder.setCancelable(true)
        builder.setNegativeButton("취소") { dialog, _ ->
            nfcAdapter!!.disableForegroundDispatch(mainActivity)
            dialog.cancel()
        }
        dialog = builder.create()
        dialog!!.show()
        nfcAdapter!!.enableForegroundDispatch(mainActivity, pIntent, filters, null)
    }

    private fun showDialogForOrderTakeoutOver200m() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setTitle("알림")
        builder.setMessage(
            "현재 고객님의 위치가 매장과 200m 이상 떨어져 있습니다.\n정말 주문하시겠습니까?"
        )
        builder.setCancelable(true)
        builder.setPositiveButton("확인") { _, _ ->
            takeOrder()
        }
        builder.setNegativeButton(
            "취소"
        ) { dialog, _ -> dialog.cancel() }
        builder.create().show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun completedOrder() {
        Toast.makeText(context, "주문이 완료되었습니다.", Toast.LENGTH_SHORT).show()
        val oldData = ApplicationClass.sharedPreferencesUtil.getGradeTitleAndStep()
        val user = ApplicationClass.sharedPreferencesUtil.getUser()
        val service = ApplicationClass.retrofit.create(UserApi::class.java)
        service.getInfo(user).enqueue(object : Callback<HashMap<String, Any>> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(
                call: Call<HashMap<String, Any>>,
                response: Response<HashMap<String, Any>>
            ) {
                val grade =
                    Gson().fromJson(response.body()?.get("grade").toString(), Grade::class.java)
                Log.d(TAG, "oldData(grade.title): ${oldData["title"]}")
                Log.d(TAG, "oldData(grade.step): ${oldData["step"]}")
                Log.d(TAG, "newData(grade.title): ${grade.title}")
                Log.d(TAG, "newData(grade.step): ${grade.step}")

                if (oldData["title"] != grade.title) { //큰 단계의 차이
                    //쿠폰 2장 추가
                    addCoupon(2)
                    ApplicationClass.sharedPreferencesUtil.setGradeTitle(grade.title)
                } else if (oldData["step"] != grade.step) { //세부단계의 차이
                    //쿠폰 1장 추가
                    addCoupon(1)
                    ApplicationClass.sharedPreferencesUtil.setGradeStep(grade.step)
                }
            }

            override fun onFailure(call: Call<HashMap<String, Any>>, t: Throwable) {
                Log.d(TAG, "onFailure: $t")
            }
        })

        if (usedCouponId != -1) {
            CouponService().deleteCouponById(usedCouponId!!)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addCoupon(howMany: Int) {
        // giving 다이얼로그 보여주고
        // n초 뒤, 다이럴로그의 화면을 taking으로 바꾼다.
        // 하지만 중요한건 아니니까 일단, Snackbar로 퉁친다
        Snackbar.make(requireView(), "레벨이 올라서 ${howMany}장의 쿠폰을 얻었어요!", Snackbar.LENGTH_SHORT).show()

        // 서버에 쿠폰관련 로직
        val coupon = Coupon(userId = ApplicationClass.sharedPreferencesUtil.getUser().id)
        for (i in 1..howMany)
            CouponService().insertCoupon(coupon, InsertCouponCallback())
    }

    fun getNFCData(intent: Intent?) {
        val rawMsgs = intent!!.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        if (rawMsgs != null) {
            val message = arrayOfNulls<NdefMessage>(rawMsgs.size)
            for (i in rawMsgs.indices) {
                message[i] = rawMsgs[i] as NdefMessage
            }
            val record_data = message[0]!!.records[0]
            val record_type = record_data.type
            val type = String(record_type)
            // 가져온 데이터를 TextView에 반영
            val data = message[0]!!.records[0].payload
            table = "${String(data, 3, data.size - 3)}"

            takeOrder()
        }
    }

    private fun takeOrder() {
        val detailList = arrayListOf<OrderDetail>()
        val orderService = ApplicationClass.retrofit.create(OrderApi::class.java)
        for (i in Cart.items.indices) {
            val orderDetail =
                OrderDetail(Cart.items[i].menuId, Cart.items[i].menuCnt, Cart.items[i].p_detail)
            detailList.add(orderDetail)
        }
        val order = Order(ApplicationClass.sharedPreferencesUtil.getUser().id, table, detailList)
        if(usedCouponId != null){
            order.totalPrice = (totalMoney.toDouble() * 0.8).toInt()
        }

        orderService.makeOrder(order).enqueue(object : Callback<Int> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResponse(call: Call<Int>, response: Response<Int>) {
                completedOrder()
                if (table.isNotEmpty()) dialog!!.dismiss()
                val tempCartSize = Cart.items.size
                Cart.items.clear()
                shoppingListRecyclerView.adapter!!.notifyItemRangeRemoved(0, tempCartSize)
                tvShoppingCount.text = ""
                tvShoppingMoney.text = ""
                view!!.findViewById<LinearLayout>(R.id.aboutDiscount).visibility = View.GONE
            }

            override fun onFailure(call: Call<Int>, t: Throwable) {
                Toast.makeText(requireContext(), t.toString(), Toast.LENGTH_SHORT).show()
            }
        })
    }

    inner class ShoppingListAdapter(
        private val context: Context,
        private val shoopingCartList: MutableList<ShoppingCart>
    ) : RecyclerView.Adapter<ShoppingListAdapter.ShoppingListHolder>() {

        inner class ShoppingListHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            fun bindInfo(shoppingItem: ShoppingCart) {
                val menuImg = itemView.findViewById<ImageView>(R.id.menuImage)
                Glide.with(itemView)
                    .load("${ApplicationClass.MENU_IMGS_URL}${shoppingItem.menuImg}")
                    .into(menuImg)
                itemView.findViewById<TextView>(R.id.textShoppingMenuName).text =
                    shoppingItem.menuName
                itemView.findViewById<TextView>(R.id.textShoppingMenuCount).text =
                    "${shoppingItem.menuCnt}잔"
                itemView.findViewById<TextView>(R.id.textShoppingMenuMoney).text =
                    "${CommonUtils.makeComma(shoppingItem.menuPrice)}"
                itemView.findViewById<TextView>(R.id.textShoppingMenuMoneyAll).text =
                    "${CommonUtils.makeComma(shoppingItem.menuPrice * shoppingItem.menuCnt)}"
                itemView.findViewById<TextView>(R.id.tv_custom_cart).text = shoppingItem.p_detail
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingListHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_shopping_list, parent, false)
            return ShoppingListHolder(view)
        }

        override fun onBindViewHolder(holder: ShoppingListHolder, position: Int) {
            holder.apply {
                bindInfo(shoopingCartList[position])
                itemView.findViewById<ImageView>(R.id.shopping_coffee_remove).setOnClickListener {
                    Cart.items.removeAt(position)
                    deleteItem()
                }
            }
        }

        override fun getItemCount(): Int {
            return shoopingCartList.size
        }

        private fun deleteItem() {
            notifyDataSetChanged()

            totalCount = 0
            totalMoney = 0

            for (i in Cart.items.indices) {
                totalCount += Cart.items[i].menuCnt
                totalMoney += Cart.items[i].menuPrice * Cart.items[i].menuCnt
            }

            tvShoppingCount.text = "총 ${totalCount}개"
            tvShoppingMoney.text = CommonUtils.makeComma(totalMoney)
        }
    }

    inner class InsertCouponCallback : RetrofitCallback<Boolean> {
        override fun onError(t: Throwable) {
            Log.d(TAG, "InsertCouponCallback-onError")
        }

        override fun onSuccess(code: Int, responseData: Boolean) {
            Log.d(TAG, "InsertCouponCallback-onSuccess")
        }

        override fun onFailure(code: Int) {
            Log.d(TAG, "InsertCouponCallback-onFailure")
        }
    }

    inner class SelectCouponByUserIdCallback : RetrofitCallback<List<Coupon>> {

        override fun onError(t: Throwable) {
        }

        override fun onSuccess(code: Int, responseData: List<Coupon>) {
            val jsonRes = Gson().toJson(responseData)
            val myCoupon = Gson().fromJson(
                jsonRes,
                Array<Coupon>::class.java
            ).toList()

            if (myCoupon.isNotEmpty()) {
                var dialog2 = Dialog(mainActivity)
                myCouponAdapter = MyCouponAdapter(myCoupon)
                myCouponAdapter.list = myCoupon
                myCouponAdapter.onItemClickListener = object : MyCouponAdapter.OnItemClickListener {
                    override fun onClick(itemView: View, position: Int) {
                        usedCouponId = myCouponAdapter.list[position].id
                        view!!.findViewById<LinearLayout>(R.id.aboutDiscount).visibility =
                            View.VISIBLE
                        view!!.findViewById<TextView>(R.id.howMuchDiscount).text =
                            "-${CommonUtils.makeComma((totalMoney.toDouble() * 0.20).toInt())}"
                        view!!.findViewById<TextView>(R.id.discountedMoney).text =
                            "${CommonUtils.makeComma((totalMoney.toDouble() * 0.80).toInt())}"
                        dialog2.dismiss()
                    }
                }

                var dialogView = layoutInflater.inflate(R.layout.dialog_coupon, null)
                dialogView.findViewById<RecyclerView>(R.id.rcvListMyCoupon).apply {
                    layoutManager =
                        LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                    adapter = myCouponAdapter
                    adapter!!.stateRestorationPolicy =
                        RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
                }
                dialog2 = Dialog(mainActivity).apply {
                    setContentView(dialogView)
                    setCancelable(true)
                }

                val lp = WindowManager.LayoutParams()
                lp.copyFrom(dialog2.window!!.attributes)
                lp.width = WindowManager.LayoutParams.MATCH_PARENT
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT
                dialog2.window!!.attributes = lp
                dialog2.show()
            }
        }

        override fun onFailure(code: Int) {
        }
    }

}