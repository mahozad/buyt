package com.pleon.buyt.database.repository

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.pleon.buyt.database.AppDatabase
import com.pleon.buyt.database.dao.PurchaseDao
import com.pleon.buyt.model.Purchase
import org.jetbrains.anko.doAsync

class PurchaseRepository(application: Application) {

    private val purchaseDao: PurchaseDao = AppDatabase.getDatabase(application).purchaseDao()
    private val insertedPurchaseId = MutableLiveData<Long>()

    fun insert(purchase: Purchase): MutableLiveData<Long> {
        doAsync {
            val purchaseId = purchaseDao.insert(purchase)
            insertedPurchaseId.postValue(purchaseId)
        }
        return insertedPurchaseId
    }
}
