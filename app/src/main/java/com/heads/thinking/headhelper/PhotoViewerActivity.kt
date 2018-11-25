package com.heads.thinking.headhelper

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import com.github.chrisbanes.photoview.PhotoView
import com.heads.thinking.headhelper.glide.GlideApp
import com.heads.thinking.headhelper.util.StorageUtil

class PhotoViewerActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var progressBar: ProgressBar
    lateinit var picturePath: String
    lateinit var onBack: ImageButton
    lateinit var photoView: PhotoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_viewer)


        progressBar = findViewById(R.id.progressBar)
        onBack = findViewById(R.id.onBack)
        photoView = findViewById(R.id.photoView)
        onBack.setOnClickListener(this)

        picturePath = intent.getStringExtra("picturePath")
        try {
            val request = GlideApp.with(this)
                    .load(StorageUtil.pathToReference(picturePath))
                    .into(photoView)
        } catch (exc: KotlinNullPointerException) {
            Log.e("GlideError", "PhotoViewer error " + exc.message)
        }
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.onBack -> onBackPressed()
        }
    }
}
