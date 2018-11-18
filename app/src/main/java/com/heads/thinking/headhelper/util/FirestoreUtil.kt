package com.heads.thinking.headhelper.util

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.heads.thinking.headhelper.models.News
import com.heads.thinking.headhelper.models.User


object FirestoreUtil {
    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private var groupReference: DocumentReference? = null
    private var currentUser: User? = null

    fun userSignOut() {
        groupReference = null
        currentUser = null
    }

    private val currentUserDocRef: DocumentReference
        get() = firestoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}")

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

    // переделать
    fun getMembers(onComplete: (isSuccessful: Boolean, message: String,  members: ArrayList<User>?) -> Unit) {
        getCurrentUser {
            if(it.groupId != null) {
                firestoreInstance.collection("groups").document(it.groupId).collection("members").get()
                        .addOnSuccessListener {
                            if(!it.isEmpty) {
                                val list: ArrayList<User> = ArrayList()
                                for(document in it) {
                                    list.add(document.toObject(User::class.java))
                                }
                                onComplete(true, "", list)
                            } else {
                                onComplete(true, "Никого нет в группе", ArrayList())
                            }
                        }
            } else onComplete(false, "Вы не состоите в группе", null)
        }
    }

    fun getUser(userPath: String, onComplete: (isSuccessful: Boolean, user: User?) -> Unit) {
        firestoreInstance.collection("users").document(userPath).get()
                .addOnSuccessListener {
                    if(it.exists())
                        onComplete(it.exists(), it.toObject(User::class.java)!!)
                    else {
                        onComplete(it.exists(), null)
                    }
                }
                .addOnCanceledListener {
                    onComplete(false, null)
                }
    }


    fun addUserListener(onChange: (documentSnapshot: DocumentSnapshot?,
                                   firebaseFirestoreException: FirebaseFirestoreException?) -> Unit) : ListenerRegistration {
        return currentUserDocRef.addSnapshotListener{ documentSnapshot: DocumentSnapshot?,
                                               firebaseFirestoreException: FirebaseFirestoreException? ->
                onChange(documentSnapshot, firebaseFirestoreException)
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


    fun initCurrentUserIfFirstTime(onComplete: () -> Unit) {
        currentUserDocRef.get().addOnSuccessListener { documentSnapshot ->
            if (!documentSnapshot.exists()) {
                val newUser = User(FirebaseAuth.getInstance().currentUser?.uid.toString(), FirebaseAuth.getInstance().currentUser?.displayName ?: "",
                        null, mutableListOf(), null)
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
            getCurrentUser { user: User ->
                if (user.groupId != null && user.groupId != "")
                    firestoreInstance.collection("groups")
                            .document("${user.groupId}").collection("members")
                            .document(FirebaseAuth.getInstance().currentUser?.uid.toString()).delete()
            }
            userFieldMap["groupId"] = groupId
            val member = mutableMapOf<String, Any>()
            member [FirebaseAuth.getInstance().currentUser?.uid
                    ?: throw NullPointerException("UID is null.")] = FirebaseAuth.getInstance().currentUser!!.displayName.toString()
            firestoreInstance.collection("groups").document(groupId)
                .collection("members").document(FirebaseAuth.getInstance().currentUser?.uid.toString())
                    .set(mapOf(Pair("memberRef",
                            FirebaseAuth.getInstance().currentUser?.uid.toString())))
        }
        currentUserDocRef.update(userFieldMap).addOnSuccessListener {
            updateGroupRef()
        }
    }

    fun createGroup(newGroupId: String, onComplete: (isSuccessful: Boolean, message: String) -> Unit) {
        if(newGroupId == "") {
            onComplete(false, "Поле не задано")
            return
        }
        firestoreInstance.collection("groups").document(newGroupId).get()
            .addOnCompleteListener {
                if(it.isSuccessful && it.getResult()?.exists() ?: false) {
                    onComplete(false, "Группа с данынм ID существует")
                } else {
                    if(it.exception is FirebaseNetworkException) {
                        onComplete(false, "Отсутствует подключение к интрнету")
                    } else {
                        updateCurrentUserData(groupId = newGroupId)
                        onComplete(true, "Группа создана")
                    }
                }
            }
            .addOnFailureListener {
                onComplete(false, it.message ?: "")
            }
    }

    fun changeGroup(newGroupId: String, onComplete: (isSuccessful:Boolean, message: String) -> Unit) {
        if(newGroupId == "") {
            onComplete(false, "Вы не состоите в группе")
            return
        }
        firestoreInstance.collection("groups")
            .document(newGroupId).get().addOnCompleteListener {
                if(it.isSuccessful && it.getResult()?.exists() ?: false) {
                    updateCurrentUserData(groupId = newGroupId)
                    onComplete(true, "Вы сменили группу")
                } else {
                    if(it.exception?.message == "Failed to get document because the client is offline.") {
                        onComplete(false, "Отсутствует подключение к интрнету")
                    } else
                        onComplete(false, "Группы с заданным номером не существует")
                }
            }
            .addOnFailureListener {
                onComplete(false, it.message ?: "")
            }
    }

    fun sendNews(news: News, onComplete: (isSuccessful: Boolean, message: String) -> Unit) {
        groupRef { isSuccessful, documentReference ->
            if(isSuccessful) {
                documentReference!!.collection("news").document(news.id).set(news).addOnCompleteListener{
                    onComplete(it.isSuccessful, it.exception?.message ?: "")
                }
            } else {
                onComplete(false,"Не получается выполнить операцию\nПроверьте состоите ли вы в группе.")
            }
        }
    }

    fun deleteNews(id: String, onComplete: (isSuccessful: Boolean) -> Unit) {
        groupRef { isSuccessful, documentReference ->
            if(isSuccessful) {
                documentReference!!.collection("news").document(id).delete()
                        .addOnCompleteListener() {
                            if(it.isSuccessful) {
                                onComplete(true)
                            } else {
                                onComplete(false)
                            }
                        }
            } else {
                onComplete(false)
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



    fun addNewsListener(onCreateListener: (listener: ListenerRegistration?) -> Unit,
                        onChange: (isSuccessful: Boolean, message: String, groupId: String?, querySnapshot: QuerySnapshot?,
                                   firebaseFirestoreException: FirebaseFirestoreException?) -> Unit) {
        groupRef{ isSuccessful: Boolean, documentReference: DocumentReference? ->
            if(isSuccessful) {
                getCurrentUser {
                    onCreateListener(
                            documentReference!!.collection("news").addSnapshotListener { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                                onChange(isSuccessful, "", it.groupId, querySnapshot, firebaseFirestoreException)
                            }
                    )
                }
            } else {
                onCreateListener(null)
                onChange(isSuccessful, "Не могу загрузить новости.\nПроверьте состоите ли вы в гурппе",
                        null, null, null)
            }
        }
    }

    fun removeListener(registration: ListenerRegistration) = registration.remove()

    fun sendMessage() {
        //TODO
    }

    fun addChatMessagesListener() {
        //TODO
    }
}