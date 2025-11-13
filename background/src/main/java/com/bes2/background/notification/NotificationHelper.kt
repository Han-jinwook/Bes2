package com.bes2.background.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.app.NotificationCompat
import com.bes2.background.R

object NotificationHelper {

    private const val FOREGROUND_CHANNEL_ID = "foreground_service_channel"
    private const val USER_INTERACTION_CHANNEL_ID = "user_interaction_channel"

    private const val CONSENT_NOTIFICATION_ID = 101
    private const val REVIEW_NOTIFICATION_ID = 102
    const val APP_STATUS_NOTIFICATION_ID = 1

    fun createForegroundNotification(context: Context, @DrawableRes notificationIcon: Int): android.app.Notification {
        return NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
            .setContentTitle("Bes2")
            .setContentText("Analyzing photos...")
            .setSmallIcon(notificationIcon)
            .setOngoing(true)
            .build()
    }

    fun showConsentRequiredNotification(context: Context, consentIntent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 
            CONSENT_NOTIFICATION_ID, 
            consentIntent, // The intent from Google is used here
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, USER_INTERACTION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("동기화 권한 필요")
            .setContentText("Google 포토와 동기화하려면 권한이 필요합니다. 탭하여 권한을 부여하세요.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(CONSENT_NOTIFICATION_ID, builder.build())
    }

    fun showReviewNotification(context: Context, @DrawableRes notificationIcon: Int, clusterCount: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // DEFINITIVE FIX: Removed FLAG_ACTIVITY_CLEAR_TASK to prevent the activity stack from being cleared.
        val intent = Intent(context, Class.forName("com.bes2.app.ui.review.ReviewActivity"))

        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            REVIEW_NOTIFICATION_ID, 
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = if (clusterCount > 1) {
            "${clusterCount}개의 새로운 사진 묶음이 준비되었습니다."
        } else {
            "새로운 사진 묶음이 준비되었습니다."
        }

        val builder = NotificationCompat.Builder(context, USER_INTERACTION_CHANNEL_ID)
            .setSmallIcon(notificationIcon)
            .setContentTitle("사진을 확인해보세요")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(REVIEW_NOTIFICATION_ID, builder.build())
    }

    fun dismissAllAppNotifications(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val foregroundChannel = NotificationChannel(
                FOREGROUND_CHANNEL_ID,
                "사진 분석",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "앱이 사진을 분석하는 동안 표시되는 알림입니다."
            }

            val userInteractionChannel = NotificationChannel(
                USER_INTERACTION_CHANNEL_ID,
                "새로운 사진 및 알림",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "새로운 사진 묶음이나 권한 요청 등 사용자의 확인이 필요한 알림입니다."
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(foregroundChannel)
            notificationManager.createNotificationChannel(userInteractionChannel)
        }
    }
}
