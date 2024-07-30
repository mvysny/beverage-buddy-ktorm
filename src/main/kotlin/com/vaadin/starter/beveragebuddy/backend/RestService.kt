package com.vaadin.starter.beveragebuddy.backend

import com.fatboyindustrial.gsonjavatime.Converters
import com.google.gson.*
import com.vaadin.starter.beveragebuddy.backend.ktorm.categories
import com.vaadin.starter.beveragebuddy.backend.ktorm.db
import jakarta.servlet.annotation.WebServlet
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.http4k.core.ContentType
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.servlet.jakarta.Http4kJakartaServletAdapter
import org.ktorm.entity.Entity
import org.ktorm.entity.toList
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaType

object RestService {
    val gson: Gson = GsonBuilder().registerJavaTimeAdapters().registerTypeHierarchyAdapter(Entity::class.java, KtormEntityGsonConverter()).create()

    private fun Response.json(obj: Any): Response =
        header("Content-Type", ContentType.APPLICATION_JSON.toHeaderValue())
            .body(gson.toJsonAsync(obj))

    val app: RoutingHttpHandler = routes(
        "categories" bind GET to { Response(OK).json(db { database.categories.toList() }) }
    )
}

/**
 * Provides access to person list. To test, just run `curl http://localhost:8080/rest/categories`
 */
@WebServlet(
    urlPatterns = ["/rest/*"],
    name = "RestServlet",
    asyncSupported = false
)
class RestServlet : HttpServlet() {
    private val adapter = Http4kJakartaServletAdapter(RestService.app.withBasePath("rest"))
    override fun service(req: HttpServletRequest, resp: HttpServletResponse) =
        adapter.handle(req, resp)
}

private fun GsonBuilder.registerJavaTimeAdapters(): GsonBuilder = apply {
    Converters.registerAll(this)
}

class KtormEntityGsonConverter : JsonSerializer<Entity<*>>, JsonDeserializer<Entity<*>> {
    override fun serialize(
        src: Entity<*>,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        val result = JsonObject()
        src.properties.forEach { (name, value) ->
            if (value != null) {
                result.add(name, context.serialize(value))
            }
        }
        return result
    }

    private fun findWritableProperties(
        entityClass: KClass<*>
    ): Map<String, KProperty1<*, *>> {
        val skipNames = Entity::class.memberProperties.map { it.name }.toSet()
        return entityClass.memberProperties
            .asSequence()
            .filter { it.isAbstract }
            .filter { it.name !in skipNames }
//            .filter { it.findAnnotationForDeserialization<JsonIgnore>() == null }
            .map { prop -> prop.name to prop }
            .toMap()
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): Entity<*> {
        val entityKClass = (typeOfT as Class<*>).kotlin
        val props = findWritableProperties(entityKClass)
        val entity = Entity.create(entityKClass)
        json.asJsonObject.entrySet().forEach { entry ->
            val property = requireNotNull(props[entry.key]) { "$entityKClass: no property for ${entry.key}" }
            entity[entry.key] = context.deserialize(entry.value, property.returnType.javaType)
        }
        return entity
    }
}
