package com.vaadin.starter.beveragebuddy.backend.ktorm

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider
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

class EntityDataProvider<T: Entity<T>>(val table: Table<T>) : AbstractBackEndDataProvider<T, ColumnDeclaring<Boolean>>(),
    ConfigurableFilterDataProvider<T, ColumnDeclaring<Boolean>, ColumnDeclaring<Boolean>> {

    private var filter: ColumnDeclaring<Boolean>? = null

    private fun calculateFilter(query: Query<T, ColumnDeclaring<Boolean>>): ColumnDeclaring<Boolean>? {
        val filter = this.filter
        val filter2 = query.filter.orElse(null)
        if (filter == null) return filter2
        if (filter2 == null) return filter
        return filter.and(filter2)
    }

    private val Query<T, ColumnDeclaring<Boolean>>.orderBy: List<OrderByExpression> get() {
        return sortOrders.map { sortOrder ->
            val column = table[sortOrder.sorted]
            if (sortOrder.direction == SortDirection.ASCENDING) column.asc() else column.desc()
        }
    }

    override fun fetchFromBackEnd(query: Query<T, ColumnDeclaring<Boolean>>): Stream<T> = db {
        var q = database.from(table).select()
        val filter = calculateFilter(query)
        if (filter != null) {
            q = q.where(filter)
        }
        q = q.offset(query.offset).limit(query.limit).orderBy(query.orderBy)
        val result = q.map { table.createEntity(it) }
        result.stream()
    }

    override fun sizeInBackEnd(query: Query<T, ColumnDeclaring<Boolean>>): Int = db {
        var seq = database.sequenceOf(table)
        val filter = calculateFilter(query)
        if (filter != null) {
            seq = seq.filter { filter }
        }
        seq.count()
    }

    override fun setFilter(filter: ColumnDeclaring<Boolean>?) {
        this.filter = filter
        refreshAll()
    }
}

val <E: Entity<E>> Table<E>.dataProvider: EntityDataProvider<E> get() = EntityDataProvider(this)
