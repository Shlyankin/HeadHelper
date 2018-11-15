package com.heads.thinking.headhelper.adapters

import android.content.Context
import android.content.Intent
import android.media.Image
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.storage.FirebaseStorage
import com.heads.thinking.headhelper.NewsViewerActivity
import com.heads.thinking.headhelper.R
import com.heads.thinking.headhelper.glide.GlideApp
import com.heads.thinking.headhelper.models.News
import com.heads.thinking.headhelper.util.FirestoreUtil
import com.heads.thinking.headhelper.util.StorageUtil

class NewsRecyclerAdapter(val context: Context, var list:List<News>): RecyclerView.Adapter<NewsRecyclerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val viewHolder = LayoutInflater.from(parent.context).inflate(R.layout.item_news_recycler_view, parent, false)
        return ViewHolder(viewHolder)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bindItems(list[position])
        viewHolder.deleteBtn.setOnClickListener {
            /*
            //TODO delete news
            if(list[position].picturePath != null)
                StorageUtil.deleteNewsImage(list[position].picturePath!!, {})
            */
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
        lateinit var deleteBtn: ImageView

        fun bindItems(news: News) {
            imageView = itemView.findViewById(R.id.newsCircleImageView)
            itemHeader = itemView.findViewById(R.id.itemHeader)
            newsCardView = itemView.findViewById(R.id.newsCardView)
            deleteBtn = itemView.findViewById(R.id.deleteNewsBtn)

            //load attribute
            itemHeader.text = news.tittle
            if(news.picturePath != null)
                GlideApp.with(imageView)
                      .load(StorageUtil.pathToReference(news.picturePath))
                      .into(imageView)
        }
    }
}