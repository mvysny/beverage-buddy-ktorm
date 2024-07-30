package com.vaadin.starter.beveragebuddy.backend.ktorm

import jakarta.validation.ConstraintViolationException
import org.ktorm.entity.Entity
import org.ktorm.entity.add
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table

/**
 * An entity which is able to validate itself via the [validate] function. It uses the standardized
 * JSR303 `jakarta.validation`, which means that the POJOs are directly compatible with
 * Vaadin's BeanValidationBinder.
 */
interface ValidatableEntity<E : Entity<E>> : Entity<E> {

    /**
     * Validates current entity. The Java JSR303 validation is performed by default: just add `jakarta.validation`
     * annotations to entity properties.
     *
     * Make sure to add the validation annotations to
     * fields or getters otherwise they will be ignored. For example `@field:NotNull` or `@get:NotNull`.
     *
     * You can override this method to perform additional validations on the level of the entire entity.
     *
     * @throws jakarta.validation.ValidationException when validation fails.
     */
    fun validate() {
        val violations = SimpleKtorm.validator.validate<Any>(this)
        if (violations.isNotEmpty()) {
            throw ConstraintViolationException(violations)
        }
    }

    /**
     * Checks whether this entity is valid: calls [validate] and returns false if [ConstraintViolationException] is thrown.
     */
    val isValid: Boolean
        get() = try {
            validate()
            true
        } catch (ex: ConstraintViolationException) {
            false
        }

    fun save(validate: Boolean = true) {
        if (validate) {
            validate()
        }
        flushChanges()
    }
}

fun <E : ValidatableEntity<E>> Table<E>.create(entity: E): E {
    entity.validate()
    db {
        database.sequenceOf(this@create).add(entity)
    }
    return entity
}
