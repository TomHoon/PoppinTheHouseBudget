package com.cardnotificationapp

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import java.net.HttpURLConnection
import java.net.URI
import org.json.JSONObject
import java.util.concurrent.Executors

class CardNotificationListenerService : NotificationListenerService() {
  private val sender = Executors.newSingleThreadExecutor()
  private val lastPayloads = mutableMapOf<String, NotificationPayload>()

  override fun onListenerConnected() {
    super.onListenerConnected()
    Log.i(TAG, "Notification listener connected")
  }

  override fun onListenerDisconnected() {
    super.onListenerDisconnected()
    Log.i(TAG, "Notification listener disconnected")
  }

  override fun onNotificationPosted(sbn: StatusBarNotification) {
    if (sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) return
    val extras = sbn.notification.extras
    val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
    val text = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
      ?.takeIf { it.isNotBlank() }
      ?: extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
      ?: extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)?.joinToString(" | ")
      ?: ""
    if (BuildConfig.DEBUG) {
      Log.i(TAG, "packageName=${sbn.packageName} / title=$title / text=$text")
    }
    val payload = NotificationPayload.fromNotification(title, text) ?: return
    if (lastPayloads[sbn.key] == payload) return
    lastPayloads[sbn.key] = payload
    sender.execute { postNotification(sbn.packageName, payload) }
  }

  override fun onNotificationRemoved(sbn: StatusBarNotification) {
    lastPayloads.remove(sbn.key)
  }

  override fun onDestroy() {
    sender.shutdown()
    super.onDestroy()
  }

  private fun postNotification(packageName: String, payload: NotificationPayload) {
    var connection: HttpURLConnection? = null
    try {
      val body = JSONObject()
        .put("packageName", packageName)
        .put("title", payload.title)
        .put("text", payload.text)
        .toString()
      if (BuildConfig.DEBUG) Log.i(TAG, "Apps Script POST request: $body")
      connection = URI(ENDPOINT).toURL().openConnection() as HttpURLConnection
      connection.requestMethod = "POST"
      connection.connectTimeout = 15000
      connection.readTimeout = 15000
      connection.doOutput = true
      connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
      connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
      // Apps Script ContentService redirects to the response URL; HttpURLConnection follows it.
      val status = connection.responseCode
      if (status in 200..299) {
        val response = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        Log.i(TAG, "Apps Script POST response: HTTP $status")
        if (BuildConfig.DEBUG) Log.i(TAG, "Apps Script response body: $response")
      } else {
        Log.e(TAG, "Apps Script POST failed: HTTP $status")
        if (BuildConfig.DEBUG) {
          val response = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
          Log.e(TAG, "Apps Script response body: $response")
        }
      }
    } catch (error: Exception) {
      Log.e(TAG, "Apps Script POST failed", error)
    } finally {
      connection?.disconnect()
    }
  }

  companion object {
    private const val TAG = "CardNotification"
    private const val ENDPOINT = "https://script.google.com/macros/s/AKfycbw4nMesGAtg7yLtmmPREwSD54H4CsxYs9agfGsOrTEKpumK2z8jnfKkqOzRTWkHTcV37A/exec"
  }
}
