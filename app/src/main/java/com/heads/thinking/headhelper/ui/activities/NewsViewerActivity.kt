package com.heads.thinking.headhelper.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import com.heads.thinking.headhelper.R
import com.heads.thinking.headhelper.glide.loadImage
import com.heads.thinking.headhelper.models.News
import com.heads.thinking.headhelper.models.User
import com.heads.thinking.headhelper.glide.CustomRequestListener
import com.heads.thinking.headhelper.util.FirestoreUtil
import com.heads.thinking.headhelper.util.StorageUtil
import kotlinx.android.synthetic.main.activity_news_viewer.*

class NewsViewerActivity : AppCompatActivity(), View.OnClickListener {

    private var adminFlag = false

    private lateinit var news: News

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_viewer)

        if(FirestoreUtil.currentUser?.privilege ?: 0 > 0) {
            editFab.show()
            adminFlag = true
        }

        mainIV.setOnClickListener(this)

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

        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val diff = linearLayout.bottom - (scrollView.height + scrollView.scrollY )
        }

        //анимация скрытия кнопок при скролле
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scrollView.setOnScrollChangeListener { view, newX, newY, oldX, oldY ->

                if(newY < oldY || (linearLayout.bottom - (scrollView.height + scrollView.scrollY ) < 100)) {
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
            loadImage(StorageUtil.pathToReference(news.picturePath ?: ""), this, mainIV
                    , CustomRequestListener {
                progressBar.visibility = View.GONE
            }) //
    }

    override fun onClick(view: View) {
        when(view.id) {
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
