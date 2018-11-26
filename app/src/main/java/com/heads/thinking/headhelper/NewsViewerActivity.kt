package com.heads.thinking.headhelper

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import com.heads.thinking.headhelper.glide.loadImage
import com.heads.thinking.headhelper.models.News
import com.heads.thinking.headhelper.models.User
import com.heads.thinking.headhelper.util.FirestoreUtil
import com.heads.thinking.headhelper.util.StorageUtil
import kotlinx.android.synthetic.main.activity_add_news.*

class NewsViewerActivity : AppCompatActivity(), View.OnClickListener {

    private var adminFlag = false

    private lateinit var imageView: ImageView
    private lateinit var tittleTV: TextView
    private lateinit var textTV: TextView
    private lateinit var authorTV: TextView
    private lateinit var editFab: FloatingActionButton
    private lateinit var backFab: FloatingActionButton
    private lateinit var news: News
    private lateinit var authorAvatarIV: ImageView
    private lateinit var scrollView: ScrollView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_viewer)

        imageView = findViewById(R.id.mainIV)
        tittleTV = findViewById(R.id.tittleTV)
        textTV = findViewById(R.id.textTV)
        authorTV = findViewById(R.id.authorTV)
        authorAvatarIV = findViewById(R.id.authorAvatarIV)
        editFab = findViewById(R.id.editFab)
        backFab = findViewById(R.id.backFab)
        scrollView = findViewById(R.id.scrollViwe)

        editFab.setOnClickListener(this)
        backFab.setOnClickListener(this)

        if(FirestoreUtil.currentUser?.privilege ?: 0 > 0) {
            editFab.show()
            adminFlag = true
        }

        imageView.setOnClickListener(this)

        news = intent.getParcelableExtra("news")
        tittleTV.text = news.tittle
        textTV.text = news.text
        if(news.authorRef != "")
            FirestoreUtil.getUser(news.authorRef!!, { isSuccessful: Boolean, user: User? ->
                if(isSuccessful) {
                    authorTV.text = "Автор: " + user!!.name
                    if(user.profilePicturePath != null)
                        loadImage(StorageUtil.pathToReference(user.profilePicturePath), this, authorAvatarIV)
                } else {
                    authorTV.text = "Автор: Неизвестно"
                }
            })
        else authorTV.text = "Автор: Неизвестно"

        //анимация скрытия кнопок при скролле
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scrollView.setOnScrollChangeListener { view, newX, newY, oldX, oldY ->
                if(newY < oldY || newY == view.height) {
                    if(adminFlag)
                        editFab.show()
                    backFab.show()
                } else {
                    if (adminFlag)
                        editFab.hide()
                    backFab.hide()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if(news.picturePath != null)
            loadImage(StorageUtil.pathToReference(news.picturePath ?: ""), this, imageView)
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
