package com.xboard.keyboard

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat

enum class ShiftMode {
    OFF, SHIFT, CAPS_LOCK
}

enum class KeyBoardMode {
    LETTERS, SYMBOLS
}

class XBoardIME : InputMethodService() {

    private var shiftMode = ShiftMode.OFF
    private var keyboardMode = KeyBoardMode.LETTERS
    private val wordPredictor = WordPredictor()
    private var lastShiftPressTime = 0L

    private lateinit var rootView: LinearLayout
    private lateinit var rowsContainer: LinearLayout
    private lateinit var suggest1: TextView
    private lateinit var suggest2: TextView
    private lateinit var suggest3: TextView
    private var vibrator: Vibrator? = null

    // Letters and their secondary hint symbols exactly from screenshot
    private val letterRow1 = listOf("Q" to "1", "W" to "2", "E" to "3", "R" to "4", "T" to "5", "Y" to "6", "U" to "7", "I" to "8", "O" to "9", "P" to "0")
    private val letterRow2 = listOf("A" to "@", "S" to "#", "D" to "&", "F" to "*", "G" to "-", "H" to "+", "J" to "=", "K" to "(", "L" to ")")
    private val letterRow3 = listOf("Z" to "_", "X" to "\"", "C" to "'", "V" to ":", "B" to ";", "N" to "/", "M" to "!")

    override fun onCreate() {
        super.onCreate()
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    override fun onCreateInputView(): View {
        rootView = layoutInflater.inflate(R.layout.keyboard_view, null) as LinearLayout
        rowsContainer = rootView.findViewById(R.id.rows_container)
        suggest1 = rootView.findViewById(R.id.suggest_1)
        suggest2 = rootView.findViewById(R.id.suggest_2)
        suggest3 = rootView.findViewById(R.id.suggest_3)

        val btnClear = rootView.findViewById<ImageView>(R.id.btn_clear_input)
        btnClear.setOnClickListener {
            currentInputConnection?.deleteSurroundingText(100, 100)
            updateSuggestions()
        }

        setupSuggestionClick(suggest1)
        setupSuggestionClick(suggest2)
        setupSuggestionClick(suggest3)

        renderKeyboardLayout()
        updateSuggestions()

        return rootView
    }

    private fun setupSuggestionClick(textView: TextView) {
        textView.setOnClickListener {
            val text = textView.text.toString()
            if (text.isNotBlank()) {
                triggerHaptic()
                commitSuggestion(text)
            }
        }
    }

    private fun commitSuggestion(word: String) {
        val ic = currentInputConnection ?: return
        val before = ic.getTextBeforeCursor(30, 0)?.toString() ?: ""
        val lastWordIndex = before.lastIndexOfAny(charArrayOf(' ', '\n', '\t'))
        val deleteCount = if (lastWordIndex == -1) before.length else before.length - lastWordIndex - 1

        if (deleteCount > 0) {
            ic.deleteSurroundingText(deleteCount, 0)
        }
        ic.commitText("$word ", 1)
        updateSuggestions()
    }

    private fun renderKeyboardLayout() {
        rowsContainer.removeAllViews()

        if (keyboardMode == KeyBoardMode.LETTERS) {
            buildLetterRow(letterRow1)
            buildLetterRow(letterRow2)
            buildRow3Letters()
            buildRow4()
        } else {
            buildSymbolsLayout()
        }
    }

    private fun buildLetterRow(keys: List<Pair<String, String>>) {
        val rowLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(56)
            ).apply { setMargins(0, dpToPx(3), 0, dpToPx(3)) }
        }

