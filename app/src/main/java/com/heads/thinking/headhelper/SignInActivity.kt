package com.heads.thinking.headhelper

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class SignInActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)
    }

    override fun onClick(view: View?) {
        when(view!!.id) {
            R.id.signInBtn ->{

            }
            R.id.signUpBtn -> {

            }
            R.id.forgotPasswordBtn -> {

            }

        }
    }
}
