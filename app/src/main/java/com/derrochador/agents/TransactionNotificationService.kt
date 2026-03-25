package com.derrochador.agents

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.derrochador.data.AppDatabase
import com.derrochador.data.TransactionEntity
import com.derrochador.data.TransactionRepository
import com.derrochador.domain.NotificationParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Watcher Agent: Listens for banking app notifications and extracts transaction data.
 * Operates entirely on-device without sending any data externally.
 */
class TransactionNotificationService : NotificationListenerService() {

    companion object {
        private const val TAG = "WatcherAgent"

        /**
         * Package names of known banking and fintech apps to monitor.
         * This list covers major Latin American banks and payment platforms.
         */
        val MONITORED_PACKAGES = setOf(
            // Colombian banks
            "com.bancolombia.app",
            "com.davivienda.movilapp",
            "co.com.bbva.col",
            "com.todo1.mobile.co.boc",
            // Mexican banks
            "com.bbva.bbvacontigo",
            "com.citibanamex.banamexmobile",
            // Payment platforms
            "com.nequi.MobileApp",
            "com.mercadopago.wallet",
            // Generic SMS apps (for bank SMS)
            "com.google.android.apps.messaging",
            "com.samsung.android.messaging",
            "com.android.mms"
        )
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var repository: TransactionRepository

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getInstance(applicationContext)
        repository = TransactionRepository(db.transactionDao())
        Log.d(TAG, "Watcher Agent initialized")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return

        val packageName = sbn.packageName
        if (packageName !in MONITORED_PACKAGES) return

        val notification = sbn.notification
        val extras = notification.extras

        val title = extras.getCharSequence("android.title")?.toString() ?: ""
        val text = extras.getCharSequence("android.text")?.toString() ?: ""
        val bigText = extras.getCharSequence("android.bigText")?.toString() ?: ""

        val rawText = listOf(title, text, bigText)
            .filter { it.isNotBlank() }
            .joinToString(" | ")

        if (rawText.isBlank()) return

        Log.d(TAG, "Intercepted notification from $packageName: ${rawText.take(50)}...")

        processNotification(rawText, packageName)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        // No action needed on notification removal
    }

    private fun processNotification(rawText: String, packageName: String) {
        serviceScope.launch {
            try {
                // Check for duplicates (same text within last 5 minutes)
                val existing = repository.findDuplicate(rawText)
                if (existing != null) {
                    Log.d(TAG, "Duplicate notification detected, skipping")
                    return@launch
                }

                // Parse the notification text
                val parsed = NotificationParser.parse(rawText)

                val transaction = if (parsed != null) {
                    TransactionEntity(
                        amount = parsed.amount,
                        description = parsed.description,
                        source = if (isMessagingApp(packageName)) "sms" else "notification",
                        packageName = packageName,
                        rawText = rawText
                    )
                } else {
                    // Store even unparseable notifications for later Brain Agent processing
                    TransactionEntity(
                        amount = 0.0,
                        description = "Pendiente de análisis",
                        source = if (isMessagingApp(packageName)) "sms" else "notification",
                        packageName = packageName,
                        rawText = rawText
                    )
                }

                val id = repository.insertTransaction(transaction)
                Log.d(TAG, "Transaction saved with id=$id, amount=${transaction.amount}")

            } catch (e: Exception) {
                Log.e(TAG, "Error processing notification", e)
            }
        }
    }

    private fun isMessagingApp(packageName: String): Boolean {
        return packageName in setOf(
            "com.google.android.apps.messaging",
            "com.samsung.android.messaging",
            "com.android.mms"
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d(TAG, "Watcher Agent destroyed")
    }
}
