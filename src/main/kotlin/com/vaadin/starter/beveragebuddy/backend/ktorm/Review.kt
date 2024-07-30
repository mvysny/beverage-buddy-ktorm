package com.vaadin.starter.beveragebuddy.backend.ktorm

import jakarta.validation.constraints.*
import org.ktorm.entity.Entity
import org.ktorm.schema.*
import java.time.LocalDate

object Reviews : Table<Review>("review") {
    val id = long("id").primaryKey().bindTo { it.id }
    val name = varchar("NAME").bindTo { it.name }
    val date = date("NAME").bindTo { it.date }
    val category = long("category").bindTo { it.category }
    val count = int("count").bindTo { it.count }
}


/**
 * Represents a beverage review.
 * @property name the beverage name
 * @property score the score, 1..5, 1 being worst, 5 being best
 * @property date when the review was done
 * @property category the beverage category [Category.id]. May be null if the category has been deleted.
 * @property count times tasted, 1..99
 */
interface Review : ValidatableEntity<Review> {

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
    var date: LocalDate

    @get:NotNull
    var category: Long?

    @get:NotNull
    @get:Min(1)
    @get:Max(99)
    var count: Int

    companion object : Entity.Factory<Review>()
}
