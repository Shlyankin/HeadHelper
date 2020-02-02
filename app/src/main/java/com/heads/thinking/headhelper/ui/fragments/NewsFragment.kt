package com.heads.thinking.headhelper.ui.fragments

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
import com.heads.thinking.headhelper.App
import com.heads.thinking.headhelper.R
import com.heads.thinking.headhelper.ui.activities.AddNewsActivity
import com.heads.thinking.headhelper.adapters.NewsRecyclerAdapter
import com.heads.thinking.headhelper.models.News
import com.heads.thinking.headhelper.models.User
import com.heads.thinking.headhelper.mvvm.DataViewModel
import kotlinx.android.synthetic.main.fragment_news.*

class NewsFragment : Fragment(), View.OnClickListener {

    private lateinit var adapterNewsRecyclerAdapter: NewsRecyclerAdapter
    private lateinit var dataViewModel: DataViewModel
    private lateinit var newsList: ArrayList<News>

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
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataViewModel = ViewModelProviders.of(this).get(DataViewModel::class.java)
        // set btn listeners
        addNewsFab.setOnClickListener(this)

        newsRecyclerView.layoutManager = LinearLayoutManager(App.instance?.applicationContext, LinearLayoutManager.VERTICAL, false)
        newsRecyclerView.hasFixedSize()
        adapterNewsRecyclerAdapter = NewsRecyclerAdapter(this.context!!, ArrayList<News>(), dataViewModel)
        newsRecyclerView.adapter = adapterNewsRecyclerAdapter

        dataViewModel.getUser().observe(this@NewsFragment, Observer<User> { user: User? ->
            if(user != null && user.privilege != 0) {
                addNewsFab.show()
            }
        })
        dataViewModel.getNews().observe(this@NewsFragment, Observer<ArrayList<News>> { changedNews ->
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
    }
}
