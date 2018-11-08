package com.heads.thinking.headhelper.models


data class User(val name: String,
                val profilePicturePath: String?,
                val registrationTokens: MutableList<String>,
                val groupId: String?) {
    constructor(): this("", null, mutableListOf(), null)
}