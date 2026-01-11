package com.vaadin.starter.beveragebuddy

import com.github.mvysny.kaributools.addMetaTag
import com.github.mvysny.ktormvaadin.ActiveKtorm
import com.vaadin.flow.component.dependency.StyleSheet
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import com.vaadin.flow.component.page.AppShellConfigurator
import com.vaadin.flow.component.page.BodySize
import com.vaadin.flow.component.page.Viewport
import com.vaadin.flow.server.ServiceInitEvent
import com.vaadin.flow.server.VaadinServiceInitListener
import com.vaadin.flow.server.VaadinSession
import com.vaadin.flow.theme.Theme
import com.vaadin.flow.theme.lumo.Lumo
import com.vaadin.starter.beveragebuddy.backend.ktorm.DemoData
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.h2.Driver
import org.slf4j.LoggerFactory
import jakarta.servlet.ServletContextEvent
import jakarta.servlet.ServletContextListener
import jakarta.servlet.annotation.WebListener
import org.ktorm.database.Database

/**
 * Boots the app:
 *
 * * Makes sure that the database is up-to-date, by running migration scripts with Flyway. This will work even in cluster as Flyway
 *   automatically obtains a cluster-wide database lock.
 * * Initializes the VaadinOnKotlin framework.
 * * Maps Vaadin to `/`, maps REST server to `/rest`
 * @author mvy
 */
@WebListener
class Bootstrap: ServletContextListener {
    override fun contextInitialized(sce: ServletContextEvent?) {
        log.info("Starting up")

        // this will configure your database. For demo purposes, an in-memory embedded H2 database is used. To use a production-ready database:
        // 1. fill in the proper JDBC URL here
        // 2. make sure to include the database driver into the classpath, by adding a dependency on the driver into the build.gradle file.
        val cfg = HikariConfig().apply {
            driverClassName = Driver::class.java.name
            jdbcUrl = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
            username = "sa"
            password = ""
        }
        dataSource = HikariDataSource(cfg)
        ActiveKtorm.database = Database.connect(dataSource)

        // Makes sure the database is up-to-date
        log.info("Running DB migrations")
        val flyway: Flyway = Flyway.configure()
            .dataSource(dataSource)
            .load()
        flyway.migrate()

        // pre-populates the database with a demo data
        log.info("Populating database with testing data")
        DemoData.createDemoData()
        log.info("Initialization complete")
    }

    override fun contextDestroyed(sce: ServletContextEvent?) {
        log.info("Shutting down");
        log.info("Closing database connections")
        dataSource.close()
        log.info("Shutdown complete")
    }

    companion object {
        private val log = LoggerFactory.getLogger(Bootstrap::class.java)
        @Volatile
        private lateinit var dataSource: HikariDataSource
    }
}

/**
 * Configures Vaadin. Registered via the Java Service Loader API.
 */
class MyServiceInitListener : VaadinServiceInitListener {
    override fun serviceInit(event: ServiceInitEvent) {
        event.addIndexHtmlRequestListener {
            it.document.head().addMetaTag("apple-mobile-web-app-capable", "yes")
            it.document.head().addMetaTag("apple-mobile-web-app-status-bar-style", "black")
        }
        event.source.addSessionInitListener { initSession(it.session) }
    }

    private fun initSession(session: VaadinSession) {
        session.setErrorHandler {
            log.error("Internal error", it.throwable)
            val n = Notification.show("We're sorry, an internal error occurred", 3000, Notification.Position.TOP_CENTER)
            n.addThemeVariants(NotificationVariant.LUMO_ERROR)
            n.open()
        }
    }

    companion object {
        @JvmStatic
        private val log = LoggerFactory.getLogger(MyServiceInitListener::class.java)
    }
}

@BodySize(width = "100vw", height = "100vh")
@StyleSheet(Lumo.STYLESHEET)
@StyleSheet("styles.css")
@Viewport("width=device-width, minimum-scale=1.0, initial-scale=1.0, user-scalable=yes")
class AppShell: AppShellConfigurator
