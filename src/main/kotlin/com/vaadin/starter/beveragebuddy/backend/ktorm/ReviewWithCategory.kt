package com.vaadin.starter.beveragebuddy.backend.ktorm

import org.ktorm.dsl.QueryRowSet
import org.ktorm.dsl.eq
import org.ktorm.dsl.from
import org.ktorm.dsl.leftJoin
import org.ktorm.dsl.select
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
                { it.from(Reviews).leftJoin(Categories, on = Reviews.category eq Categories.id) },
                { it.select(*Reviews.columns.toTypedArray(), Categories.name)},
                { from(it) })
    }
}
