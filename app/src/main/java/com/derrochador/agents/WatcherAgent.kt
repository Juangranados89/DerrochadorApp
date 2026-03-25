package com.derrochador.agents

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.derrochador.data.model.Transaction
import com.derrochador.data.model.TransactionSource
import com.derrochador.domain.usecase.ClassifyExpenseUseCase
import com.derrochador.domain.usecase.SaveTransactionUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

// Banking app packages to monitor
private val BANKING_PACKAGES = setOf(
    "com.bbva.bbvacontigo",
    "com.citibanamex.banamexmobile",
    "com.hsbc.hsbcmexicoapp",
    "com.banorte.bien",
    "com.santander.app",
    "com.scotiabank.banking",
    "financiera.pagandocheck",
    "com.kueski.mobile",
    "com.nubank.nubank"
)

@AndroidEntryPoint
class WatcherAgent : NotificationListenerService() {

    @Inject
    lateinit var saveTransactionUseCase: SaveTransactionUseCase

    @Inject
    lateinit var classifyExpenseUseCase: ClassifyExpenseUseCase

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        if (packageName !in BANKING_PACKAGES) return

        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: text

        val rawText = "$title $bigText".trim()
        if (rawText.isBlank()) return

        val amount = extractAmount(rawText)
        if (amount <= 0.0) return

        serviceScope.launch {
            val category = classifyExpenseUseCase(rawText)
            val transaction = Transaction(
                amount = amount,
                description = title.ifBlank { "Notificación bancaria" },
                category = category.name,
                source = TransactionSource.NOTIFICATION,
                rawText = rawText,
                isProcessed = true
            )
            saveTransactionUseCase(transaction)
        }
    }
}
