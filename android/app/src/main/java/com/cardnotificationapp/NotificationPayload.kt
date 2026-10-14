package com.cardnotificationapp

data class NotificationPayload(val title: String, val text: String) {
  companion object {
    private val amountPattern = Regex("(?<![\\d,])(?:\\d{1,3}(?:,\\d{3})+|\\d+)\\s*원")

    fun fromNotification(title: String, text: String): NotificationPayload? {
      val content = text.ifBlank { title }.trim()
      val amount = amountPattern.find(content) ?: return null
      val remainder = content.removeRange(amount.range).replace(Regex("\\s+"), " ").trim()
      return NotificationPayload(remainder, amount.value.replace(Regex("\\s+"), ""))
    }
  }
}
