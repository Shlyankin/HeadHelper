package com.heads.thinking.headhelper.util

import android.app.Activity
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.heads.thinking.headhelper.models.News
import com.heads.thinking.headhelper.models.User
import java.lang.Exception
import java.util.concurrent.Executor


object CustomFirestoreUtil {
    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val taskCanceled : Task<Void> = object: Task<Void>() {
        override fun isCanceled(): Boolean {
            return true
        }

        override fun isComplete(): Boolean {
            return false
        }

        override fun isSuccessful(): Boolean {
            return false
        }

        override fun getException(): Exception? {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun addOnFailureListener(p0: Executor, p1: OnFailureListener): Task<Void> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun addOnFailureListener(p0: Activity, p1: OnFailureListener): Task<Void> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun getResult(): Void? {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun <X : Throwable?> getResult(p0: Class<X>): Void? {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun addOnSuccessListener(p0: OnSuccessListener<in Void>): Task<Void> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun addOnSuccessListener(p0: Executor, p1: OnSuccessListener<in Void>): Task<Void> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun addOnSuccessListener(p0: Activity, p1: OnSuccessListener<in Void>): Task<Void> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun addOnFailureListener(p0: OnFailureListener): Task<Void> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }


    }

    private val currentUserDocRef: DocumentReference
        get() = firestoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}")

    fun initCurrentUserIfFirstTime(onComplete: () -> Unit) {
        currentUserDocRef.get().addOnSuccessListener { documentSnapshot ->
            if (!documentSnapshot.exists()) {
                val newUser = User(FirebaseAuth.getInstance().currentUser?.displayName ?: "", null, mutableListOf(), "")
                currentUserDocRef.set(newUser).addOnSuccessListener {
                    onComplete()
                }
            }
            else
                onComplete()
        }
    }

    fun updateCurrentUser(name: String = "", profilePicturePath: String? = null, groupId: String?) {
        val userFieldMap = mutableMapOf<String, Any>()
        if (name.isNotBlank()) userFieldMap["name"] = name
        if (profilePicturePath != null)
            userFieldMap["profilePicturePath"] = profilePicturePath
        if (groupId != null) {
            val groupReference = firestoreInstance.collection("groups").document("$groupId")
            if (groupReference.get().exception != null && groupReference.get().isSuccessful) {
                userFieldMap["groupId"] = groupId
                val member = mutableMapOf<String, Any>()
                member ["${FirebaseAuth.getInstance().currentUser?.uid
                        ?: throw NullPointerException("UID is null.")}"] = FirebaseAuth.getInstance().currentUser!!.displayName.toString()
                firestoreInstance.collection("groups")
                        .document("${groupId}/members/${FirebaseAuth.getInstance().currentUser?.uid}").update(member)
            }
        }
        currentUserDocRef.update(userFieldMap)
    }

    fun getCurrentUser(onComplete: (User) -> Unit) {
        currentUserDocRef.get()
                .addOnSuccessListener {
                    onComplete(it.toObject(User::class.java)!!)
                }
    }

    fun createGroup(newGroupId: String, onComplete: (Task<Void>) -> Unit) {
        if(newGroupId == "") {
            onComplete(taskCanceled)
            return
        }
        getCurrentUser {user: User ->
            val newGroupRef = firestoreInstance.collection("groups").document(newGroupId)
            if(newGroupRef.get().exception == null && newGroupRef.get().isSuccessful) {
                if (user.groupId != null && user.groupId != "")
                    firestoreInstance.collection("groups").document("${user.groupId}")
                            .collection("members").document(FirebaseAuth.getInstance().currentUser?.uid.toString()).delete()
                val allGroupsRef = firestoreInstance.collection("groups")
                val currentGroupRef = allGroupsRef.document(newGroupId)
                val curMemberRef = currentGroupRef.collection("members").document(FirebaseAuth.getInstance().currentUser?.uid.toString())

                curMemberRef.set(User(it@ user.name, it@ user.profilePicturePath, it@ user.registrationTokens, newGroupId))
                        .addOnCompleteListener() {
                            if (it.isSuccessful)
                                updateCurrentUser(it@ user.name, it@ user.profilePicturePath, newGroupId)
                            onComplete(it)
                        }
            } else {
                onComplete(taskCanceled)
            }
        }
    }

    fun getUsers(onComplete: () -> Unit) {
        //TODO add in users USER from firestore
    }

    fun changeGroup(newGroupId: String, onComplete: (Task<Void>) -> Unit) {
        if(newGroupId == "") {
            onComplete(taskCanceled)
            return
        }
        getCurrentUser {user: User ->
            if(user.groupId != null && user.groupId != "")
                firestoreInstance.collection("groups").document("${user.groupId}")
                        .collection("members").document(FirebaseAuth.getInstance().currentUser?.uid.toString()).delete()

            val newGroupRef = firestoreInstance.collection("groups").document(newGroupId)
            if(newGroupRef.get().exception == null && newGroupRef.get().isSuccessful) {
                updateCurrentUser(it@ user.name, it@ user.profilePicturePath, newGroupId)
                val curMemberRef = newGroupRef.collection("members").document(FirebaseAuth.getInstance().currentUser?.uid.toString())

                curMemberRef.set(User(it@user.name, it@user.profilePicturePath, it@user.registrationTokens, newGroupId))
                        .addOnCompleteListener() {
                            if(it.isSuccessful)
                                updateCurrentUser(it@user.name, it@user.profilePicturePath, newGroupId)
                            onComplete(it)
                        }
            } else {
                onComplete(taskCanceled)
            }
        }
    }

    fun sendNews(news: News) {
        //TODO send news to firebase
    }

    fun getNews(onComplete: () -> Unit){
        //TODO add in news NEWS from firestore
    }
}