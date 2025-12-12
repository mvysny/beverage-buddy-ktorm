package com.vaadin.starter.beveragebuddy.backend.ktorm

import com.github.mvysny.ktormvaadin.QueryDataProvider
import org.ktorm.dsl.*
import org.ktorm.schema.VarcharSqlType
import org.ktorm.support.postgresql.ilike
import java.io.Serializable

/**
 * Holds the join of Review and its Category.
 * @property categoryName the [Category.name]
 */
data class ReviewWithCategory(
    var review: Review? = null,
    var categoryName: String? = null
) : Serializable {
    companion object {
        fun from(row: QueryRowSet): ReviewWithCategory = ReviewWithCategory(Reviews.createEntity(row), row[Categories.name])
        /**
         * Fetches the reviews matching the given filter text.
         *
         * This data provider provides sorting/paging/filtering and may be used for
         * SELECTs returning huge amount of data.
         */
        val dataProvider: QueryDataProvider<ReviewWithCategory>
            // we need to use SQL alias here, since both r.name and c.name exist and H2 would complain of a name clash.
            get() = QueryDataProvider(
                { it.from(Reviews).leftJoin(Categories, on = Reviews.category eq Categories.id)
                    .select(*Reviews.columns.toTypedArray(), Categories.name)},
                { from(it) })
    }
}

/**
 * This utility function returns a new loader which searches for given [filter] text
 * in all [Review] and [ReviewWithCategory] fields.
 */
fun QueryDataProvider<ReviewWithCategory>.setFilterText(filter: String?) {
    if (filter.isNullOrBlank()) {
        setFilter(null)
    } else {
        val normalizedFilter: String = filter.trim().lowercase() + "%"
        val c = Reviews.name.ilike(normalizedFilter) or
                Categories.name.ilike(normalizedFilter) or
               Reviews.score.cast(VarcharSqlType).ilike(normalizedFilter) or
                Reviews.count.cast(VarcharSqlType).ilike(normalizedFilter)
        setFilter(c)
    }
}
