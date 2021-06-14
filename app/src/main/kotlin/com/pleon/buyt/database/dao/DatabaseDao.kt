package com.pleon.buyt.database.dao

import androidx.room.Dao
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery

@Dao
abstract class DatabaseDao {

    /**
     * See [this post](https://stackoverflow.com/a/51560124).
     */
    open suspend fun flushDatabase() {
        checkpoint(SimpleSQLiteQuery("pragma wal_checkpoint(full)"))
    }

    @RawQuery
    protected abstract suspend fun checkpoint(supportSQLiteQuery: SupportSQLiteQuery): Int
}
