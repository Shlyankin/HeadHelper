package com.heads.thinking.headhelper.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Parcelable
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityCompat.requestPermissions
import android.support.v4.content.FileProvider
import com.heads.thinking.headhelper.BuildConfig
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object CustomImageManager {

    val REQUEST_CODE_PERMISSION_RECEIVE_CAMERA = 102
    val REQUEST_CODE_TAKE_PHOTO = 103

    lateinit var tempPhoto: File
    var imageUri = ""


    fun getPhotoMakerIntent(activity: Activity): Intent? {
        //Проверяем разрешение на работу с камерой
        var isCameraPermissionGranted = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.CAMERA) === PackageManager.PERMISSION_GRANTED
        //Проверяем разрешение на работу с внешнем хранилещем телефона
        var isWritePermissionGranted = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) === PackageManager.PERMISSION_GRANTED

        //Если разрешения != true
        if (!isCameraPermissionGranted || !isWritePermissionGranted) {

            val permissions: Array<String>//Разрешения которые хотим запросить у пользователя

            if (!isCameraPermissionGranted && !isWritePermissionGranted) {
                permissions = arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else if (!isCameraPermissionGranted) {
                permissions = arrayOf(android.Manifest.permission.CAMERA)
            } else {
                permissions = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
            //Запрашиваем разрешения у пользователя
            requestPermissions(activity, permissions, REQUEST_CODE_PERMISSION_RECEIVE_CAMERA)
            //снова проверяем разрешения и запускаем метод заново, если разрешения получены
            isCameraPermissionGranted = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.CAMERA) === PackageManager.PERMISSION_GRANTED
            isWritePermissionGranted = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) === PackageManager.PERMISSION_GRANTED
            if (isCameraPermissionGranted && isWritePermissionGranted)
                getPhotoMakerIntent(activity)
            return null
        } else {
            //Если все разрешения получены
            tempPhoto = createTempImageFile(activity.getExternalCacheDir())
            //Создаём лист с интентами для работы с изображениями
            var intentList = ArrayList<Intent>()
            var chooserIntent: Intent? = null
            val pickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            takePhotoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            val uri = FileProvider.getUriForFile(activity,
                    BuildConfig.APPLICATION_ID + ".provider",
                    tempPhoto)
            imageUri = uri.toString()
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri)

            intentList = addIntentsToList(activity, intentList, pickIntent) as ArrayList<Intent>
            intentList = addIntentsToList(activity, intentList, takePhotoIntent) as ArrayList<Intent>

            if (!intentList.isEmpty()) {
                chooserIntent = Intent.createChooser(intentList.removeAt(intentList.size - 1), "Choose your image source")
                chooserIntent!!.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(arrayOf<Parcelable>()))
            }
            return chooserIntent;
        }
    }

    //not working
    fun getRealPathFromURI(activity: Activity, uri: Uri): String {
        val projection = arrayOf<String>(MediaStore.Images.Media.DATA)
        val cursor = activity.contentResolver.
                query(uri, projection, null, null, null) //return null??

        val columnIndex = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(columnIndex)
    }

    private fun createTempImageFile(storageDir: File?): File {
        // Генерируем имя файла по текущему времени
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "photo_$timeStamp"

        return File.createTempFile(
                imageFileName, /* prefix */
                ".jpg", /* suffix */
                storageDir      /* directory */
        )
    }

    private fun addIntentsToList(context: Context, list: MutableList<Intent>, intent: Intent): List<Intent> {
        val resInfo = context.packageManager.queryIntentActivities(intent, 0)
        for (resolveInfo in resInfo) {
            val packageName = resolveInfo.activityInfo.packageName
            val targetedIntent = Intent(intent)
            targetedIntent.setPackage(packageName)
            list.add(targetedIntent)
        }
        return list
    }
}