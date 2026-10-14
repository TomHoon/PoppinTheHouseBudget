package com.cardnotificationapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NotificationPayloadTest {
  @Test fun extractsRequestedAmount() {
    assertEquals(
      NotificationPayload("출금 쿠팡(주) 38***", "55,000원"),
      NotificationPayload.fromNotification("은행", "출금 쿠팡(주) 38*** 55,000원")
    )
  }

  @Test fun supportsTitleOnlyAndUnformattedAmounts() {
    assertEquals(NotificationPayload("출금", "55000원"), NotificationPayload.fromNotification("출금 55000 원", ""))
  }

  @Test fun ignoresNotificationsWithoutAmount() {
    assertNull(NotificationPayload.fromNotification("메시지", "새 메시지입니다"))
  }
}
