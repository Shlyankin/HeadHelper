package com.heads.thinking.headhelper.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.heads.thinking.headhelper.R
import org.jetbrains.anko.find

class ChangePasswordDialog : DialogFragment() {

    lateinit var oldPasswordET: EditText
    lateinit var newPasswordET: EditText


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_change_password, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = getActivity()?.getLayoutInflater()
        val builder: AlertDialog.Builder = AlertDialog.Builder(this!!.activity!!)
        val layoutInflater = inflater?.inflate(R.layout.dialog_change_password, null)
        builder.setView(layoutInflater)

        newPasswordET = layoutInflater!!.findViewById(R.id.newPasswordET)
        oldPasswordET = layoutInflater!!.findViewById(R.id.oldPasswordET)

        builder.setTitle("Смена группы")
        builder.setPositiveButton("Сменить", { dialogInterface: DialogInterface, i: Int ->
            val oldPasswordString = oldPasswordET.text.toString()
            val newPasswordString = newPasswordET.text.toString()
            if(newPasswordET.length() < 6)
                Toast.makeText(this.context, "Пароль должен быть больше 6 символов. Попробуйте еще раз", Toast.LENGTH_SHORT)

            val userTemp = FirebaseAuth.getInstance().getCurrentUser()
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

        })
        builder.setNegativeButton("Отмена", null)
        return builder.create()
    }
}