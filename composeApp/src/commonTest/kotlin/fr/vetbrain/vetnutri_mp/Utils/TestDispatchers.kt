package fr.vetbrain.vetnutri_mp.Utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher

class TestDispatchers(private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()) {
    val main: CoroutineDispatcher = testDispatcher
    val io: CoroutineDispatcher = testDispatcher
    val default: CoroutineDispatcher = testDispatcher
}
