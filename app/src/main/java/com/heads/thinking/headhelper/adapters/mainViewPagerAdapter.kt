package com.heads.thinking.headhelper.adapters

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class mainViewPagerAdapter(var fragmentsArray: List<Fragment>, fm: FragmentManager?) : FragmentPagerAdapter(fm) {

    override fun getItem(index: Int): Fragment = fragmentsArray[index]

    override fun getCount() = fragmentsArray.size

}