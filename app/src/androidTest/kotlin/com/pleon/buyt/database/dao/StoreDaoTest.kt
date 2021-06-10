package com.pleon.buyt.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.pleon.buyt.InstantExecutorExtension
import com.pleon.buyt.database.AppDatabase
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class)
class StoreDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var storeDao: StoreDao

    @BeforeEach fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        storeDao = database.storeDao()
    }

    @Test
    fun insertOneStore() {
        // val name = "A Store"
        // storeDao.insert(Store(Coordinates(10.0, 20.0), name, Category.DRUG))
        //
        // val list = storeDao
        //         .getAll(Sort.STORE_NAME, SortDirection.ASCENDING)
        //         .blockingObserve()
        //
        // assertThat(list.size).isEqualTo(1)
        // assertThat(list.first().store.name).isEqualTo(name)
    }

    @AfterEach
    fun tearDown() {
        database.close()
    }
}
