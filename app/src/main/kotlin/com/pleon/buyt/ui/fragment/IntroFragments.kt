package com.pleon.buyt.ui.fragment

import android.os.Bundle
import android.view.View
import com.pleon.buyt.R
import com.pleon.buyt.util.animateIconInfinitely
import kotlinx.android.synthetic.main.fragment_intro_1.*

class IntroFragment1 : BaseFragment() {

    override fun layout() = R.layout.fragment_intro_1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        animateIconInfinitely(img.drawable, startDelay = 750, repeatDelay = 2000)
        super.onViewCreated(view, savedInstanceState)
    }
}

class IntroFragment2 : BaseFragment() {

    override fun layout() = R.layout.fragment_intro_2

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        animateIconInfinitely(img.drawable, startDelay = 500, repeatDelay = 2000)
        super.onViewCreated(view, savedInstanceState)
    }
}

class IntroFragment3 : BaseFragment() {

    override fun layout() = R.layout.fragment_intro_3

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        animateIconInfinitely(img.drawable, startDelay = 500, repeatDelay = 2000)
        super.onViewCreated(view, savedInstanceState)
    }
}
