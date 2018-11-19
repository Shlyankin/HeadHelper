package com.heads.thinking.headhelper.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.media.Image
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.storage.FirebaseStorage
import com.heads.thinking.headhelper.App
import com.heads.thinking.headhelper.NewsViewerActivity
import com.heads.thinking.headhelper.R
import com.heads.thinking.headhelper.glide.GlideApp
import com.heads.thinking.headhelper.models.News
import com.heads.thinking.headhelper.mvvm.NewsViewModel
import com.heads.thinking.headhelper.util.FirestoreUtil
import com.heads.thinking.headhelper.util.StorageUtil
import java.text.SimpleDateFormat

class NewsRecyclerAdapter(val context: Context, var list:ArrayList<News>, val newsViewModel: NewsViewModel): RecyclerView.Adapter<NewsRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val viewHolder = LayoutInflater.from(parent.context).inflate(R.layout.item_news_recycler_view, parent, false)
        return ViewHolder(viewHolder)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bindItems(list[position])
        viewHolder.newsCardView.setOnLongClickListener {
            if (FirestoreUtil.currentUser?.privilege ?: 0 != 0) {
                val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                builder.setTitle("Вы действительно хотите удалить новость?")
                builder.setPositiveButton("Удалить", { dialogInterface: DialogInterface, i: Int ->
                    val deletingNews: News = list[position]
                    FirestoreUtil.deleteNews(deletingNews.id, {
                        if (it) {
                            Toast.makeText(App.instance, "Новость удалена", Toast.LENGTH_SHORT).show()
                            if (list.size == 1)
                                newsViewModel.updateListener()
                            if (deletingNews.picturePath != null)
                                StorageUtil.deleteNewsImage(deletingNews.picturePath!!, {})
                        } else
                            Toast.makeText(App.instance, "Не получилось удалить", Toast.LENGTH_SHORT).show()
                    })
                })
                .setNegativeButton("Отмена", null)
                builder.create().show()
            }
            true
        }
        viewHolder.newsCardView.setOnClickListener {
            context.startActivity(Intent(context, NewsViewerActivity::class.java).apply {
                putExtra("news", list[position])
            })
        }
    }

    override fun getItemCount() = list.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        lateinit var newsCardView: CardView
        lateinit var imageView: ImageView
        lateinit var itemHeader: TextView
        lateinit var dateTV: TextView

        fun bindItems(news: News) {
            imageView = itemView.findViewById(R.id.newsCircleImageView)
            itemHeader = itemView.findViewById(R.id.itemHeader)
            newsCardView = itemView.findViewById(R.id.newsCardView)
            dateTV = itemView.findViewById(R.id.dateTV)

            //load attribute
            itemHeader.text = news.tittle
            dateTV.text = SimpleDateFormat.getInstance().format(news.date)
            if(news.picturePath != null)
                GlideApp.with(imageView)
                      .load(StorageUtil.pathToReference(news.picturePath))
                      .into(imageView)
            else {
                GlideApp.with(imageView)
                        .load(R.drawable.logo)
                        .into(imageView)
            }
        }
    }
}