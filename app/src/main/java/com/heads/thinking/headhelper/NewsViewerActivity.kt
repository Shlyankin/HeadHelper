package com.heads.thinking.headhelper

import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.util.Log
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
    lateinit var editFab: FloatingActionButton
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
        editFab = findViewById(R.id.editFab)

        if(FirestoreUtil.currentUser?.privilege ?: 0 > 0)
            editFab.show()

        imageView.setOnClickListener(this)

        news = intent.getParcelableExtra("news")
        tittleTV.text = news.tittle
        textTV.text = news.text
        if(news.authorRef != "")
            FirestoreUtil.getUser(news.authorRef!!, { isSuccessful: Boolean, user: User? ->
                if(isSuccessful) {
                    authorTV.text = "Автор: " + user!!.name
                    if(user.profilePicturePath != null)
                        try {
                            GlideApp.with(this)
                                    .load(StorageUtil.pathToReference(user.profilePicturePath))
                                    .into(authorAvatarIV)
                        } catch (exc: KotlinNullPointerException) {
                            Log.e("GlideError", "AuthorImage loading error " + exc.message)
                        }
                } else {
                    authorTV.text = "Автор: Неизвестно"
                }
            })
        else authorTV.text = "Автор: Неизвестно"
    }

    override fun onStart() {
        super.onStart()
        if(news.picturePath != null)
            try {
                GlideApp.with(this)
                        .load(StorageUtil.pathToReference(news.picturePath ?: ""))
                        .into(imageView)
            } catch(exc: KotlinNullPointerException) {
                Log.e("GlideError", "MainImage loading error " + exc.message)
            }
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
                if(news.picturePath != null) {
                    startActivity(Intent(this, PhotoViewerActivity::class.java).apply {
                        putExtra("picturePath", news.picturePath)
                    })
                }
            }
        }
    }
}
