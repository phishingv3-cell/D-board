package com.xboard.keyboard

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.text.TextUtils
import android.util.TypedValue
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.util.Locale

class XBoardIME : InputMethodService() {

    private lateinit var keyboardRoot: View
    private lateinit var keyboardLayout: LinearLayout
    private lateinit var candidateStrip: HorizontalScrollView
    private lateinit var candidateContainer: LinearLayout
    private lateinit var statusLanguage: TextView
    private lateinit var btnSettings: View

    private var isShifted = false
    private var isSymbols = false
    private val currentWordBuffer = StringBuilder()
    private val wordPredictor = WordPredictor()

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

    override fun onCreate() {
        super.onCreate()
    }

    override fun onCreateInputView(): View {
        keyboardRoot = layoutInflater.inflate(R.layout.keyboard_view, null)
        keyboardLayout = keyboardRoot.findViewById(R.id.keyboard_layout)
        candidateStrip = keyboardRoot.findViewById(R.id.candidate_strip)
        candidateContainer = keyboardRoot.findViewById(R.id.candidate_container)
        statusLanguage = keyboardRoot.findViewById(R.id.status_language)
        btnSettings = keyboardRoot.findViewById(R.id.btn_keyboard_settings)

        setupCandidateDefaults()
        renderKeyboard()

        btnSettings.setOnClickListener {
            // Switch between primary QWERTY and symbols or reset
            isSymbols = !isSymbols
            renderKeyboard()
        }

        return keyboardRoot
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        currentWordBuffer.clear()
        isShifted = false
        isSymbols = false
        setupCandidateDefaults()
        renderKeyboard()
    }

    override fun onFinishInput() {
        super.onFinishInput()
        currentWordBuffer.clear()
        setupCandidateDefaults()
    }

    private fun setupCandidateDefaults() {
        updateCandidateStrip(emptyList())
    }

    private fun renderKeyboard() {
        keyboardLayout.removeAllViews()
        val rows = if (isSymbols) symbolRows else qwertyRows

        for ((rowIndex, rowKeys) in rows.withIndex()) {
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

                val isSpecial = key in listOf("⇧", "⌫", "?123", "ABC", "↵")
                val keyLabel = if (!isSymbols && isShifted && key.length == 1) {
                    key.uppercase(Locale.ROOT)
                } else {
                    key
                }

                val button = createKeyButton(keyLabel, weight, isSpecial)
                row.addView(button)
            }

            keyboardLayout.addView(row)
        }
    }

    private fun createKeyButton(label: String, weight: Float, isAccent: Boolean = false): Button {
        return Button(this).apply {
            text = label
            layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.MATCH_PARENT,
                weight
            ).apply {
                setMargins(4, 4, 4, 4)
            }
            setBackgroundResource(if (isAccent) R.drawable.bg_key_accent else R.drawable.bg_key_normal)
            setTextColor(ContextCompat.getColor(context, if (isAccent) R.color.key_text_accent else R.color.key_text))
            isAllCaps = false
            setPadding(0, 0, 0, 0)
            
            // Text size adaptation
            when {
                label.length > 2 -> setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                label in listOf("⇧", "⌫", "123", "ABC", "↵", "CLR") -> setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                else -> setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            }

            setOnClickListener {
                vibrate()
                handleKey(label)
            }

            // Long click on backspace clears word or input
            if (label == "⌫") {
                setOnLongClickListener {
                    vibrate(60)
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
                text = "X-Board • Fast Typing"
                setTextColor(ContextCompat.getColor(context, R.color.suggestion_text_dim))
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
                setPadding(16, 8, 16, 8)
            }
            candidateContainer.addView(hint)
            return
        }

        for ((index, word) in suggestions.take(5).withIndex()) {
            val view = TextView(this).apply {
                text = word
                setTextSize(TypedValue.COMPLEX_UNIT_SP, if (index == 0) 15f else 14f)
                setTextColor(ContextCompat.getColor(
                    context,
                    if (index == 0) R.color.color_accent else R.color.key_text
                ))
                setPadding(24, 8, 24, 8)
                isClickable = true
                isFocusable = true
                setBackgroundResource(R.drawable.bg_suggestion_item)

                setOnClickListener {
                    vibrate(20)
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
                    val suggestions = wordPredictor.getPredictions(currentWordBuffer.toString())
                    updateCandidateStrip(suggestions)
                }
                ic.deleteSurroundingText(1, 0)
            }
            "CLR" -> {
                clearAllText()
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
                ic.commitText(label, 1)
                if (label.length == 1 && label[0].isLetter()) {
                    currentWordBuffer.append(label)
                    val suggestions = wordPredictor.getPredictions(currentWordBuffer.toString())
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
}
