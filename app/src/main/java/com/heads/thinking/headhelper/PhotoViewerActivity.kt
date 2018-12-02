package com.heads.thinking.headhelper

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import com.github.chrisbanes.photoview.PhotoView
import com.heads.thinking.headhelper.glide.loadImage
import com.heads.thinking.headhelper.util.StorageUtil

class PhotoViewerActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var progressBar: ProgressBar
    private lateinit var picturePath: String
    private lateinit var onBack: ImageButton
    private lateinit var photoView: PhotoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_viewer)


        progressBar = findViewById(R.id.progressBar)
        onBack = findViewById(R.id.onBack)
        photoView = findViewById(R.id.photoView)
        onBack.setOnClickListener(this)

        picturePath = intent.getStringExtra("picturePath")
        loadImage(StorageUtil.pathToReference(picturePath), this, photoView)
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.onBack -> onBackPressed()
        }
    }
}
