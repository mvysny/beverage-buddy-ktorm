package com.vaadin.starter.beveragebuddy.ui

import com.github.mvysny.kaributesting.v10.*
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.starter.beveragebuddy.AbstractAppTest
import com.vaadin.starter.beveragebuddy.backend.ktorm.Categories
import com.vaadin.starter.beveragebuddy.backend.ktorm.Category
import com.vaadin.starter.beveragebuddy.backend.ktorm.categories
import com.vaadin.starter.beveragebuddy.backend.ktorm.create
import com.vaadin.starter.beveragebuddy.backend.ktorm.db
import com.vaadin.starter.beveragebuddy.ui.categories.CategoryEditorDialog
import org.junit.jupiter.api.Test
import org.ktorm.entity.single
import org.ktorm.entity.toList
import kotlin.test.expect

/**
 * Tests the UI. Uses the Browserless Testing approach as provided by the [Karibu Testing](https://github.com/mvysny/karibu-testing) library.
 */
class CategoryEditorDialogTest : AbstractAppTest() {
    @Test
    fun `create new category`() {
        CategoryEditorDialog {}.createNew()

        // make sure that the "New Category" dialog is opened
        _expectOne<EditorDialogFrame<*>>()

        // do the happy flow: fill in the form with valid values and click "Save"
        _get<TextField> { label = "Category Name" }.value = "Beer"
        _get<Button> { text = "Create" }._click()
        expectNotifications("Category successfully added.")

        _expectNone<EditorDialogFrame<*>>()     // expect the dialog to close
        expect("Beer") { db { database.categories.single().name } }
    }

    @Test
    fun `dont create category with blank name`() {
        CategoryEditorDialog {}.createNew()

        // make sure that the "New Category" dialog is opened
        _expectOne<EditorDialogFrame<*>>()

        // do the happy flow: fill in the form with valid values and click "Save"
        _get<TextField> { label = "Category Name" }.value = ""
        _get<Button> { text = "Create" }._click()

        _expectOne<EditorDialogFrame<*>>()     // expect the dialog to close
        _get<TextField> { label = "Category Name" }._expectInvalid()
        expectList() { db { database.categories.toList() } }
    }

    @Test
    fun `edit existing category`() {
        val cat = Categories.create(Category { name = "Foo" })

        CategoryEditorDialog {}.edit(cat)

        // make sure that the "New Category" dialog is opened
        _expectOne<EditorDialogFrame<*>>()

        // do the happy flow: fill in the form with valid values and click "Save"
        _get<TextField> { label = "Category Name" }.value = "Beer"
        _get<Button> { text = "Save" }._click()
        expectNotifications("Category successfully saved.")

        _expectNone<EditorDialogFrame<*>>()     // expect the dialog to close
        expect("Beer") { db { database.categories.single().name } }
    }
}
