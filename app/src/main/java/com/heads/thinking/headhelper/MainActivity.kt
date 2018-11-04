package com.heads.thinking.headhelper

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import com.heads.thinking.headhelper.adapters.mainViewPagerAdapter

class MainActivity : AppCompatActivity() {

    private lateinit var prevItemMenu: MenuItem
    private lateinit var viewPager: ViewPager
    private lateinit var navigation: BottomNavigationView

    private val fragmentsArray = ArrayList<Fragment>()

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_chat -> {
                viewPager.setCurrentItem(0)
                true
            }
            R.id.navigation_news -> {
                viewPager.setCurrentItem(1)
                true
            }
            R.id.navigation_cabinet -> {
                viewPager.setCurrentItem(2)
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

        prevItemMenu = navigation.menu.getItem(0)

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {

            }

            override fun onPageSelected(position: Int) {
                if(prevItemMenu != null)
                    prevItemMenu.setChecked(false)
                else
                    navigation.menu.getItem(0).setChecked(false)
                navigation.menu.getItem(position).setChecked(true)
                prevItemMenu = navigation.menu.getItem(position)
            }

            override fun onPageScrollStateChanged(position: Int) {

            }
        })
    }


}
