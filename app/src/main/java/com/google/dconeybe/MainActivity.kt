package com.google.dconeybe

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.dconeybe.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.firestore
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

  private lateinit var binding: ActivityMainBinding
  private lateinit var mainHandler: Handler

  private val firestore: FirebaseFirestore by lazy {
    logMessage("Getting Firestore instance")
    FirebaseFirestore.setLoggingEnabled(true)
    Firebase.firestore.apply {
      if (binding.useFirestoreEmulator.isChecked) {
        logMessage("Connecting to Firestore emulator")
        useEmulator("10.0.2.2", 8080)
      } else {
        logMessage("Connecting to Firestore project: ${getString(R.string.project_id)}")
      }
    }
  }
  private val collection: CollectionReference by lazy {
    firestore.collection("foo")
  }

  private var snapshotListenerRegistration: ListenerRegistration? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    Log.i("SqliteCrashDemo", "MainActivity.onCreate() called")
    super.onCreate(savedInstanceState)

    mainHandler = Handler(Looper.getMainLooper())

    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    loadSharedPreferences()

    binding.createDocument.setOnClickListener {
      createDocument()
    }
    binding.crash.setOnClickListener {
      logMessage(binding.crash.text.toString() + " Clicked")
      mainHandler.post {
        thread(name = "Crasher") {
          CauseSegmentationFault()
        }
      }
    }
    binding.exit.setOnClickListener {
      logMessage(binding.exit.text.toString() + " Clicked")
      mainHandler.post {
        thread(name = "Crasher") {
          finish()
        }
      }
    }
  }

  override fun onDestroy() {
    Log.i("SqliteCrashDemo", "MainActivity.onDestroy() called")
    snapshotListenerRegistration?.remove()
    super.onDestroy()
  }

  override fun onStart() {
    Log.i("SqliteCrashDemo", "MainActivity.onStart() called")
    super.onStart()
    registerSnapshotListener()
  }

  override fun onStop() {
    Log.i("SqliteCrashDemo", "MainActivity.onStop() called")
    super.onStop()
  }

  override fun onResume() {
    Log.i("SqliteCrashDemo", "MainActivity.onResume() called")
    super.onResume()
  }

  override fun onPause() {
    Log.i("SqliteCrashDemo", "MainActivity.onPause() called")
    storeSharedPreferences()
    super.onPause()
  }

  private fun loadSharedPreferences() {
    val useFirestoreEmulatorIsChecked =
      getSharedPreferences("settings", MODE_PRIVATE).getBoolean("use_firestore_emulator", true)
    binding.useFirestoreEmulator.isChecked = useFirestoreEmulatorIsChecked
  }

  private fun storeSharedPreferences() {
    getSharedPreferences("settings", MODE_PRIVATE).edit().apply {
      putBoolean("use_firestore_emulator", binding.useFirestoreEmulator.isChecked)
      apply()
    }
  }

  private fun logMessage(message: String) {
    mainHandler.post {
      Log.i("SqliteCrashDemo", message)
      binding.messages.text = binding.messages.text.toString() + "\n" + message
    }
  }

  private fun createDocument() {
    val doc = collection.document();
    logMessage("Creating document: ${doc.path}")

    doc.set(mapOf("foo" to "bar")).addOnCompleteListener {
      it.exception?.let {
        logMessage("Creating document \"${doc.path}\" failed: $it")
      }
    }
  }

  private fun registerSnapshotListener() {
    if (snapshotListenerRegistration != null) {
      return
    }

    logMessage("Registering snapshot listener with collection: ${collection.path}")
    snapshotListenerRegistration = collection.addSnapshotListener { value, error ->
      if (error != null) {
        logMessage("Snapshot listener got error: ${error}")
      } else if (value != null) {
        val documentsString = value.documents.map { it.id }.joinToString(", ")
        logMessage("Snapshot listener got ${value.size()} documents: $documentsString")
      } else {
        logMessage("Snapshot listener internal error: both value and error were null")
      }
    }
  }

  external fun CauseSegmentationFault(): String

  private companion object {
    init {
      System.loadLibrary("dconeybe")
    }
  }
}

