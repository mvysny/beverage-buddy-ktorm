package com.vaadin.starter.beveragebuddy.backend.ktorm

import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.provider.*
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.function.SerializableFunction
import org.ktorm.dsl.*
import org.ktorm.entity.Entity
import org.ktorm.entity.count
import org.ktorm.entity.filter
import org.ktorm.entity.sequenceOf
import org.ktorm.expression.OrderByExpression
import org.ktorm.schema.*
import org.ktorm.support.postgresql.ilike
import java.util.stream.Stream
import kotlin.reflect.KProperty1

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

    fun withStringFilter(filterConverter: SerializableFunction<String, ColumnDeclaring<Boolean>?>): DataProvider<T, String> {
        return withConvertedFilter { filter: String? ->
            val postProcessedFilter = filter?.trim() ?: ""
            if (postProcessedFilter.isNotEmpty()) filterConverter.apply(
                postProcessedFilter
            ) else null
        }
    }
}

/**
 * Returns Vaadin {@link EntityDataProvider} which loads instances of this entity. See {@link EntityDataProvider}
 * for more information.
 */
val <E: Entity<E>> Table<E>.dataProvider: EntityDataProvider<E> get() = EntityDataProvider(this)

/**
 * Returns a [DataProvider] which accepts a [String] filter; when a non-blank String is provided,
 * a `col ilike string%` where clause is added to the query.
 * Example of use:
 * ```
 * setItems(Categories.dataProvider.withStringFilterOn(Categories.name))
 * ```
 * @param column the [Table] column from entity [T]
 * @param T the entity type
 * @return [DataProvider] which matches filter string against the value of given [column].
 */
fun <T: Entity<T>> EntityDataProvider<T>.withStringFilterOn(column: Column<String>): DataProvider<T, String> =
    withStringFilter {
        column.ilike("${it.trim()}%")
    }

/**
 * A type-safe binding which binds only to a property of given type, found on given bean.
 * @param column the ktorm column
 */
fun <BEAN, FIELDVALUE:Any> Binder.BindingBuilder<BEAN, FIELDVALUE?>.bind(column: Column<out FIELDVALUE>): Binder.Binding<BEAN, FIELDVALUE?> {
    val binding: KProperty1<*, *>
    if (column.binding is ReferenceBinding) {
        binding = (column.binding as ReferenceBinding).onProperty
    } else {
        val properties = (column.binding as NestedBinding).properties
        require(properties.size == 1) { "$column: nested properties aren't supported: ${column.binding}" }
        binding = properties[0]
    }

    // oh crap, don't use binding by getter and setter - validations won't work!
    // we need to use bind(String) even though that will use undebuggable crappy Java 8 lambdas :-(
    //        bind({ bean -> prop.get(bean) }, { bean, value -> prop.set(bean, value) })
    var name = binding.name
    if (name.startsWith("is")) {
        // Kotlin KProperties named "isFoo" are represented with just "foo" in the bean property set
        name = name[2].lowercase() + name.drop(3)
    }
    return bind(name)
}
