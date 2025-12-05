package com.vaadin.starter.beveragebuddy.backend.ktorm

import com.vaadin.flow.data.provider.AbstractBackEndDataProvider
import com.vaadin.flow.data.provider.ConfigurableFilterDataProvider
import com.vaadin.flow.data.provider.DataProvider
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.data.provider.SortDirection
import com.vaadin.flow.function.SerializableFunction
import org.ktorm.database.Database
import org.ktorm.dsl.QueryRowSet
import org.ktorm.dsl.QuerySource
import org.ktorm.dsl.and
import org.ktorm.dsl.asc
import org.ktorm.dsl.desc
import org.ktorm.dsl.from
import org.ktorm.dsl.limit
import org.ktorm.dsl.map
import org.ktorm.dsl.offset
import org.ktorm.dsl.orderBy
import org.ktorm.dsl.select
import org.ktorm.dsl.where
import org.ktorm.entity.count
import org.ktorm.entity.filter
import org.ktorm.entity.sequenceOf
import org.ktorm.expression.OrderByExpression
import org.ktorm.schema.ColumnDeclaring
import java.util.stream.Stream

/**
 * Loads data from a ktorm [Query]. Mostly used for more complex stuff like joins; for selecting
 * entities use [EntityDataProvider]. Example of use:
 * ```
 * val dp = QueryDataProvider({ it
 *   .from(Employees)
 *   .leftJoin(Departments, on = Employees.departmentId eq Departments.id))
 *   .select(Employees.name, Departments.name)
 * }, { row -> Names(row[Employees.name], row[Departments.name]) })
 * TODO dp.filter
 * ```
 * @param query creates [org.ktorm.dsl.Query]
 * @param rowMapper converts [QueryRowSet] to the bean [T]
 * @param T the bean type returned by this data provider.
 */
class QueryDataProvider<T>(val query: (Database) -> org.ktorm.dsl.Query, val rowMapper: (QueryRowSet) -> T) : AbstractBackEndDataProvider<T, ColumnDeclaring<Boolean>>(),
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

    /**
     * Converts this data provider to one which accepts a [String] filter value. The string filter
     * is converted via [filterConverter] to a ktorm where clause.
     * @param filterConverter converts String filter to a WHERE clause.
     * @return [DataProvider]
     */
    fun withStringFilter(filterConverter: SerializableFunction<String, ColumnDeclaring<Boolean>?>): DataProvider<T, String> {
        return withConvertedFilter { filter: String? ->
            val postProcessedFilter = filter?.trim() ?: ""
            if (postProcessedFilter.isNotEmpty()) filterConverter.apply(
                postProcessedFilter
            ) else null
        }
    }
}