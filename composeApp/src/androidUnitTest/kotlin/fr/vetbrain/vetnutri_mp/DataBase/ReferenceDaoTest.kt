package fr.vetbrain.vetnutri_mp.DataBase

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class ReferenceDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var referenceDao: ReferenceDao

    @Before
    fun setupDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
                Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                        .allowMainThreadQueries()
                        .build()
        referenceDao = database.referenceDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertAndGetEquation() = runTest {
        val equation =
                EquationEntity(
                        UUID = "eq-uuid",
                        script = "x + y",
                        refBiblio = "ref-biblio",
                        name = "Test Equation",
                        description = "Test Description",
                        speciesRef = "dog",
                        kind = 1,
                        consistent = 1,
                        nutrient = 1
                )

        referenceDao.insertEquation(equation)

        val retrievedEquation = referenceDao.getEquationById("eq-uuid")
        assertNotNull(retrievedEquation)
        assertEquals(equation.UUID, retrievedEquation.UUID)
        assertEquals(equation.script, retrievedEquation.script)
    }

    @Test
    fun insertAndGetSupplementVariable() = runTest {
        val variable = SupplementVariableEntity(id = 0, refEquation = "eq-uuid", VariableKind = 1)

        referenceDao.insertSupplementVariable(variable)

        val retrievedVariables = referenceDao.getSupplementVariablesForEquation("eq-uuid")
        assertNotNull(retrievedVariables)
        assertEquals(1, retrievedVariables.size)
        assertEquals(variable.refEquation, retrievedVariables[0].refEquation)
    }

    @Test
    fun insertAndGetBiblio() = runTest {
        val biblio =
                BiblioEntity(
                        UUID = "biblio-uuid",
                        fAuthor = "First Author",
                        year = "2024",
                        fullRef = "Full Reference",
                        comments = "Test Comments",
                        consistent = 1
                )

        referenceDao.insertBiblio(biblio)

        val retrievedBiblio = referenceDao.getBiblioById("biblio-uuid")
        assertNotNull(retrievedBiblio)
        assertEquals(biblio.UUID, retrievedBiblio.UUID)
    }

    @Test
    fun insertAndGetMethod() = runTest {
        val method =
                MethodEntity(
                        UUID = "method-uuid",
                        name = "Test Method",
                        species = "dog",
                        description = "Test Description"
                )

        referenceDao.insertMethod(method)

        val retrievedMethod = referenceDao.getMethodById("method-uuid")
        assertNotNull(retrievedMethod)
        assertEquals(method.UUID, retrievedMethod.UUID)
    }

    @Test
    fun insertAndGetTargetMethod() = runTest {
        val targetMethod =
                TargetMethodEntity(
                        id = 0,
                        refMethod = "method-uuid",
                        ord = 1,
                        kind = 1,
                        value = 100f,
                        unit = 1,
                        percent = 0f,
                        measure = 0f
                )

        referenceDao.insertTargetMethod(targetMethod)

        val retrievedTargets = referenceDao.getTargetMethodsForMethod("method-uuid")
        assertNotNull(retrievedTargets)
        assertEquals(1, retrievedTargets.size)
        assertEquals(targetMethod.refMethod, retrievedTargets[0].refMethod)
    }

    @Test
    fun insertAndGetDataRef() = runTest {
        val dataRef =
                DataRefEntity(
                        UUID = "ref-uuid",
                        name = "Test Reference",
                        description = "Test Description",
                        disease = 1,
                        BWeqRef = null,
                        SERName = null,
                        SERRef = null,
                        DEcomRef = null,
                        DErawRef = null,
                        k1Name = null,
                        k1Ref = null,
                        k2Name = null,
                        k2Ref = null,
                        k3Name = null,
                        k3Ref = null,
                        k4Name = null,
                        k4Ref = null,
                        k5Name = null,
                        k5Ref = null,
                        specie = null,
                        consistent = 1
                )

        referenceDao.insertDataRef(dataRef)

        val retrievedRef = referenceDao.getDataRefById("ref-uuid")
        assertNotNull(retrievedRef)
        assertEquals(dataRef.UUID, retrievedRef.UUID)
    }
}
