package com.example.chesshelper

import android.content.Context
import android.graphics.*
import android.view.*
import kotlin.math.*

/** Полноэкранный оверлей: рамка доски (режим правки) и стрелка лучшего хода. */
class OverlayView(ctx: Context) : View(ctx) {
    val board = RectF()
    var editing = false
    var from: PointF? = null
    var to: PointF? = null
    var info = ""
    private var lx = 0f
    private var ly = 0f
    private val edge = Paint().apply { color = Color.GREEN; style = Paint.Style.STROKE; strokeWidth = 6f }
    private val grid = Paint().apply { color = 0x8800FF00.toInt(); strokeWidth = 2f }
    private val arrow = Paint().apply { color = 0xCCFF5722.toInt(); isAntiAlias = true }
    private val head = Paint().apply { color = 0xCCFF5722.toInt(); style = Paint.Style.FILL; isAntiAlias = true }
    private val txt = Paint().apply { color = Color.YELLOW; textSize = 52f; setShadowLayer(6f, 0f, 0f, Color.BLACK) }

    private val sg = ScaleGestureDetector(ctx, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(d: ScaleGestureDetector): Boolean {
            val h = board.width() * d.scaleFactor / 2
            val cx = board.centerX(); val cy = board.centerY()
            board.set(cx - h, cy - h, cx + h, cy + h)
            invalidate(); return true
        }
    })

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (!editing) return false
        sg.onTouchEvent(e)
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> { lx = e.x; ly = e.y }
            MotionEvent.ACTION_POINTER_UP -> {
                val k = if (e.actionIndex == 0) 1 else 0
                lx = e.getX(k); ly = e.getY(k)
            }
            MotionEvent.ACTION_MOVE -> if (e.pointerCount == 1) {
                board.offset(e.x - lx, e.y - ly); lx = e.x; ly = e.y; invalidate()
            }
        }
        return true
    }

    override fun onDraw(c: Canvas) {
        val cell = board.width() / 8
        if (editing) {
            c.drawRect(board, edge)
            for (i in 1..7) {
                c.drawLine(board.left + i * cell, board.top, board.left + i * cell, board.bottom, grid)
                c.drawLine(board.left, board.top + i * cell, board.right, board.top + i * cell, grid)
            }
        }
        val a = from; val b = to
        if (a != null && b != null) {
            val ang = atan2(b.y - a.y, b.x - a.x)
            val hl = cell * 0.5f
            arrow.strokeWidth = cell * 0.16f
            c.drawLine(a.x, a.y, b.x - cos(ang) * hl * 0.7f, b.y - sin(ang) * hl * 0.7f, arrow)
            val p = Path().apply {
                moveTo(b.x, b.y)
                lineTo(b.x - hl * cos(ang - 0.45f), b.y - hl * sin(ang - 0.45f))
                lineTo(b.x - hl * cos(ang + 0.45f), b.y - hl * sin(ang + 0.45f))
                close()
            }
            c.drawPath(p, head)
        }
        if (info.isNotEmpty()) c.drawText(info, board.left + 16, max(70f, board.top - 24), txt)
    }
}
