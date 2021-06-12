package com.pleon.buyt.repository

import com.pleon.buyt.database.dao.ItemDao
import com.pleon.buyt.database.dao.PurchaseDao
import com.pleon.buyt.database.dao.StoreDao
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

// Using both Mockito and MockK for demonstration purposes
@ExtendWith(
    MockKExtension::class,
    MockitoExtension::class
)
class MainRepositoryTest {

    // Using Mockito
    @Mock lateinit var itemDao: ItemDao
    // Using MockK
    @MockK lateinit var storeDao: StoreDao
    @Mock lateinit var purchaseDao: PurchaseDao

    lateinit var repository: MainRepository

    @BeforeEach fun setUp() {
        // Using Mockito
        `when`(itemDao.getAll()).thenReturn(flowOf(emptyList()))
        // Using MockK (every for regular functions, coEvery and coAnswers for suspend functions)
        coEvery { storeDao.getAllStores() } returns emptyList()
        repository = MainRepository(itemDao, storeDao, purchaseDao)
    }

    @Test fun getAllStores() = runBlocking {
        val stores = repository.getAllStores()

        assertThat(stores).isEmpty()
    }
}
