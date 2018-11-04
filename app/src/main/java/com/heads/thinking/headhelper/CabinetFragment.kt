package com.heads.thinking.headhelper

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.EmailAuthProvider
import android.support.v7.app.AlertDialog
import android.widget.*
import com.firebase.ui.auth.AuthUI


class CabinetFragment : Fragment(), View.OnClickListener {

    var auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_cabinet, container, false)

        val changePasswordBtn = rootView.findViewById<Button>(R.id.changePasswordBtn)
        val changeGroupBtn = rootView.findViewById<Button>(R.id.changeGroupBtn)
        val signOutBtn = rootView.findViewById<Button>(R.id.signOutBtn)

        changeGroupBtn.setOnClickListener(this)
        signOutBtn.setOnClickListener(this)
        changePasswordBtn.setOnClickListener(this)

        return rootView
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
    }

    companion object {
        @JvmStatic
        fun newInstance() = CabinetFragment()
    }

    override fun onClick(view: View?) {
        when(view!!.id) {
            R.id.changeGroupBtn -> {}
            R.id.changePasswordBtn -> {
                onCreateChangePasswordDialog().show()
            }
            R.id.signOutBtn -> {
                signOut()
            }
            else -> false
        }
    }

    fun signOut() {
        AlertDialog.Builder(this.context!!).setTitle("Выйти из аккаунта?")
                .setPositiveButton("Выйти", {dialogInterface, i ->
                    AuthUI.getInstance().signOut(this.context!!).addOnCompleteListener{
                        if(it.isSuccessful)
                            startActivity(Intent(this.context, SplashActivity::class.java))
                        else
                            Toast.makeText(this.context, "Не получается выйти из аккаунта", Toast.LENGTH_SHORT)
                    }
                })
                .setNegativeButton("Отмена", null)
                .show()
    }

    fun onCreateChangePasswordDialog(): Dialog {
        val builder = AlertDialog.Builder(this.context!!)

        val inflater = this.layoutInflater
        val dialogLayout = inflater.inflate(R.layout.dialog_change_password, view?.findViewById(R.id.dialogLayout))
        val oldPassword = dialogLayout.findViewById(R.id.oldPasswordET) as EditText
        val newPassword = dialogLayout.findViewById(R.id.newPasswordET) as EditText

        builder.setView(dialogLayout)
                .setPositiveButton("Сменить пароль", { dialog, id ->
                    val oldPasswordString = oldPassword.text.toString()
                    val newPasswordString = newPassword.text.toString()
                    if(newPassword.length() < 6)
                        Toast.makeText(this.context, "Пароль должен быть больше 6 символов. Попробуйте еще раз", Toast.LENGTH_SHORT)
                    if(oldPassword.onCheckIsTextEditor() && newPassword.onCheckIsTextEditor()) {
                        val userTemp = auth.getCurrentUser()
                        val credential = EmailAuthProvider.getCredential(userTemp?.getEmail()!!, oldPasswordString)
                        userTemp?.reauthenticate(credential)?.addOnCompleteListener{ task ->
                            if (task.isSuccessful) {
                                userTemp.updatePassword(newPasswordString).addOnCompleteListener{ task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(this.context, "Пароль изменен", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(this.context, "Ошибка. Пароль не изменен", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(this.context, "Ошибка: " + task.exception!!.message, Toast.LENGTH_LONG).show()
                            }
                        }
                        }
                    })
                .setNegativeButton("Отмена", null)
        return builder.create()
    }
}
