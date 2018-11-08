package com.heads.thinking.headhelper.dialogs

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import org.jetbrains.anko.layoutInflater
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.heads.thinking.headhelper.R
import com.heads.thinking.headhelper.util.FirestoreUtil


class ChangeGroupDialog() : DialogFragment() {

    lateinit var idGroupET: EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_change_group, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this!!.activity!!)
        val inflater = getActivity()?.getLayoutInflater()
        val layoutInflater = inflater?.inflate(R.layout.dialog_change_group, null)
        if (layoutInflater != null) {
            idGroupET = layoutInflater.findViewById(R.id.groupIdET)
        }

        builder.setView(layoutInflater)

        builder.setTitle("Смена группы")
        builder.setPositiveButton("Вступить в группу", { dialogInterface: DialogInterface, i: Int ->
                FirestoreUtil.changeGroup(idGroupET.text.toString(), {})
                })
                .setNeutralButton("Создать группу", { dialogInterface: DialogInterface, i: Int ->
                    FirestoreUtil.createGroup {}
                })
                .setNegativeButton("Отмена", null)
        return builder.create()
    }
}