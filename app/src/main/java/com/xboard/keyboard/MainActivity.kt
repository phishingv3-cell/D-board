package com.xboard.keyboard

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val COLOR_BLACK = Color.parseColor("#000000")
    private val COLOR_WHITE = Color.parseColor("#FFFFFF")
    private val COLOR_DARK_GRAY = Color.parseColor("#1C1C1C")
    private val COLOR_ACCENT_GREEN = Color.parseColor("#07F57E")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rootLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(COLOR_BLACK)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dpToPx(24), dpToPx(48), dpToPx(24), dpToPx(32))
        }

        val title = TextView(this).apply {
            text = "X-BOARD"
            setTextColor(COLOR_ACCENT_GREEN)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }
        rootLayout.addView(title)

        val subtitle = TextView(this).apply {
            text = "Next-Gen Minimalist Android Keyboard"
            setTextColor(COLOR_WHITE)
            alpha = 0.7f
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            gravity = Gravity.CENTER
            setPadding(0, dpToPx(8), 0, dpToPx(36))
        }
        rootLayout.addView(subtitle)

        val btnEnable = createActionButton("1. Enable Keyboard", COLOR_ACCENT_GREEN, COLOR_BLACK) {
            val intent = Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
        rootLayout.addView(btnEnable)

        rootLayout.addView(createSpacer(14))

        val btnSelect = createActionButton("2. Select X Board", COLOR_DARK_GRAY, COLOR_WHITE) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.showInputMethodPicker()
        }
        rootLayout.addView(btnSelect)

        rootLayout.addView(createSpacer(14))

        val btnDetails = createActionButton("3. Details", COLOR_DARK_GRAY, COLOR_ACCENT_GREEN) {
            showDetailsDialog()
        }
        rootLayout.addView(btnDetails)

        val flexibleSpacer = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f
            )
        }
        rootLayout.addView(flexibleSpacer)

        val footerCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = createRoundedDrawable(COLOR_DARK_GRAY, 8f)
            setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val footerText = TextView(this).apply {
            text = "Creator: AvishkaX • Version: 0.1V\nStatus: This keyboard not fully completed"
            setTextColor(COLOR_WHITE)
            alpha = 0.85f
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            gravity = Gravity.CENTER
            setLineSpacing(dpToPx(4).toFloat(), 1.0f)
        }
        footerCard.addView(footerText)
        rootLayout.addView(footerCard)

        setContentView(rootLayout)
    }

    private fun createActionButton(label: String, bgColor: Int, textColor: Int, onClick: () -> Unit): Button {
        return Button(this).apply {
            text = label
            setTextColor(textColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            typeface = Typeface.DEFAULT_BOLD
            isAllCaps = false
            background = createRoundedDrawable(bgColor, 8f)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(52)
            )
            setOnClickListener { onClick() }
        }
    }

    private fun showDetailsDialog() {
        val message = """
            👤 Creator: AvishkaX
            📦 Version: 0.1V
            
            ⚠️ Note:
            This keyboard not fully completed
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("X-BOARD Information")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun createSpacer(dp: Int): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(dp)
            )
        }
    }

    private fun createRoundedDrawable(bgColor: Int, radiusDp: Float): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(bgColor)
            cornerRadius = dpToPx(radiusDp.toInt()).toFloat()
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
