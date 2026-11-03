package com.drnitz.youcan.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.drnitz.youcan.YoucanApplication
import com.drnitz.youcan.data.repository.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            val app = context.applicationContext as? YoucanApplication ?: return
            val reminderRepository = ReminderRepository(app, app.database.reminderDao())
            val planRepository = com.drnitz.youcan.data.repository.PlanRepository(app, app.database.dailyPlanDao())
            val countdownRepository = com.drnitz.youcan.data.repository.CountdownRepository(app, app.database.countdownDao())

            CoroutineScope(Dispatchers.IO).launch {
                reminderRepository.rescheduleAllEnabledReminders()
                planRepository.rescheduleAllReminders()
                countdownRepository.rescheduleAllCountdowns()
            }
        }
    }
}
