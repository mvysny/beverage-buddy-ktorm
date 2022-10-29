package com.vaadin.starter.beveragebuddy.backend

import com.github.mvysny.dynatest.DynaNodeGroup
import com.github.mvysny.dynatest.DynaTest
import com.github.mvysny.dynatest.DynaTestDsl
import com.github.mvysny.dynatest.expectList
import eu.vaadinonkotlin.restclient.exec
import eu.vaadinonkotlin.restclient.jsonArray
import com.vaadin.starter.beveragebuddy.ui.usingApp
import eu.vaadinonkotlin.restclient.OkHttpClientVokPlugin
import io.javalin.Javalin
import okhttp3.OkHttpClient
import okhttp3.Request
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.util.resource.EmptyResource
import org.eclipse.jetty.webapp.WebAppContext

/**
 * Uses the VoK `vok-rest-client` module for help with testing of the REST endpoints. See docs on the
 * [vok-rest-client](https://github.com/mvysny/vaadin-on-kotlin/tree/master/vok-rest-client) module for more details.
 */
class PersonRestClient(val baseUrl: String) {
    init {
        require(!baseUrl.endsWith("/")) { "$baseUrl must not end with a slash" }
    }
    private val client: OkHttpClient = OkHttpClientVokPlugin.okHttpClient!!
    fun getAllCategories(): List<Category> {
        val request = Request.Builder().url("$baseUrl/categories").build()
        return client.exec(request) { response -> response.jsonArray(Category::class.java) }
    }
    fun getAllReviews(): List<Review> {
        val request = Request.Builder().url("$baseUrl/reviews").build()
        return client.exec(request) { response -> response.jsonArray(Review::class.java) }
    }
}

@DynaTestDsl
fun DynaNodeGroup.usingJavalin() {
    lateinit var server: Server
    beforeGroup {
        val ctx = WebAppContext()
        ctx.baseResource = EmptyResource.INSTANCE
        ctx.addServlet(JavalinRestServlet::class.java, "/rest/*")
        server = Server(9876)
        server.handler = ctx
        server.start()
    }
    afterGroup { server.stop() }
}

/**
 * The REST test. It bootstraps the app, then it starts Javalin with Jetty so that we can access it via the
 * [PersonRestClient].
 */
class RestServiceTest : DynaTest({
    usingApp()
    usingJavalin()

    lateinit var client: PersonRestClient
    beforeEach { client = PersonRestClient("http://localhost:9876/rest") }

    test("categories smoke test") {
        expectList() { client.getAllCategories() }
    }

    test("reviews smoke test") {
        expectList() { client.getAllReviews() }
    }
})
