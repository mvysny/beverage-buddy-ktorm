package com.vaadin.starter.beveragebuddy.backend.ktorm

import jakarta.validation.constraints.NotBlank
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.*
import org.ktorm.schema.Table
import org.ktorm.schema.long
import org.ktorm.schema.varchar

object Categories : Table<Category>("category") {
    val id = long("id").primaryKey().bindTo { it.id }
    val name = varchar("NAME").bindTo { it.name }
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

    companion object : Entity.Factory<Category>() {
        fun findByName(name: String): Category? = db { database.categories.singleOrNull { it.name eq name } }
        fun getByName(name: String): Category = db { database.categories.single { it.name eq name } }
        fun existsWithName(name: String): Boolean = db { database.categories.any { it.name eq name } }
        // @TODO finalize
/*
        fun deleteAll() {
            db {
                handle.createUpdate("update Review set category = NULL").execute()
                super.deleteAll()
            }
        }
*/
    }

    // @todo finalize
/*
    override fun delete(): Int {
        db {
            if (id != null) {
                handle.createUpdate("update Review set category = NULL where category=:catId")
                        .bind("catId", id!!)
                        .execute()
            }
            return super.delete()
        }
    }
*/
}
