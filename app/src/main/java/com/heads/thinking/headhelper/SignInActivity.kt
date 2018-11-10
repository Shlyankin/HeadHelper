package com.heads.thinking.headhelper

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.CardView
import android.view.View
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.heads.thinking.headhelper.util.CustomFirestoreUtil
import kotlinx.android.synthetic.main.activity_sign_in.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.design.longSnackbar
import org.jetbrains.anko.indeterminateProgressDialog
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask

class SignInActivity : AppCompatActivity(), View.OnClickListener {

    val TAG = "SignIn"

    private val CODE_SIGN_IN = 1;
    private lateinit var signInBtn: CardView

    private var signProvider = listOf(AuthUI.IdpConfig.EmailBuilder()
            .setAllowNewAccounts(true)
            .setRequireName(true)
            .build())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        signInBtn = findViewById(R.id.signInBtn)
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.signInBtn -> {
                val intent = AuthUI.getInstance().createSignInIntentBuilder()
                        .setAvailableProviders(signProvider)
                        .setLogo(R.drawable.logo)
                        .setTheme(R.style.AppCompat)
                        .build()
                startActivityForResult(intent, CODE_SIGN_IN)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CODE_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                /*
                val progressDialog = indeterminateProgressDialog("Setting up your account")
                CustomFirestoreUtil.initCurrentUserIfFirstTime {
                    startActivity(intentFor<MainActivity>().newTask().clearTask())

                    val registrationToken = FirebaseInstanceId.getInstance().token
                    MyFirebaseInstanceIDService.addTokenToFirestore(registrationToken)

                    progressDialog.dismiss()
                }
                 */
                val progressDialog = indeterminateProgressDialog("Проверка вашего аккаунта")
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    if (!currentUser.isEmailVerified) {
                        currentUser.sendEmailVerification()
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        Toast.makeText(this, "Подтверждение аккаунта выслано на почту: ${currentUser.email}", Toast.LENGTH_SHORT).show()
                                        progressDialog.dismiss()
                                    } else {
                                        Toast.makeText(this, "Не удалось отправить подтверждение на почту: ${currentUser.email}", Toast.LENGTH_SHORT).show()
                                        progressDialog.dismiss()
                                    }
                                }
                    } else {
                        CustomFirestoreUtil.initCurrentUserIfFirstTime {
                            startActivity(intentFor<MainActivity>().newTask().clearTask())
                            progressDialog.dismiss()
                        }
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                if (response == null) return

                when (response.error?.errorCode) {
                    ErrorCodes.NO_NETWORK ->
                        longSnackbar(constraint_layout, "No network")
                    ErrorCodes.UNKNOWN_ERROR ->
                        longSnackbar(constraint_layout, "Unknown error")
                }
            }
        }
    }
}
