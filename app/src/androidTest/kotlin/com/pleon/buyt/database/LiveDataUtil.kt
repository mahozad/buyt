package com.pleon.buyt.database

import androidx.lifecycle.LiveData
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

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

    latch.await(2, TimeUnit.SECONDS)
    return value
}
