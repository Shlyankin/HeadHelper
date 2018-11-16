package com.heads.thinking.headhelper.mvvm

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.widget.Toast
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.heads.thinking.headhelper.App
import com.heads.thinking.headhelper.models.News
import com.heads.thinking.headhelper.util.FirestoreUtil

class NewsViewModel: ViewModel() {

    var newsListener: ListenerRegistration? = null
    var groupId: String? = null
    var news : MutableLiveData<ArrayList<News>> = MutableLiveData<ArrayList<News>>()

    fun getListener(): LiveData<ListenerRegistration?> {
        return MutableLiveData<ListenerRegistration?>().apply {
            postValue(newsListener)
        }
    }

    fun getNews(): LiveData<ArrayList<News>> {
        FirestoreUtil.getCurrentUser {
            if(it.groupId != groupId) {
                removeListener()
                addListener()
            }
        }
        return news
    }

    fun removeListener() {
        if(newsListener != null)
            FirestoreUtil.removeListener(newsListener!!)
    }

    fun addListener() {
        news.postValue(ArrayList<News>())
        FirestoreUtil.addNewsListener(
            {
                newsListener = it
            },
            { isSuccessful, message,  groupId, querySnapshot, firebaseFirestoreException ->
                if (isSuccessful && !(querySnapshot?.isEmpty ?: true)) {
                    news!!.postValue(toListNews(querySnapshot!!))
                } else {
                    Toast.makeText(App.instance?.applicationContext, message, Toast.LENGTH_SHORT).show() //TODO Надо ли это делать?
                }
            }
        )
    }

    private fun toListNews(querySnapshot: QuerySnapshot) : ArrayList<News> {
        val news: ArrayList<News> = ArrayList<News>()
        for(doc: QueryDocumentSnapshot in  querySnapshot) {
            val examplerNews: News = News( //TODO check error
                    id = doc.get("id") as String,
                    category    = doc.get("category") as String,
                    picturePath = doc.get("picturePath") as String?,
                    tittle      = doc.get("tittle") as String,
                    text        = doc.get("text") as String,
                    authorRef   = doc.get("authorRef") as String?)
            news.add(examplerNews)
        }
        return news
    }
}