
package com.bhtravel.app.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bhtravel.app.data.entity.ExpenseEntity
import com.bhtravel.app.data.entity.FavoriteOfferEntity
import com.bhtravel.app.data.entity.ItineraryItemEntity
import com.bhtravel.app.data.entity.TripEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TripDatabaseTest {
    private lateinit var database: TripDatabase

    @Before
    fun createDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        database =
            Room
                .inMemoryDatabaseBuilder(
                    context,
                    TripDatabase::class.java,
                ).allowMainThreadQueries()
                .build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    // --------------------------------------------------
    // Helper methods
    // --------------------------------------------------

    private fun createTrip(
        id: Int = 1,
        destination: String = "Paris",
    ) = TripEntity(
        id = id,
        destination = destination,
        country = "France",
        startDate = "2026-12-01",
        endDate = "2026-12-05",
        travelers = 2,
        notes = "Holiday",
        imageUrl = null,
    )

    private fun createExpense(
        tripId: Int = 1,
        category: String = "Hotel",
        amount: Double = 100.0,
    ) = ExpenseEntity(
        id = 0,
        tripId = tripId,
        category = category,
        amount = amount,
        description = "Test expense",
        date = "2026-12-01",
    )

    private fun createItineraryItem(
        tripId: Int = 1,
        title: String = "Visit museum",
    ) = ItineraryItemEntity(
        id = 0,
        tripId = tripId,
        title = title,
        description = "Test activity",
        date = "2026-12-02",
        time = "10:00",
        location = "City center",
    )

    private fun createFavorite(destination: String) =
        FavoriteOfferEntity(
            destination = destination,
            country = "Test country",
            description = "Test offer",
            duration = "5 days",
            price = "500.0",
            imageName = "paris",
        )

    // --------------------------------------------------
    // TRIP DAO TESTS
    // --------------------------------------------------

    @Test
    fun tripDao_insertTrip_savesTrip() =
        runBlocking {
            database.tripDao().insertTrip(createTrip())

            val result = database.tripDao().getAllTrips().first()

            assertEquals(1, result.size)
            assertEquals("Paris", result.first().destination)
        }

    @Test
    fun tripDao_getTripById_returnsCorrectTrip() =
        runBlocking {
            database.tripDao().insertTrip(createTrip(id = 1))

            val result = database.tripDao().getTripById(1)

            assertNotNull(result)
            assertEquals("Paris", result?.destination)
        }

    @Test
    fun tripDao_getTripById_returnsNullForMissingTrip() =
        runBlocking {
            val result = database.tripDao().getTripById(999)

            assertNull(result)
        }

    @Test
    fun tripDao_updateTrip_changesTrip() =
        runBlocking {
            database.tripDao().insertTrip(createTrip())

            val updatedTrip =
                createTrip().copy(
                    destination = "Rome",
                )

            database.tripDao().updateTrip(updatedTrip)

            val result = database.tripDao().getTripById(1)

            assertEquals("Rome", result?.destination)
        }

    @Test
    fun tripDao_deleteTrip_removesTrip() =
        runBlocking {
            database.tripDao().insertTrip(createTrip())

            database.tripDao().deleteTrip(createTrip())

            val result = database.tripDao().getAllTrips().first()

            assertTrue(result.isEmpty())
        }

    @Test
    fun tripDao_deleteTrip_alsoRemovesExpensesAndItineraryItems() =
        runBlocking {
            val trip = createTrip()
            database.tripDao().insertTrip(trip)
            database.expenseDao().insertExpense(createExpense())
            database.itineraryDao().insertItem(createItineraryItem())

            database.tripDao().deleteTrip(trip)

            assertTrue(
                database
                    .expenseDao()
                    .getExpensesForTrip(trip.id)
                    .first()
                    .isEmpty(),
            )
            assertTrue(
                database
                    .itineraryDao()
                    .getItemsForTrip(trip.id)
                    .first()
                    .isEmpty(),
            )
            assertNull(database.tripDao().getTripById(trip.id))
        }

    @Test
    fun tripDao_getAllTrips_returnsAllTrips() =
        runBlocking {
            database.tripDao().insertTrip(createTrip(1, "Paris"))
            database.tripDao().insertTrip(createTrip(2, "Rome"))
            database.tripDao().insertTrip(createTrip(3, "London"))

            val result = database.tripDao().getAllTrips().first()

            assertEquals(3, result.size)
        }

    // --------------------------------------------------
    // EXPENSE DAO TESTS
    // --------------------------------------------------

    @Test
    fun expenseDao_insertExpense_savesExpense() =
        runBlocking {
            database.expenseDao().insertExpense(createExpense())

            val result =
                database
                    .expenseDao()
                    .getExpensesForTrip(1)
                    .first()

            assertEquals(1, result.size)
            assertEquals(100.0, result.first().amount, 0.001)
        }

    @Test
    fun expenseDao_getAllExpenses_returnsAllExpenses() =
        runBlocking {
            database.expenseDao().insertExpense(createExpense(amount = 50.0))
            database.expenseDao().insertExpense(createExpense(amount = 75.0))

            val result = database.expenseDao().getAllExpenses().first()

            assertEquals(2, result.size)
        }

    @Test
    fun expenseDao_updateExpense_changesExpense() =
        runBlocking {
            database.expenseDao().insertExpense(createExpense())

            val original =
                database
                    .expenseDao()
                    .getExpensesForTrip(1)
                    .first()
                    .first()

            val updated = original.copy(amount = 250.0)

            database.expenseDao().updateExpense(updated)

            val result =
                database
                    .expenseDao()
                    .getExpensesForTrip(1)
                    .first()

            assertEquals(250.0, result.first().amount, 0.001)
        }

    @Test
    fun expenseDao_deleteExpense_removesExpense() =
        runBlocking {
            database.expenseDao().insertExpense(createExpense())

            val expense =
                database
                    .expenseDao()
                    .getExpensesForTrip(1)
                    .first()
                    .first()

            database.expenseDao().deleteExpense(expense)

            val result =
                database
                    .expenseDao()
                    .getExpensesForTrip(1)
                    .first()

            assertTrue(result.isEmpty())
        }

    @Test
    fun expenseDao_getTotalForTrip_calculatesTotal() =
        runBlocking {
            database.expenseDao().insertExpense(createExpense(amount = 100.0))
            database.expenseDao().insertExpense(createExpense(amount = 50.0))
            database.expenseDao().insertExpense(createExpense(amount = 25.0))

            val result =
                database
                    .expenseDao()
                    .getTotalForTrip(1)
                    .first()

            assertEquals(175.0, result ?: 0.0, 0.001)
        }

    @Test
    fun expenseDao_getExpensesForTrip_returnsOnlySelectedTrip() =
        runBlocking {
            database.expenseDao().insertExpense(createExpense(tripId = 1))
            database.expenseDao().insertExpense(createExpense(tripId = 2))

            val result =
                database
                    .expenseDao()
                    .getExpensesForTrip(1)
                    .first()

            assertEquals(1, result.size)
        }

    // --------------------------------------------------
    // ITINERARY DAO TESTS
    // --------------------------------------------------

    @Test
    fun itineraryDao_insertItem_savesItem() =
        runBlocking {
            database.itineraryDao().insertItem(createItineraryItem())

            val result =
                database
                    .itineraryDao()
                    .getItemsForTrip(1)
                    .first()

            assertEquals(1, result.size)
            assertEquals("Visit museum", result.first().title)
        }

    @Test
    fun itineraryDao_updateItem_changesItem() =
        runBlocking {
            database.itineraryDao().insertItem(createItineraryItem())

            val original =
                database
                    .itineraryDao()
                    .getItemsForTrip(1)
                    .first()
                    .first()

            val updated = original.copy(title = "Visit Eiffel Tower")

            database.itineraryDao().updateItem(updated)

            val result =
                database
                    .itineraryDao()
                    .getItemsForTrip(1)
                    .first()

            assertEquals("Visit Eiffel Tower", result.first().title)
        }

    @Test
    fun itineraryDao_deleteItem_removesItem() =
        runBlocking {
            database.itineraryDao().insertItem(createItineraryItem())

            val item =
                database
                    .itineraryDao()
                    .getItemsForTrip(1)
                    .first()
                    .first()

            database.itineraryDao().deleteItem(item)

            val result =
                database
                    .itineraryDao()
                    .getItemsForTrip(1)
                    .first()

            assertTrue(result.isEmpty())
        }

    @Test
    fun itineraryDao_getItemsForTrip_returnsOnlySelectedTrip() =
        runBlocking {
            database.itineraryDao().insertItem(
                createItineraryItem(tripId = 1),
            )
            database.itineraryDao().insertItem(
                createItineraryItem(tripId = 2),
            )

            val result =
                database
                    .itineraryDao()
                    .getItemsForTrip(1)
                    .first()

            assertEquals(1, result.size)
        }

    @Test
    fun itineraryDao_getItemsForTrip_returnsItemsInDateOrder() =
        runBlocking {
            database.itineraryDao().insertItem(
                createItineraryItem(title = "Later activity").copy(
                    date = "2026-12-04",
                ),
            )

            database.itineraryDao().insertItem(
                createItineraryItem(title = "Earlier activity").copy(
                    date = "2026-12-02",
                ),
            )

            val result =
                database
                    .itineraryDao()
                    .getItemsForTrip(1)
                    .first()

            assertEquals("Earlier activity", result.first().title)
        }

    // --------------------------------------------------
    // FAVORITE OFFER DAO TESTS
    // --------------------------------------------------

    @Test
    fun favoriteDao_addFavorite_savesFavorite() =
        runBlocking {
            database.favoriteOfferDao().addFavorite(
                createFavorite("Paris"),
            )

            val result =
                database
                    .favoriteOfferDao()
                    .getAllFavorites()
                    .first()

            assertEquals(1, result.size)
            assertEquals("Paris", result.first().destination)
        }

    @Test
    fun favoriteDao_getFavorite_returnsCorrectFavorite() =
        runBlocking {
            database.favoriteOfferDao().addFavorite(
                createFavorite("Rome"),
            )

            val result =
                database
                    .favoriteOfferDao()
                    .getFavorite("Rome")

            assertNotNull(result)
            assertEquals("Rome", result?.destination)
        }

    @Test
    fun favoriteDao_getFavorite_returnsNullForMissingFavorite() =
        runBlocking {
            val result =
                database
                    .favoriteOfferDao()
                    .getFavorite("Unknown")

            assertNull(result)
        }

    @Test
    fun favoriteDao_removeFavorite_removesFavorite() =
        runBlocking {
            val favorite = createFavorite("Paris")

            database.favoriteOfferDao().addFavorite(favorite)
            database.favoriteOfferDao().removeFavorite(favorite)

            val result =
                database
                    .favoriteOfferDao()
                    .getAllFavorites()
                    .first()

            assertTrue(result.isEmpty())
        }

    @Test
    fun favoriteDao_removeFavoriteByDestination_removesFavorite() =
        runBlocking {
            database.favoriteOfferDao().addFavorite(
                createFavorite("Paris"),
            )

            database
                .favoriteOfferDao()
                .removeFavoriteByDestination("Paris")

            val result =
                database
                    .favoriteOfferDao()
                    .getAllFavorites()
                    .first()

            assertTrue(result.isEmpty())
        }

    @Test
    fun favoriteDao_isFavorite_returnsTrueForSavedFavorite() =
        runBlocking {
            database.favoriteOfferDao().addFavorite(
                createFavorite("Paris"),
            )

            val result =
                database
                    .favoriteOfferDao()
                    .isFavorite("Paris")

            assertTrue(result)
        }

    @Test
    fun favoriteDao_isFavorite_returnsFalseForMissingFavorite() =
        runBlocking {
            val result =
                database
                    .favoriteOfferDao()
                    .isFavorite("London")

            assertTrue(!result)
        }

    @Test
    fun favoriteDao_favoritesAreOrderedByDestination() =
        runBlocking {
            database.favoriteOfferDao().addFavorite(
                createFavorite("Rome"),
            )
            database.favoriteOfferDao().addFavorite(
                createFavorite("Amsterdam"),
            )
            database.favoriteOfferDao().addFavorite(
                createFavorite("Paris"),
            )

            val result =
                database
                    .favoriteOfferDao()
                    .getAllFavorites()
                    .first()

            assertEquals(
                listOf("Amsterdam", "Paris", "Rome"),
                result.map { it.destination },
            )
        }
}
