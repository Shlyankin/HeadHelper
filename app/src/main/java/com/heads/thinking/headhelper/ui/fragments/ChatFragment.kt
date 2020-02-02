package com.heads.thinking.headhelper.ui.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.heads.thinking.headhelper.App
import com.heads.thinking.headhelper.R
import com.heads.thinking.headhelper.adapters.ChatRecyclerAdapter
import com.heads.thinking.headhelper.models.Message
import com.heads.thinking.headhelper.models.User
import com.heads.thinking.headhelper.mvvm.DataViewModel
import com.heads.thinking.headhelper.util.FirestoreUtil
import kotlinx.android.synthetic.main.fragment_chat.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class ChatFragment : Fragment(), View.OnClickListener {

    private lateinit var dataViewModel: DataViewModel
    private lateinit var chatRecyclerAdapter: ChatRecyclerAdapter

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.sendMessageBtn -> {
                val id = UUID.randomUUID().toString()
                val textMessage = editMessageET.text.toString()
                editMessageET.setText("")
                if(textMessage == "") {
                    Toast.makeText(this.context, "Вы ничего не ввели", Toast.LENGTH_SHORT).show()
                } else {
                    val message: Message = Message(id, FirestoreUtil.currentUser?.id!!, textMessage,
                            Calendar.getInstance().time, null)
                    FirestoreUtil.sendMessage(message, {
                        if(!it) Toast.makeText(this.context, "Не получилось отправить сообщение", Toast.LENGTH_SHORT).show()
                    })
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // set btn listeners
        sendMessageBtn.setOnClickListener(this)

        chatRecyclerView.layoutManager = LinearLayoutManager(App.instance?.applicationContext, LinearLayoutManager.VERTICAL, false)
        chatRecyclerView.hasFixedSize()
        chatRecyclerAdapter = ChatRecyclerAdapter(this, ArrayList<Message>(), HashMap<String, User>())
        chatRecyclerView.adapter = chatRecyclerAdapter

        dataViewModel = ViewModelProviders.of(this).get(DataViewModel::class.java)
        dataViewModel.getMembers().observe(this@ChatFragment, object : Observer<HashMap<String, User>> {
            override fun onChanged(membersMap: HashMap<String, User>?) {
                if(membersMap != null) {
                    chatRecyclerAdapter.members = membersMap
                    chatRecyclerAdapter.notifyDataSetChanged()
                }
            }
        })
        dataViewModel.getMessagesArray().observe(this@ChatFragment, object : Observer<ArrayList<Message>> {
            override fun onChanged(messages: ArrayList<Message>?) {
                progressBar.visibility = View.GONE
                if (messages != null) {
                    chatRecyclerAdapter.updateMessages(messages)
                    chatRecyclerAdapter.notifyDataSetChanged()
                    if(chatRecyclerView.verticalScrollbarPosition == chatRecyclerAdapter.itemCount - 2 || chatRecyclerView.verticalScrollbarPosition == 0) // check this
                        chatRecyclerView.getLayoutManager()
                                ?.scrollToPosition(chatRecyclerAdapter.itemCount - 1)
                }
            }
        })
    }
}
