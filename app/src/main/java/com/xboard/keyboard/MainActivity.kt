package com.xboard.keyboard

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
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

            setPadding(
                dpToPx(24),
                dpToPx(48),
                dpToPx(24),
                dpToPx(32)
            )
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // X-BOARD Title
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        val title = TextView(this).apply {
            text = "X-BOARD"

            setTextColor(
                Color.parseColor("#07F57E")
            )

            setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                32f
            )

            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
        }

        rootLayout.addView(title)

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // Subtitle
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        val subtitle = TextView(this).apply {
            text = "Next-Gen Minimalist Android Keyboard"

            setTextColor(COLOR_WHITE)

            alpha = 0.7f

            setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                14f
            )

            gravity = Gravity.CENTER

            setPadding(
                0,
                dpToPx(8),
                0,
                dpToPx(36)
            )
        }

        rootLayout.addView(subtitle)

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 1. Enable Keyboard
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        val btnEnable = createActionButton(
            "1. Enable Keyboard",
            Color.parseColor("#07F57E"),
            COLOR_BLACK
        ) {
            val intent = Intent(
                Settings.ACTION_INPUT_METHOD_SETTINGS
            )

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            startActivity(intent)
        }

        rootLayout.addView(btnEnable)

        rootLayout.addView(
            createSpacer(14)
        )

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 2. Select X Board
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        val btnSelect = createActionButton(
            "2. Select X Board",
            COLOR_DARK_GRAY,
            COLOR_WHITE
        ) {
            val imm = getSystemService(
                Context.INPUT_METHOD_SERVICE
            ) as? InputMethodManager

            imm?.showInputMethodPicker()
        }

        rootLayout.addView(btnSelect)

        rootLayout.addView(
            createSpacer(14)
        )

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // 3. Details
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        val btnDetails = createActionButton(
            "3. Details",
            COLOR_DARK_GRAY,
            Color.parseColor("#07F57E")
        ) {
            showDetailsDialog()
        }

        rootLayout.addView(btnDetails)

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // Flexible Spacer
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        val flexibleSpacer = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f
            )
        }

        rootLayout.addView(flexibleSpacer)

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // Footer Card
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        val footerCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            background = createRoundedDrawable(
                COLOR_DARK_GRAY,
                8f
            )

            setPadding(
                dpToPx(16),
                dpToPx(16),
                dpToPx(16),
                dpToPx(16)
            )

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // Footer Text
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        val footerText = TextView(this).apply {

            text = """
                Creator: Avishka_X • Version: v1.0.0
                Status: Under Development
            """.trimIndent()

            setTextColor(COLOR_WHITE)

            alpha = 0.85f

            setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                12f
            )

            gravity = Gravity.CENTER

            setLineSpacing(
                dpToPx(4).toFloat(),
                1.0f
            )
        }

        footerCard.addView(footerText)

        rootLayout.addView(footerCard)

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // Set Content View
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        setContentView(rootLayout)
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Action Button
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    private fun createActionButton(
        label: String,
        bgColor: Int,
        textColor: Int,
        onClick: () -> Unit
    ): Button {

        return Button(this).apply {

            text = label

            setTextColor(textColor)

            setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                15f
            )

            typeface = Typeface.DEFAULT_BOLD

            isAllCaps = false

            background = createRoundedDrawable(
                bgColor,
                8f
            )

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(52)
            )

            setOnClickListener {
                onClick()
            }
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Details Dialog
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    private fun showDetailsDialog() {

        val message = """
╭━━━━━━━━━━━━━━━━━━━━━━━━━━╮
┃      💚  KEYBOARD APP  💚
╰━━━━━━━━━━━━━━━━━━━━━━━━━━╯

┏━━━━━━━━━━━➤ ◍ ◉ ➤
│  ✦ Creator : Avishka_X
│  ✦ Version : v1.0.0
┗━━━━━━━━━━━━━━➤ ◍ ◉ ➤

┏━━━━━━━━━━━➤ ◍ ◉ ➤
│  ⚠️ DEVELOPMENT NOTE
│
│  This keyboard is still under development
│  and is not fully completed yet.
│
│  🚀 More features, improvements & updates
│     will be added in future versions.
│
│  💚 Stay tuned for upcoming updates!
┗━━━━━━━━━━━━━━➤ ◍ ◉ ➤

    ──「 Thank You 」──
   💚 powered by Avishka_X 💚
        """.trimIndent()

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // Green Text - #07F57E
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        val spannable = SpannableString(message)

        spannable.setSpan(
            ForegroundColorSpan(
                Color.parseColor("#07F57E")
            ),
            0,
            spannable.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // Dialog Message View
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        val messageView = TextView(this).apply {

            text = spannable

            setTextColor(
                Color.parseColor("#07F57E")
            )

            setTextSize(
                TypedValue.COMPLEX_UNIT_SP,
                13f
            )

            gravity = Gravity.CENTER

            setLineSpacing(
                dpToPx(3).toFloat(),
                1.0f
            )

            setPadding(
                dpToPx(8),
                dpToPx(8),
                dpToPx(8),
                dpToPx(8)
            )
        }

        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
        // Create Dialog
        // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

        val dialog = AlertDialog.Builder(this)
            .setTitle("X-BOARD Information")
            .setView(messageView)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.setOnShowListener {

            // Dialog Background → Black
            dialog.window?.setBackgroundDrawable(
                ColorDrawable(
                    Color.parseColor("#000000")
                )
            )

            // Dialog Title → #07F57E
            val titleView = dialog.findViewById<TextView>(
                androidx.appcompat.R.id.alertTitle
            )

            titleView?.setTextColor(
                Color.parseColor("#07F57E")
            )

            // OK Button → #07F57E
            dialog.getButton(
                AlertDialog.BUTTON_POSITIVE
            ).setTextColor(
                Color.parseColor("#07F57E")
            )
        }

        dialog.show()
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Spacer
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    private fun createSpacer(dp: Int): View {

        return View(this).apply {

            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(dp)
            )
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // Rounded Drawable
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    private fun createRoundedDrawable(
        bgColor: Int,
        radiusDp: Float
    ): GradientDrawable {

        return GradientDrawable().apply {

            shape = GradientDrawable.RECTANGLE

            setColor(bgColor)

            cornerRadius = dpToPx(
                radiusDp.toInt()
            ).toFloat()
        }
    }

    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
    // DP → PX
    // ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

    private fun dpToPx(dp: Int): Int {

        return (
            dp * resources.displayMetrics.density
        ).toInt()
    }
}
