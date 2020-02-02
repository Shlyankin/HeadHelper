package com.heads.thinking.headhelper.ui.activities

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.heads.thinking.headhelper.R
import com.heads.thinking.headhelper.util.FirestoreUtil

class SplashActivity : AppCompatActivity() {

    private val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun onStart() {
        super.onStart()
        startActivity(
                if(currentUser != null) {
                    if(currentUser.isEmailVerified) {
                        FirestoreUtil.userSignIn()
                        Intent(this, MainActivity::class.java)
                    }
                    else {
                        currentUser.sendEmailVerification().addOnCompleteListener {
                            if(it.isSuccessful) Toast.makeText(this, "Ваша почта не подтверждена. " +
                                    "Вам отправлено повторное письмо на почту: ${currentUser.email}", Toast.LENGTH_SHORT).show()
                            else Toast.makeText(this, "Ваша почта не подтверждена. " +
                                    "Не получаеся оптравить письмо с подтверждением на почту: ${currentUser.email}", Toast.LENGTH_SHORT).show()
                        }
                        Intent(this, SignInActivity::class.java)
                    }
                } else Intent(this, SignInActivity::class.java)
        )
        finish()
    }
}
