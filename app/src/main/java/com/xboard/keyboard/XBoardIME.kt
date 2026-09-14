package com.xboard.keyboard

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import java.util.Locale

class XBoardIME : InputMethodService() {

    private lateinit var mainContainer: LinearLayout
    private lateinit var candidateStrip: HorizontalScrollView
    private lateinit var candidateContainer: LinearLayout
    private lateinit var keyboardLayout: LinearLayout
    private lateinit var statusText: TextView

    private var isShifted = false
    private var isSymbols = false
    private val currentWordBuffer = StringBuilder()

    // STRICT COLOR PALETTE: Only Black, White, DarkGray, #07F57E
    private val COLOR_BLACK = Color.parseColor("#000000")
    private val COLOR_WHITE = Color.parseColor("#FFFFFF")
    private val COLOR_DARK_GRAY = Color.parseColor("#212121")
    private val COLOR_SPECIAL_DARK_GRAY = Color.parseColor("#2F2F2F")
    private val COLOR_ACCENT_GREEN = Color.parseColor("#07F57E")

    private val commonWords = listOf(
        "the", "be", "to", "of", "and", "a", "in", "that", "have", "i",
        "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
        "this", "but", "his", "by", "from", "they", "we", "say", "her", "she",
        "or", "an", "will", "my", "one", "all", "would", "there", "their", "what",
        "so", "up", "out", "if", "about", "who", "get", "which", "go", "me",
        "when", "make", "can", "like", "time", "no", "just", "him", "know", "take",
        "people", "into", "year", "your", "good", "some", "could", "them", "see", "other",
        "than", "then", "now", "look", "only", "come", "its", "over", "think", "also",
        "back", "after", "use", "two", "how", "our", "work", "first", "well", "way",
        "even", "new", "want", "because", "any", "these", "give", "day", "most", "us",
        "please", "thanks", "hello", "today", "tomorrow", "tonight", "great", "nice",
        "love", "happy", "yes", "sure", "fine", "cool", "alright", "sorry", "call", "send"
    )

