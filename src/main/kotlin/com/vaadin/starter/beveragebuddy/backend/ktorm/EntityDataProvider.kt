package com.vaadin.starter.beveragebuddy.backend.ktorm

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.data.provider.SortDirection
import org.ktorm.dsl.*
import org.ktorm.entity.Entity
import org.ktorm.entity.count
import org.ktorm.entity.filter
import org.ktorm.entity.sequenceOf
import org.ktorm.expression.OrderByExpression
import org.ktorm.schema.ColumnDeclaring
import org.ktorm.schema.Table
import java.util.stream.Stream

class EntityDataProvider<T: Entity<T>>(val table: Table<T>) : AbstractBackEndDataProvider<T, ColumnDeclaring<Boolean>>() {
    private val Query<T, ColumnDeclaring<Boolean>>.orderBy: List<OrderByExpression> get() {
        return sortOrders.map { sortOrder ->
            val column = table.get(sortOrder.sorted)
            if (sortOrder.direction == SortDirection.ASCENDING) column.asc() else column.desc()
        }
    }

    override fun fetchFromBackEnd(query: Query<T, ColumnDeclaring<Boolean>>): Stream<T> = db {
        var q = database.from(table).select()
        if (query.filter.isPresent) {
            q = q.where(query.filter.get())
        }
        q = q.offset(query.offset).limit(query.limit).orderBy(query.orderBy)
        val result = q.map { table.createEntity(it) }
        result.stream()
    }

    override fun sizeInBackEnd(query: Query<T, ColumnDeclaring<Boolean>>): Int = db {
        var seq = database.sequenceOf(table)
        if (query.filter.isPresent) {
            seq = seq.filter { query.filter.get() }
        }
        seq.count()
    }
}
