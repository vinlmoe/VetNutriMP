package fr.vetbrain.vetnutri_mp

import fr.vetbrain.vetnutri_mp.Utils.AppDispatchers
import fr.vetbrain.vetnutri_mp.Utils.TestDispatchers
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
open class BaseTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val testDispatchers = TestDispatchers(testDispatcher)

    @BeforeTest
    fun setupCoroutines() {
        Dispatchers.setMain(testDispatcher)
        AppDispatchers.setDispatchers(
                io = testDispatchers.io,
                default = testDispatchers.default,
                main = testDispatchers.main
        )
    }

    @AfterTest
    fun cleanupCoroutines() {
        Dispatchers.resetMain()
        AppDispatchers.resetDispatchers()
    }
}
