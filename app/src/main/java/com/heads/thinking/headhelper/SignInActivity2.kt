package com.heads.thinking.headhelper

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*

// НЕИСПОЛЬЗУЕМАЯ АКТИВНОСТЬ

class SignInActivity2 : AppCompatActivity(), View.OnClickListener {

    lateinit var auth: FirebaseAuth

    lateinit var emailET: EditText
    lateinit var passwordET: EditText
    lateinit var autoLogin: CheckBox

    lateinit var signInBtn: Button
    lateinit var signUpBtn: Button
    lateinit var forgotPasswordBtn: Button

    lateinit var user: FirebaseUser


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in2)
        //init view
        auth = FirebaseAuth.getInstance()
        emailET = findViewById(R.id.emailET)
        passwordET = findViewById(R.id.passwordET)
        autoLogin = findViewById(R.id.checkAutoLogin)
        signInBtn = findViewById(R.id.signInBtn)
        signUpBtn = findViewById(R.id.signUpBtn)
        forgotPasswordBtn = findViewById(R.id.forgotPasswordBtn)

        /*TODO: check autologin in SharedPreferences and try SignIn
            check Intent. If parents intent is MainActivity ? dont autoSignIn : checkAutoLogin
         */
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
    }

    override fun onClick(view: View?) {
        when(view!!.id) {
            R.id.signInBtn ->{
                //add loading screen
                signIn(emailET.text.toString(), passwordET.text.toString())
            }
            R.id.signUpBtn -> {
                startActivity(Intent(this, SignUpActivity2::class.java))
            }
            R.id.forgotPasswordBtn -> {
                //TODO: create forgot password Dialog
            }

        }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, {
                    if(it.isSuccessful()) {
                        completeSignIn(it)
                    }
                    else {
                        failedSignIn(it)
                        // User cant sign in app. No connection or other errors
                        // What am i should doing?
                    }
                })
    }

    private fun completeSignIn(task: Task<AuthResult>) {
        user = auth.currentUser!!
        if(autoLogin.isChecked)
        {
            //TODO: write email/pass to SharedPreferences
        }
        //TODO: get username, groupId from Firebase
        startActivity(Intent(this, SignUpActivity2::class.java).apply {
            putExtra("username", "username")
            putExtra("groupId", "groupId")
        })
    }

    private fun failedSignIn(task: Task<AuthResult>) {
        when(task.exception) {
            is FirebaseNetworkException -> {
                Toast.makeText(this, "Проверьте интернет соединение",
                        Toast.LENGTH_SHORT).show()
            }
            is FirebaseAuthInvalidUserException -> {
                Toast.makeText(this, "Не верно введены почта или пароль.\n" +
                        " Проверьте, пожалуйста.", Toast.LENGTH_SHORT).show()
            }
            is FirebaseAuthEmailException -> {
                Toast.makeText(this, "Не верно введена почат.\n" +
                        " Проверьте пожалуйста", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
