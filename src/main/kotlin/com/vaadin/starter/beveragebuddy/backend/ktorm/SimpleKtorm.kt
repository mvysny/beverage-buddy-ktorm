package com.vaadin.starter.beveragebuddy.backend.ktorm

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.ktorm.database.Database
import org.ktorm.dsl.deleteAll
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.single
import org.ktorm.entity.toList
import org.ktorm.schema.Table

object SimpleKtorm {
    /**
     * The jakarta.validation validator.
     */
    @Volatile
    var validator: Validator = Validation.buildDefaultValidatorFactory().validator

    /**
     * The [db] function obtains the JDBC connection from here. Use HikariCP connection pooling.
     */
    @Volatile
    lateinit var database: Database
}

fun <E : Entity<E>> Table<E>.findAll(): List<E> = db {
    database.sequenceOf(this@findAll).toList()
}
fun <E : Entity<E>> Table<E>.single(): E = db {
    database.sequenceOf(this@single).single()
}
fun <E : Entity<E>> Table<E>.deleteAll() {
    db {
        database.deleteAll(this@deleteAll)
    }
}