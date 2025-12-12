package com.vaadin.starter.beveragebuddy.backend.ktorm

import com.github.mvysny.kaributesting.v10.expectList
import com.github.mvysny.ktormvaadin.q
import com.vaadin.flow.data.provider.Query
import com.vaadin.flow.data.provider.QuerySortOrder
import com.vaadin.flow.data.provider.SortDirection
import com.vaadin.starter.beveragebuddy.AbstractAppTest
import org.junit.jupiter.api.Test
import org.ktorm.schema.ColumnDeclaring
import java.time.LocalDate
import kotlin.test.expect

class ReviewWithCategoryTest : AbstractAppTest() {
    @Test
    fun smoke() {
        val category = Category { name = "Foo" }
        category.save()
        val review = Review {
            name = "Bar"; count = 1; this.score = 1; date =
            LocalDate.now(); this.category = category.id
        }
        review.save()

        expectList(
            ReviewWithCategory(
                review,
                "Foo"
            )
        ) { ReviewWithCategory.dataProvider.fetch(Query()).toList() }
        expect(1) { ReviewWithCategory.dataProvider.size(Query()) }
        val query = Query<ReviewWithCategory, ColumnDeclaring<Boolean>>(
            0,
            30,
            listOf(QuerySortOrder(Reviews.name.q.key, SortDirection.ASCENDING)),
            null,
            null
        )
        expectList(
            ReviewWithCategory(
                review,
                "Foo"
            )
        ) { ReviewWithCategory.dataProvider.fetch(query).toList() }
    }
}
