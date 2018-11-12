package com.heads.thinking.headhelper

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.heads.thinking.headhelper.adapters.NewsRecyclerAdapter
import com.heads.thinking.headhelper.models.News
import com.heads.thinking.headhelper.util.CustomFirestoreUtil

class NewsFragment : Fragment(), View.OnClickListener {

    lateinit var addNewsBtn: FloatingActionButton
    lateinit var newsRecyclerView: RecyclerView
    lateinit var adapterNewsRecyclerAdapter: NewsRecyclerAdapter
    lateinit var newsList: ArrayList<News>

    var listReady: Boolean = false

    override fun onClick(view: View?) {
        when(view!!.id) {
            R.id.addNewsFab -> {
                if(listReady) {
                    startActivity(Intent(this.activity, AddNewsActivity::class.java))
                } else {
                    Toast.makeText(this.context, "Идет загрузка новостей. Подождите", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_news, container, false)

        newsRecyclerView = view.findViewById(R.id.newsRecyclerView)
        addNewsBtn = view.findViewById(R.id.addNewsFab)
        addNewsBtn.setOnClickListener(this)

        CustomFirestoreUtil.getNews { isSuccessful: Boolean, arrayList: ArrayList<News>? ->
            if(isSuccessful) {
                newsList = arrayList!!
                newsRecyclerView.layoutManager = LinearLayoutManager(App.instance?.applicationContext, LinearLayoutManager.HORIZONTAL, false)
                newsRecyclerView.hasFixedSize()
                if (context != null) {
                    adapterNewsRecyclerAdapter = NewsRecyclerAdapter(activity!!, newsList)
                    newsRecyclerView.adapter = adapterNewsRecyclerAdapter
                    listReady = true
                }
            } else {
                Toast.makeText(App.instance!!.applicationContext,
                        "Не получилось обновить новости.\nПроверьте состоите ли вы в группе в своем кабинете",
                        Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }
}
