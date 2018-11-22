package com.heads.thinking.headhelper.models

import java.util.Date

data class Message(
        val id: String,
        val userRef: String,
        val textMessage: String,
        val date: Date?,
        val picturePath: String?
) {
    constructor() : this("", "", "", null, null)
}