package com.heads.thinking.headhelper.glide

import android.graphics.drawable.Drawable
import android.widget.Toast
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.heads.thinking.headhelper.App

class CustomRequestListener(val onReady: (isSuccessfull: Boolean) -> Unit) : RequestListener<Drawable> {

    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
        Toast.makeText(App.instance, "Ошибка при загрузке изображения", Toast.LENGTH_LONG)
        onReady(false)
        return false
    }

    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
        onReady(true)
        return false
    }
}