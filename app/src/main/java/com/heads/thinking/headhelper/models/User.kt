package com.heads.thinking.headhelper.models

data class User(val id: String,
                val name: String,
                val privilege: Int,
                val profilePicturePath: String?,
                val registrationTokens: MutableList<String>,
                val groupId: String?) {
    //конструктор по умолчанию для firestore
    constructor(): this("", "", 0,null, mutableListOf(), null)
}