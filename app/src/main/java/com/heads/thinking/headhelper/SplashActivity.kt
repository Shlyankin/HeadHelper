package com.heads.thinking.headhelper

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.heads.thinking.headhelper.dialogs.ChangeGroupDialog

class SplashActivity : AppCompatActivity() {

    val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun onStart() {
        super.onStart()
        startActivity(
                if(currentUser != null) {
                    if(currentUser.isEmailVerified) {
                        val dialogFragment = ChangeGroupDialog()
                        Intent(this, MainActivity::class.java)
                    }
                    else {
                        currentUser.sendEmailVerification().addOnCompleteListener {
                            if(it.isSuccessful) Toast.makeText(this, "Ваша почта не подтверждена. " +
                                    "Вам отправлено повторное письмо на почту: ${currentUser.email}", Toast.LENGTH_SHORT)
                            else Toast.makeText(this, "Ваша почта не подтверждена. " +
                                    "Не получаеся оптравить письмо с подтверждением на почту: ${currentUser.email}", Toast.LENGTH_SHORT)
                        }
                        Intent(this, SignInActivity::class.java)
                    }
                } else Intent(this, SignInActivity::class.java)
        )
    }
}
