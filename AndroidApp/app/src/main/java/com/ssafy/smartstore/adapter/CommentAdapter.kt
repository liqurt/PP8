package com.ssafy.smartstore.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ssafy.smartstore.R
import com.ssafy.smartstore.api.CommentApi
import com.ssafy.smartstore.config.ApplicationClass
import com.ssafy.smartstore.dto.Comment
import com.ssafy.smartstore.response.MenuDetailWithCommentResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val TAG = "싸피"

class CommentAdapter(var list: MutableList<MenuDetailWithCommentResponse>, var productId: Int) :
    RecyclerView.Adapter<CommentAdapter.CommentHolder>() {
    constructor() : this(arrayListOf<MenuDetailWithCommentResponse>(), -1)

    inner class CommentHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val me: String = ApplicationClass.sharedPreferencesUtil.getUser().id
        var myComment: EditText = itemView.findViewById(R.id.et_comment_content)
        var otherComment: TextView = itemView.findViewById(R.id.textNoticeContent)
        var save: ImageView = itemView.findViewById(R.id.iv_modify_accept_comment)
        var cancel: ImageView = itemView.findViewById(R.id.iv_modify_cancel_comment)
        var modify: ImageView = itemView.findViewById(R.id.iv_modify_comment)
        var delete: ImageView = itemView.findViewById(R.id.iv_delete_comment)


        fun bindInfo(position : Int, data: MenuDetailWithCommentResponse) {
            otherComment.text = data.commentContent

            myComment.visibility = View.GONE
            save.visibility = View.GONE
            cancel.visibility = View.GONE
            if (me != data.userId) {
                modify.visibility = View.GONE
                delete.visibility = View.GONE
            }

            modify.setOnClickListener {
                myComment.hint = otherComment.text.toString()
                otherComment.visibility = View.GONE
                myComment.visibility = View.VISIBLE

                save.visibility = View.VISIBLE
                cancel.visibility = View.VISIBLE
            }

            cancel.setOnClickListener {
                myComment.visibility = View.GONE
                otherComment.visibility = View.VISIBLE
                save.visibility = View.GONE
                cancel.visibility = View.GONE
            }

            //update
            save.setOnClickListener {
                val service = ApplicationClass.retrofit.create(CommentApi::class.java)
                val updateComment = Comment(
                    id = data.commentId,
                    userId = data.userId!!,
                    productId = productId,
                    comment = myComment.text.toString(),
                    rating = data.productRating.toFloat()
                )
                service.update(updateComment).enqueue(object : Callback<Boolean>{
                    override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                        Log.d(TAG, "onResponse(response): $response")
                        data.commentContent = myComment.text.toString()
                        list[position] = data
                        notifyItemChanged(position)
                    }
                    override fun onFailure(call: Call<Boolean>, t: Throwable) {
                        Log.d(TAG, "onFailure: 업데이트 안 됨")
                    }
                })
            }
            //delete
            delete.setOnClickListener {
                val service = ApplicationClass.retrofit.create(CommentApi::class.java)
                val commentId = data.commentId
                service.delete(commentId).enqueue(object : Callback<Boolean>{
                    override fun onResponse(call: Call<Boolean>, response: Response<Boolean>) {
                        Log.d(TAG, "onResponse: 삭제 됨")
                        itemView.visibility = View.GONE
                        list.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, list.size)
                    }
                    override fun onFailure(call: Call<Boolean>, t: Throwable) {
                        Log.d(TAG, "onFailure: 삭제 안 됨")
                    }
                })
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item_comment, parent, false)
        return CommentHolder(view)
    }
    override fun onBindViewHolder(holder: CommentHolder, position: Int) {
        holder.apply {
            bindInfo(position, list[position])
        }
    }
    override fun getItemCount(): Int {
        return list.size
    }
}

