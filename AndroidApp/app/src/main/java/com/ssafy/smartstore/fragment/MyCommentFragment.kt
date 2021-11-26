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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.ssafy.smartstore.R
import com.ssafy.smartstore.activity.MainActivity
import com.ssafy.smartstore.adapter.MyCommentAdapter
import com.ssafy.smartstore.dto.Comment
import com.ssafy.smartstore.service.UserService
import com.ssafy.smartstore.util.RetrofitCallback
import com.ssafy.smartstore.dto.User
import org.json.JSONObject

private const val TAG = "싸피"
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private lateinit var mainActivity: MainActivity

class MyCommentFragment : Fragment() {
    private var myId: String? = null
    private var myPw: String? = null
    private lateinit var myCommentAdapter : MyCommentAdapter
    private lateinit var myCommentList : ArrayList<Comment>
    private lateinit var root: View

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainActivity.hideBottomNav(true)
        arguments?.let {
            myId = it.getString(ARG_PARAM1)
            myPw = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.fragment_my_comment, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getMyCommentFromServer()
        root.findViewById<FloatingActionButton>(R.id.floatingActionButton).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun getMyCommentFromServer() {
        val user = User(myId!!, myPw!!)
        UserService().getMyComment(user, MyCommentCallback())
    }

    inner class MyCommentCallback : RetrofitCallback<HashMap<String,Any>> {

        override fun onSuccess(code: Int, responseData: HashMap<String, Any>) {
            val jsonRes = Gson().toJson(responseData["comment"])
            val comment: Array<Comment> = Gson().fromJson(
                jsonRes,
                Array<Comment>::class.java
            )
            myCommentList = comment.toCollection(ArrayList())
            myCommentAdapter = MyCommentAdapter(myCommentList)
            myCommentAdapter.list = myCommentList // 안해도되나
            root.findViewById<RecyclerView>(R.id.rcvMyComment).apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                adapter = myCommentAdapter
                adapter!!.stateRestorationPolicy =
                    RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
            }
        }
        override fun onError(t: Throwable) {
        }
        override fun onFailure(code: Int) {
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        mainActivity.hideBottomNav(false)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyCommentFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

