package com.heads.thinking.headhelper

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.heads.thinking.headhelper.glide.GlideApp
import com.heads.thinking.headhelper.models.News
import com.heads.thinking.headhelper.models.User
import com.heads.thinking.headhelper.util.FirestoreUtil
import com.heads.thinking.headhelper.util.StorageUtil

class NewsViewerActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var imageView: ImageView
    lateinit var tittleTV: TextView
    lateinit var textTV: TextView
    lateinit var authorTV: TextView
    lateinit var news: News
    lateinit var authorAvatarIV: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_viewer)

        imageView = findViewById(R.id.mainIV)
        tittleTV = findViewById(R.id.tittleTV)
        textTV = findViewById(R.id.textTV)
        authorTV = findViewById(R.id.authorTV)
        authorAvatarIV = findViewById(R.id.authorAvatarIV)

        news = intent.getParcelableExtra("news")
        tittleTV.text = news.tittle
        textTV.text = news.text
        if(news.authorRef != "")
            FirestoreUtil.getUser(news.authorRef!!, { isSuccessful: Boolean, user: User? ->
                if(isSuccessful) {
                    authorTV.text = "Автор: " + user!!.name
                    if(user.profilePicturePath != null)
                        GlideApp.with(this)
                                .load(StorageUtil.pathToReference(user.profilePicturePath))
                                .into(authorAvatarIV)
                } else {
                    authorTV.text = "Автор: Неизвестно"
                }
            })
        else authorTV.text = "Автор: Неизвестно"
    }

    override fun onStart() {
        super.onStart()
        if(news.picturePath != null)
            GlideApp.with(this)
                    .load(StorageUtil.pathToReference(news.picturePath!!))
                    .into(imageView)
    }

    override fun onClick(view: View?) {
        when(view!!.id) {
            R.id.backFab -> {
                onBackPressed()
            }
            R.id.editFab -> {
                startActivity(Intent(this, AddNewsActivity::class.java).apply {
                    putExtra("news", news)
                })
            }
            R.id.mainIV -> {
                //TODO start imageViewer
            }
        }
    }
}
