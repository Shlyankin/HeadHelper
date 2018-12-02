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

    private lateinit var idGroupET: EditText

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

        //не получается выводить крутилку, т.к. не получается обработать поворот экрана. isAdded, context != null возвращают true
        builder.setTitle("Смена группы")
        builder.setPositiveButton("Вступить в группу", { dialogInterface: DialogInterface, i: Int ->
                FirestoreUtil.changeGroup(idGroupET.text.toString(), { isSuccessful: Boolean, message: String ->
                    if(isSuccessful)
                        Toast.makeText(App.instance, "Вы сменили группу", Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(App.instance, "Не получилось сменить группу\n" + message, Toast.LENGTH_SHORT).show()
                })
                })
                .setNeutralButton("Создать группу", { dialogInterface: DialogInterface, i: Int ->
                    FirestoreUtil.createGroup(idGroupET.text.toString()) { isSuccessful: Boolean, message: String ->
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