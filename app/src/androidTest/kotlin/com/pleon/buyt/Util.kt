package com.pleon.buyt

import android.app.Activity
import android.view.View
import androidx.annotation.StringRes
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import androidx.lifecycle.LiveData
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.IdlingResource.ResourceCallback
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage.RESUMED
import com.google.android.material.textfield.TextInputLayout
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.util.concurrent.CountDownLatch

/**
 * Better alternative to Thread::sleep.
 *
 * Use it like this: `onView(isRoot()).perform(waitFor(2000))`
 */
fun waitFor(millis: Long) = object : ViewAction {
    override fun getConstraints() = isRoot()

    override fun getDescription() = "Waiting for $millis ms"

    override fun perform(controller: UiController, view: View) {
        controller.loopMainThreadForAtLeast(millis)
    }
}

/**
 * Register an instance of this class with
 * `IdlingRegistry.getInstance().register(MyIdlingResource())`
 * at the start of the desired test case and Espresso will wait for the
 * MyIdlingResource::isIdleNow to become true to continue the test case.
 * Remember to unregister the object again at the end of the test case.
 * See [this example](https://stackoverflow.com/q/50628219)
 */
class MyIdlingResource : IdlingResource {

    override fun getName() = this::class.simpleName!!

    override fun isIdleNow() = true // indicate when the activity is idle

    override fun registerIdleTransitionCallback(callback: ResourceCallback) {
        callback.onTransitionToIdle()
    }
}


fun changeOrientationTo(orientation: Int) = object : ViewAction {
    override fun getConstraints() = isRoot()

    override fun getDescription() = "Changing orientation to $orientation"

    override fun perform(uiController: UiController, view: View) {
        uiController.loopMainThreadUntilIdle()
        val activity = view.context as Activity
        activity.requestedOrientation = orientation
        val resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(RESUMED)
        if (resumedActivities.isEmpty()) {
            throw RuntimeException("Could not change orientation")

        }
    }
}

/**
 * The `withHint()` matcher of Hamcrest had a bug with `TexInputLayout` hints.
 * See [this post](https://stackoverflow.com/a/28986731)
 */
fun withTextInputLayoutHint(@StringRes resourceId: Int) = object : TypeSafeMatcher<View>() {
    override fun matchesSafely(view: View): Boolean {
        val expectedHint = view.resources.getString(resourceId)
        return view is TextInputLayout && view.hint.toString() == expectedHint
    }

    override fun describeTo(description: Description) {}
}

fun withTextInputLayoutError(@StringRes resourceId: Int) = object : TypeSafeMatcher<View>() {
    override fun matchesSafely(view: View): Boolean {
        val expectedError = view.resources.getString(resourceId)
        return view is TextInputLayout && view.error.toString() == expectedError
    }

    override fun describeTo(description: Description) {}
}

/**
 * For LiveData to return its data synchronously.
 * Equivalent of the same-name Junit 4 rule for JUnit 5.
 * See [this post](https://jeroenmols.com/blog/2019/01/17/livedatajunit5/)
 * for more information.
 */
class InstantExecutorExtension : BeforeEachCallback, AfterEachCallback {

    override fun beforeEach(context: ExtensionContext) {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) = runnable.run()

            override fun postToMainThread(runnable: Runnable) = runnable.run()

            override fun isMainThread(): Boolean = true
        })
    }

    override fun afterEach(context: ExtensionContext) {
        ArchTaskExecutor.getInstance().setDelegate(null)
    }
}

/**
 * For unit tests we want the behavior of LiveData to be synchronous,
 * so we must block the test thread and wait for the value to be passed to the observer.
 * See [https://stackoverflow.com/a/44991770/8583692]
 */
fun <T : Any> LiveData<T>.blockingObserve(): T {
    lateinit var value: T
    val latch = CountDownLatch(1)

    observeForever {
        value = it
        latch.countDown()
    }

    latch.await()
    return value
}
