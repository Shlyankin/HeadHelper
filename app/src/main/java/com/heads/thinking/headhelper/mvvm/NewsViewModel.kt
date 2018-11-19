package com.heads.thinking.headhelper.mvvm

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.heads.thinking.headhelper.models.News
import com.heads.thinking.headhelper.models.User
import com.heads.thinking.headhelper.util.FirestoreUtil
import java.util.*

class NewsViewModel: ViewModel() {

    var user: MutableLiveData<User> = MutableLiveData()
    var newsListener: ListenerRegistration? = null
    var userListener: ListenerRegistration? = null
    var groupId: String? = null
    var news : MutableLiveData<ArrayList<News>> = MutableLiveData<ArrayList<News>>()

    fun getUser() : LiveData<User> {
        if(userListener == null) addUserListener()
        return user
    }

    fun getNews(): LiveData<ArrayList<News>> {
        if(userListener == null) addUserListener()
        return news
    }

    fun removeNewsListener() {
        if(newsListener != null)
            FirestoreUtil.removeListener(newsListener!!)
    }

    fun addNewsListener() {
        news.postValue(ArrayList<News>())
        FirestoreUtil.addNewsListener(
            {
                newsListener = it
            },
            { isSuccessful, message,  groupId, querySnapshot, firebaseFirestoreException ->
                if (isSuccessful && !(querySnapshot?.isEmpty ?: true)) {
                    news!!.postValue(toListNews(querySnapshot!!))
                }
            }
        )
    }

    private fun toListNews(querySnapshot: QuerySnapshot) : ArrayList<News> {
        val news: ArrayList<News> = ArrayList<News>()
        for(doc: QueryDocumentSnapshot in  querySnapshot) {
            val examplerNews = doc.toObject(News::class.java)
            news.add(examplerNews)
        }
        return news
    }

    fun addUserListener() {
        userListener = FirestoreUtil.addUserListener { documentSnapshot, firebaseFirestoreException ->
            if(firebaseFirestoreException == null && documentSnapshot?.exists() ?: false) {
                user.postValue(documentSnapshot?.toObject(User::class.java))
                updateListener()
            }
        }
    }

    fun updateListener() {
        if(newsListener != null)
            removeNewsListener()
        addNewsListener()
    }
}