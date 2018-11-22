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
    private var newsListener: ListenerRegistration? = null
    private var messagesListener: ListenerRegistration? = null
    private var userListener: ListenerRegistration? = null
    private var memberListener: ListenerRegistration? = null

    var membersMap: MutableLiveData<HashMap<String, User>> = MutableLiveData<HashMap<String, User>>()
    var messages: MutableLiveData<ArrayList<Message>> = MutableLiveData<ArrayList<Message>>()

    fun getMessagesArray():MutableLiveData<ArrayList<Message>> {
        if(userListener == null) addUserListener()
        return messages
    }

    fun getMembers(): MutableLiveData<HashMap<String, User>> {
        if(memberListener == null) addUserListener()
        return membersMap
    }

    fun addMembersListener() {
        membersMap.postValue(HashMap())
        memberListener = FirestoreUtil.getMemb { isSuccessful, members ->
            if(isSuccessful) {
                membersMap.postValue(members)
            }
        }
    }
    /*
    fun getMembers(): MutableLiveData<HashMap<String, User>> {
        FirestoreUtil.getMembers { isSuccessful, message, members ->
            if(isSuccessful)
                membersMap.postValue(members)
        }
        return membersMap
    }*/

    fun getUser() : LiveData<User> {
        if(userListener == null) addUserListener()
        return user
    }

    fun getNews(): LiveData<ArrayList<News>> {
        if(userListener == null) addUserListener()
        return news
    }

    //удалить ссылку на новости
    private fun removeListener(listener: ListenerRegistration?) {
        if(listener != null)
            FirestoreUtil.removeListener(listener!!)
    }

    //добавить ссылку на новости
    private fun addNewsListener() {
        news.postValue(ArrayList<News>())
        newsListener = FirestoreUtil.addNewsListener{ isSuccessful, message, querySnapshot,
                                                       firebaseFirestoreException ->
                if (isSuccessful && !(querySnapshot?.isEmpty ?: true)) {
                    news.postValue(toListNews(querySnapshot!!))
                }
            }
    }

    private fun addMessagesListener() {
        messagesListener = FirestoreUtil.addChatMessagesListener { isSuccessful, message, querySnapshot,
                                                                   firebaseFirestoreException ->
            if (isSuccessful && !(querySnapshot?.isEmpty ?: true)) {
                messages.postValue(toListMessages(querySnapshot!!))
            }
        }
    }

    // обновить ссылкку на новости
    fun updateListeners() {
        removeListener(newsListener)
        removeListener(messagesListener)
        removeListener(memberListener)
        addMembersListener()
        addMessagesListener()
        addNewsListener()
    }

    //Переводит данные из объекта snapshot в список
    private fun toListNews(querySnapshot: QuerySnapshot) : ArrayList<News> {
        val news: ArrayList<News> = ArrayList<News>()
        for(doc: QueryDocumentSnapshot in  querySnapshot) {
            val examplerNews = doc.toObject(News::class.java)
            news.add(examplerNews)
        }
        return news
    }

    private fun toListMessages(querySnapshot: QuerySnapshot) : ArrayList<Message> {
        val messages: ArrayList<Message> = ArrayList<Message>()
        for(doc: QueryDocumentSnapshot in  querySnapshot) {
            val examplerMessage = doc.toObject(Message::class.java)
            messages.add(examplerMessage)
        }
        return messages
    }

    // добавит слушателя на пользователя
    private fun addUserListener() {
        userListener = FirestoreUtil.addUserListener { documentSnapshot, firebaseFirestoreException ->
            if(firebaseFirestoreException == null && documentSnapshot?.exists() ?: false) {
                user.postValue(documentSnapshot?.toObject(User::class.java))
                updateListeners()
            }
        }
    }
}