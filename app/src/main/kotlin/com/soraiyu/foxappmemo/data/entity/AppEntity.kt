package com.soraiyu.foxappmemo.data.entity

import androidx.annotation.StringRes
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.soraiyu.foxappmemo.R
import kotlinx.serialization.Serializable

enum class AppRating(val value: Int, @StringRes val labelResId: Int) {
    NOT_FOR_ME(1, R.string.rating_not_for_me),
    NORMAL(2, R.string.rating_normal),
    LIKE(3, R.string.rating_like);

    companion object {
        fun fromValue(value: Int?): AppRating? = entries.firstOrNull { it.value == value }
    }
}

enum class AppStatus(val label: String, @StringRes val labelResId: Int) {
    TRYING("trying", R.string.status_trying),
    ONGOING("ongoing", R.string.status_ongoing),
    MAIN("main", R.string.status_main),
    AVOID("avoid", R.string.status_avoid),
    BLACKLIST("blacklist", R.string.status_blacklist),
    RECONSIDER("reconsider", R.string.status_reconsider);

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
    /** Rating as [AppRating.value] (1=自分向きじゃない, 2=ふつう, 3=好き), null if not rated */
    val rating: Int? = null,
    /** Stored as the [AppStatus.label] string */
    val status: String = AppStatus.TRYING.label,
    val memo: String? = null,
)
