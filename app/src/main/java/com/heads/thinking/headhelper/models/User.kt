package com.heads.thinking.headhelper.models

import android.os.Parcel
import android.os.Parcelable


data class User(val id: String,
                val name: String,
                val profilePicturePath: String?,
                val registrationTokens: MutableList<String>,
                val groupId: String?) {
    constructor(): this("", "", null, mutableListOf(), null)
}