package com.app.financetracker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Placeholder table: Room rejects a database with no entities. Keep it as a small
 * key/value store or replace it when the first real table is added.
 */
@Entity(tableName = "app_meta")
data class AppMetaEntity(
    @PrimaryKey val key: String,
    val value: String,
)
