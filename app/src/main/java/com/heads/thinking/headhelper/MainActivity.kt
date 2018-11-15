package com.heads.thinking.headhelper

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.Toast
import com.heads.thinking.headhelper.adapters.mainViewPagerAdapter
import com.heads.thinking.headhelper.dialogs.ChangeGroupDialog
import com.heads.thinking.headhelper.util.FirestoreUtil

class MainActivity : AppCompatActivity() {

    private lateinit var prevItemMenu: MenuItem
    private lateinit var viewPager: ViewPager
    private lateinit var navigation: BottomNavigationView

    private val fragmentsArray = ArrayList<Fragment>()

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_chat -> {
                viewPager.currentItem = 0
                true
            }
            R.id.navigation_news -> {
                viewPager.currentItem = 1
                true
            }
            R.id.navigation_cabinet -> {
                viewPager.currentItem = 2
                true
            }
            else -> false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation = findViewById(R.id.navigation)
        viewPager = findViewById(R.id.viewPager)

        fragmentsArray.add(ChatFragment())
        fragmentsArray.add(NewsFragment())
        fragmentsArray.add(CabinetFragment())
        viewPager.adapter = mainViewPagerAdapter(fragmentsArray, supportFragmentManager)
        navigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        prevItemMenu = navigation.menu.getItem(1)
        prevItemMenu.isChecked = true
        viewPager.currentItem = 1


        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}

            override fun onPageSelected(position: Int) {
                prevItemMenu.isChecked = false
                navigation.menu.getItem(position).isChecked = true
                prevItemMenu = navigation.menu.getItem(position)
            }

            override fun onPageScrollStateChanged(position: Int) {}
        })
        checkGroup()
    }

    private fun checkGroup() {
        FirestoreUtil.getCurrentUser {
            if (it.groupId == null || it.groupId == "") {
                Toast.makeText(this,"Вы не состоите в группе", Toast.LENGTH_SHORT).show()
                val dialogFragment = ChangeGroupDialog()
                dialogFragment.show(this.supportFragmentManager, "changeGroup")
            }
        }
    }
}
