package asterhaven.vega.arboretum.data

import android.content.ContentValues.TAG
import android.util.Log
import asterhaven.vega.arboretum.data.model.CanonicalSpecification
import asterhaven.vega.arboretum.lsystems.Systems
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object ArbRepository {
    private val db: FirebaseFirestore = Firebase.firestore

    /*suspend fun addSpec(item: Specification): Boolean {
        val document = db.collection("items").add()
        if(document)
            document.set(item.copy(id = document.id)).await()
            true
        } catch (e: Exception) {
            false
        }
    }*/

    suspend fun systemsInitialStore() {
        withContext(Dispatchers.IO) {
            Systems.list.forEach {
                val cs = CanonicalSpecification(it)
                db.collection("items")
                    .document(cs.name)
                    .set(cs)
                    .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
                    .await()
            }
        }
    }
}