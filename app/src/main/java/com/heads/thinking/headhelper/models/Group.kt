package com.heads.thinking.headhelper.models

class Group(
        val members: Map<String, User>?,
        val news: List<News>?) {
    constructor(): this(null, null)
}