package com.rtneg.foxappmemo.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "app_tag_cross_ref",
    primaryKeys = ["packageName", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = AppEntity::class,
            parentColumns = ["packageName"],
            childColumns = ["packageName"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["tagId"])],
)
data class AppTagCrossRef(
    val packageName: String,
    val tagId: Long,
)
