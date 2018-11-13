package com.heads.thinking.headhelper

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.heads.thinking.headhelper.models.News
import com.heads.thinking.headhelper.util.FirestoreUtil
import com.heads.thinking.headhelper.util.StorageUtil
import java.util.*

class AddNewsActivity: AppCompatActivity(), View.OnClickListener {

    private lateinit var textET: EditText
    private lateinit var headerET: EditText

    override fun onClick(view: View?) {
        when(view!!.id) {
            R.id.backFab -> {
                onBackPressed()
            }
            R.id.addImageFab -> {

            }
            R.id.addNewsFab -> {
                FirestoreUtil.getCurrentUser {
                    val newsId: String = UUID.randomUUID().toString()
                    val news: News = News(id = newsId, tittle = headerET.text.toString(),
                            category = "", text = textET.text.toString(), picturePath = "groups/${it.groupId}/$newsId")
                    FirestoreUtil.sendNews(news, { isSuccessful: Boolean, message: String ->
                        if(isSuccessful) {
                            //TODO Upload image
                        } else {
                            Toast.makeText(App.instance!!.applicationContext, message, Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_news)

        headerET = findViewById(R.id.headerET)
        textET = findViewById(R.id.textET)
    }
}
