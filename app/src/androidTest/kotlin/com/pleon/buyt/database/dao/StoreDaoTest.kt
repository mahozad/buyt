package com.pleon.buyt.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.pleon.buyt.database.AppDatabase
import com.pleon.buyt.model.Category
import com.pleon.buyt.model.Coordinates
import com.pleon.buyt.model.Store
import com.pleon.buyt.viewmodel.StoresViewModel
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class StoreDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var storeDao: StoreDao

    @BeforeEach
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        storeDao = database.storeDao()
    }

    @Test
    fun insertOneStore() {
        storeDao.insert(Store(Coordinates(10.0, 20.0), "A Store", Category.DRUG))

        val stores = storeDao.getAll(StoresViewModel.Sort.STORE_NAME)
        assertEquals("A Store", stores)
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }
}
