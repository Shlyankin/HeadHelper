package com.heads.thinking.headhelper.mvvm

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.widget.Toast
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.heads.thinking.headhelper.App
import com.heads.thinking.headhelper.models.News
import com.heads.thinking.headhelper.util.FirestoreUtil

class NewsViewModel: ViewModel() {

    var news : MutableLiveData<ArrayList<News>>? = null

    fun getNews(): LiveData<ArrayList<News>> {
        if(news == null) {
            news = MutableLiveData<ArrayList<News>>()
            news!!.postValue(ArrayList<News>())
            FirestoreUtil.addNewsListener { isSuccessful, message, querySnapshot, firebaseFirestoreException ->
                if (isSuccessful && !(querySnapshot?.isEmpty ?: true)) {
                    news!!.postValue(toListNews(querySnapshot!!))
                } else {
                    Toast.makeText(App.instance?.applicationContext, message, Toast.LENGTH_SHORT).show() //TODO Надо ли это делать?
                }
            }
        }
        return news as MutableLiveData<ArrayList<News>>
    }

    private fun toListNews(querySnapshot: QuerySnapshot) : ArrayList<News> {
        val news: ArrayList<News> = ArrayList<News>()
        for(doc: QueryDocumentSnapshot in  querySnapshot) {
            val examplerNews: News = News( //TODO check error
                    id = doc.get("id") as String,
                    category    = doc.get("category") as String,
                    picturePath = doc.get("picturePath") as String?,
                    tittle      = doc.get("tittle") as String,
                    text        = doc.get("text") as String )
            news.add(examplerNews)
        }
        return news
    }
}