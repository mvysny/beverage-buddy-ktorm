package com.vaadin.starter.beveragebuddy.backend.ktorm

import jakarta.validation.constraints.NotBlank
import org.ktorm.database.Database
import org.ktorm.dsl.deleteAll
import org.ktorm.dsl.eq
import org.ktorm.dsl.update
import org.ktorm.entity.*
import org.ktorm.schema.Column
import org.ktorm.schema.Table
import org.ktorm.schema.long
import org.ktorm.schema.varchar

object Categories : Table<Category>("category") {
    val id = long("id").primaryKey().bindTo { it.id }
    val name = varchar("NAME").bindTo { it.name }

    fun deleteAll() {
        db {
            database.update(Reviews) {
                set(it.category, null)
            }
            database.deleteAll(Categories)
        }
    }
}

val Database.categories get() = this.sequenceOf(Categories)

/**
 * Represents a beverage category.
 * @property id
 * @property name the category name
 */
interface Category : ValidatableEntity<Category> {
    var id: Long?

    @get:NotBlank
    var name: String

    override val table: Table<Category> get() = Categories

    companion object : Entity.Factory<Category>() {
        fun findByName(name: String): Category? =
            db { database.categories.singleOrNull { it.name eq name } }

        fun getByName(name: String): Category =
            db { database.categories.single { it.name eq name } }

        fun existsWithName(name: String): Boolean =
            db { database.categories.any { it.name eq name } }
    }

    fun deleteAndClearFromReview(): Int = db {
        val id = id
        if (id != null) {
           database.update(Reviews) {
               set(it.category, null)
               where { it.category eq id }
           }
        }
        delete()
    }
}
