package com.heads.thinking.headhelper.util

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.heads.thinking.headhelper.models.News
import com.heads.thinking.headhelper.models.User


object FirestoreUtil {
    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private var groupReference: DocumentReference? = null
    var currentUser: User? = null

    private val currentUserDocRef: DocumentReference
        get() = firestoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}")

    var useListener: ListenerRegistration = currentUserDocRef.addSnapshotListener {
        documentSnapshot: DocumentSnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
        if(firebaseFirestoreException == null && documentSnapshot?.exists() ?: false) {
            currentUser = documentSnapshot!!.toObject(User::class.java)
            if(currentUser?.groupId != null)
                groupReference = firestoreInstance.collection("groups").document(currentUser?.groupId!!)
        }
    }

    fun userSignOut() {
        removeListener(useListener)
        groupReference = null
        currentUser = null
    }

    // переделать
    fun getMembers(onComplete: (isSuccessful: Boolean, message: String,  members: ArrayList<User>?) -> Unit) {
        if(currentUser != null) {
            if (currentUser?.groupId != null) {
                firestoreInstance.collection("groups").document(currentUser?.groupId ?: "").collection("members").get()
                        .addOnSuccessListener {
                            if (!it.isEmpty) {
                                val list: ArrayList<User> = ArrayList()
                                for (document in it) {
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

    //получить любого пользователя по ссылке
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

    fun initCurrentUserIfFirstTime(onComplete: () -> Unit) {
        currentUserDocRef.get().addOnSuccessListener { documentSnapshot ->
            if (!documentSnapshot.exists()) {
                val newUser = User(FirebaseAuth.getInstance().currentUser?.uid.toString(), FirebaseAuth.getInstance().currentUser?.displayName ?: "",
                        0, null, mutableListOf(), null)
                currentUserDocRef.set(newUser).addOnSuccessListener {
                    onComplete()
                }
            }
            else
                onComplete()
        }
    }

    fun updateCurrentUserData(name: String = "", privilege: Int? = null, profilePicturePath: String? = null, groupId: String?) {
        val userFieldMap = mutableMapOf<String, Any>()
        if (name.isNotBlank()) userFieldMap["name"] = name
        if (privilege != null) userFieldMap["privilege"] = privilege
        if (profilePicturePath != null)
            userFieldMap["profilePicturePath"] = profilePicturePath
        if (groupId != null) {
                if (currentUser!!.groupId != null && currentUser!!.groupId != "")
                    firestoreInstance.collection("groups")
                            .document("${currentUser!!.groupId}").collection("members")
                            .document(FirebaseAuth.getInstance().currentUser?.uid.toString()).delete()
            userFieldMap["groupId"] = groupId
            val member = mutableMapOf<String, Any>()
            member [FirebaseAuth.getInstance().currentUser?.uid
                    ?: throw NullPointerException("UID is null.")] = FirebaseAuth.getInstance().currentUser!!.displayName.toString()
            firestoreInstance.collection("groups").document(groupId)
                .collection("members").document(FirebaseAuth.getInstance().currentUser?.uid.toString())
                    .set(mapOf(Pair("memberRef",
                            FirebaseAuth.getInstance().currentUser?.uid.toString())))
        }
        currentUserDocRef.update(userFieldMap)
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
                        updateCurrentUserData(privilege = 1, groupId = newGroupId)
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
                    updateCurrentUserData(privilege = 0, groupId = newGroupId)
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
        val documentReference = groupReference
        if(documentReference != null)
            documentReference.collection("news").document(news.id).set(news).addOnCompleteListener {
                onComplete(it.isSuccessful, it.exception?.message ?: "")
            }
        else onComplete(false, "Проверьте состоите ли вы в группе")
    }

    fun deleteNews(id: String, onComplete: (isSuccessful: Boolean) -> Unit) {
        val documentReference = groupReference
        if(documentReference != null) {
                documentReference.collection("news").document(id).delete()
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

    fun getNews(onComplete: (isSuccessful: Boolean, news: ArrayList<News>?) -> Unit){
        val documentReference = FirestoreUtil.groupReference
        if(documentReference != null) {
                documentReference.collection("news").get().addOnSuccessListener {
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



    fun addNewsListener(onCreateListener: (listener: ListenerRegistration?) -> Unit,
                        onChange: (isSuccessful: Boolean, message: String, groupId: String?, querySnapshot: QuerySnapshot?,
                                   firebaseFirestoreException: FirebaseFirestoreException?) -> Unit) {
        val documentReference = FirestoreUtil.groupReference
        if(documentReference != null) {
            val user = currentUser
            if (user != null) {
                onCreateListener(
                        documentReference.collection("news").addSnapshotListener { querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                            if(firebaseFirestoreException == null && querySnapshot != null)
                                onChange(true, "", user.groupId, querySnapshot, firebaseFirestoreException)
                            else
                                onChange(false, "Что-то пошло не так", user.groupId, querySnapshot, firebaseFirestoreException)
                        }
                )
            } else {
                onCreateListener(null)
                onChange(false, "Не могу загрузить новости.\nПроверьте состоите ли вы в гурппе",
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