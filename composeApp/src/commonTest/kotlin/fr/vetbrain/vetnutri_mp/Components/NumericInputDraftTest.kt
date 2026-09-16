package fr.vetbrain.vetnutri_mp.Components

import kotlin.test.Test
import kotlin.test.assertEquals

class NumericInputDraftTest {
    @Test
    fun backspaceCanRemoveEveryCharacterDespiteModelFormatting() {
        var draft = NumericInputDraft("1.00", isFocused = true)
        for (text in listOf("1.0", "1.", "1", "")) {
            draft = draft.copy(text = text).synchronize("1.0")
            assertEquals(text, draft.text)
        }
    }

    @Test
    fun decimalCommaAndTrailingZerosSurviveModelUpdates() {
        var draft = NumericInputDraft("", isFocused = true)
        for ((text, model) in listOf("0" to "0.0", "0," to "0.0", "0,50" to "0.5")) {
            draft = draft.copy(text = text).synchronize(model)
            assertEquals(text, draft.text)
        }
    }

    @Test
    fun modelUpdatesAreAppliedAfterEditingAndWhenUnfocused() {
        val draft = NumericInputDraft("2,50", isFocused = true)
        val blurred = draft.copy(isFocused = false).synchronize("2.5")
        assertEquals("2.5", blurred.text)
        assertEquals("3.0", blurred.synchronize("3.0").text)
    }
}
