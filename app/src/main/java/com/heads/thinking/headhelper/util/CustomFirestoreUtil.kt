package com.heads.thinking.headhelper.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.heads.thinking.headhelper.models.News
import com.heads.thinking.headhelper.models.User


object CustomFirestoreUtil {
    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private var groupReference: DocumentReference? = null
    private var currentUser: User? = null


    private fun getUpdatedCurrentUser(onComplete: (User) -> Unit) {
        currentUserDocRef.get()
                .addOnSuccessListener {
                    currentUser = it.toObject(User::class.java)!!
                    onComplete(currentUser!!)
                }
    }

    fun getCurrentUser(onComplete: (user: User) -> Unit) {
        if(currentUser == null) {
            getUpdatedCurrentUser {
                currentUser = it
                onComplete(currentUser!!)
            }
        } else {
            onComplete(currentUser!!)
        }
    }

    private fun updateGroupRef() {
        getUpdatedCurrentUser {
            if(it.groupId != null)
                groupReference = firestoreInstance.collection("groups").document(it.groupId)
        }
    }

    private fun groupRef(onComplete: (isSuccessful: Boolean, initilGroupRef: DocumentReference?) -> Unit) {
        if(groupReference == null)
            getUpdatedCurrentUser {
                if(it.groupId != null && it.groupId != "") {
                    groupReference = firestoreInstance.collection("groups").document(it.groupId)
                    onComplete(true, groupReference)
                } else {
                    onComplete(false, null)
                }
            }
        else
            onComplete(true, groupReference)
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

    fun updateCurrentUserData(name: String = "", profilePicturePath: String? = null, groupId: String?) {
        val userFieldMap = mutableMapOf<String, Any>()
        if (name.isNotBlank()) userFieldMap["name"] = name
        if (profilePicturePath != null)
            userFieldMap["profilePicturePath"] = profilePicturePath
        if (groupId != null) {
            val groupReference = firestoreInstance.collection("groups").document("$groupId")
            userFieldMap["groupId"] = groupId
            val member = mutableMapOf<String, Any>()
            member [FirebaseAuth.getInstance().currentUser?.uid
                    ?: throw NullPointerException("UID is null.")] = FirebaseAuth.getInstance().currentUser!!.displayName.toString()
            firestoreInstance.collection("groups")
                    .document("${groupId}/members/${FirebaseAuth.getInstance().currentUser?.uid}").update(member)
                    .addOnCompleteListener {
                        updateGroupRef()
                    }
        }
        currentUserDocRef.update(userFieldMap)
        getUpdatedCurrentUser { user: User -> } // update user object
    }

    fun createGroup(newGroupId: String, onComplete: (isSuccessful: Boolean, message: String) -> Unit) {
        if(newGroupId == "") {
            onComplete(false, "Поле не задано")
            return
        }
        getCurrentUser {user: User ->
            val newGroupRef = firestoreInstance.collection("groups").document(newGroupId)
            newGroupRef.get().addOnCompleteListener {
                if(it.isSuccessful && it.getResult()?.exists() ?: false) {
                    onComplete(false, "Группа с данынм ID существует")
                } else {
                    if (user.groupId != null && user.groupId != "")
                        firestoreInstance.collection("groups").document("${user.groupId}")
                                .collection("members").document(FirebaseAuth.getInstance().currentUser?.uid.toString()).delete()
                    val allGroupsRef = firestoreInstance.collection("groups")
                    val currentGroupRef = allGroupsRef.document(newGroupId)
                    val curMemberRef = currentGroupRef.collection("members").document(FirebaseAuth.getInstance().currentUser?.uid.toString())

                    curMemberRef.set(User(user.name, user.profilePicturePath, user.registrationTokens, newGroupId))
                            .addOnCompleteListener() {
                                if (it.isSuccessful)
                                    updateCurrentUserData(user.name, user.profilePicturePath, newGroupId)
                                onComplete(it.isSuccessful, it.exception?.message ?: "")
                            }
                }
            }
        }
    }

    fun getUsers(onComplete: () -> Unit) {
        //TODO add in users USER from firestore
    }

    fun addUsersListener() {
        //TODO user listener
    }

    fun changeGroup(newGroupId: String, onComplete: (isSuccessful:Boolean, message: String) -> Unit) {
        if(newGroupId == "") {
            onComplete(false, "Вы не состоите в группе")
            return
        }
        getCurrentUser {user: User ->
            if(user.groupId != null && user.groupId != "")
                firestoreInstance.collection("groups").document("${user.groupId}")
                        .collection("members").document(FirebaseAuth.getInstance().currentUser?.uid.toString()).delete()

            val newGroupRef = firestoreInstance.collection("groups").document(newGroupId)
            newGroupRef.get().addOnCompleteListener {
                if(it.isSuccessful && it.getResult()?.exists() ?: false) {
                    updateCurrentUserData(user.name, user.profilePicturePath, newGroupId)
                    val curMemberRef = newGroupRef.collection("members").document(FirebaseAuth.getInstance().currentUser?.uid.toString())

                    curMemberRef.set(User(user.name, user.profilePicturePath, user.registrationTokens, newGroupId))
                            .addOnCompleteListener() {
                                if(it.isSuccessful)
                                    updateCurrentUserData(user.name, user.profilePicturePath, newGroupId)
                                onComplete(it.isSuccessful, it.exception?.message ?: "")
                            }
                } else {
                    onComplete(false, "Группы с заданным номером не существует")
                }
            }
        }
    }

    fun sendNews(news: News, onComplete: (isSuccessful: Boolean, message: String) -> Unit) {
        groupRef { isSuccessful, documentReference ->
            if(isSuccessful) {
                documentReference!!.collection("news").document().set(news).addOnCompleteListener{
                    onComplete(it.isSuccessful, it.exception?.message ?: "")
                }
            } else {
                onComplete(false,"Не получается выполнить операцию\nПроверьте состоите ли вы в группе.")
            }
        }
    }

    fun getNews(onComplete: (isSuccessful: Boolean, news: ArrayList<News>?) -> Unit){
        groupRef { isSuccessful, documentReference ->
            if(isSuccessful) {
                documentReference!!.collection("news").get().addOnSuccessListener {
                    val list : ArrayList<News> = ArrayList()
                    for (i in it.documents) {
                        list.add(i.toObject(News::class.java)!!)
                    }
                    onComplete(true, list)
                }
            } else {
                onComplete(false, null)
            }
        }
    }

    fun addNewsListener() {
        //TODO
    }

    fun sendMessage() {
        //TODO
    }

    fun addChatMessagesListener() {
        //TODO
    }

    fun removeListener() {
        //TODO
    }
}