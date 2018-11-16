package com.heads.thinking.headhelper

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.firestore.ListenerRegistration
import com.heads.thinking.headhelper.adapters.NewsRecyclerAdapter
import com.heads.thinking.headhelper.models.News
import com.heads.thinking.headhelper.mvvm.NewsViewModel

class NewsFragment : Fragment(), View.OnClickListener {

    lateinit var newsViewModel: NewsViewModel
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
        newsRecyclerView.layoutManager = LinearLayoutManager(App.instance?.applicationContext, LinearLayoutManager.VERTICAL, false)
        newsRecyclerView.hasFixedSize()
        adapterNewsRecyclerAdapter = NewsRecyclerAdapter(this.context!!, ArrayList<News>())
        newsRecyclerView.adapter = adapterNewsRecyclerAdapter
        addNewsBtn = view.findViewById(R.id.addNewsFab)
        addNewsBtn.setOnClickListener(this)

        newsViewModel = ViewModelProviders.of(this).get(NewsViewModel::class.java)
        return view
    }

    override fun onResume() {
        super.onResume()
        newsViewModel.getListener().observe(this.activity!!, Observer<ListenerRegistration?> {
            if(it == null) {
                newsViewModel.getNews().observe(this.activity!!, Observer<ArrayList<News>> { changedNews ->
                    if(changedNews != null) {
                        newsList = changedNews
                        adapterNewsRecyclerAdapter.list = newsList
                        adapterNewsRecyclerAdapter.notifyDataSetChanged()
                        listReady = true
                    } else {
                        Toast.makeText(this.context,
                                "Не получилось обновить новости.\nПроверьте состоите ли вы в группе в своем кабинете",
                                Toast.LENGTH_SHORT).show()
                    }
                })
            }
        })
    }
}
