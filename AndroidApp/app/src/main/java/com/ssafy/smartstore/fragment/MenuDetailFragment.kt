package com.ssafy.smartstore.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButtonToggleGroup
import com.ssafy.smartstore.R
import com.ssafy.smartstore.activity.MainActivity
import com.ssafy.smartstore.adapter.CommentAdapter
import com.ssafy.smartstore.api.CommentApi
import com.ssafy.smartstore.api.ProductApi
import com.ssafy.smartstore.config.ApplicationClass
import com.ssafy.smartstore.databinding.FragmentMenuDetailBinding
import com.ssafy.smartstore.dto.Comment
import com.ssafy.smartstore.dto.ShoppingCart
import com.ssafy.smartstore.response.MenuDetailWithCommentResponse
import com.ssafy.smartstore.service.ProductService
import com.ssafy.smartstore.util.Cart
import com.ssafy.smartstore.util.CommonUtils
import com.ssafy.smartstore.util.RetrofitCallback
import org.w3c.dom.Text
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.round

//메뉴 상세 화면 . Order탭 - 특정 메뉴 선택시 열림
private const val TAG = "MenuDetailFragment_싸피"
class MenuDetailFragment : Fragment(){
    private lateinit var mainActivity: MainActivity
    private var commentAdapter = CommentAdapter()
    private var commentList = mutableListOf<MenuDetailWithCommentResponse>()

    private var userId: String = ApplicationClass.sharedPreferencesUtil.getUser().id
    private var productId: Int = -1
    private var rating: Float = 0.0f
    private var comment: String = ""
    private var menuImage: String? = null
    private lateinit var binding: FragmentMenuDetailBinding
    private var isCommentSucceed: Int = -1

    private var custom:String = ""

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity.hideBottomNav(true)
        arguments?.let {
            productId = it.getInt("productId", -1)
            Log.d(TAG, "onCreate: $productId")
        }
        commentAdapter = CommentAdapter(arrayListOf(), productId)
        commentAdapter.list = selectAllComments(productId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMenuDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initListener()

        binding.btnCreateComment.setOnClickListener {
            comment = binding.commentEt.text.toString()
            showDialogRatingStar()
        }
    }

