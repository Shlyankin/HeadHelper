package com.heads.thinking.headhelper.ui.fragments

import android.app.Activity.RESULT_OK
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.DialogInterface
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
import android.support.v7.widget.LinearLayoutManager
import android.widget.*
import com.firebase.ui.auth.AuthUI
import com.heads.thinking.headhelper.App
import com.heads.thinking.headhelper.R
import com.heads.thinking.headhelper.ui.activities.SplashActivity
import com.heads.thinking.headhelper.adapters.MembersRecyclerViewAdapter
import com.heads.thinking.headhelper.dialogs.ChangeGroupDialog
import com.heads.thinking.headhelper.dialogs.ChangePasswordDialog
import com.heads.thinking.headhelper.glide.loadImage
import com.heads.thinking.headhelper.models.User
import com.heads.thinking.headhelper.mvvm.DataViewModel
import com.heads.thinking.headhelper.util.CustomImageManager
import com.heads.thinking.headhelper.glide.CustomRequestListener
import com.heads.thinking.headhelper.util.StorageUtil
import com.heads.thinking.headhelper.util.FirestoreUtil
import kotlinx.android.synthetic.main.fragment_cabinet.*

import java.io.ByteArrayOutputStream


class CabinetFragment : Fragment(), View.OnClickListener {

    private lateinit var membersRecyclerViewAdapter: MembersRecyclerViewAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_cabinet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // set btn listeners
        changePasswordBtn.setOnClickListener(this)
        changeGroupBtn.setOnClickListener(this)
        signOutBtn.setOnClickListener(this)
        makePhotoBtn.setOnClickListener(this)


        membersRecyclerView.layoutManager = LinearLayoutManager(App.instance?.applicationContext,
                LinearLayoutManager.VERTICAL, false)
        membersRecyclerView.hasFixedSize()
        membersRecyclerViewAdapter = MembersRecyclerViewAdapter(ArrayList())
        membersRecyclerView.adapter = membersRecyclerViewAdapter
        val dataViewModel : DataViewModel = ViewModelProviders.of(this).get(DataViewModel::class.java)
        dataViewModel.getUser().observe(this@CabinetFragment, object : Observer<User> {
            override fun onChanged(changedUser: User?) {
                if (changedUser != null) {
                    usernameTV.text = changedUser.name
                    groupIdTV.text = changedUser.groupId ?: ""
                    if (changedUser.profilePicturePath != null && this@CabinetFragment.activity != null)
                        loadImage(StorageUtil.pathToReference(changedUser.profilePicturePath),
                                this@CabinetFragment.context, avatarIV, CustomRequestListener {
                            progressBar.visibility = View.GONE
                        })
                    else progressBar.visibility = View.GONE
                }
            }
        })
        dataViewModel.getMembers().observe(this@CabinetFragment, object : Observer<HashMap<String, User>> {
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

    override fun onClick(view: View) {
        when(view.id) {
            R.id.changeGroupBtn -> {
                val user = FirestoreUtil.currentUser
                if(user != null) {
                    if (user.privilege < 2) {
                        val dialogFragment = ChangeGroupDialog()
                        dialogFragment.show(this.childFragmentManager, "changeGroup")
                    } else {
                        val moderators = membersRecyclerViewAdapter.getModeratorsList()
                        val moderatorsNames = Array<String>(moderators.size, {
                            moderators[it].name
                        })
                        if(membersRecyclerViewAdapter.members.size > 1) {
                            AlertDialog.Builder(this.context!!).setTitle("Выберите нового админа из ваших модераторов")
                                .setItems(moderatorsNames, { dialogInterface: DialogInterface, position: Int ->
                                    FirestoreUtil.updateMembersPrivileges(moderators[position].id, 2, { isSuccessful, message ->
                                        if (isSuccessful) {
                                            val dialogFragment = ChangeGroupDialog()
                                            dialogFragment.show(this.childFragmentManager, "changeGroup")
                                        }
                                    })
                                })
                                .setNegativeButton("Отмена", null)
                                .setCancelable(false)
                                .create().show()
                        } else {
                            val dialogFragment = ChangeGroupDialog()
                            dialogFragment.show(this.childFragmentManager, "changeGroup")
                        }
                    }
                }
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
