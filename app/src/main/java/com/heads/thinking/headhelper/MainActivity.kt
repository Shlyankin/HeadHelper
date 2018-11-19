package com.heads.thinking.headhelper

import com.heads.thinking.headhelper.mvvm.MainActivityViewModel
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.Toast
import com.heads.thinking.headhelper.util.FirestoreUtil

class MainActivity : AppCompatActivity() {

    private lateinit var userViewModel : MainActivityViewModel
    private lateinit var prevItemMenu: MenuItem
    private lateinit var viewPager: ViewPager
    private lateinit var navigation: BottomNavigationView

    private val fragmentsArray = ArrayList<Fragment>()

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_chat -> {
                userViewModel.selectedMenusId = R.id.navigation_chat
                loadFragment(ChatFragment())
            }
            R.id.navigation_news -> {
                userViewModel.selectedMenusId = R.id.navigation_news
                loadFragment(NewsFragment())
            }
            R.id.navigation_cabinet -> {
                userViewModel.selectedMenusId = R.id.navigation_cabinet
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
        userViewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
        navigation.selectedItemId = userViewModel.selectedMenusId
        //TODO предложить пользователю вступить в группу
        //checkUser()
    }

    /*private fun checkUser() {
        val user = FirestoreUtil.currentUser
            if (user != null && user.groupId == null) {
                Toast.makeText(this,"Вы не состоите в группе\n" +
                        "Зайдите в кабинет и поменяйте группе", Toast.LENGTH_SHORT).show()
            }
    }*/
}
