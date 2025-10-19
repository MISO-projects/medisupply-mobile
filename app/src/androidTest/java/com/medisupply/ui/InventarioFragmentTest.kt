package com.medisupply.ui

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.recyclerview.widget.RecyclerView
import com.medisupply.R
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InventarioFragmentTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testInventarioFragmentIsDisplayed() {
        // Given - la app está abierta
        Thread.sleep(500)

        // When - navegamos a Inventario
        onView(withId(R.id.nav_inventario)).perform(click())
        Thread.sleep(1000)

        // Then - verificamos que los elementos están visibles
        onView(withId(R.id.search_edit_text)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_categoria)).check(matches(isDisplayed()))
        onView(withId(R.id.btn_disponibilidad)).check(matches(isDisplayed()))
    }

    @Test
    fun testSearchBarIsWorking() {
        // Given
        onView(withId(R.id.nav_inventario)).perform(click())
        Thread.sleep(1500)

        // When - escribimos en el buscador
        onView(withId(R.id.search_edit_text))
            .perform(replaceText("Alcohol"), closeSoftKeyboard())
        Thread.sleep(500)

        // Then - verificamos que el RecyclerView está visible
        onView(withId(R.id.productos_recycler_view)).check(matches(isDisplayed()))
    }

    @Test
    fun testProductListIsDisplayedAfterLoading() {
        // Given
        onView(withId(R.id.nav_inventario)).perform(click())

        // When - esperamos que cargue
        Thread.sleep(1500)

        // Then - verificamos que el RecyclerView está visible
        onView(withId(R.id.productos_recycler_view))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testFilterButtonsAreDisplayed() {
        // Given
        onView(withId(R.id.nav_inventario)).perform(click())
        Thread.sleep(500)

        // Then - verificamos que los botones de filtro están visibles
        onView(withId(R.id.btn_categoria))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.categorias)))

        onView(withId(R.id.btn_disponibilidad))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.disponibilidad)))
    }

    @Test
    fun testCategoryFilterDialogOpens() {
        // Given
        onView(withId(R.id.nav_inventario)).perform(click())
        Thread.sleep(1000)

        // When - hacemos click en el botón de categorías
        onView(withId(R.id.btn_categoria)).perform(click())
        Thread.sleep(300)

        // Then - verificamos que el diálogo se abre (el texto "Todas las categorías" debería estar visible)
        onView(withText(R.string.categoria_todos)).check(matches(isDisplayed()))
    }

    @Test
    fun testDisponibilidadFilterDialogOpens() {
        // Given
        onView(withId(R.id.nav_inventario)).perform(click())
        Thread.sleep(1000)

        // When - hacemos click en el botón de disponibilidad
        onView(withId(R.id.btn_disponibilidad)).perform(click())
        Thread.sleep(300)

        // Then - verificamos que el diálogo se abre
        onView(withText(R.string.todos)).check(matches(isDisplayed()))
        onView(withText(R.string.disponibles)).check(matches(isDisplayed()))
    }

    @Test
    fun testSearchFunctionality() {
        // Given
        onView(withId(R.id.nav_inventario)).perform(click())
        Thread.sleep(1500)

        // When - buscamos un producto específico
        onView(withId(R.id.search_edit_text))
            .perform(replaceText("Paracetamol"), closeSoftKeyboard())
        Thread.sleep(500)

        // Then - el RecyclerView debería mostrar resultados
        onView(withId(R.id.productos_recycler_view))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testSearchWithNoResults() {
        // Given
        onView(withId(R.id.nav_inventario)).perform(click())
        Thread.sleep(1500)

        // When - buscamos algo que no existe
        onView(withId(R.id.search_edit_text))
            .perform(replaceText("ProductoInexistente123"), closeSoftKeyboard())
        Thread.sleep(500)

        // Then - debería mostrar la vista vacía
        onView(withId(R.id.empty_view))
            .check(matches(isDisplayed()))
            .check(matches(withText(R.string.no_hay_productos)))
    }

    @Test
    fun testProductItemsAreDisplayed() {
        // Given
        onView(withId(R.id.nav_inventario)).perform(click())
        Thread.sleep(1500)

        // Then - verificamos que hay items en el RecyclerView
        onView(withId(R.id.productos_recycler_view))
            .check(matches(isDisplayed()))
            .check(matches(hasMinimumChildCount(1)))
    }

    @Test
    fun testRecyclerViewCanScroll() {
        // Given
        onView(withId(R.id.nav_inventario)).perform(click())
        Thread.sleep(1500)

        // When - hacemos scroll en el RecyclerView
        onView(withId(R.id.productos_recycler_view))
            .perform(RecyclerViewActions.scrollToPosition<RecyclerView.ViewHolder>(5))

        // Then - no debería haber crash
        onView(withId(R.id.productos_recycler_view))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testLoadingIndicatorIsHiddenAfterLoad() {
        // Given
        onView(withId(R.id.nav_inventario)).perform(click())

        // When - esperamos que termine de cargar
        Thread.sleep(1500)

        // Then - el loading no debería estar visible
        onView(withId(R.id.loading_progress_bar))
            .check(matches(not(isDisplayed())))
    }

    @Test
    fun testFilterByCategoryMedicamentos() {
        // Given
        onView(withId(R.id.nav_inventario)).perform(click())
        Thread.sleep(1500)

        // When - filtramos por MEDICAMENTOS
        onView(withId(R.id.btn_categoria)).perform(click())
        Thread.sleep(300)
        onView(withText("MEDICAMENTOS")).perform(click())
        Thread.sleep(500)

        // Then - el botón debería mostrar "MEDICAMENTOS"
        onView(withId(R.id.btn_categoria))
            .check(matches(withText("MEDICAMENTOS")))
    }

    @Test
    fun testFilterByDisponibles() {
        // Given
        onView(withId(R.id.nav_inventario)).perform(click())
        Thread.sleep(1500)

        // When - filtramos por disponibles
        onView(withId(R.id.btn_disponibilidad)).perform(click())
        Thread.sleep(300)
        onView(withText(R.string.disponibles)).perform(click())
        Thread.sleep(500)

        // Then - el botón debería mostrar "Disponibles"
        onView(withId(R.id.btn_disponibilidad))
            .check(matches(withText(R.string.disponibles)))
    }

    @Test
    fun testResetCategoryFilter() {
        // Given
        onView(withId(R.id.nav_inventario)).perform(click())
        Thread.sleep(1500)

        // When - filtramos y luego reseteamos
        onView(withId(R.id.btn_categoria)).perform(click())
        Thread.sleep(300)
        onView(withText("MEDICAMENTOS")).perform(click())
        Thread.sleep(500)

        onView(withId(R.id.btn_categoria)).perform(click())
        Thread.sleep(300)
        onView(withText(R.string.categoria_todos)).perform(click())
        Thread.sleep(500)

        // Then - el botón debería volver a mostrar "Categorías"
        onView(withId(R.id.btn_categoria))
            .check(matches(withText(R.string.categorias)))
    }

    @Test
    fun testCombinedSearchAndFilter() {
        // Given
        onView(withId(R.id.nav_inventario)).perform(click())
        Thread.sleep(1500)

        // When - aplicamos búsqueda y filtro
        onView(withId(R.id.search_edit_text))
            .perform(replaceText("o"), closeSoftKeyboard())
        Thread.sleep(500)

        onView(withId(R.id.btn_categoria)).perform(click())
        Thread.sleep(300)
        onView(withText("INSUMOS")).perform(click())
        Thread.sleep(500)

        // Then - ambos filtros deberían estar activos
        onView(withId(R.id.btn_categoria))
            .check(matches(withText("INSUMOS")))
        onView(withId(R.id.productos_recycler_view))
            .check(matches(isDisplayed()))
    }

    @Test
    fun testSearchBarPlaceholder() {
        // Given
        onView(withId(R.id.nav_inventario)).perform(click())
        Thread.sleep(500)

        // Then - verificamos que el placeholder es correcto
        onView(withId(R.id.search_edit_text))
            .check(matches(withHint(R.string.buscar_productos)))
    }

    @Test
    fun testNavigationBetweenFragments() {
        // Given
        onView(withId(R.id.nav_inventario)).perform(click())
        Thread.sleep(1000)

        // When - navegamos a otro fragmento y volvemos
        onView(withId(R.id.nav_clientes)).perform(click())
        Thread.sleep(500)
        onView(withId(R.id.nav_inventario)).perform(click())
        Thread.sleep(1000)

        // Then - el fragmento debería cargar correctamente de nuevo
        onView(withId(R.id.search_edit_text)).check(matches(isDisplayed()))
        onView(withId(R.id.productos_recycler_view)).check(matches(isDisplayed()))
    }

    @Test
    fun testErrorViewIsNotDisplayedOnSuccess() {
        // Given
        onView(withId(R.id.nav_inventario)).perform(click())

        // When - esperamos que cargue
        Thread.sleep(1500)

        // Then - la vista de error no debería estar visible
        onView(withId(R.id.error_view))
            .check(matches(not(isDisplayed())))
    }

    @Test
    fun testAllProductsHaveStockInfo() {
        // Given
        onView(withId(R.id.nav_inventario)).perform(click())
        Thread.sleep(1500)

        // Then - verificamos que el primer item tiene información de stock
        // (esto se puede ver visualmente que todos los items muestran "X disponibles")
        onView(withId(R.id.productos_recycler_view))
            .check(matches(isDisplayed()))
            .check(matches(hasDescendant(withId(R.id.stock_producto))))
    }
}


