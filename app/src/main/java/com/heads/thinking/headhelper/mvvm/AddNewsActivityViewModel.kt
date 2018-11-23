package com.heads.thinking.headhelper.mvvm

import android.arch.lifecycle.ViewModel
import com.google.firebase.storage.UploadTask

class AddNewsActivityViewModel : ViewModel() {
    //храним необходимые объекты при загрузке фото
    var uploadTask: UploadTask? = null
    var byteArray: ByteArray? = null
    var urlNewsImage: String? = null
}