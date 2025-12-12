# Beverage Buddy App Starter for Vaadin
:coffee::tea::sake::baby_bottle::beer::cocktail::tropical_drink::wine_glass:

This is a [Vaadin](https://vaadin.com/)+[Kotlin](https://kotlinlang.org/)+[Ktorm](https://www.ktorm.org/) example application,
used to demonstrate features of the Vaadin Java framework.
A full-stack app: uses the H2 database instead of a dummy service. Requires Java 17+.
Uses [ktorm-vaadin](https://github.com/mvysny/ktorm-vaadin).

The Starter demonstrates the core Vaadin Flow concepts:
* [Building UIs in Kotlin](https://github.com/mvysny/karibu-dsl) with components
  such as `TextField`, `Button`, `ComboBox`, `DatePicker`, `VerticalLayout` and `Grid` (see `CategoriesList`)
* [Creating forms with `Binder`](https://github.com/vaadin/free-starter-flow/blob/master/documentation/using-binder-in-review-editor-dialog.asciidoc) (`ReviewEditorDialog`)
* Making reusable Components on server side with `KComposite` (`AbstractEditorDialog`)
* [Creating Navigation with the Router API](https://github.com/vaadin/free-starter-flow/blob/master/documentation/using-annotation-based-router-api.asciidoc) (`MainLayout`, `ReviewsList`, `CategoriesList`)
* [Browserless testing](https://github.com/mvysny/karibu-testing): see the
  [test suite package](src/test/kotlin/com/vaadin/starter/beveragebuddy/ui) for the complete test implementation.

This version of Beverage Buddy demoes the possibility of developing a Vaadin
web application purely server-side in the Kotlin language. There is no
JavaScript code in this project. We use Vaadin to avoid touching JavaScript after all.

See the [online demo](https://beverage-buddy-ktorm.5678912.xyz/).

# Documentation

Please see the [Vaadin Boot](https://github.com/mvysny/vaadin-boot#preparing-environment) documentation
on how you run, develop and package this Vaadin-Boot-based app.

## Database

Without the database, we could store the categories and reviews into session only, which would then be gone when the server rebooted.
We will use the [Ktorm](https://www.ktorm.org/)'s SQL database support. To make things easy we'll
use in-memory H2 database which will be gone when the server is rebooted - *touche* :-D

We will use [Flyway](https://flywaydb.org/) for database migration. Check out [Bootstrap.kt](src/main/kotlin/com/vaadin/starter/beveragebuddy/Bootstrap.kt)
on how the [migration scripts](src/main/resources/db/migration) are ran when the app is initialized.

The [Category](src/main/kotlin/com/vaadin/starter/beveragebuddy/backend/ktorm/Category.kt)
and [Review](src/main/kotlin/com/vaadin/starter/beveragebuddy/backend/ktorm/Review.kt)
entities are mapped to the database tables; inheriting from Entity and Dao
will make it inherit bunch of useful methods such as `findAll()` and `save()`. It will also gain means of
providing all of its instances via a `DataProvider`. See the [CategoriesList.kt](src/main/kotlin/com/vaadin/starter/beveragebuddy/ui/categories/CategoriesList.kt)
Grid configuration for details.

## JOOQ

* For a JOOQ version of this app, please see [beverage-buddy-jooq](https://github.com/mvysny/beverage-buddy-jooq).
* For a vok-orm version of this app, please see [beverage-buddy-vok](https://github.com/mvysny/beverage-buddy-vok).
