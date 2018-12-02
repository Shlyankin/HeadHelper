package com.heads.thinking.headhelper.glide

import android.app.Activity
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.request.RequestListener

/**
 Содержимое данного пакета контролирует загрузку изображений библиотекой Glide.
 При использовании класса GlideApp могут возникать ошибки связанные с lifecycle объектов.
 Метод loadImage обрабатывает возможные ошибки c lifecycle подгружаемых объектов
**/
fun loadImage(url : Any?, context: Context?, view: ImageView, listener: RequestListener<Drawable>? = null){
    if (context == null) {
        return
    } else if (context is Activity) {
        if (context.isDestroyed || context.isFinishing)
            return
        else
            load(url, context, view, listener)
    } else return
}

private fun load(url : Any?, context: Context, view: ImageView, listener: RequestListener<Drawable>?) {
    try {
        GlideApp.with(context)
                .load(url)
                .listener(listener)
                .into(view)
    } catch (exc: Exception) {
        Log.e("GlideError", "Image loading is failed\n" + exc.message)
    }
}