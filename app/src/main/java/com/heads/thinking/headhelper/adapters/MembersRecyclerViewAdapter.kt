package com.heads.thinking.headhelper.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import com.heads.thinking.headhelper.App
import com.heads.thinking.headhelper.R
import com.heads.thinking.headhelper.glide.loadImage
import com.heads.thinking.headhelper.models.User
import com.heads.thinking.headhelper.util.FirestoreUtil
import com.heads.thinking.headhelper.util.StorageUtil
import de.hdodenhof.circleimageview.CircleImageView

class MembersRecyclerViewAdapter(var members: ArrayList<User>) : RecyclerView.Adapter<MembersRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val viewHolder = LayoutInflater.from(parent.context).inflate(R.layout.item_members_recycler_view, parent, false)
        return ViewHolder(viewHolder)
    }

    override fun getItemCount(): Int {
        return members.size
    }

    fun getModeratorsList() : ArrayList<User> {
        return ArrayList<User>().apply {
            for(user in members)
                if(user.privilege > 0)
                    if(FirestoreUtil.currentUser?.id != user.id)
                        add(user)
        }
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            viewHolder.bindItems(members[position])
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        lateinit var usernameTV: TextView
        lateinit var membersAvatar: CircleImageView
        lateinit var privilegesRadioGroup: RadioGroup
        lateinit var noPrivRadioButton: RadioButton
        lateinit var moderatorRadioButton: RadioButton

        fun bindItems(user: User) {
            usernameTV = itemView.findViewById(R.id.usernameTV)
            membersAvatar = itemView.findViewById(R.id.circleImageView)
            privilegesRadioGroup = itemView.findViewById(R.id.privilegesRadioGroup)
            noPrivRadioButton = itemView.findViewById(R.id.radioButton1)
            moderatorRadioButton = itemView.findViewById(R.id.radioButton2)

            val onComlete : (isSuccessful: Boolean, message: String) -> Unit = {
                isSuccessful: Boolean, message: String ->
                if(!isSuccessful) {
                    Toast.makeText(App.instance, "Не получилось поменять привилегии\n" + message, Toast.LENGTH_SHORT).show()
                }
            }
            noPrivRadioButton.setOnClickListener {
                FirestoreUtil.updateMembersPrivileges(user.id, 0, onComlete)
            }
            moderatorRadioButton.setOnClickListener {
                FirestoreUtil.updateMembersPrivileges(user.id, 1, onComlete)
            }

            usernameTV.setText(user.name)
            if(user.profilePicturePath != null) {
                loadImage(
                        StorageUtil.pathToReference(user.profilePicturePath), membersAvatar.context, membersAvatar)
            }

            if(FirestoreUtil.currentUser?.privilege ?: 0 == 2) {
                privilegesRadioGroup.visibility = View.VISIBLE
                when(user.privilege) {
                    0 -> privilegesRadioGroup.check(R.id.radioButton1)
                    1 -> privilegesRadioGroup.check(R.id.radioButton2)
                    2 -> privilegesRadioGroup.visibility = View.GONE
                }
            }
        }
    }
}