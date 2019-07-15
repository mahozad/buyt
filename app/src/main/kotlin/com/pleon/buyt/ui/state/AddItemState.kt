package com.pleon.buyt.ui.state

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.drawable.Animatable
import android.view.View
import com.google.android.material.bottomappbar.BottomAppBar
import com.pleon.buyt.R
import com.pleon.buyt.ui.fragment.AddItemFragment
import com.pleon.buyt.ui.state.Event.*
import kotlinx.android.synthetic.main.activity_main.*

object AddItemState : UIState {

    override fun event(event: Event) {
        when (event) {
            is FabClicked -> onFabClicked()
            is HomeClicked, BackClicked -> shiftToIdleState()
            is OptionsMenuCreated -> onOptionsMenuCreated()
            is RestoreInstanceCalled -> onRestoreInstanceCalled()
            is SaveInstanceCalled, ItemListEmptied -> {}
            else -> throw IllegalStateException("Event $event is not valid in $this")
        }
    }

    private fun onFabClicked() {
        val addItemFragment = activity.supportFragmentManager.findFragmentById(R.id.fragContainer) as AddItemFragment
        addItemFragment.onDonePressed()
    }

    private fun shiftToIdleState() {
        closeAddItemPopup()

        activity.fab.setImageResource(R.drawable.avd_done_buyt)
        (activity.fab.drawable as Animatable).start()

        activity.bottom_bar.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_CENTER
        activity.bottom_bar.setNavigationIcon(R.drawable.avd_cancel_nav)
        (activity.bottom_bar.navigationIcon as Animatable).start()

        activity.storeMenuItem.isVisible = false
        activity.reorderMenuItem.setIcon(R.drawable.avd_skip_reorder).setTitle(R.string.menu_hint_reorder_items).isVisible = true
        (activity.reorderMenuItem.icon as Animatable).start()
        activity.addMenuItem.setIcon(R.drawable.avd_add_show).apply { (icon as Animatable).start() }.also { it.isVisible = true }

        activity.viewModel.state = IdleState
    }

    private fun closeAddItemPopup() {
        activity.supportFragmentManager.popBackStack()
        activity.scrim.animate().alpha(0f).setDuration(300).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(anim: Animator?) = activity.scrim.setVisibility(View.GONE)
        })
    }

    private fun onOptionsMenuCreated() {
        activity.bottom_bar.setNavigationIcon(R.drawable.avd_nav_cancel)
        (activity.bottom_bar.navigationIcon as Animatable).start()
        activity.addMenuItem.isVisible = false
        // Because animating views was buggy in onOptionsItemSelected we do it here
        activity.reorderMenuItem.isVisible = false
        activity.bottom_bar.fabAlignmentMode = BottomAppBar.FAB_ALIGNMENT_MODE_END
        activity.fab.setImageResource(R.drawable.avd_find_done)
        (activity.fab.drawable as Animatable).start()
        activity.scrim.animate().alpha(1f).setDuration(300).setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(anim: Animator?) = activity.scrim.setVisibility(View.VISIBLE)
        })
    }

    private fun onRestoreInstanceCalled() = activity.fab.setImageResource(R.drawable.ic_done)

    override fun toString() = "ADD ITEM state"

}
