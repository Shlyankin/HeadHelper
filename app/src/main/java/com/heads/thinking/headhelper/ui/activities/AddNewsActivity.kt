package com.heads.thinking.headhelper.ui.activities

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.UploadTask
import com.heads.thinking.headhelper.App
import com.heads.thinking.headhelper.R
import com.heads.thinking.headhelper.glide.loadImage
import com.heads.thinking.headhelper.models.News
import com.heads.thinking.headhelper.mvvm.AddNewsActivityViewModel
import com.heads.thinking.headhelper.util.CustomImageManager
import com.heads.thinking.headhelper.util.FirestoreUtil
import com.heads.thinking.headhelper.util.StorageUtil
import kotlinx.android.synthetic.main.activity_add_news.*
import org.jetbrains.anko.contentView
import java.io.ByteArrayOutputStream
import java.util.*

class AddNewsActivity: AppCompatActivity(), View.OnClickListener {

    private var news: News? = null
    private var uploadImageTask : UploadTask? = null
    private var selectedImageBytes : ByteArray? = null
    private var urlNewsImage : String? = null

    private lateinit var viewModel : AddNewsActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_news)

        viewModel = ViewModelProviders.of(this).get(AddNewsActivityViewModel::class.java)

        news = intent.getParcelableExtra("news")

        if(news != null) {
            if(headerET.text.toString().isEmpty()) headerET.setText(news!!.tittle)
            if(textET.text.isEmpty()) textET.setText(news!!.text)
            if(news!!.picturePath != null) {
                urlNewsImage = news!!.picturePath!!
                loadImage(StorageUtil.pathToReference(news!!.picturePath!!), this, imageView)
            }
        }

        if(viewModel.urlNewsImage != null)
            urlNewsImage = viewModel.urlNewsImage
        selectedImageBytes = viewModel.byteArray
        if (selectedImageBytes != null)
            loadImage(selectedImageBytes, this, imageView)
        uploadImageTask = viewModel.uploadTask
        startUpload(uploadImageTask)

        //отслеживаем появление клавиатуры на экране
        contentView?.viewTreeObserver?.addOnGlobalLayoutListener {
            val r: Rect = Rect()
            contentView!!.getWindowVisibleDisplayFrame(r)
            val screenHeight: Int = contentView!!.rootView.height
            val keyPadHeight = screenHeight - r.bottom

            if(keyPadHeight > screenHeight * 0.15) {
                // 0.15 to determinate keypad height
                //клавиатура открыта
                addNewsFab.hide()
                addImageFab.hide()
                backFab.hide()
            } else {
                //клавиатура закрыта
                addNewsFab.show()
                addImageFab.show()
                backFab.show()
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.backFab -> {
                onBackPressed()
            }
            R.id.addImageFab -> {
                when {
                    uploadImageTask == null -> // изображение не загружалось ранее
                        CustomImageManager.getPhotoMakerIntent(this)?.let {
                            startActivityForResult(it, CustomImageManager.REQUEST_CODE_TAKE_PHOTO)
                        }
                    uploadImageTask!!.isSuccessful -> {
                        // изображение уже загружено и его надо удалить
                        StorageUtil.deleteNewsImage(urlNewsImage!!, {})
                        CustomImageManager.getPhotoMakerIntent(this)?.let {
                            startActivityForResult(it, CustomImageManager.REQUEST_CODE_TAKE_PHOTO)
                        }
                    }
                    else -> {
                        // изображение в процессе загрузки и надо отменить операцию
                        cancelUpload(uploadImageTask)// Отменяем загрузку
                        CustomImageManager.getPhotoMakerIntent(this)?.let {
                            startActivityForResult(it, CustomImageManager.REQUEST_CODE_TAKE_PHOTO)
                        }
                    }
                }
            }
            R.id.addNewsFab -> {
                if (uploadImageTask != null && uploadImageTask!!.isInProgress) {
                    Toast.makeText(App.instance!!.applicationContext, "Подождите, идет загрузка фото на сервер", Toast.LENGTH_SHORT).show()
                } else {
                    val tittle: String = headerET.text.toString()
                    if(tittle == "") {
                        Toast.makeText(this, "Напишите заголовок, чтобы новость была более информативной", Toast.LENGTH_SHORT).show()
                    } else {
                        val user = FirestoreUtil.currentUser
                        if(user != null) {
                            val newsId: String = news?.id ?: UUID.randomUUID().toString()
                            val news: News = News(id = newsId, tittle = tittle,
                                    date = Calendar.getInstance().time, text = textET.text.toString(),
                                    picturePath = urlNewsImage, authorRef =  user.id)
                            FirestoreUtil.sendNews(news, { isSuccessful: Boolean, message: String ->
                                if (isSuccessful) {
                                    Toast.makeText(App.instance!!.applicationContext, "Новость выложена", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(App.instance!!.applicationContext, message, Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                }
            }
        }
    }
    override fun onBackPressed() {
        if (uploadImageTask == null) super.onBackPressed()
        else {
            if(uploadImageTask!!.isInProgress) {
                cancelUpload(uploadImageTask)
                super.onBackPressed()
            }
            if(uploadImageTask!!.isSuccessful) {
                StorageUtil.deleteNewsImage(urlNewsImage!!, {})
                super.onBackPressed()
            } else {
                super.onBackPressed()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CustomImageManager.REQUEST_CODE_TAKE_PHOTO -> if (resultCode == Activity.RESULT_OK) {
                if (data != null && data.data != null) {
                    setImage(data.data)
                } else if (CustomImageManager.imageUri != "") {
                    CustomImageManager.imageUri = Uri.fromFile(CustomImageManager.tempPhoto).toString()
                    setImage(Uri.parse(CustomImageManager.imageUri))
                } else {
                    Toast.makeText(this, "Не могу найти изображение. Попробуйте снова", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Не могу найти изображение. Попробуйте снова", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setImage(uri: Uri) {
        val selectedImagePath = uri.toString()
        val selectedImageBmp = MediaStore.Images.Media
                .getBitmap(this.contentResolver, Uri.parse(selectedImagePath))

        val outputStream = ByteArrayOutputStream()
        if (selectedImageBmp != null) {
            selectedImageBmp.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            selectedImageBytes = outputStream.toByteArray()
            viewModel.byteArray = selectedImageBytes
            loadImage(selectedImageBytes, this, imageView)

            uploadImageTask = StorageUtil.uploadNewsImage(selectedImageBytes!!, { urlImage:String ->
                urlNewsImage = urlImage
                viewModel.urlNewsImage = urlImage
            })
            viewModel.uploadTask = uploadImageTask
            startUpload(uploadImageTask)

            uploadImageTask!!.addOnSuccessListener {
                Toast.makeText(App.instance, "Фото новости загружено", Toast.LENGTH_SHORT).show()
            }

            uploadImageTask!!.addOnCanceledListener {
                urlNewsImage = null
                viewModel.urlNewsImage = null
                Toast.makeText(App.instance, "Загрузка фото отменена", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(App.instance, "Не удалось загрузить изображение", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startUpload(uploadImageTask: UploadTask?) {
        if(uploadImageTask != null && uploadImageTask.isInProgress) {
            uploadProgressBar.visibility = View.VISIBLE
            addNewsFab.hide()
            uploadImageTask.addOnProgressListener(OnProgressListener<UploadTask.TaskSnapshot> { taskSnapshot ->
                if (taskSnapshot?.bytesTransferred == taskSnapshot?.totalByteCount) {
                    uploadProgressBar.visibility = View.GONE
                    addNewsFab.show()
                }
            })
        }
    }

    private fun cancelUpload(uploadImageTask: UploadTask?) {
        if(uploadImageTask != null) {
            uploadImageTask.cancel()
            uploadProgressBar.visibility = View.GONE
            addNewsFab.show()
            imageView.setImageDrawable(null)
        }
    }
}
