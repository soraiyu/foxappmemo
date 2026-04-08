package com.rtneg.foxappmemo.data.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import kotlinx.serialization.Serializable

@Serializable
data class AppWithTags(
    @Embedded val app: AppEntity,
    @Relation(
        parentColumn = "packageName",
        entityColumn = "id",
        associateBy = Junction(
            value = AppTagCrossRef::class,
            parentColumn = "packageName",
            entityColumn = "tagId",
        ),
    )
    val tags: List<TagEntity>,
)
