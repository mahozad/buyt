package com.pleon.buyt.viewmodel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.pleon.buyt.InstantExecutorExtension
import com.pleon.buyt.model.Item
import com.pleon.buyt.repository.MainRepository
import com.pleon.buyt.ui.state.IdleState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.jetbrains.anko.defaultSharedPreferences
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

/**
 * NOTE: Could not run Robolectric (i.e. ApplicationProvider::getApplicationContext)
 *  with JUnit 5 as a unit test (*test* source set). Wait until Robolectric
 *  provides a JUnit 5 extension and move this class to *test* source set
 *  for the benefit of faster tests.
 */
@ExtendWith(MockKExtension::class, InstantExecutorExtension::class)
class MainViewModelTest {

    @MockK lateinit var repository: MainRepository
    lateinit var viewModel: MainViewModel

    @BeforeEach fun setUp() {
        val app: Application = getApplicationContext()
        val prefs = app.defaultSharedPreferences
        val itemsLiveData = MutableLiveData<List<Item>>().apply {
            value = emptyList()
        }
        every { repository.items } returns flowOf(emptyList())
        viewModel = MainViewModel(app, repository, prefs, IdleState)
    }

    @Test fun itemsShouldBeEmpty() {
        assertThat(viewModel.items.value).isEmpty()
    }

    /**
     * In our ViewModel test, we don't have an activity or fragment to observe the LiveData.
     * To get around this, we can use the observeForever method, which ensures the LiveData
     * is constantly observed, without needing a LifecycleOwner. When we observeForever, we
     * need to remember to remove our observer or risk an observer leak.
     */
    @Test fun storesShouldNotBeNull() = runBlocking<Unit> {
        coEvery { repository.getAllStores() } returns emptyList()

        val stores = viewModel.getAllStores()

        assertThat(stores).isNotNull()
    }
}
