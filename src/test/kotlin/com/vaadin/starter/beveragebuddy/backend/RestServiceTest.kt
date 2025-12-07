package com.vaadin.starter.beveragebuddy.backend

import com.github.mvysny.kaributesting.v10.expectList
import com.vaadin.starter.beveragebuddy.AbstractAppTest
import com.vaadin.starter.beveragebuddy.backend.ktorm.Category
import com.vaadin.starter.beveragebuddy.backend.ktorm.Review
import eu.vaadinonkotlin.restclient.*
import org.eclipse.jetty.ee10.webapp.WebAppContext
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.util.resource.Resource
import org.junit.jupiter.api.*
import java.io.FileNotFoundException
import java.net.http.HttpClient
import java.net.URI
import java.time.LocalDate
import java.nio.file.Path

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
            ctx.baseResource = EmptyResource()
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

    @Test fun `reviews retrieval`() {
        val cat = Category{name = "Beers"}
        cat.save()
        val r = Review{score = 1; name = "Good!"; category = cat.id; count = 1;date = LocalDate.now()}
        r.save()
        expectList(RestReview.of(r)) { client.getAllReviews() }
    }
    @Test fun `404`() {
        assertThrows<FileNotFoundException> {
            client.nonexistingEndpoint()
        }
    }
}

class EmptyResource : Resource() {
    override fun getPath(): Path? = null
    override fun isDirectory(): Boolean = true
    override fun isReadable(): Boolean = true
    override fun getURI(): URI? = null
    override fun getName(): String = "EmptyResource"
    override fun getFileName(): String? = null
    override fun resolve(subUriPath: String?): Resource? = null
}
