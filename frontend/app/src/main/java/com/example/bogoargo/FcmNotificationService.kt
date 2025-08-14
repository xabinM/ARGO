package com.example.bogoargo

import android.Manifest
import android.os.Build
import android.util.Log
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.bogoargo.util.FcmCommandParser
import com.example.bogoargo.data.repository.FCMPushSender
import com.example.bogoargo.data.storage.SecureStorage
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint
//fcm 수신 코드
@AndroidEntryPoint
class FcmNotificationService : FirebaseMessagingService() {

    @Inject
    lateinit var secureStorage: SecureStorage

    @Inject
    lateinit var pushSender: FCMPushSender

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        secureStorage.saveFcmToken(token)
        pushSender.sendCurrentToken()
    }

    override fun onMessageReceived(msg: RemoteMessage) {
        super.onMessageReceived(msg)

        val cmd = FcmCommandParser.parse(msg.data)
        val body = cmd.teamName?.let { "신청 팀: $it" }.orEmpty()
        showNotification(cmd.message, body)
    }

    private fun showNotification(title: String, body: String) {

        if (Build.VERSION.SDK_INT >= 33 &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "알림 권한 없음")
            return
        }

        createChannelIfNeeded()

        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or
                    (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        NotificationManagerCompat.from(this)
            .notify((System.currentTimeMillis() and 0x7FFFFFFF).toInt(), notification)
    }

    private fun createChannelIfNeeded() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val nm = getSystemService(NotificationManager::class.java)
        if (nm.getNotificationChannel(CHANNEL_ID) != null) return

        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ch = NotificationChannel(
            CHANNEL_ID, "메시지 알림", NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "메시지/명령 알림"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            enableVibration(true)
            enableLights(true)
            setSound(
                sound,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
        }
        nm.createNotificationChannel(ch)
    }

    companion object {
        private const val TAG = "FCM"
        private const val CHANNEL_ID = "msg_high_banner_v4"
    }
}