package com.heads.thinking.headhelper

import android.app.Activity.RESULT_OK
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.support.v7.app.AlertDialog
import android.support.v7.widget.CardView
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.*
import com.firebase.ui.auth.AuthUI
import com.heads.thinking.headhelper.adapters.MembersRecyclerViewAdapter
import com.heads.thinking.headhelper.dialogs.ChangeGroupDialog
import com.heads.thinking.headhelper.dialogs.ChangePasswordDialog
import com.heads.thinking.headhelper.glide.GlideApp
import com.heads.thinking.headhelper.models.User
import com.heads.thinking.headhelper.mvvm.DataViewModel
import com.heads.thinking.headhelper.util.CustomImageManager
import com.heads.thinking.headhelper.util.StorageUtil
import com.heads.thinking.headhelper.util.FirestoreUtil
import kotlinx.android.synthetic.*

import java.io.ByteArrayOutputStream


class CabinetFragment : Fragment(), View.OnClickListener {

    private lateinit var avatarIV: ImageView
    private lateinit var usersNameTV: TextView
    private lateinit var groupIdTV: TextView

    private lateinit var membersCardView: CardView
    private lateinit var membersRecyclerView: RecyclerView
    private lateinit var membersRecyclerViewAdapter: MembersRecyclerViewAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_cabinet, container, false)

        //init views
        val makePhotoBtn: ImageButton = view.findViewById(R.id.makePhotoBtn)
        val changeGroupBtn: Button = view.findViewById(R.id.changeGroupBtn)
        val changePasswordBtn: Button = view.findViewById(R.id.changePasswordBtn)
        val signOutBtn: Button = view.findViewById(R.id.signOutBtn)

        avatarIV = view.findViewById(R.id.avatarIV)
        usersNameTV = view.findViewById(R.id.usernameTV)
        groupIdTV = view.findViewById(R.id.groupIdTV)
        //end init views

        membersCardView = view.findViewById(R.id.membersCardView)
        membersRecyclerView = view.findViewById(R.id.membersRecyclerView)
        membersRecyclerView.layoutManager = LinearLayoutManager(App.instance?.applicationContext,
                LinearLayoutManager.VERTICAL, false)
        membersRecyclerView.hasFixedSize()
        membersRecyclerViewAdapter = MembersRecyclerViewAdapter(ArrayList())
        membersRecyclerView.adapter = membersRecyclerViewAdapter

        makePhotoBtn.setOnClickListener(this)
        changeGroupBtn.setOnClickListener(this)
        changePasswordBtn.setOnClickListener(this)
        signOutBtn.setOnClickListener(this)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val dataViewModel : DataViewModel = ViewModelProviders.of(this).get(DataViewModel::class.java)
        dataViewModel.getUser().observe(this.activity!!, object : Observer<User> {
            override fun onChanged(changedUser: User?) {
                if (changedUser != null) {
                    usersNameTV.text = changedUser.name
                    groupIdTV.text = changedUser.groupId ?: ""
                    if (changedUser.profilePicturePath != null && this@CabinetFragment.activity != null)
                        GlideApp.with(this@CabinetFragment)
                                .load(StorageUtil.pathToReference(changedUser.profilePicturePath))
                                .into(avatarIV)
                }
            }
        })
        dataViewModel.getMembers().observe(this@CabinetFragment.activity!!, object : Observer<HashMap<String, User>> {
            override fun onChanged(map: HashMap<String, User>?) {
                if (map != null) {
                    membersRecyclerViewAdapter.members = ArrayList<User>().apply {
                        for (i in map)
                            add(i.value)
                    }
                    membersRecyclerViewAdapter.notifyDataSetChanged()
                }
            }
        })
    }

    override fun onClick(view: View?) {
        when(view!!.id) {
            R.id.changeGroupBtn -> {
                val dialogFragment = ChangeGroupDialog()
                dialogFragment.show(this.childFragmentManager, "changeGroup")
            }
            R.id.changePasswordBtn -> {
                val dialogFragment = ChangePasswordDialog()
                dialogFragment.show(this.childFragmentManager, "changePassword")
            }
            R.id.signOutBtn -> {
                signOut()
            }
            R.id.makePhotoBtn -> {
                CustomImageManager.getPhotoMakerIntent(this.activity!!)?.let {
                    startActivityForResult(it, CustomImageManager.REQUEST_CODE_TAKE_PHOTO)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CustomImageManager.REQUEST_CODE_TAKE_PHOTO -> if (resultCode == RESULT_OK) {
                if (data != null && data.data != null) {
                    setImage(data.data)
                } else if (CustomImageManager.imageUri != "" && CustomImageManager.tempPhoto != null) {
                    CustomImageManager.imageUri = Uri.fromFile(CustomImageManager.tempPhoto).toString()
                    setImage(Uri.parse(CustomImageManager.imageUri))
                } else {
                    Toast.makeText(this.context, "Не могу найти изображение. Попробуйте снова", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this.context, "Не могу найти изображение. Попробуйте снова", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //устанавливает изображение в imageView по Uri
    private fun setImage(uri: Uri) {
        var selectedImageBytes: ByteArray
        val selectedImagePath = uri.toString()
        val selectedImageBmp = MediaStore.Images.Media
                .getBitmap(activity?.contentResolver, Uri.parse(selectedImagePath))

        val outputStream = ByteArrayOutputStream()
        if (selectedImageBmp != null) {
            selectedImageBmp.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            selectedImageBytes = outputStream.toByteArray()

            StorageUtil.uploadProfilePhoto(selectedImageBytes, { refPath: String ->
                FirestoreUtil.updateCurrentUserData("",null, refPath, null)
                Toast.makeText(App.instance, "Фото загружено", Toast.LENGTH_SHORT).show()
            })
        } else {
            Toast.makeText(App.instance, "Не удалось загрузить изображение", Toast.LENGTH_SHORT).show()
        }
    }

    //выход из акккаунта
    fun signOut() {
        AlertDialog.Builder(this.context!!).setTitle("Выйти из аккаунта?")
                .setPositiveButton("Выйти", {dialogInterface, i ->
                    FirestoreUtil.userSignOut()
                    AuthUI.getInstance().signOut(this.context!!).addOnCompleteListener{
                        if(it.isSuccessful)
                            startActivity(Intent(this.context, SplashActivity::class.java))
                        else
                            Toast.makeText(App.instance, "Не получается выйти из аккаунта", Toast.LENGTH_SHORT).show()
                    }
                })
                .setNegativeButton("Отмена", null)
                .show()
    }
}
