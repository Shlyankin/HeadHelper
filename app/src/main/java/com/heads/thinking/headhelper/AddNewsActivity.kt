package com.heads.thinking.headhelper

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.storage.UploadTask
import com.heads.thinking.headhelper.glide.GlideApp
import com.heads.thinking.headhelper.models.News
import com.heads.thinking.headhelper.mvvm.AddNewsActivityViewModel
import com.heads.thinking.headhelper.util.CustomImageManager
import com.heads.thinking.headhelper.util.FirestoreUtil
import com.heads.thinking.headhelper.util.StorageUtil
import java.io.ByteArrayOutputStream
import java.util.UUID

class AddNewsActivity: AppCompatActivity(), View.OnClickListener {

    private var news: News? = null
    private var uploadImageTask : UploadTask? = null
    private var selectedImageBytes : ByteArray? = null
    private var urlNewsImage : String? = null
    private lateinit var textET: EditText
    private lateinit var headerET: EditText
    private lateinit var imageView: ImageView
    lateinit var viewModel : AddNewsActivityViewModel

    override fun onClick(view: View?) {
        when (view!!.id) {
            R.id.backFab -> {
                onBackPressed()
            }
            R.id.addImageFab -> {
                if (uploadImageTask == null)
                    CustomImageManager.getPhotoMakerIntent(this)?.let {
                        startActivityForResult(it, CustomImageManager.REQUEST_CODE_TAKE_PHOTO)
                    }
                else if(uploadImageTask!!.isComplete) {
                    // изображение уже загружено и его надо удалить
                    StorageUtil.deleteNewsImage(urlNewsImage!!, {})
                    CustomImageManager.getPhotoMakerIntent(this)?.let {
                        startActivityForResult(it, CustomImageManager.REQUEST_CODE_TAKE_PHOTO)
                    }
                } else {
                    // изображение в процессе загрузки и надо отменить операцию
                    uploadImageTask!!.cancel() // Отменяем загрузку
                    CustomImageManager.getPhotoMakerIntent(this)?.let {
                        startActivityForResult(it, CustomImageManager.REQUEST_CODE_TAKE_PHOTO)
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
                        FirestoreUtil.getCurrentUser {
                            val newsId: String = news?.id ?: UUID.randomUUID().toString()
                            val news: News = News(id = newsId, tittle = tittle,
                                    category = "", text = textET.text.toString(),
                                    picturePath = urlNewsImage, authorRef =  it.id)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_news)

        headerET = findViewById(R.id.headerET)
        textET = findViewById(R.id.textET)
        imageView = findViewById(R.id.imageView)

        viewModel = ViewModelProviders.of(this).get(AddNewsActivityViewModel::class.java)

        news = intent.getParcelableExtra("news")

        if(news != null) {
            if(headerET.text.toString().isEmpty()) headerET.setText(news!!.tittle)
            if(textET.text.isEmpty()) textET.setText(news!!.text)
            if(news!!.picturePath != null) {
                urlNewsImage = news!!.picturePath!!
                GlideApp.with(this)
                        .load(StorageUtil.pathToReference(news!!.picturePath!!))
                        .into(imageView)
            }
        }

        if(viewModel.urlNewsImage != null)
            urlNewsImage = viewModel.urlNewsImage
        selectedImageBytes = viewModel.byteArray
        uploadImageTask = viewModel.uploadTask

        if (selectedImageBytes != null)
            GlideApp.with(this)
                    .load(selectedImageBytes)
                    .into(imageView)
    }

    override fun onBackPressed() {
        if (uploadImageTask == null) super.onBackPressed()
        else {
            if(uploadImageTask!!.isInProgress) {
                uploadImageTask!!.cancel()
                super.onBackPressed()
            }
            else {
                StorageUtil.deleteNewsImage(urlNewsImage!!, {})
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
            GlideApp.with(this)
                    .load(selectedImageBytes)
                    .into(imageView)

            uploadImageTask = StorageUtil.uploadNewsImage(selectedImageBytes!!, { urlImage:String ->
                urlNewsImage = urlImage
                viewModel.urlNewsImage = urlImage
            })
            viewModel.uploadTask = uploadImageTask

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
}
