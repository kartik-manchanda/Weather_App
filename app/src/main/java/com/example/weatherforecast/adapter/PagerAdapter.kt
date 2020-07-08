package com.example.weatherforecast.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.weatherforecast.fragment.CurrentFragment
import com.example.weatherforecast.fragment.DateFragment
import com.example.weatherforecast.fragment.ReportFragment

class PagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                CurrentFragment()
            }
            1 -> {
                DateFragment()
            }
            else -> {
                return ReportFragment()
            }
        }
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> "Current"
            1 -> "Date"
            else -> return "Hourly Report"
        }
    }
}