package com.heads.thinking.headhelper

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import com.heads.thinking.headhelper.adapters.NewsRecyclerAdapter
import com.heads.thinking.headhelper.models.News
import com.heads.thinking.headhelper.models.User
import com.heads.thinking.headhelper.mvvm.DataViewModel
import kotlinx.android.synthetic.main.fragment_chat.*

class NewsFragment : Fragment(), View.OnClickListener {

    lateinit var progressBar: ProgressBar
    lateinit var dataViewModel: DataViewModel
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

        dataViewModel = ViewModelProviders.of(this).get(DataViewModel::class.java)

        progressBar = view.findViewById(R.id.progressBar)
        newsRecyclerView = view.findViewById(R.id.newsRecyclerView)
        newsRecyclerView.layoutManager = LinearLayoutManager(App.instance?.applicationContext, LinearLayoutManager.VERTICAL, false)
        newsRecyclerView.hasFixedSize()
        adapterNewsRecyclerAdapter = NewsRecyclerAdapter(this.context!!, ArrayList<News>(), dataViewModel)
        newsRecyclerView.adapter = adapterNewsRecyclerAdapter
        addNewsBtn = view.findViewById(R.id.addNewsFab)
        addNewsBtn.setOnClickListener(this)

        dataViewModel.getUser().observe(this.activity!!, Observer<User> { user: User? ->
            if(user != null && user.privilege != 0) {
                addNewsBtn.show()
            }
        })
        dataViewModel.getNews().observe(this.activity!!, Observer<ArrayList<News>> { changedNews ->
            if(changedNews != null) {
                progressBar.visibility = View.GONE
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
        return view
    }
}
