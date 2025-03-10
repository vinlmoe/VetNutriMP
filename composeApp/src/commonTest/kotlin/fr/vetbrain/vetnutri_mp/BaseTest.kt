package fr.vetbrain.vetnutri_mp

import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
open class BaseTest {
    @BeforeTest
    open fun setUp() {
        // Méthode appelée avant chaque test
    }

    @AfterTest
    open fun tearDown() {
        // Méthode appelée après chaque test
    }

    protected fun assertValeurPositive(
            valeur: Float,
            message: String = "La valeur doit être positive"
    ) {
        if (valeur < 0) throw AssertionError(message)
    }

    protected fun assertValeurPositive(
            valeur: Double,
            message: String = "La valeur doit être positive"
    ) {
        if (valeur < 0) throw AssertionError(message)
    }

    protected fun assertListeSansDoublons(
            liste: List<*>,
            message: String = "La liste ne doit pas contenir de doublons"
    ) {
        if (liste.size != liste.distinct().size) throw AssertionError(message)
    }
}