    private fun initData() {
        ProductService().getProductWithComments(productId, ProductWithCommentInsertCallback())
        var count = binding.textMenuCount.text.toString().toInt()
        binding.btnMinusCount.setOnClickListener {
            if (count > 1) {
                count--
            }
            binding.textMenuCount.text = count.toString()
        }
        binding.btnAddCount.setOnClickListener {
            count++
            binding.textMenuCount.text = count.toString()
        }
        binding.recyclerViewMenuDetail.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = commentAdapter
            //원래의 목록위치로 돌아오게함
            adapter!!.stateRestorationPolicy =
                RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        }
    }

    // 초기 화면 설정
    @SuppressLint("SetTextI18n")
    private fun setScreen(menu: MenuDetailWithCommentResponse) {

        Glide.with(this)
            .load("${ApplicationClass.MENU_IMGS_URL}${menu.productImg}")
            .into(binding.menuImage)
        menuImage = menu.productImg
        binding.txtMenuName.text = menu.productName
        binding.txtMenuPrice.text = CommonUtils.makeComma(menu.productPrice)
        binding.txtRating.text = "${(round(menu.productRatingAvg * 10) / 10)}점"
        binding.ratingBar.rating = menu.productRatingAvg.toFloat() / 2

        selectAllComments(productId)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun initListener() {
        binding.btnAddList.setOnClickListener {
            if(productId != 12) {
                showDialogCustom()
            } else {
                insertCart()
            }

        }
        binding.btnCreateComment.setOnClickListener {
                showDialogRatingStar()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity.hideBottomNav(false)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showDialogRatingStar() {
        val builder = AlertDialog.Builder(mainActivity)

        val v1 = layoutInflater.inflate(R.layout.dialog_menu_comment, null)
        builder.setView(v1)
        val listener = DialogInterface.OnClickListener { p0, p1 ->
            val alert = p0 as AlertDialog
            val rb = alert.findViewById<RatingBar>(R.id.ratingBar2)
            insertComment(comment, productId, rb.rating, userId)

            binding.commentEt.setText("")
        }

        builder.setPositiveButton("확인", listener)
        builder.setNegativeButton("취소", null)
        builder.show()
    }

    private fun showDialogCustom() {
        val builder = AlertDialog.Builder(mainActivity)

        val v1 = layoutInflater.inflate(R.layout.dialog_menu_custom, null)
        builder.setView(v1)

        val listener = DialogInterface.OnClickListener { p0, p1 ->
            val alert = p0 as AlertDialog

            var mbtg = alert.findViewById<MaterialButtonToggleGroup>(R.id.btnsCategory_temp)
            var selectedButtonId = mbtg.checkedButtonId;
            var selectedButton = mbtg.findViewById<Button>(selectedButtonId)
            var temp = selectedButton.text.toString()

            mbtg = alert.findViewById<MaterialButtonToggleGroup>(R.id.btnsCategory_size)
            selectedButtonId = mbtg.checkedButtonId;
            selectedButton = mbtg.findViewById<Button>(selectedButtonId)
            var size = selectedButton.text.toString()

            mbtg = alert.findViewById<MaterialButtonToggleGroup>(R.id.btnsCategory_shot)
            selectedButtonId = mbtg.checkedButtonId;
            selectedButton = mbtg.findViewById<Button>(selectedButtonId)
            var shot = selectedButton.text.toString()

            mbtg = alert.findViewById<MaterialButtonToggleGroup>(R.id.btnsCategory_Ice)
            selectedButtonId = mbtg.checkedButtonId;
            selectedButton = mbtg.findViewById<Button>(selectedButtonId)
            var ice = selectedButton.text.toString()

            mbtg = alert.findViewById<MaterialButtonToggleGroup>(R.id.btnsCategory_whip)
            selectedButtonId = mbtg.checkedButtonId;
            selectedButton = mbtg.findViewById<Button>(selectedButtonId)
            var whip = selectedButton.text.toString()

            mbtg = alert.findViewById<MaterialButtonToggleGroup>(R.id.btnsCategory_drizzle)
            selectedButtonId = mbtg.checkedButtonId;
            selectedButton = mbtg.findViewById<Button>(selectedButtonId)
            var drizzle = selectedButton.text.toString()

            custom = "$temp, $size, 샷 추가: $shot, 얼음 $ice, 휘핑크림: $whip, 드리즐: $drizzle"

//            Toast.makeText(context, custom,Toast.LENGTH_SHORT).show()

            insertCart()
        }

        builder.setPositiveButton("확인", listener)
        builder.setNegativeButton("취소", null)
        builder.show()
    }

    private fun insertCart() {
        Toast.makeText(context,"상품이 장바구니에 담겼습니다.",Toast.LENGTH_SHORT).show()
        val menuId = productId
        val menuName = binding.txtMenuName.text.toString()
        val menuImg = menuImage
        val count = binding.textMenuCount.text.toString().toInt()
        val unitPrice = binding.txtMenuPrice.text.toString()
        val menuPrice = Integer.parseInt(unitPrice.replace(",", "").replace("원", "").trim())
        val totalPrice = menuPrice * count
        var type: String? = null
        if (menuImage!!.contains("coffee")) {
            type = "coffee"
        } else if(menuImage!!.contains("tea")) {
            type = "tea"
        } else {
            type = "cookie"
        }

        val shoppingCart = ShoppingCart(menuId, menuImg!!, menuName, count, custom, menuPrice, totalPrice, type)
        Cart.items.add(shoppingCart)
        Log.d(TAG, "cart 현 상태 ${Cart.items.size}: ${Cart.items}")
    }

    private fun insertComment(comment: String, productId: Int, rating: Float, userId: String) {
        val service = ApplicationClass.retrofit.create(CommentApi::class.java)
        val newComment =
            Comment(comment = comment, productId = productId, rating = rating, userId = userId)
        service.insert(newComment).enqueue(object : Callback<Boolean> {
            override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                Toast.makeText(requireContext(), "새 comment가 등록되었습니다.", Toast.LENGTH_SHORT).show()
                commentAdapter.list.add(MenuDetailWithCommentResponse(commentContent = newComment.comment, productCommentTotalCnt = 100, productRatingAvg = -100.0, userId = newComment.userId, productTotalSellCnt = -100, productPrice = -100, productName = "", productRating = -100.0, commentUserName = "", commentId = -100, type = "", productImg = ""))
                binding.recyclerViewMenuDetail.adapter = commentAdapter
                commentAdapter.notifyItemInserted(commentAdapter.itemCount)
                Log.d(TAG, "onResponse: ${commentAdapter.list.size}")
                isCommentSucceed = 1
            }
            override fun onFailure(call: Call<Boolean>, t: Throwable) {
                Toast.makeText(requireContext(), "새 comment 등록에 실패했습니다.", Toast.LENGTH_SHORT).show()
                isCommentSucceed = 0
            }
        })
    }

    private fun selectAllComments(productId: Int): MutableList<MenuDetailWithCommentResponse> {
        var result = mutableListOf<MenuDetailWithCommentResponse>()
        val service = ApplicationClass.retrofit.create(ProductApi::class.java)
        service.getProductWithComments(productId)
            .enqueue(object : Callback<List<MenuDetailWithCommentResponse>> {
                override fun onResponse(
                    call: Call<List<MenuDetailWithCommentResponse>>,
                    response: Response<List<MenuDetailWithCommentResponse>>
                ) {
                    if(response.code() == 200){
                        result = (response.body() ?: mutableListOf()) as MutableList<MenuDetailWithCommentResponse>
                        commentAdapter.list = result
                        commentAdapter.notifyDataSetChanged()
                        Log.d(TAG, "cAdpt : (${commentAdapter.list})")
                    }else{
                        Log.d(TAG, "onResponse 중 에러발생: ${response.code()}")
                    }
                }

                override fun onFailure(
                    call: Call<List<MenuDetailWithCommentResponse>>,
                    t: Throwable
                ) {
                    Log.d(TAG, "onFailure: 통신 오류")
                }
            })
        return result
    }


    companion object {
        @JvmStatic
        fun newInstance(key:String, value:Int) =
            MenuDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt(key, value)
                }
            }
    }

    inner class ProductWithCommentInsertCallback :
        RetrofitCallback<MutableList<MenuDetailWithCommentResponse>> {
        override fun onSuccess(
            code: Int,
            responseData: MutableList<MenuDetailWithCommentResponse>
        ) {
            if (responseData.isNotEmpty()) {

                Log.d(TAG, "initData: ${responseData}")

                // comment 가 없을 경우 -> 들어온 response가 1개이고 해당 userId 가 null일 경우 빈 배열 Adapter 연결
                commentAdapter = if (responseData.size == 1 && responseData[0].userId == null) {
                    CommentAdapter(arrayListOf(), productId)
                } else {
                    CommentAdapter(responseData, productId)
                }

                // 화면 정보 갱신
                setScreen(responseData[0])
            }

        }

        override fun onError(t: Throwable) {
            Log.d(TAG, t.message ?: "물품 정보 받아오는 중 통신오류")
        }

        override fun onFailure(code: Int) {
            Log.d(TAG, "onResponse: Error Code $code")
        }
    }



}
