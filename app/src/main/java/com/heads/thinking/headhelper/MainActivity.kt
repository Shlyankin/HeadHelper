package com.heads.thinking.headhelper

import com.heads.thinking.headhelper.mvvm.MainActivityViewModel
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel : MainActivityViewModel
    private lateinit var navigation: BottomNavigationView

    private val fragmentsArray = ArrayList<Fragment>()

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_chat -> {
                viewModel.selectedMenusId = R.id.navigation_chat
                loadFragment(ChatFragment())
            }
            R.id.navigation_news -> {
                viewModel.selectedMenusId = R.id.navigation_news
                loadFragment(NewsFragment())
            }
            R.id.navigation_cabinet -> {
                viewModel.selectedMenusId = R.id.navigation_cabinet
                loadFragment(CabinetFragment())
            }

            else -> false
        }
    }

    private fun loadFragment(fragment: Fragment?): Boolean {
        if (fragment != null) {
            supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_layout, fragment)
                    .commit()
            return true
        }
        return false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigation = findViewById(R.id.navigation)
        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
        navigation.selectedItemId = viewModel.selectedMenusId
        //TODO предложить пользователю вступить в группу
        //checkUser()
    }
}
