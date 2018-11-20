package com.heads.thinking.headhelper.mvvm

import android.arch.lifecycle.ViewModel
import com.google.firebase.storage.UploadTask

class AddNewsActivityViewModel : ViewModel() {
    //храним важные объекты при добавление новости
    var uploadTask: UploadTask? = null
    var byteArray: ByteArray? = null
    var urlNewsImage: String? = null
}