package com.vaadin.starter.beveragebuddy.backend

import com.vaadin.starter.beveragebuddy.backend.ktorm.Categories
import com.vaadin.starter.beveragebuddy.backend.ktorm.Category
import com.vaadin.starter.beveragebuddy.backend.ktorm.Reviews
import com.vaadin.starter.beveragebuddy.backend.ktorm.Review
import com.vaadin.starter.beveragebuddy.backend.ktorm.findAll
import eu.vaadinonkotlin.rest.*
import io.javalin.Javalin
import jakarta.servlet.annotation.WebServlet
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.time.LocalDate

/**
 * Provides access to person list. To test, just run `curl http://localhost:8080/rest/categories`
 */
@WebServlet(urlPatterns = ["/rest/*"], name = "JavalinRestServlet", asyncSupported = false)
class JavalinRestServlet : HttpServlet() {
    val javalin = Javalin.createStandalone { it.gsonMapper(VokRest.gson) } .apply {
        get("/rest/categories") { ctx -> ctx.json(Categories.findAll().map { RestCategory.of(it) }) }
        get("/rest/reviews") { ctx -> ctx.json(Reviews.findAll().map { RestReview.of(it) }) }
    }.javalinServlet()

    override fun service(req: HttpServletRequest, resp: HttpServletResponse) {
        javalin.service(req, resp)
    }
}

data class RestCategory(
    var id: Long? = null,
    var name: String = ""
) {
    companion object {
        fun of(cat: Category): RestCategory = RestCategory(
            cat.id, cat.name
        )
    }
}

data class RestReview(var id: Long? = null,
                      var score: Int = 1,
                      var name: String = "",
                      var date: LocalDate? = null,
                      var category: Long? = null,
                      var count: Int = 1) {
    companion object {
        fun of(review: Review) = RestReview(
            review.id, review.score, review.name, review.date, review.category, review.count
        )
    }
}
