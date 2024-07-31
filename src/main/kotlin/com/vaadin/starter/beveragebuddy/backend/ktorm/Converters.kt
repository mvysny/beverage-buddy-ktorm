package com.vaadin.starter.beveragebuddy.backend.ktorm

import com.github.vokorm.KEntity
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.binder.Result
import com.vaadin.flow.data.binder.ValueContext
import com.vaadin.flow.data.converter.Converter
import org.ktorm.dsl.eq
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.entity.singleOrNull
import org.ktorm.schema.Column
import org.ktorm.schema.Table

/**
 * Converts an entity to its ID and back. Useful for combo boxes which shows a list of entities as their options while being bound to a
 * field containing ID of that entity.
 * @param T the type of the entity
 * @param ID the type of the ID field of the entity
 */
class EntityToIdConverter<ID: Any, T: Entity<T>>(val idColumn: Column<ID>) : Converter<T?, ID?> {

    override fun convertToModel(value: T?, context: ValueContext?): Result<ID?> =
        Result.ok(value?.get(idColumn.name) as ID?)

    override fun convertToPresentation(value: ID?, context: ValueContext?): T? {
        if (value == null) return null
        return db {
            database.sequenceOf(idColumn.table as Table<T>).singleOrNull { idColumn.eq(value) }
        }
    }
}

/**
 * Converts an entity to its ID and back. Useful for combo boxes which shows a list of entities as their options while being bound to a
 * field containing ID of that entity:
 * ```kotlin
 * data class Category(override var id: Long? = null, var name: String = "") : Entity<Long>
 * data class Review(override var id: Long? = null, var category: Long? = null) : Entity<Long>
 *
 * // editing the Review, we want the user to be able to choose the Review's category
 * val binder = BeanValidationBinder(Review::class.java)
 * categoryBox = comboBox("Choose a category") {
 *     setItemLabelGenerator { it.name }
 *     isAllowCustomValue = false
 *     dataProvider = Category.dataProvider
 *     bind(binder).toId().bind(Review::category)
 * }
 * ```
 */
fun <BEAN, ID: Any, ENTITY: Entity<ENTITY>> Binder.BindingBuilder<BEAN, ENTITY?>.toId(idColumn: Column<ID>): Binder.BindingBuilder<BEAN, ID?> =
    withConverter(EntityToIdConverter(idColumn))