        for ((char, hint) in keys) {
            val keyView = createKeyView(char, hint, 1f) {
                val isUpper = shiftMode != ShiftMode.OFF
                val textToCommit = if (isUpper) char.uppercase() else char.lowercase()
                currentInputConnection?.commitText(textToCommit, 1)

                if (shiftMode == ShiftMode.SHIFT) {
                    shiftMode = ShiftMode.OFF
                    renderKeyboardLayout()
                }
                updateSuggestions()
            }
            rowLayout.addView(keyView)
        }
        rowsContainer.addView(rowLayout)
    }

    private fun buildRow3Letters() {
        val rowLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(56)
            ).apply { setMargins(0, dpToPx(3), 0, dpToPx(3)) }
        }

        // Shift Key (Neon Green)
        val shiftKey = createSpecialKey(
            label = when (shiftMode) {
                ShiftMode.CAPS_LOCK -> "⇪"
                ShiftMode.SHIFT -> "⇧"
                ShiftMode.OFF -> "⇧"
            },
            weight = 1.4f,
            isAccent = true
        ) {
            val now = System.currentTimeMillis()
            shiftMode = when {
                shiftMode == ShiftMode.CAPS_LOCK -> ShiftMode.OFF
                shiftMode == ShiftMode.SHIFT && (now - lastShiftPressTime < 400) -> ShiftMode.CAPS_LOCK
                shiftMode == ShiftMode.SHIFT -> ShiftMode.OFF
                else -> ShiftMode.SHIFT
            }
            lastShiftPressTime = now
            renderKeyboardLayout()
        }
        rowLayout.addView(shiftKey)

        // Letters Z-M
        for ((char, hint) in letterRow3) {
            val keyView = createKeyView(char, hint, 1f) {
                val isUpper = shiftMode != ShiftMode.OFF
                val textToCommit = if (isUpper) char.uppercase() else char.lowercase()
                currentInputConnection?.commitText(textToCommit, 1)

                if (shiftMode == ShiftMode.SHIFT) {
                    shiftMode = ShiftMode.OFF
                    renderKeyboardLayout()
                }
                updateSuggestions()
            }
            rowLayout.addView(keyView)
        }

        // Backspace Key: if text is selected, deletes selection; else deletes 1 character
        val backspaceKey = createSpecialKey("⌫", 1.4f, isAccent = false) {
            val ic = currentInputConnection
            if (ic != null) {
                val selected = ic.getSelectedText(0)
                if (!selected.isNullOrEmpty()) {
                    // Deletes selected text
                    ic.commitText("", 1)
                } else {
                    ic.deleteSurroundingText(1, 0)
                }
            }
            updateSuggestions()
        }
        rowLayout.addView(backspaceKey)

        rowsContainer.addView(rowLayout)
    }

    private fun buildRow4() {
        val rowLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dpToPx(56)
            ).apply { setMargins(0, dpToPx(3), 0, dpToPx(3)) }
        }

        // 123 switch key
        rowLayout.addView(createSpecialKey(if (keyboardMode == KeyBoardMode.LETTERS) "123" else "ABC", 1.2f, false) {
            keyboardMode = if (keyboardMode == KeyBoardMode.LETTERS) KeyBoardMode.SYMBOLS else KeyBoardMode.LETTERS
            renderKeyboardLayout()
        })

        // Emoji key
        rowLayout.addView(createSpecialKey("😊", 1.0f, false) {
            currentInputConnection?.commitText("😊", 1)
        })

        // Comma key
        rowLayout.addView(createKeyView(",", "🎙", 1.0f) {
            currentInputConnection?.commitText(",", 1)
            updateSuggestions()
        })

        // Space bar: "X BOARD" in neon green
        val spaceBar = createSpecialKey("X BOARD", 3.8f, isAccent = true) {
            currentInputConnection?.commitText(" ", 1)
            updateSuggestions()
        }
        rowLayout.addView(spaceBar)

        // Period key
        rowLayout.addView(createKeyView(".", ",!?", 1.0f) {
            currentInputConnection?.commitText(".", 1)
            updateSuggestions()
        })

        // Enter key in neon green
        rowLayout.addView(createSpecialKey("↵", 1.2f, isAccent = true) {
            sendKeyChar('\n')
            updateSuggestions()
        })

        rowsContainer.addView(rowLayout)
    }

    private fun buildSymbolsLayout() {
        val symRow1 = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
        val symRow2 = listOf("@", "#", "$", "%", "&", "*", "-", "+", "(", ")")
        val symRow3 = listOf("!", "\"", "'", ":", ";", "/", "?")

        val r1 = createSimpleRow(symRow1)
        val r2 = createSimpleRow(symRow2)

        val r3 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(56))
        }
        r3.addView(createSpecialKey("=\\<", 1.4f, false) {})
        for (sym in symRow3) {
            r3.addView(createSpecialKey(sym, 1f, false) {
                currentInputConnection?.commitText(sym, 1)
            })
        }
        r3.addView(createSpecialKey("⌫", 1.4f, false) {
            currentInputConnection?.deleteSurroundingText(1, 0)
        })

        rowsContainer.addView(r1)
        rowsContainer.addView(r2)
        rowsContainer.addView(r3)
        buildRow4()
    }

    private fun createSimpleRow(chars: List<String>): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(56))
            chars.forEach { char ->
                addView(createSpecialKey(char, 1f, false) {
                    currentInputConnection?.commitText(char, 1)
                })
            }
        }
    }

    private fun createKeyView(primary: String, hint: String, weight: Float, onClick: () -> Unit): View {
        val frame = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, weight).apply {
                setMargins(dpToPx(2), 0, dpToPx(2), 0)
            }
            background = ContextCompat.getDrawable(this@XBoardIME, R.drawable.bg_key_normal)
            isClickable = true
            isFocusable = true
        }

        val hintView = TextView(this).apply {
            text = hint
            textSize = 10sp
            setTextColor(ContextCompat.getColor(this@XBoardIME, R.color.key_hint_symbol))
            gravity = Gravity.CENTER_HORIZONTAL or Gravity.TOP
            setPadding(0, dpToPx(3), 0, 0)
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val isUpper = shiftMode != ShiftMode.OFF
        val mainTextView = TextView(this).apply {
            text = if (isUpper) primary.uppercase() else primary.lowercase()
            textSize = 18sp
            setTextColor(ContextCompat.getColor(this@XBoardIME, R.color.key_text_primary))
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        frame.addView(hintView)
        frame.addView(mainTextView)

        frame.setOnClickListener {
            triggerHaptic()
            onClick()
        }

        return frame
    }

    private fun createSpecialKey(label: String, weight: Float, isAccent: Boolean, onClick: () -> Unit): View {
        val key = TextView(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, weight).apply {
                setMargins(dpToPx(2), 0, dpToPx(2), 0)
            }
            text = label
            textSize = if (label.length > 3) 14sp else 18sp
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true

            if (isAccent) {
                background = ContextCompat.getDrawable(this@XBoardIME, R.drawable.bg_key_accent)
                setTextColor(ContextCompat.getColor(this@XBoardIME, R.color.accent_text_dark))
                paint.isFakeBoldText = true
            } else {
                background = ContextCompat.getDrawable(this@XBoardIME, R.drawable.bg_key_normal)
                setTextColor(ContextCompat.getColor(this@XBoardIME, R.color.key_text_primary))
            }
        }

        key.setOnClickListener {
            triggerHaptic()
            onClick()
        }

        return key
    }

    private fun updateSuggestions() {
        val textBefore = currentInputConnection?.getTextBeforeCursor(50, 0)?.toString()
        val suggestions = wordPredictor.getSuggestions(textBefore)
        suggest1.text = suggestions.getOrNull(0) ?: ""
        suggest2.text = suggestions.getOrNull(1) ?: ""
        suggest3.text = suggestions.getOrNull(2) ?: ""
    }

    private fun triggerHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(15, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(15)
            }
        } catch (_: Exception) {}
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }
}