    private val qwertyRows = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
        listOf("⇧", "z", "x", "c", "v", "b", "n", "m", "⌫"),
        listOf("?123", ",", "SPACE", ".", "↵")
    )

    private val symbolRows = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
        listOf("@", "#", "$", "%", "&", "-", "+", "(", ")", "/"),
        listOf("=", "*", "\"", "'", ":", ";", "!", "?", "⌫"),
        listOf("ABC", "_", "SPACE", "/", "↵")
    )

    override fun onCreateInputView(): View {
        mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(COLOR_BLACK)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(8, 8, 8, 12)
        }

        val headerRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(28)
            )
            setPadding(12, 0, 12, 0)
        }

        val brandLabel = TextView(this).apply {
            text = "X-BOARD"
            setTextColor(COLOR_ACCENT_GREEN)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        headerRow.addView(brandLabel)

        statusText = TextView(this).apply {
            text = "FAST TYPING"
            setTextColor(COLOR_WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
        }
        headerRow.addView(statusText)
        mainContainer.addView(headerRow)

        candidateStrip = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(40)
            ).apply {
                setMargins(4, 2, 4, 6)
            }
            background = createRoundedDrawable(COLOR_DARK_GRAY, 12f)
        }

        candidateContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(8, 0, 8, 0)
        }
        candidateStrip.addView(candidateContainer)
        mainContainer.addView(candidateStrip)

        keyboardLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(215)
            )
        }
        mainContainer.addView(keyboardLayout)

        updateCandidateStrip(emptyList())
        renderKeyboard()

        return mainContainer
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        currentWordBuffer.clear()
        isShifted = false
        isSymbols = false
        updateCandidateStrip(emptyList())
        renderKeyboard()
    }

    override fun onFinishInput() {
        super.onFinishInput()
        currentWordBuffer.clear()
        updateCandidateStrip(emptyList())
    }

    private fun renderKeyboard() {
        keyboardLayout.removeAllViews()
        val rows = if (isSymbols) symbolRows else qwertyRows

        for (rowKeys in rows) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1.0f
                )
            }

            for (key in rowKeys) {
                val weight = when (key) {
                    "SPACE" -> 4.0f
                    "⇧", "⌫" -> 1.5f
                    "?123", "ABC", "↵" -> 1.5f
                    else -> 1.0f
                }

                val keyLabel = if (!isSymbols && isShifted && key.length == 1) {
                    key.uppercase(Locale.ROOT)
                } else {
                    key
                }

                val button = createKeyButton(key, keyLabel, weight)
                row.addView(button)
            }

            keyboardLayout.addView(row)
        }
    }

    private fun createKeyButton(rawKey: String, displayLabel: String, weight: Float): Button {
        val isAccentGreen = rawKey in listOf("↵", "SPACE", "⇧")
        val displayText = if (rawKey == "SPACE") "X BOARD" else displayLabel

        return Button(this).apply {
            text = displayText
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                weight
            ).apply {
                setMargins(4, 4, 4, 4)
            }

            if (isAccentGreen) {
                background = createRoundedDrawable(COLOR_ACCENT_GREEN, 14f)
                setTextColor(COLOR_BLACK)
                typeface = Typeface.DEFAULT_BOLD
            } else if (rawKey in listOf("⌫", "?123", "ABC")) {
                background = createRoundedDrawable(COLOR_SPECIAL_DARK_GRAY, 14f)
                setTextColor(COLOR_WHITE)
            } else {
                background = createRoundedDrawable(COLOR_DARK_GRAY, 14f)
                setTextColor(COLOR_WHITE)
            }

            isAllCaps = false
            setPadding(0, 0, 0, 0)

            when {
                rawKey == "SPACE" -> {
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                }
                rawKey.length > 2 -> setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                rawKey in listOf("⇧", "⌫", "?123", "ABC", "↵") -> setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                else -> setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            }

            setOnClickListener {
                vibrate(15)
                handleKey(rawKey)
            }

            if (rawKey == "⌫") {
                setOnLongClickListener {
                    vibrate(50)
                    clearAllText()
                    true
                }
            }
        }
    }

    private fun updateCandidateStrip(suggestions: List<String>) {
        candidateContainer.removeAllViews()
        if (suggestions.isEmpty()) {
            val hint = TextView(this).apply {
                text = "Type words to predict • Long-press ⌫ to clear all"
                setTextColor(COLOR_WHITE)
                alpha = 0.6f
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                setPadding(16, 6, 16, 6)
            }
            candidateContainer.addView(hint)
            return
        }

        for ((index, word) in suggestions.take(5).withIndex()) {
            val isTopSuggestion = (index == 0)
            val view = TextView(this).apply {
                text = word
                setTextSize(TypedValue.COMPLEX_UNIT_SP, if (isTopSuggestion) 14f else 13f)
                
                if (isTopSuggestion) {
                    setTextColor(COLOR_BLACK)
                    typeface = Typeface.DEFAULT_BOLD
                    background = createRoundedDrawable(COLOR_ACCENT_GREEN, 8f)
                } else {
                    setTextColor(COLOR_WHITE)
                    background = createRoundedDrawable(COLOR_DARK_GRAY, 8f)
                }

                setPadding(28, 8, 28, 8)
                isClickable = true
                isFocusable = true

                setOnClickListener {
                    vibrate(15)
                    commitSuggestion(word)
                }
            }
            candidateContainer.addView(view)
        }
    }

    private fun commitSuggestion(word: String) {
        val ic = currentInputConnection ?: return
        val currentWord = currentWordBuffer.toString()
        if (currentWord.isNotEmpty()) {
            ic.deleteSurroundingText(currentWord.length, 0)
        }
        ic.commitText("$word ", 1)
        currentWordBuffer.clear()
        updateCandidateStrip(emptyList())
    }

    private fun handleKey(label: String) {
        val ic = currentInputConnection ?: return

        when (label) {
            "⌫" -> {
                if (currentWordBuffer.isNotEmpty()) {
                    currentWordBuffer.deleteCharAt(currentWordBuffer.length - 1)
                    val suggestions = predictWords(currentWordBuffer.toString())
                    updateCandidateStrip(suggestions)
                }
                ic.deleteSurroundingText(1, 0)
            }
            "⇧" -> {
                isShifted = !isShifted
                renderKeyboard()
            }
            "?123" -> {
                isSymbols = true
                renderKeyboard()
            }
            "ABC" -> {
                isSymbols = false
                renderKeyboard()
            }
            "SPACE" -> {
                ic.commitText(" ", 1)
                currentWordBuffer.clear()
                updateCandidateStrip(emptyList())
            }
            "↵" -> {
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                currentWordBuffer.clear()
                updateCandidateStrip(emptyList())
            }
            else -> {
                val output = if (!isSymbols && isShifted && label.length == 1) {
                    label.uppercase(Locale.ROOT)
                } else {
                    label
                }
                ic.commitText(output, 1)

                if (label.length == 1 && label[0].isLetter()) {
                    currentWordBuffer.append(output)
                    val suggestions = predictWords(currentWordBuffer.toString())
                    updateCandidateStrip(suggestions)
                } else {
                    currentWordBuffer.clear()
                    updateCandidateStrip(emptyList())
                }

                if (isShifted) {
                    isShifted = false
                    renderKeyboard()
                }
            }
        }
    }

    private fun predictWords(prefix: String): List<String> {
        if (prefix.isBlank()) return emptyList()
        val lowerPrefix = prefix.lowercase(Locale.ROOT)
        val matched = commonWords.filter { it.startsWith(lowerPrefix) && it != lowerPrefix }
        val isCap = prefix[0].isUpperCase()

        val list = mutableListOf<String>()
        for (w in matched.take(5)) {
            if (isCap) {
                list.add(w.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() })
            } else {
                list.add(w)
            }
        }
        if (list.isEmpty() && prefix.length > 1) {
            list.add(prefix)
        }
        return list
    }

    private fun clearAllText() {
        val ic = currentInputConnection ?: return
        val before = ic.getTextBeforeCursor(2000, 0) ?: ""
        val after = ic.getTextAfterCursor(2000, 0) ?: ""
        ic.deleteSurroundingText(before.length, after.length)
        currentWordBuffer.clear()
        updateCandidateStrip(emptyList())
    }

    private fun vibrate(durationMs: Long = 15) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {
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
