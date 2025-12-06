package com.vaadin.starter.beveragebuddy.backend

import com.github.mvysny.kaributesting.v10.expectList
import com.github.vokorm.KEntity
import com.vaadin.starter.beveragebuddy.AbstractAppTest
import com.vaadin.starter.beveragebuddy.backend.ktorm.Category
import eu.vaadinonkotlin.restclient.*
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.PastOrPresent
import jakarta.validation.constraints.Size
import org.eclipse.jetty.ee10.webapp.WebAppContext
import org.eclipse.jetty.server.Server
import org.junit.jupiter.api.*
import java.io.FileNotFoundException
import java.net.http.HttpClient
import java.time.LocalDate

/**
 * Uses the VoK `vok-rest-client` module for help with testing of the REST endpoints. See docs on the
 * [vok-rest-client](https://github.com/mvysny/vaadin-on-kotlin/tree/master/vok-rest-client) module for more details.
 */
class PersonRestClient(val baseUrl: String) {
    init {
        require(!baseUrl.endsWith("/")) { "$baseUrl must not end with a slash" }
    }
    private val client: HttpClient = VokRestClient.httpClient
    fun getAllCategories(): List<RestCategory> {
        val request = "$baseUrl/categories".buildUrl().buildRequest()
        return client.exec(request) { response -> response.jsonArray(RestCategory::class.java) }
    }
    fun getAllReviews(): List<RestReview> {
        val request = "$baseUrl/reviews".buildUrl().buildRequest()
        return client.exec(request) { response -> response.jsonArray(RestReview::class.java) }
    }
    fun nonexistingEndpoint() {
        val request = "$baseUrl/nonexisting".buildUrl().buildRequest()
        client.exec(request) { }
    }
}

/**
 * The REST test. It bootstraps the app, then it starts Javalin with Jetty so that we can access it via the
 * [PersonRestClient].
 */
class RestServiceTest : AbstractAppTest() {
    companion object {
        private lateinit var server: Server
        @BeforeAll @JvmStatic fun startJavalin() {
            val ctx = WebAppContext()
            // This used to be EmptyResource, but it got removed in Jetty 12. Let's use some dummy resource instead.
            ctx.baseResource = ctx.resourceFactory.newClassLoaderResource("java/lang/String.class")
            ctx.addServlet(JavalinRestServlet::class.java, "/rest/*")
            server = Server(9876)
            server.handler = ctx
            server.start()
        }
        @AfterAll @JvmStatic fun stopJavalin() { server.stop() }
    }

    private lateinit var client: PersonRestClient
    @BeforeEach fun createClient() { client = PersonRestClient("http://localhost:9876/rest") }

    @Test fun `categories smoke test`() {
        expectList() { client.getAllCategories() }
    }

    @Test fun `categories retrieval`() {
        val cat = Category { name = "foo" } .apply { save() }
        expectList(RestCategory.of(cat)) { client.getAllCategories() }
    }

    @Test fun `reviews smoke test`() {
        expectList() { client.getAllReviews() }
    }

    @Test fun `404`() {
        assertThrows<FileNotFoundException> {
            client.nonexistingEndpoint()
        }
    }
}
