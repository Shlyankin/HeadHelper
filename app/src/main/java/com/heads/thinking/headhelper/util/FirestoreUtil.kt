package com.heads.thinking.headhelper.util

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.heads.thinking.headhelper.models.Group
import com.heads.thinking.headhelper.models.News
import com.heads.thinking.headhelper.models.User
import java.util.*
import java.util.UUID.randomUUID




object FirestoreUtil {
    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

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
            if (groupReference != null) {
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

    fun createGroup(onComplete: (Task<Void>) -> Unit) {
        getCurrentUser {user: User ->
            if(user.groupId != null && user.groupId != "")
                firestoreInstance.collection("groups").document("${user.groupId}")
                        .collection("members").document(FirebaseAuth.getInstance().currentUser?.uid.toString()).delete()
            val allGroupsRef = firestoreInstance.collection("groups")
            val groupId: String = randomUUID().mostSignificantBits.toString()
            val currentGroupRef = allGroupsRef.document(groupId)
            val curMemberRef = currentGroupRef.collection("members").document(FirebaseAuth.getInstance().currentUser?.uid.toString())

            curMemberRef.set(User(it@user.name, it@user.profilePicturePath, it@user.registrationTokens, groupId))
                    .addOnCompleteListener() {
                        if(it.isSuccessful)
                            updateCurrentUser(it@user.name, it@user.profilePicturePath, groupId)
                        onComplete(it)
                    }
        }
    }

    fun getUsers(onComplete: () -> Unit) {
        //TODO add in users USER from firestore
    }

    fun changeGroup(newGroupId: String, onComplete: (Task<Void>) -> Unit) {
        getCurrentUser {user: User ->
            if(user.groupId != null && user.groupId != "")
                firestoreInstance.collection("groups").document("${user.groupId}")
                        .collection("members").document(FirebaseAuth.getInstance().currentUser?.uid.toString()).delete()

            val newGroupRef = firestoreInstance.collection("groups").document(newGroupId)
            if(newGroupRef.get().exception == null) {
                updateCurrentUser(it@ user.name, it@ user.profilePicturePath, newGroupId)
                val curMemberRef = newGroupRef.collection("members").document(FirebaseAuth.getInstance().currentUser?.uid.toString())

                curMemberRef.set(User(it@user.name, it@user.profilePicturePath, it@user.registrationTokens, newGroupId))
                        .addOnCompleteListener() {
                            if(it.isSuccessful)
                                updateCurrentUser(it@user.name, it@user.profilePicturePath, newGroupId)
                            onComplete(it)
                        }
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