package com.vaadin.starter.beveragebuddy.backend

import com.fatboyindustrial.gsonjavatime.Converters
import com.github.mvysny.kaributesting.v10.expectList
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.vaadin.starter.beveragebuddy.AbstractAppTest
import com.vaadin.starter.beveragebuddy.backend.ktorm.Category
import com.vaadin.starter.beveragebuddy.backend.ktorm.Review
import io.ktor.client.HttpClient
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.runBlocking
import org.eclipse.jetty.ee10.webapp.WebAppContext
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.util.resource.Resource
import org.junit.jupiter.api.*
import java.net.URI
import java.nio.file.Path
import java.time.LocalDate

private fun GsonBuilder.registerJavaTimeAdapters(): GsonBuilder = apply {
    Converters.registerAll(this)
}
private val gson: Gson = GsonBuilder().registerJavaTimeAdapters().create()
private inline suspend fun <reified T> HttpResponse.jsonArray(): List<T> {
    val type = TypeToken.getParameterized(List::class.java, T::class.java).type
    return gson.fromJson<List<T>>(bodyAsChannel().toInputStream().reader().buffered(), type)
}

class PersonRestClient(val baseUrl: String) {
    init {
        require(!baseUrl.endsWith("/")) { "$baseUrl must not end with a slash" }
    }
    private val client = HttpClient {
        expectSuccess = true
    }
    fun getAllCategories(): List<RestCategory> = runBlocking {
            val response = client.get("$baseUrl/categories")
            response.jsonArray<RestCategory>()
        }

    fun getAllReviews(): List<RestReview> = runBlocking {
        val response = client.get("$baseUrl/reviews")
        response.jsonArray<RestReview>()
    }
    fun nonexistingEndpoint() = runBlocking {
        client.get("$baseUrl/nonexisting").bodyAsText()
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
        assertThrows<ClientRequestException> {
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
