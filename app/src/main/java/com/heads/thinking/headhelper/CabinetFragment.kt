package com.heads.thinking.headhelper

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import android.support.v7.app.AlertDialog
import android.widget.*
import com.firebase.ui.auth.AuthUI
import com.heads.thinking.headhelper.dialogs.ChangeGroupDialog
import com.heads.thinking.headhelper.dialogs.ChangePasswordDialog
import com.heads.thinking.headhelper.glide.GlideApp
import com.heads.thinking.headhelper.util.CustomImageManager
import com.heads.thinking.headhelper.util.StorageUtil
import com.heads.thinking.headhelper.util.CustomFirestoreUtil

import java.io.ByteArrayOutputStream


class CabinetFragment : Fragment(), View.OnClickListener {


    private lateinit var selectedImageBytes: ByteArray

    private lateinit var avatarIV: ImageView
    private lateinit var usersNameTV: TextView

    var auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        CustomFirestoreUtil.getCurrentUser {
            if (this.isVisible) {
                usersNameTV.setText(it.name)
                if (it.profilePicturePath != null)
                    GlideApp.with(this)
                            .load(StorageUtil.pathToReference(it.profilePicturePath))
                            .into(avatarIV)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_cabinet, container, false)

        avatarIV = view.findViewById(R.id.avatarIV)

        val makePhotoBtn: ImageButton = view.findViewById(R.id.makePhotoBtn)
        val changeGroupBtn: Button = view.findViewById(R.id.changeGroupBtn)
        val changePasswordBtn: Button = view.findViewById(R.id.changePasswordBtn)
        val signOutBtn: Button = view.findViewById(R.id.signOutBtn)
        usersNameTV = view.findViewById(R.id.usernameTV)

        makePhotoBtn.setOnClickListener(this)
        changeGroupBtn.setOnClickListener(this)
        changePasswordBtn.setOnClickListener(this)
        signOutBtn.setOnClickListener(this)

        return view
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

    fun setImage(uri: Uri) {
        val selectedImagePath = uri.toString()
        val selectedImageBmp = MediaStore.Images.Media
                .getBitmap(activity?.contentResolver, Uri.parse(selectedImagePath))

        val outputStream = ByteArrayOutputStream()
        if (selectedImageBmp != null) {
            selectedImageBmp.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            selectedImageBytes = outputStream.toByteArray()

            StorageUtil.uploadProfilePhoto(selectedImageBytes, { refPath: String ->
                CustomFirestoreUtil.updateCurrentUser(usersNameTV.text.toString(), refPath, null)
                GlideApp.with(this)
                        .load(StorageUtil.pathToReference(refPath))
                        .into(avatarIV)
                Toast.makeText(App.instance, "Фото загружено", Toast.LENGTH_SHORT).show()
            })
        } else {
            Toast.makeText(App.instance, "Не удалось загрузить изображение", Toast.LENGTH_SHORT).show()
        }
    }

    fun signOut() {
        AlertDialog.Builder(this.context!!).setTitle("Выйти из аккаунта?")
                .setPositiveButton("Выйти", {dialogInterface, i ->
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
