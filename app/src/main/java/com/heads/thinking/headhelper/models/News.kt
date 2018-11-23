package com.heads.thinking.headhelper.models

import android.os.Parcel
import android.os.Parcelable
import java.util.*

data class News(
        val id: String,
        val date: Date?,
        val picturePath: String?,
        val tittle: String,
        val text: String,
        val authorRef:String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            null, //дату не инициализируем
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()) {}

    //конструктор по умолчанию для firestore
    constructor(): this("", null, null, "", "", null)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(picturePath)
        parcel.writeString(tittle)
        parcel.writeString(text)
        parcel.writeString(authorRef)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<News> {
        override fun createFromParcel(parcel: Parcel): News {
            return News(parcel)
        }

        override fun newArray(size: Int): Array<News?> {
            return arrayOfNulls(size)
        }
    }
}