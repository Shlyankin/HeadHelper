package com.heads.thinking.headhelper

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class SignUpActivity2 : AppCompatActivity(), View.OnClickListener {

    private lateinit var emailET: EditText
    private lateinit var passwordET: EditText
    private lateinit var repeatPasswordET: EditText
    private lateinit var idGroupET: EditText
    private lateinit var checkHeadman: CheckBox
    private lateinit var checkAutoLogin: CheckBox

    private lateinit var auth: FirebaseAuth
    private lateinit var user: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up2)

        emailET = findViewById(R.id.emailET)
        passwordET = findViewById(R.id.passwordET)
        repeatPasswordET = findViewById(R.id.repeatPasswordET)
        idGroupET = findViewById(R.id.idGroupET)
        checkHeadman = findViewById(R.id.checkHeadman)
        checkAutoLogin = findViewById(R.id.checkAutoLogin)
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.signUpBtn -> {
                val email = emailET.text.toString()
                val password = passwordET.text.toString()
                if(password == repeatPasswordET.text.toString())
                    signUp(email, password)
                else {
                    Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
                    passwordET.setText("")
                    repeatPasswordET.setText("")
                }
            }
        }
    }

    private fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this,
                {
                    if(it.isSuccessful) {
                        completeSignUp(it)
                    } else {
                        failedSignUp(it)
                    }
                })
    }

    private fun completeSignUp(task: Task<AuthResult>) {
        user = auth.currentUser!!
        if(checkHeadman.isChecked) {
            //TODO: create groupId { groupId:#123, chat: { #123: { username : HeadHelper, msg: Hi, group #123}} } in FirebaseDatabase
        }
        //TODO: write username, groupId FirebaseDatabase
        if(checkAutoLogin.isChecked) {
            //TODO: write email/username in SharedPreferences
        }
        onBackPressed() // to SignInActivity
    }

    fun failedSignUp(task: Task<AuthResult>) {
        when(task.exception) {
            is FirebaseNetworkException -> {
                Toast.makeText(this, "Проверьте интернет соединение",
                        Toast.LENGTH_SHORT).show()
            }
            //TODO: write other exc
            else -> {
                Toast.makeText(this, "Извините, ошибка.\n${task.exception}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
