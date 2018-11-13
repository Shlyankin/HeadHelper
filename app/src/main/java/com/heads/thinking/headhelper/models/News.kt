package com.heads.thinking.headhelper.models

data class News(
        val id: String,
        val category: String,
        val picturePath: String?,
        val tittle: String,
        val text: String
) {
    constructor(): this("", "", null, "", "")
}