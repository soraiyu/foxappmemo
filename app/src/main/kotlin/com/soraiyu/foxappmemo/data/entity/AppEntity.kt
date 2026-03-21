package com.soraiyu.foxappmemo.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

enum class AppStatus(val label: String) {
    TRYING("trying"),
    MAIN("main"),
    AVOID("avoid"),
    BLACKLIST("blacklist"),
    RECONSIDER("reconsider");

    companion object {
        fun fromLabel(label: String): AppStatus =
            entries.firstOrNull { it.label == label } ?: TRYING
    }
}

@Serializable
@Entity(tableName = "apps")
data class AppEntity(
    @PrimaryKey
    val packageName: String,
    val appName: String,
    val installDate: Long? = null,
    val uninstallDate: Long? = null,
    val lastUsedDate: Long? = null,
    /** Rating 1–5, null if not rated */
    val rating: Int? = null,
    /** Stored as the [AppStatus.label] string */
    val status: String = AppStatus.TRYING.label,
    val memo: String? = null,
)
