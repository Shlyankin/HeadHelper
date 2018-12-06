package com.heads.thinking.headhelper.util

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.heads.thinking.headhelper.models.Message
import com.heads.thinking.headhelper.models.News
import com.heads.thinking.headhelper.models.User
import java.util.*


object FirestoreUtil {
    private val firestoreInstance: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private var groupReference: DocumentReference? = null
    var currentUser: User? = null
        get() {
            if(field == null) {
                userSignIn()
                return null
            } else return field
        }

    // ссылка на текущего пользователя
    private val currentUserDocRef: DocumentReference
        get() = firestoreInstance.document("users/${FirebaseAuth.getInstance().currentUser?.uid
                ?: throw NullPointerException("UID is null.")}")

    // слушатель текущего пользователя
    var userListener: ListenerRegistration? = null

    // метод, который НЕОБХОДИМО вызвать при авторизации пользователя
    // инициализирует слушатели и обновляет ссылки
    fun userSignIn() {
        if(userListener == null) {
            userListener = currentUserDocRef.addSnapshotListener { documentSnapshot: DocumentSnapshot?, firebaseFirestoreException: FirebaseFirestoreException? ->
                if (firebaseFirestoreException == null && documentSnapshot?.exists() ?: false) {
                    val user = documentSnapshot!!.toObject(User::class.java)
                    currentUser = user
                    if (user?.groupId != null)
                        groupReference = firestoreInstance.collection("groups").document(user?.groupId!!)
                }
            }
        }
    }

    // метод, который НЕОБХОДИМО вызвать при выходе пользователя из аккаунта
    // удаляет все слушатели на текщуего пользователя и стирает некоторые его данные сессии
    fun userSignOut() {
        if(userListener != null)
            removeListener(userListener!!)
        userListener = null
        groupReference = null
        currentUser = null
    }

    // возвращает Map всех пользователей группы, где ключ - User.id, а также устанавливает слушатель
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
            onChange(false, null)
            return null
        }
    }

    //получить данные любого пользователя по url
    fun getUser(usersUrl: String, onComplete: (isSuccessful: Boolean, user: User?) -> Unit) {
        firestoreInstance.collection("users").document(usersUrl).get()
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


    // добавить слушателя для текущего пользователя
    fun addCurrentUserListener(onChange: (documentSnapshot: DocumentSnapshot?,
                                   firebaseFirestoreException: FirebaseFirestoreException?) -> Unit) : ListenerRegistration {
        // метод get() этого объекта чекает на null,
        // а методы обновления ссылок в ViewModel обращаются к currentUser, поэтому стоит заранее его проверить
        currentUser
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
                        0, null, mutableListOf(), "start")
                currentUserDocRef.set(newUser).addOnSuccessListener {
                    onComplete()
                }
            }
            else
                onComplete()
        }
    }

    //обновить привилегии пользователя
    fun updateMembersPrivileges(userId: String, privilege: Int, onComplete: (isSuccessful: Boolean, message: String) -> Unit) {
        val userFieldMap = mutableMapOf<String, Any>()
        userFieldMap["privilege"] = privilege
        firestoreInstance.document("users/" + userId).update(userFieldMap)
                .addOnCompleteListener {
                    onComplete(it.isSuccessful, it.exception?.message ?: "")
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
            if(user != null) {
                if (user.groupId != null && user.groupId != "")
                    firestoreInstance.collection("groups")
                            .document("${currentUser!!.groupId}").collection("members")
                            .document(FirebaseAuth.getInstance().currentUser?.uid.toString()).delete()
                userFieldMap["groupId"] = groupId
                val member = mutableMapOf<String, Any>()
                member[FirebaseAuth.getInstance().currentUser?.uid
                        ?: throw NullPointerException("UID is null.")] = FirebaseAuth.getInstance().currentUser!!.displayName.toString()
                firestoreInstance.collection("groups").document(groupId)
                        .collection("members").document(FirebaseAuth.getInstance().currentUser?.uid.toString())
                        .set(mapOf(Pair("memberRef",
                                FirebaseAuth.getInstance().currentUser?.uid.toString())))
            } else {
                return
            }
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
                        firestoreInstance.collection("groups").document(newGroupId)
                                .set(mapOf(Pair("id", newGroupId)))
                        updateCurrentUserData(privilege = 2, groupId = newGroupId)
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

                if(it.isSuccessful) {
                    val snapshot: DocumentSnapshot = it.result!!
                    if(snapshot.exists()) {
                        updateCurrentUserData(privilege = 0, groupId = newGroupId)
                        onComplete(true, "Вы сменили группу")
                    }  else
                        onComplete(false, "Группы с заданным номером не существует")
                } else {
                    if(it.exception?.message == "Failed to get document because the client is offline.")
                        onComplete(false, "Отсутствует подключение к интрнету")
                    else
                        onComplete(false, "Error " + it.exception?.message )
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

    //отправка сообщения на сервер
    fun sendMessage(message: Message, onComplete: (isSuccessful: Boolean) -> Unit) {
        val documentReference = FirestoreUtil.groupReference
        if(documentReference != null) {
            documentReference.collection("chat").document(message.id).set(message).addOnCompleteListener {
                onComplete(it.isSuccessful)
            }
        }
    }

    //добавляет слушатель на сообщения чата
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