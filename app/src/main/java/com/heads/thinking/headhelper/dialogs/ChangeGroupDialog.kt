package com.heads.thinking.headhelper.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.heads.thinking.headhelper.App
import com.heads.thinking.headhelper.R
import com.heads.thinking.headhelper.util.FirestoreUtil
import kotlinx.coroutines.delay
import org.jetbrains.anko.support.v4.indeterminateProgressDialog


class ChangeGroupDialog: DialogFragment() {

    lateinit var idGroupET: EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_change_group, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = activity?.layoutInflater
        val builder: AlertDialog.Builder = AlertDialog.Builder(this.activity!!)
        val layoutInflater = inflater?.inflate(R.layout.dialog_change_group, null)
        builder.setView(layoutInflater)

        idGroupET = layoutInflater!!.findViewById(R.id.groupIdET)



        builder.setTitle("Смена группы")
        builder.setPositiveButton("Вступить в группу", { dialogInterface: DialogInterface, i: Int ->
            val dialog = indeterminateProgressDialog("Меняем группу") // создаю диалоговое окно
            dialog.setCancelable(false)
            dialog.setCanceledOnTouchOutside(false)
                FirestoreUtil.changeGroup(idGroupET.text.toString(), { isSuccessful: Boolean, message: String ->
                    object : CountDownTimer(1500, 1000) {
                        override fun onFinish() {
                            //TODO проверка
                                dialog.dismiss()
                        }
                        override fun onTick(p0: Long) {}
                    }.start()
                    if(isSuccessful)
                        Toast.makeText(App.instance, "Вы сменили группу", Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(App.instance, "Не получилось сменить группу\n" + message, Toast.LENGTH_SHORT).show()
                })
                })
                .setNeutralButton("Создать группу", { dialogInterface: DialogInterface, i: Int ->
                    val dialog = indeterminateProgressDialog("Создаем группу")
                    dialog.setCancelable(false)
                    dialog.setCanceledOnTouchOutside(false)
                    FirestoreUtil.createGroup(idGroupET.text.toString()) { isSuccessful: Boolean, message: String ->
                        object : CountDownTimer(2000, 1000) {
                            override fun onFinish() {
                                //TODO проверка
                                    dialog.dismiss()
                            }
                            override fun onTick(p0: Long) {}
                        }.start()
                        if(isSuccessful)
                            Toast.makeText(App.instance, "Вы создали группу", Toast.LENGTH_SHORT).show()
                        else
                            Toast.makeText(App.instance, "Не получилось создать группу\n" + message, Toast.LENGTH_SHORT).show()
                    }
                })
                .setNegativeButton("Отмена", null)
        return builder.create()
    }
}