package com.heads.thinking.headhelper.mvvm

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.heads.thinking.headhelper.models.Message
import com.heads.thinking.headhelper.models.News
import com.heads.thinking.headhelper.models.User
import com.heads.thinking.headhelper.util.FirestoreUtil
import kotlin.collections.ArrayList

class DataViewModel: ViewModel() {

    var user: MutableLiveData<User> = MutableLiveData()
    var news : MutableLiveData<ArrayList<News>> = MutableLiveData<ArrayList<News>>()
    var membersMap: MutableLiveData<HashMap<String, User>> = MutableLiveData<HashMap<String, User>>()
    var messages: MutableLiveData<ArrayList<Message>> = MutableLiveData<ArrayList<Message>>()

    private var newsListener: ListenerRegistration? = null
    private var messagesListener: ListenerRegistration? = null
    private var userListener: ListenerRegistration? = null
    private var memberListener: ListenerRegistration? = null

    // у нас зависимость от группы пользователя,
    // поэтому надо держать актуальным, чтобы обновлять ссылки на остальные компоненты,
    // поэтому вызывается addUserListener() в ниже перечисленных методах
    fun getMessagesArray():MutableLiveData<ArrayList<Message>> {
        if(userListener == null) addUserListener()
        return messages
    }

    fun getMembers(): MutableLiveData<HashMap<String, User>> {
        if(memberListener == null) addUserListener()
        return membersMap
    }

    fun getUser() : LiveData<User> {
        if(userListener == null) addUserListener()
        return user
    }

    fun getNews(): LiveData<ArrayList<News>> {
        if(userListener == null) addUserListener()
        return news
    }

    //добавить слушателей на данные
    fun addMembersListener() {
        membersMap.postValue(HashMap())
        memberListener = FirestoreUtil.getMemb { isSuccessful, members ->
            if(isSuccessful) {
                membersMap.postValue(members)
            }
        }
    }

    private fun addUserListener() {
        userListener = FirestoreUtil.addCurrentUserListener { documentSnapshot, firebaseFirestoreException ->
            if(firebaseFirestoreException == null && documentSnapshot?.exists() ?: false) {
                user.postValue(documentSnapshot?.toObject(User::class.java))
                updateListeners()
            }
        }
    }

    private fun addMessagesListener() {
        messagesListener = FirestoreUtil.addChatMessagesListener { isSuccessful, message, querySnapshot,
                                                                   firebaseFirestoreException ->
            if (isSuccessful && !(querySnapshot?.isEmpty ?: true)) {
                messages.postValue(toListMessages(querySnapshot!!))
            } else {
                messages.postValue(ArrayList<Message>())
            }
        }
    }

    private fun addNewsListener() {
        news.postValue(ArrayList<News>())
        newsListener = FirestoreUtil.addNewsListener{ isSuccessful, message, querySnapshot,
                                                      firebaseFirestoreException ->
            if (isSuccessful && !(querySnapshot?.isEmpty ?: true)) {
                news.postValue(toListNews(querySnapshot!!))
            }
        }
    }

    //удалить слушатели
    private fun removeListener(listener: ListenerRegistration?) {
        if(listener != null)
            FirestoreUtil.removeListener(listener!!)
    }

    // обновить все слушатели. Полезно при смене пользователем группы
    fun updateListeners() {
        removeListener(newsListener)
        removeListener(messagesListener)
        removeListener(memberListener)
        addMembersListener()
        addMessagesListener()
        addNewsListener()
    }

    //Переводит данные из объекта snapshot в список новостей
    private fun toListNews(querySnapshot: QuerySnapshot) : ArrayList<News> {
        val news: ArrayList<News> = ArrayList<News>()
        for(doc: QueryDocumentSnapshot in  querySnapshot) {
            val examplerNews = doc.toObject(News::class.java)
            news.add(examplerNews)
        }
        return news
    }

    //Переводит данные из объекта snapshot в список сообщений
    private fun toListMessages(querySnapshot: QuerySnapshot) : ArrayList<Message> {
        val messages: ArrayList<Message> = ArrayList<Message>()
        for(doc: QueryDocumentSnapshot in  querySnapshot) {
            val examplerMessage = doc.toObject(Message::class.java)
            messages.add(examplerMessage)
        }
        return messages
    }
}