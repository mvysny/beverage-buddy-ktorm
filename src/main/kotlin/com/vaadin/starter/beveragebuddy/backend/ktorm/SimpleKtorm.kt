package com.vaadin.starter.beveragebuddy.backend.ktorm

import jakarta.validation.Validation
import jakarta.validation.Validator
import org.ktorm.database.Database

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
