package com.heads.thinking.headhelper.ui.activities

import com.heads.thinking.headhelper.mvvm.MainActivityViewModel
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.heads.thinking.headhelper.ui.fragments.CabinetFragment
import com.heads.thinking.headhelper.ui.fragments.ChatFragment
import com.heads.thinking.headhelper.ui.fragments.NewsFragment
import com.heads.thinking.headhelper.R
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    private lateinit var viewModel : MainActivityViewModel

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

    private fun loadFragment(fragment: Fragment): Boolean {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_layout, fragment)
                .commit()
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
    }

    override fun onResume() {
        super.onResume()
        navigation.selectedItemId = viewModel.selectedMenusId
    }
}
