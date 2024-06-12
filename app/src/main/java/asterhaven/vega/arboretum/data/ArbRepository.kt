package asterhaven.vega.arboretum.data

import android.util.Log
import asterhaven.vega.arboretum.lsystems.Specification
import asterhaven.vega.arboretum.lsystems.Systems
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object ArbRepository {
    private val db: FirebaseFirestore = Firebase.firestore
    private const val collectionPath = "items"
    private const val TAG = "ArbRepository"

    suspend fun systemsInitialStore() {
        withContext(Dispatchers.IO) {
            Systems.list.forEach {
                db.collection(collectionPath)
                    .document(it.name)
                    .set(it.serialize())
                    .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
            } //todo tidy up message for all complete
        }
    }

    fun getSystemsFromDB(
        onSuccess : (List<Specification>) -> Unit,
        onFailure : (Exception) -> Unit)
    {
        db.collection(collectionPath)
            .get()
            .addOnSuccessListener { documents ->
                onSuccess(documents.map {
                    deserializeSpecification(it.data)
                })
            }
            .addOnFailureListener(onFailure)
    }
}