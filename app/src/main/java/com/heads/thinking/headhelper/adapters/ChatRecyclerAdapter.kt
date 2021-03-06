package com.heads.thinking.headhelper.adapters

import android.support.v4.app.Fragment
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.heads.thinking.headhelper.R
import com.heads.thinking.headhelper.glide.loadImage
import com.heads.thinking.headhelper.models.Message
import com.heads.thinking.headhelper.models.User
import com.heads.thinking.headhelper.util.FirestoreUtil
import com.heads.thinking.headhelper.util.StorageUtil
import de.hdodenhof.circleimageview.CircleImageView

class ChatRecyclerAdapter(val fragment: Fragment, var messages: ArrayList<Message>, var members: HashMap<String, User>):
        RecyclerView.Adapter<ChatRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val viewHolder = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_recycler_view, parent, false)
        return ChatRecyclerAdapter.ViewHolder(viewHolder)
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // пользователь написавший сообщение может быть не в группе, поэтому надо подгрузить его данные отдельно
        if(!members.contains(messages[position].userRef)){
            FirestoreUtil.getUser(messages[position].userRef, { isSuccessful, user ->
                if(isSuccessful)
                    members.put(user!!.id, user)
                if(!this.fragment.isRemoving)
                    this.notifyDataSetChanged()
            })
        }
        viewHolder.bindItems(messages[position], members.get(messages[position].userRef))
    }

    fun updateMessages(newMessages: List<Message>) {
        val startAt: Int = messages.size
        val endAt: Int = newMessages.size
        for(i in startAt until endAt) {
            messages.add(newMessages[i])
        }
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        lateinit var mainLayout: FrameLayout
        lateinit var cardView: CardView
        lateinit var circleImageView: CircleImageView
        lateinit var messageTV: TextView
        lateinit var usernameTV: TextView

        fun bindItems(message: Message, user: User?) {
            mainLayout = itemView.findViewById(R.id.mainLayout)
            circleImageView = itemView.findViewById(R.id.circleImageView)
            messageTV = itemView.findViewById(R.id.messageTV)
            usernameTV = itemView.findViewById(R.id.usernameTV)
            cardView = itemView.findViewById(R.id.cardView)

            messageTV.text = message.textMessage
            if(user != null) {
                usernameTV.text = user.name
                if (user.profilePicturePath != null)
                    loadImage(StorageUtil.pathToReference(user.profilePicturePath), circleImageView.context, circleImageView)
                if(user.id == FirestoreUtil.currentUser?.id) {
                    val layoutParams: FrameLayout.LayoutParams = cardView.layoutParams as FrameLayout.LayoutParams
                    layoutParams.gravity = Gravity.LEFT
                    layoutParams.marginEnd = 80
                    layoutParams.marginStart = 10
                } else {
                    val layoutParams: FrameLayout.LayoutParams = cardView.layoutParams as FrameLayout.LayoutParams
                    layoutParams.gravity = Gravity.RIGHT
                    layoutParams.marginStart = 80
                    layoutParams.marginEnd = 10
                }
            }
        }
    }
}