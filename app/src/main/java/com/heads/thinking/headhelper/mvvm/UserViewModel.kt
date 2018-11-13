package com.heads.thinking.headhelper.mvvm

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestoreException
import com.heads.thinking.headhelper.models.User
import com.heads.thinking.headhelper.util.FirestoreUtil

class UserViewModel(application: Application) : AndroidViewModel(application) {

    var user : MutableLiveData<User>? = null

    fun getUser(): LiveData<User> {
        if(user == null) {
            user = MutableLiveData<User>()
            user!!.postValue(User())
            FirestoreUtil.addUserListener { documentSnapshot: DocumentSnapshot?,
                                            firebaseFirestoreException: FirebaseFirestoreException? ->
                if (firebaseFirestoreException != null && documentSnapshot?.exists() ?: true)
                else {
                    user!!.postValue(documentSnapshot?.toObject(User::class.java)!!)
                }
            }
        }
        return user as MutableLiveData<User>
    }
}
