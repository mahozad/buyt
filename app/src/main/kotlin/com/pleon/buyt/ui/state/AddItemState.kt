package com.pleon.buyt.ui.state

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import com.google.android.material.bottomappbar.BottomAppBar.FAB_ALIGNMENT_MODE_END
import com.pleon.buyt.R
import com.pleon.buyt.ui.fragment.AddItemFragment
import com.pleon.buyt.util.AnimationUtil.animateAlpha
import com.pleon.buyt.util.AnimationUtil.animateIcon
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
        animateAlpha(scrim, toAlpha = 0f, duration = 300)
    }

    private fun hideKeyboard() {
        val imm = activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        activity.currentFocus?.run { imm.hideSoftInputFromWindow(this.windowToken, 0) }
    }

    override fun onOptionsMenuCreated() = with(activity) {
        // Because animating views was buggy in onOptionsItemSelected we do it here
        super.onOptionsMenuCreated()
        reorderMenuItem.isVisible = false
        bottom_bar.fabAlignmentMode = FAB_ALIGNMENT_MODE_END
        animateAlpha(scrim, toAlpha = 1f, duration = 300)
        fab.setImageResource(R.drawable.avd_find_done).also { animateIcon(fab.drawable) }
    }

    override fun onRestoreInstance(savedState: Bundle) = activity.fab.setImageResource(R.drawable.ic_done)

    override fun toString() = "ADD ITEM state"

}
