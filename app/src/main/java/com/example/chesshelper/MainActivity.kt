package com.example.chesshelper

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.*

@Suppress("DEPRECATION")
class MainActivity : Activity() {
    private var white = true

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(48, 120, 48, 48) }
        root.addView(TextView(this).apply { text = "Chess Helper\nПомощник для игры против ботов"; textSize = 20f })
        val rg = RadioGroup(this)
        rg.addView(RadioButton(this).apply { id = 1; text = "Я играю белыми"; isChecked = true })
        rg.addView(RadioButton(this).apply { id = 2; text = "Я играю чёрными" })
        rg.setOnCheckedChangeListener { _, id -> white = id == 1 }
        root.addView(rg)
        root.addView(Button(this).apply { text = "СТАРТ"; setOnClickListener { start() } })
        setContentView(root)
    }

    private fun start() {
        if (!Settings.canDrawOverlays(this)) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            return
        }
        val mpm = getSystemService(MediaProjectionManager::class.java)
        startActivityForResult(mpm.createScreenCaptureIntent(), 1)
    }

    override fun onActivityResult(req: Int, res: Int, data: Intent?) {
        super.onActivityResult(req, res, data)
        if (req == 1 && res == RESULT_OK && data != null) {
            startForegroundService(
                Intent(this, CaptureService::class.java)
                    .putExtra("code", res).putExtra("data", data).putExtra("white", white)
            )
            moveTaskToBack(true)
        }
    }
}
