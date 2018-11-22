package com.heads.thinking.headhelper.util

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.local.QueryData
import com.heads.thinking.headhelper.models.Message
import com.heads.thinking.headhelper.models.News
import com.heads.thinking.headhelper.models.User


object FirestoreUtil {
    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private var groupReference: DocumentReference? = null
    var currentUser: User? = null

    // ссылка на текущего пользователя
    private val currentUserDocRef: DocumentReference
        get() = firestoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}")

    //листенер текущего пользователя
    var userListener: ListenerRegistration? = null

    fun userSignIn() {
        if(userListener == null) {
            userListener = currentUserDocRef.addSnapshotListener { documentSnapshot: DocumentSnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                if (firebaseFirestoreException == null && documentSnapshot?.exists() ?: false) {
                    currentUser = documentSnapshot!!.toObject(User::class.java)
                    if (currentUser?.groupId != null)
                        groupReference = firestoreInstance.collection("groups").document(currentUser?.groupId!!)
                }
            }
        }
    }

    // действи, которые НЕОБХОДИМО выполнить при выходе пользователя
    fun userSignOut() {
        if(userListener != null)
            removeListener(userListener!!)
        userListener = null
        groupReference = null
        currentUser = null
    }
/*
    fun addMembersListener(onChange: (isSuccessful: Boolean, membersRef: ArrayList<String>?) -> Unit): ListenerRegistration? {
        val ref = groupReference
        if(ref != null) {
            return ref.collection("users").addSnapshotListener {
                querySnapshot, firebaseFirestoreException ->
                if(firebaseFirestoreException == null) {
                    val membersRef = ArrayList<String>().apply {
                        for(doc in querySnapshot!!)
                            add(doc["memberRef"].toString())
                    }
                    onChange(true, membersRef)
                } else {
                    onChange(false, null)
                }
            }
        } else return null
    }*/

    fun getMemb(onChange: (isSuccessful: Boolean, members: HashMap<String, User>?) -> Unit)
            : ListenerRegistration? {
        val currUser = currentUser
        if(currUser != null) {
            return firestoreInstance.collection("users").whereEqualTo("groupId", currUser.groupId)
                    .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                        if(firebaseFirestoreException == null) {
                            val map: HashMap<String, User> = HashMap()
                            for (doc in querySnapshot!!) {
                                val user = doc.toObject(User::class.java)
                                map.put(user.id, user)
                            }
                            onChange(true, map)
                        } else onChange(false, null)
                    }
        } else {
            return null
        }
    }

    /*// переделать
    fun getMembers(onComplete: (isSuccessful: Boolean, message: String,  members: HashMap<String, User>?) -> Unit) {
        val ref = groupReference
        if(ref != null) {
            ref.collection("members").get()
                .addOnSuccessListener {
                    if (!it.isEmpty) {
                        val map: HashMap<String, User> = HashMap<String, User>()
                        for (document in it) {
                            val memberRef: String = document["memberRef"].toString()
                            getUser(memberRef, { isSuccessful, user ->
                                if(isSuccessful) map.put(memberRef, user!!)
                            })
                        }
                        onComplete(true, "", map)
                    } else {
                        onComplete(true, "Никого нет в группе", HashMap())
                    }
                }
        }
    }*/

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


    // добавить слушателя для пользователя
    fun addUserListener(onChange: (documentSnapshot: DocumentSnapshot?,
                                   firebaseFirestoreException: FirebaseFirestoreException?) -> Unit) : ListenerRegistration {
        return currentUserDocRef.addSnapshotListener{ documentSnapshot: DocumentSnapshot?,
                                               firebaseFirestoreException: FirebaseFirestoreException? ->
                onChange(documentSnapshot, firebaseFirestoreException)
        }
    }

    //инициализировать пользователя, если он зашел в первый раз
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

    //обновление данных о пользователе на сервере
    fun updateCurrentUserData(name: String = "", privilege: Int? = null, profilePicturePath: String? = null, groupId: String?) {
        val userFieldMap = mutableMapOf<String, Any>()
        if (name.isNotBlank()) userFieldMap["name"] = name
        if (privilege != null) userFieldMap["privilege"] = privilege
        if (profilePicturePath != null)
            userFieldMap["profilePicturePath"] = profilePicturePath
        if (groupId != null) {
            val user = currentUser
                if (user != null && user.groupId != null && user.groupId != "")
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

    // создает коллекцию группу с заданным именем
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

    //смены группы пользователя
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

    // отправка новости в группу пользователя
    fun sendNews(news: News, onComplete: (isSuccessful: Boolean, message: String) -> Unit) {
        val documentReference = groupReference
        if(documentReference != null)
            documentReference.collection("news").document(news.id).set(news).addOnCompleteListener {
                onComplete(it.isSuccessful, it.exception?.message ?: "")
            }
        else onComplete(false, "Проверьте состоите ли вы в группе")
    }

    //удаление новости из группы пользователя
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

    // получить все новости
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

    //добавить слушателя на новости группы
    fun addNewsListener(onChange: (isSuccessful: Boolean, message: String, querySnapshot: QuerySnapshot?,
                                   firebaseFirestoreException: FirebaseFirestoreException?) -> Unit): ListenerRegistration? {
        val documentReference = FirestoreUtil.groupReference
        if(documentReference != null) {
            return documentReference.collection("news").orderBy("date", Query.Direction.DESCENDING)
                    .addSnapshotListener { querySnapshot: QuerySnapshot?,
                                           firebaseFirestoreException: FirebaseFirestoreException? ->
                if(firebaseFirestoreException == null && querySnapshot != null)
                    onChange(true, "", querySnapshot, firebaseFirestoreException)
                else
                    onChange(false, "Что-то пошло не так", querySnapshot, firebaseFirestoreException)
            }
        }
        return null
    }

    //удалить любой слушатель
    fun removeListener(registration: ListenerRegistration) = registration.remove()

    fun sendMessage(message: Message, onComplete: (isSuccessful: Boolean) -> Unit) {
        val documentReference = FirestoreUtil.groupReference
        if(documentReference != null) {
            documentReference.collection("chat").document(message.id).set(message).addOnCompleteListener {
                onComplete(it.isSuccessful)
            }
        }
    }

    fun addChatMessagesListener(onChange: (isSuccessful: Boolean, message: String, querySnapshot: QuerySnapshot?, firebaseFirestoreException: FirebaseFirestoreException?) -> Unit): ListenerRegistration? {
        val documentReference = FirestoreUtil.groupReference
        if(documentReference != null) {
            return documentReference.collection("chat")
                    .orderBy("date", Query.Direction.ASCENDING)
                    .limit(100)
                    .addSnapshotListener { querySnapshot: QuerySnapshot?,
                                           firebaseFirestoreException: FirebaseFirestoreException? ->
                        if(firebaseFirestoreException == null && querySnapshot != null)
                            onChange(true, "", querySnapshot, firebaseFirestoreException)
                        else
                            onChange(false, "Что-то пошло не так", querySnapshot, firebaseFirestoreException)
                    }
        }
        return null
    }
}