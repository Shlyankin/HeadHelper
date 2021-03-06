package com.heads.thinking.headhelper.ui.activities

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import com.github.chrisbanes.photoview.PhotoView
import com.heads.thinking.headhelper.R
import com.heads.thinking.headhelper.glide.loadImage
import com.heads.thinking.headhelper.util.StorageUtil
import kotlinx.android.synthetic.main.activity_photo_viewer.*

class PhotoViewerActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var picturePath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_viewer)
        picturePath = intent.getStringExtra("picturePath")
        loadImage(StorageUtil.pathToReference(picturePath), this, photoView)
    }

    override fun onClick(view: View) {
        when(view.id) {
            R.id.onBack -> onBackPressed()
        }
    }
}
