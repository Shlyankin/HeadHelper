package com.heads.thinking.headhelper.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import com.heads.thinking.headhelper.App
import com.heads.thinking.headhelper.R
import com.heads.thinking.headhelper.glide.GlideApp
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

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            viewHolder.bindItems(members[position])
        viewHolder.privilegesRadioGroup.setOnCheckedChangeListener { radioGroup, id ->
            val onComlete : (isSuccessful: Boolean, message: String) -> Unit = {
                isSuccessful: Boolean, message: String ->
                if(!isSuccessful) {
                    Toast.makeText(App.instance, "Не получилось поменять привилегии\n" + message, Toast.LENGTH_SHORT).show()
                }
            }
            when(id) {
                R.id.radioButton1 -> FirestoreUtil.updateMembersPrivileges(members[position].id, 0, onComlete)
                R.id.radioButton2 -> FirestoreUtil.updateMembersPrivileges(members[position].id, 1, onComlete)
            }
        }
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        lateinit var usernameTV: TextView
        lateinit var membersAvatar: CircleImageView
        lateinit var privilegesRadioGroup: RadioGroup

        fun bindItems(user: User) {
            usernameTV = itemView.findViewById(R.id.usernameTV)
            membersAvatar = itemView.findViewById(R.id.circleImageView)
            privilegesRadioGroup = itemView.findViewById(R.id.privilegesRadioGroup)

            usernameTV.setText(user.name)
            if(user.profilePicturePath != null) {
                GlideApp.with(membersAvatar)
                        .load(StorageUtil.pathToReference(user.profilePicturePath))
                        .into(membersAvatar)
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