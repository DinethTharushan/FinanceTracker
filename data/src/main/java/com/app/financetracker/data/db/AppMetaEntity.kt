package com.app.financetracker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Small key/value store for app state, e.g. whether the first inbox import ran. */
@Entity(tableName = "app_meta")
data class AppMetaEntity(
    @PrimaryKey val key: String,
    val value: String,
)
