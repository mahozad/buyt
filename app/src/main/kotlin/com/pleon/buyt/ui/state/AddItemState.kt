package com.pleon.buyt.ui.state

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.drawable.Animatable
import android.view.View.GONE
import android.view.View.VISIBLE
import com.google.android.material.bottomappbar.BottomAppBar.FAB_ALIGNMENT_MODE_END
import com.pleon.buyt.R
import com.pleon.buyt.ui.fragment.AddItemFragment
import com.pleon.buyt.ui.state.Event.*
import kotlinx.android.synthetic.main.activity_main.*

object AddItemState : State() {

    override fun event(event: Event) {
        when (event) {
            is FabClicked -> onFabClicked()
            is HomeClicked, BackClicked -> shiftToIdleState()
            is RestoreInstanceCalled -> onRestoreInstanceCalled()
            is OptionsMenuCreated -> onOptionsMenuCreated()
            is SaveInstanceCalled -> {}
            is ItemListChanged -> {}
            else -> throw IllegalStateException("Event $event is not valid in $this")
        }
    }

    private fun onFabClicked() = with(activity) {
        val addItemFragment = supportFragmentManager.findFragmentById(R.id.fragContainer) as AddItemFragment
        addItemFragment.onDonePressed()
    }

    private fun shiftToIdleState() {
        super.shiftToIdleState(fabResId = R.drawable.avd_done_buyt)
        closeAddItemPopup()
    }

    private fun closeAddItemPopup() = with(activity) {
        supportFragmentManager.popBackStack()
        scrim.animate().alpha(0f).setDuration(300).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(anim: Animator?) = scrim.setVisibility(GONE)
        })
    }

    private fun onOptionsMenuCreated() = with(activity) {
        bottom_bar.setNavigationIcon(R.drawable.avd_nav_cancel)
        (bottom_bar.navigationIcon as Animatable).start()
        // Because animating views was buggy in onOptionsItemSelected we do it here
        addMenuItem.isVisible = false
        reorderMenuItem.isVisible = false
        bottom_bar.fabAlignmentMode = FAB_ALIGNMENT_MODE_END
        fab.setImageResource(R.drawable.avd_find_done)
        (fab.drawable as Animatable).start()
        scrim.animate().alpha(1f).setDuration(300).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(anim: Animator?) = scrim.setVisibility(VISIBLE)
        })
    }

    private fun onRestoreInstanceCalled() = activity.fab.setImageResource(R.drawable.ic_done)

    override fun toString() = "ADD ITEM state"

}
