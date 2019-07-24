package com.pleon.buyt.ui.state

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context.INPUT_METHOD_SERVICE
import android.graphics.drawable.Animatable
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import com.google.android.material.bottomappbar.BottomAppBar.FAB_ALIGNMENT_MODE_END
import com.pleon.buyt.R
import com.pleon.buyt.ui.fragment.AddItemFragment
import kotlinx.android.synthetic.main.activity_main.*

object AddItemState : State() {

    override fun onFabClicked() = with(activity) {
        val addItemFragment = supportFragmentManager.findFragmentById(R.id.fragContainer) as AddItemFragment
        addItemFragment.onDonePressed()
    }

    override fun onBackClicked() {
        super.shiftToIdleState(fabResId = R.drawable.avd_done_buyt)
        closeAddItemPopup()
        hideKeyboard()
    }

    private fun closeAddItemPopup() = with(activity) {
        supportFragmentManager.popBackStack()
        scrim.animate().alpha(0f).setDuration(300).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(anim: Animator?) = scrim.setVisibility(GONE)
        })
    }

    private fun hideKeyboard() {
        val imm = activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
    }

    override fun onOptionsMenuCreated() = with(activity) {
        bottom_bar.setNavigationIcon(R.drawable.avd_nav_cancel)
        (bottom_bar.navigationIcon as Animatable).start()
        // Because animating views was buggy in onOptionsItemSelected we do it here
        addMenuItem.isVisible = false
        reorderMenuItem.isVisible = false
        bottom_bar.fabAlignmentMode = FAB_ALIGNMENT_MODE_END
        scrim.animate().alpha(1f).setDuration(300).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(anim: Animator?) = scrim.setVisibility(VISIBLE)
        })
        fab.setImageResource(R.drawable.avd_find_done)
        (fab.drawable as Animatable).start()
    }

    override fun onRestoreInstance(savedState: Bundle) = activity.fab.setImageResource(R.drawable.ic_done)

    override fun toString() = "ADD ITEM state"

}
