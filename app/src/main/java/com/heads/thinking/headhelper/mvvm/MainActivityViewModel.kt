package com.heads.thinking.headhelper.mvvm

import android.arch.lifecycle.ViewModel
import com.heads.thinking.headhelper.R

class MainActivityViewModel  : ViewModel() {
    //храним выбранный пункт меню
    var selectedMenusId : Int = R.id.navigation_news
}