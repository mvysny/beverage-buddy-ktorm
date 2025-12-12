package com.vaadin.starter.beveragebuddy.backend.ktorm

import com.github.mvysny.ktormvaadin.ActiveEntity
import com.github.mvysny.ktormvaadin.db
import com.github.mvysny.ktormvaadin.sql
import jakarta.validation.constraints.*
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import java.time.LocalDate

object Reviews : Table<Review>("review") {
    val id = long("id").primaryKey().bindTo { it.id }
    val score = int("score").bindTo { it.score }
    val name = varchar("NAME").bindTo { it.name }
    val date = date("DATE").bindTo { it.date }
    val category = long("category").bindTo { it.category }
    val count = int("COUNT").bindTo { it.count }
}

val Database.reviews get() = this.sequenceOf(Reviews)

/**
 * Represents a beverage review.
 * @property name the beverage name
 * @property score the score, 1..5, 1 being worst, 5 being best
 * @property date when the review was done
 * @property category the beverage category [Category.id]. May be null if the category has been deleted.
 * @property count times tasted, 1..99
 */
interface Review : ActiveEntity<Review> {

    var id: Long?

    @get:NotNull
    @get:Min(1)
    @get:Max(5)
    var score: Int

    @get:NotBlank
    @get:Size(min = 3)
    var name: String

    @get:NotNull
    @get:PastOrPresent
    var date: LocalDate?

    @get:NotNull
    var category: Long?

    @get:NotNull
    @get:Min(1)
    @get:Max(99)
    var count: Int

    override val table: Table<Review> get() = Reviews

    companion object : Entity.Factory<Review>() {
        /**
         * Computes the total sum of [count] for all reviews belonging to given [categoryId].
         * @return the total sum, 0 or greater.
         */
        fun getTotalCountForReviewsInCategory(categoryId: Long): Long = db {
            sql("select sum(r.count) from Review r where r.category = ?",
                { setLong(1, categoryId) }) { it.getLong(1) }
                .firstOrNull() ?: 0L
        }
    }
}
