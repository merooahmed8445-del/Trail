package com.mcode.trail.ui.widgets

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * الـ Receiver اللي الأندرويد بيستخدمه عشان يعرض الـ Widget.
 * مربوط بالـ TrailWidget اللي فيه التصميم والمنطق.
 */
class TrailWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TrailWidget()
}