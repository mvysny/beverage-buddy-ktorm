package com.vaadin.starter.beveragebuddy.backend.ktorm

import org.ktorm.database.Database
import org.ktorm.database.Transaction

/**
 * Makes sure given block is executed in a DB transaction. When the block finishes normally, the transaction commits;
 * if the block throws any exception, the transaction is rolled back.
 *
 * Example of use: `db { ddl("yada yada") }`
 * @param block the block to run in the transaction.
 */
fun <R> db(block: KtormContext.() -> R): R =
    SimpleKtorm.database.useTransaction {
        KtormContext(
            it,
            SimpleKtorm.database
        ).block()
    }

data class KtormContext(val transaction: Transaction, val database: Database)

fun Transaction.ddl(ddl: String) {
    connection.prepareStatement(ddl).executeUpdate()
}

fun KtormContext.ddl(ddl: String) {
    transaction.ddl(ddl)
}
