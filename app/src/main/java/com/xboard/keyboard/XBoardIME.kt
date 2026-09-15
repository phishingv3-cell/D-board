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
    private lateinit var topToolbar: LinearLayout
    private lateinit var candidateStrip: HorizontalScrollView
    private lateinit var candidateContainer: LinearLayout
    private lateinit var keyboardLayout: LinearLayout

    // State Variables
    private var isShifted = false
    private var isCapsLock = false
    private var lastShiftClickTime = 0L

    private enum class Mode {
        QWERTY, SYMBOLS_1, SYMBOLS_2, EMOJI
    }
    private var currentMode = Mode.QWERTY

    private var currentFontStyleIndex = 0 // 0 to 19 (20 Styles)
    private val currentWordBuffer = StringBuilder()

    // STRICT COLOR PALETTE: Black, White, DarkGray, #07F57E
    private val COLOR_BLACK = Color.parseColor("#000000")
    private val COLOR_WHITE = Color.parseColor("#FFFFFF")
    private val COLOR_DARK_GRAY = Color.parseColor("#212121")
    private val COLOR_SPECIAL_DARK_GRAY = Color.parseColor("#2F2F2F")
    private val COLOR_ACCENT_GREEN = Color.parseColor("#07F57E")

    // Dynamic Auto-saved Vocabulary
    private val userLearnedWords = LinkedHashSet<String>()
    private val defaultVocabulary = listOf(
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

    // QWERTY Layout
    private val qwertyRows = listOf(
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
        listOf("⇧", "z", "x", "c", "v", "b", "n", "m", "⌫"),
        listOf("?123", ",", "SPACE", ".", "↵")
    )

    // Symbols Page 1
    private val symbolRows1 = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
        listOf("@", "#", "$", "%", "&", "-", "+", "(", ")", "/"),
        listOf("=\\<", "*", "\"", "'", ":", ";", "!", "?", "⌫"),
        listOf("ABC", "_", "SPACE", "/", "↵")
    )

    // Symbols Page 2
    private val symbolRows2 = listOf(
        listOf("[", "]", "{", "}", "#", "%", "^", "*", "+", "="),
        listOf("_", "\\", "|", "~", "<", ">", "€", "£", "¥", "¢"),
        listOf("?123", "©", "®", "¿", "¡", "°", "•", "`", "´", "⌫"),
        listOf("ABC", ",", "SPACE", ".", "↵")
    )

    // 20 Unicode Fancy Font Styles
    private val fontStyleNames = listOf(
        "Default", "𝗕𝗼𝗹𝗱", "𝘐𝘵𝘢𝘭𝘪𝘤", "𝘽𝙤𝙡𝙙 𝙄𝘵𝘢𝘭", "𝙼𝚘𝚗𝚘",
        "𝔊𝔬𝔱𝔥𝔦𝔠", "𝕲𝖔𝖙𝖍𝖎𝖈 𝕭", "𝒞𝓊𝓇𝓈𝒾𝓋", "𝓒𝓾𝓻𝓼𝓲𝓿 𝓑", "𝔻𝕠𝕦𝕓𝕝𝕖",
        "Ⓒⓘⓡⓒⓛⓔ", "🅒🅘🅡🅒🅛🅔", "🅂🅀🅄🄰🅁🄴", "🆂🆀🆄🅰🆁🅴", "SᴍᴀʟʟCᴀᴘ",
        "W i d e", "F l i p", "U̲n̲d̲e̲r̲", "S̶t̶r̶i̶k̶e̶", "⚡X-Style"
    )

    // USER COMPLETE EMOJI LIST
    private val allEmojis = listOf(
        // FACES & EMOTIONS
        "😀","😃","😄","😁","😆","😅","😂","🤣","🥲","☺️","😊","😇","🙂","🙃","🫠","😉","😌","😍","🥰","😘","😗","😙","😚","😋","😛","😝","😜","🤪","🤨","🧐","🤓","😎","🤩","🥳","🥸","🤑","🤗","🤭","🫢","🫣","🤫","🤔","🫡","🤐","🤥","😶","😐","😑","😏","😒","🙄","😬","🫨","😔","😪","🤤","😴","😷","🤒","🤕","🤢","🤮","🤧","🥵","🥶","🥴","😵","🤯","🤠","😕","🫤","😟","🙁","☹️","😮","😯","😲","😳","🥺","🥹","😦","😧","😨","😰","😥","😢","😭","😱","😖","😣","😞","😓","😩","😫","🥱","😤","😡","😠","🤬","😈","👿","💀","☠️","💩","🤡","👹","👺","👻","👽","👾","🤖",
        // HEARTS & LOVE
        "❤️","🩷","🧡","💛","💚","🩵","💙","💜","🤎","🖤","🩶","🤍","💔","❤️‍🔥","❤️‍🩹","💕","💞","💓","💗","💖","💝","💘","💌","❣️","💋","💯","💢","💥","💫","💦","💨","💬","💭","💤",
        // HANDS & BODY
        "👋","🤚","🖐️","✋","🖖","🫱","🫲","🫳","🫴","🫷","🫸","👌","🤌","🤏","✌️","🤞","🫰","🤟","🤘","🤙","👈","👉","👆","🖕","👇","☝️","🫵","👍","👎","✊","👊","🤛","🤜","👏","🙌","🫶","👐","🤲","🤝","🙏","✍️","💅","🤳","💪","🦾","🦿","🦵","🦶","👂","🦻","👃","🧠","🫀","🫁","🦷","🦴","👀","👁️","👅","👄","🫦",
        // PEOPLE
        "👶","🧒","👦","👧","🧑","👱","👨","🧔","👩","🧓","👴","👵","🙍","🙎","🙅","🙆","💁","🙋","🧏","🙇","🤦","🤷","👮","🕵️","💂","👷","🤴","👸","👳","👲","🧕","🤵","👰","🤰","🤱","🎅","🤶","🧙","🧚","🧛","🧜","🧝","🧞","🧟","🥷","🦸","🦹",
        // ACTIVITIES & SPORTS
        "🚶","🧍","🧎","🏃","💃","🕺","🧗","🤺","🏇","⛷️","🏂","🏌️","🏄","🚣","🏊","⛹️","🏋️","🚴","🚵","🤸","🤼","🤽","🤾","🤹","🧘","🛀","🛌","⚽","🥏","🎾","🏉","🏈","🏐","🏀","🥎","⚾","🎳","🏏","🏑","🏒","🥍","🏓","🏸","🥊","🥋","🛷","🎿","🎮","🕹️","🎲","🧩","♟️","🎯",
        // ANIMALS & BIRDS
        "🐵","🐒","🦍","🦧","🐶","🐕","🦮","🐩","🐺","🦊","🦝","🐱","🐈","🦁","🐯","🐅","🐆","🐴","🐎","🦄","🦓","🦌","🦬","🐮","🐂","🐃","🐄","🐷","🐖","🐗","🐽","🐏","🐑","🐐","🐪","🐫","🦙","🦒","🐘","🦣","🦏","🦛","🐭","🐁","🐀","🐹","🐰","🐇","🐿️","🦫","🦔","🦇","🐻","🐨","🐼","🦥","🦦","🦨","🦘","🦡","🐾","🦃","🐔","🐓","🐣","🐤","🐥","🐦","🐧","🕊️","🦅","🦆","🦢","🦉","🦤","🦩","🦚","🦜","🪿","🐸","🐊","🐢","🦎","🐍","🐲","🐉","🦕","🦖","🐳","🐋","🐬","🦭","🐟","🐠","🐡","🦈","🐙","🐚","🦀","🦞","🦐","🦑","🐛","🦋","🐌","🦂","🕷️","🦗","🐞","🐝","🐜","🪱","🦟",
        // NATURE & FOOD
        "🌱","🪴","🌿","☘️","🍀","🌷","🌸","🏵️","🌼","🌻","🌺","🥀","🌹","🌳","🌲","🌴","🌵","🍁","🍂","🍃","🌾","🍄","🍇","🍈","🍉","🍊","🍋","🍌","🍍","🥭","🍎","🍏","🍐","🍑","🍒","🍓","🫐","🥝","🫒","🥥","🥑","🍅","🍆","🥔","🥕","🌽","🌶️","🫑","🥜","🧅","🧄","🥦","🥒","🍔","🍟","🍕","🌭","🥪","🌮","🌯","🧀","🍳","🍿","🥞","🧇","🥩","🍗","🍖","🥓","🍜","🍝","🍣","🍱","🍛","🍙","🍚","🥟","🍤","🍦","🍧","🍨","🍩","🍪","🎂","🍰","🧁","🍫","🍬","🍭","☕","🫖","🍵","🥛","🧃","🥤","🧋","🍺","🍻","🥂","🍷","🥃","🍸",
        // TRAVEL, PLACES, VEHICLES
        "🌍","🌎","🌏","🌐","🗺️","🧭","🏔️","⛰️","🌋","🗻","🏕️","🏖️","🏜️","🏝️","🏞️","🏟️","🏛️","🏗️","🏘️","🏚️","🏠","🏡","🏢","🏣","🏤","🏥","🏦","🏨","🏩","🏪","🏫","🏬","🏭","🏯","🏰","💒","🗼","🗽","⛪","🕌","🛕","🕍","⛩️","🕋","⛲","⛺","🌁","🌃","🏙️","🌄","🌅","🌆","🌇","🌉","♨️","🎠","🎡","🎢","💈","🎪","🚂","🚃","🚄","🚅","🚆","🚇","🚈","🚉","🚊","🚝","🚞","🚋","🚌","🚍","🚎","🚐","🚑","🚒","🚓","🚔","🚕","🚖","🚗","🚘","🚙","🛻","🚚","🚛","🚜","🏎️","🏍️","🛵","🦽","🦼","🛺","🚲","🛴","🛹","🛼","🚏","🛣️","🛤️","🛢️","⛽","🚨","🚥","🚦","🛑","🚧","⚓","⛵","🛶","🚤","🚢","✈️","🛩️","🛫","🛬","🪂","💺","🚁","🚟","🚠","🚡","🛰️","🚀","🛸",
        // CELEBRATION, OBJECTS, SYMBOLS
        "🎉","🎊","🎈","🎁","🎀","🏆","🥇","🥈","🥉","🎖️","🎗️","🎫","🎟️","🎪","🎭","🎨","🎬","🎤","🎧","🎼","🎹","🥁","🎷","🎺","🎸","🪕","🎻","🎲","♟️","🎳","🎮","🎰","🧩","📱","📲","💻","⌨️","🖥️","🖨️","🖱️","📷","📸","📹","🎥","📽️","📞","☎️","📟","📠","📺","📻","🎙️","⏱️","⏲️","⏰","🕰️","⌛","⏳","📡","🔋","🔌","💡","🔦","🕯️","🧯","💵","💴","💶","💷","🪙","💰","💳","💎","⚖️","🪜","🧰","🔧","🔨","⚒️","⛏️","🔩","⚙️","⛓️","🧲","🔫","💣","🧨","🪓","🔪","🗡️","⚔️","🛡️","🚬","⚰️","🪦","⚱️","🏺","🔮","🧿","🪬","💈","🔬","🔭","📡","💉","🩸","💊","🩹","🩺","🚪","🛗","🪞","🪟","🛏️","🛋️","🪑","🚽","🪠","🚿","🛁","🪤","🪒","🧼","🪥","🧽","🧹","🧺","🧻","🗝️","🔑","🔒","🔓","🔏","🔐",
        // FLAGS
        "🏁","🚩","🎌","🏴","🏳️","🏳️‍🌈","🏳️‍⚧️","🏴‍☠️","🇺🇳","🇦🇺","🇧🇷","🇨🇦","🇨🇳","🇩🇪","🇪🇸","🇫🇷","🇬🇧","🇮🇳","🇮🇹","🇯🇵","🇰🇷","🇱🇰","🇲🇽","🇷🇺","🇺🇸"
    )

    override fun onCreateInputView(): View {
        mainContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(COLOR_BLACK)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(8, 4, 8, 10)
        }

        setupTopToolbar()
        mainContainer.addView(topToolbar)

        candidateStrip = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(38)
            ).apply {
                setMargins(4, 2, 4, 6)
            }
            background = createRoundedDrawable(COLOR_DARK_GRAY, 6f)
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

    private fun setupTopToolbar() {
        topToolbar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(34)
            ).apply {
                setMargins(4, 2, 4, 4)
            }
            setPadding(4, 0, 4, 0)
        }

        val brandLabel = TextView(this).apply {
            text = "X-BOARD"
            setTextColor(COLOR_ACCENT_GREEN)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            typeface = Typeface.DEFAULT_BOLD
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f)
        }
        topToolbar.addView(brandLabel)

        // Emoji Button
        val emojiButton = Button(this).apply {
            text = if (currentMode == Mode.EMOJI) "ABC" else "😊"
            setTextColor(COLOR_BLACK)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            typeface = Typeface.DEFAULT_BOLD
            isAllCaps = false
            background = createRoundedDrawable(COLOR_ACCENT_GREEN, 6f)
            layoutParams = LinearLayout.LayoutParams(dpToPx(42), dpToPx(30)).apply {
                setMargins(0, 0, 8, 0)
            }
            setPadding(0, 0, 0, 0)
            setOnClickListener {
                vibrate(15)
                currentMode = if (currentMode == Mode.EMOJI) Mode.QWERTY else Mode.EMOJI
                text = if (currentMode == Mode.EMOJI) "ABC" else "😊"
                renderKeyboard()
            }
        }
        topToolbar.addView(emojiButton)

        // Font Style Change Button (20 Styles)
        val fontButton = Button(this).apply {
            text = "𝔉 20"
            setTextColor(COLOR_BLACK)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            typeface = Typeface.DEFAULT_BOLD
            isAllCaps = false
            background = createRoundedDrawable(COLOR_ACCENT_GREEN, 6f)
            layoutParams = LinearLayout.LayoutParams(dpToPx(56), dpToPx(30))
            setPadding(0, 0, 0, 0)
            setOnClickListener {
                vibrate(20)
                currentFontStyleIndex = (currentFontStyleIndex + 1) % 20
                val styleName = fontStyleNames[currentFontStyleIndex]
                text = "𝔉 ${currentFontStyleIndex + 1}"
                updateCandidateStrip(listOf("Style [${currentFontStyleIndex + 1}/20]: $styleName"))
            }
        }
        topToolbar.addView(fontButton)
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        currentWordBuffer.clear()
        isShifted = false
        isCapsLock = false
        currentMode = Mode.QWERTY
        updateCandidateStrip(emptyList())
        renderKeyboard()
    }

    override fun onFinishInput() {
        super.onFinishInput()
        saveCurrentWordIfAny()
        currentWordBuffer.clear()
        updateCandidateStrip(emptyList())
    }

    private fun renderKeyboard() {
        keyboardLayout.removeAllViews()

        if (currentMode == Mode.EMOJI) {
            renderEmojiKeyboard()
            return
        }

        val rows = when (currentMode) {
            Mode.QWERTY -> qwertyRows
            Mode.SYMBOLS_1 -> symbolRows1
            Mode.SYMBOLS_2 -> symbolRows2
            else -> qwertyRows
        }

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
                    "⇧", "⇪", "⌫" -> 1.5f
                    "?123", "ABC", "=\\<", "↵" -> 1.5f
                    else -> 1.0f
                }

                val isShiftActive = (isShifted || isCapsLock)
                val keyLabel = if (currentMode == Mode.QWERTY && isShiftActive && key.length == 1) {
                    key.uppercase(Locale.ROOT)
                } else if (key == "⇧" && isCapsLock) {
                    "⇪"
                } else {
                    key
                }

                val button = createKeyButton(key, keyLabel, weight)
                row.addView(button)
            }

            keyboardLayout.addView(row)
        }
    }

    // Scrollable Multi-row Emoji Layout (Browses all 500+ emojis easily)
    private fun renderEmojiKeyboard() {
        val emojiScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = true
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f
            )
        }

        val rowsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }

        // Split all emojis into 3 rows for comfortable finger tapping
        val emojisPerRow = (allEmojis.size + 2) / 3
        for (r in 0..2) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    0,
                    1.0f
                )
            }

            val startIndex = r * emojisPerRow
            val endIndex = minOf(startIndex + emojisPerRow, allEmojis.size)

            for (i in startIndex until endIndex) {
                val emoji = allEmojis[i]
                val btn = TextView(this).apply {
                    text = emoji
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
                    gravity = Gravity.CENTER
                    layoutParams = LinearLayout.LayoutParams(dpToPx(44), ViewGroup.LayoutParams.MATCH_PARENT)
                    isClickable = true
                    isFocusable = true
                    setOnClickListener {
                        vibrate(10)
                        currentInputConnection?.commitText(emoji, 1)
                    }
                }
                row.addView(btn)
            }
            rowsContainer.addView(row)
        }
        emojiScroll.addView(rowsContainer)
        keyboardLayout.addView(emojiScroll)

        // Bottom control row for Emojis
        val bottomRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(44)
            )
        }

        val btnBackToAbc = Button(this).apply {
            text = "ABC"
            setTextColor(COLOR_BLACK)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            typeface = Typeface.DEFAULT_BOLD
            background = createRoundedDrawable(COLOR_ACCENT_GREEN, 6f)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.5f).apply {
                setMargins(4, 4, 4, 4)
            }
            setOnClickListener {
                vibrate(15)
                currentMode = Mode.QWERTY
                renderKeyboard()
            }
        }
        bottomRow.addView(btnBackToAbc)

        val btnSpace = Button(this).apply {
            text = "X BOARD"
            setTextColor(COLOR_BLACK)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            typeface = Typeface.DEFAULT_BOLD
            background = createRoundedDrawable(COLOR_ACCENT_GREEN, 6f)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 4.0f).apply {
                setMargins(4, 4, 4, 4)
            }
            setOnClickListener {
                vibrate(15)
                currentInputConnection?.commitText(" ", 1)
            }
        }
        bottomRow.addView(btnSpace)

        val btnDelete = Button(this).apply {
            text = "⌫"
            setTextColor(COLOR_WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            background = createRoundedDrawable(COLOR_SPECIAL_DARK_GRAY, 6f)
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.5f).apply {
                setMargins(4, 4, 4, 4)
            }
            setOnClickListener {
                vibrate(15)
                currentInputConnection?.deleteSurroundingText(1, 0)
            }
        }
        bottomRow.addView(btnDelete)

        keyboardLayout.addView(bottomRow)
    }

    private fun createKeyButton(rawKey: String, displayLabel: String, weight: Float): Button {
        val isAccentGreen = rawKey in listOf("↵", "SPACE", "⇧", "⇪")
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
                background = createRoundedDrawable(COLOR_ACCENT_GREEN, 6f)
                setTextColor(COLOR_BLACK)
                typeface = Typeface.DEFAULT_BOLD
            } else if (rawKey in listOf("⌫", "?123", "ABC", "=\\<")) {
                background = createRoundedDrawable(COLOR_SPECIAL_DARK_GRAY, 6f)
                setTextColor(COLOR_WHITE)
            } else {
                background = createRoundedDrawable(COLOR_DARK_GRAY, 6f)
                setTextColor(COLOR_WHITE)
            }

            isAllCaps = false
            setPadding(0, 0, 0, 0)

            when {
                rawKey == "SPACE" -> setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                rawKey.length > 2 -> setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
                rawKey in listOf("⇧", "⇪", "⌫", "?123", "ABC", "=\\<", "↵") -> setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
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

        for ((index, word) in suggestions.take(6).withIndex()) {
            val isTopSuggestion = (index == 0)
            val view = TextView(this).apply {
                text = word
                setTextSize(TypedValue.COMPLEX_UNIT_SP, if (isTopSuggestion) 14f else 13f)

                if (isTopSuggestion) {
                    setTextColor(COLOR_BLACK)
                    typeface = Typeface.DEFAULT_BOLD
                    background = createRoundedDrawable(COLOR_ACCENT_GREEN, 6f)
                } else {
                    setTextColor(COLOR_WHITE)
                    background = createRoundedDrawable(COLOR_DARK_GRAY, 6f)
                }

                setPadding(24, 8, 24, 8)
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
        userLearnedWords.add(word)
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
            "⇧", "⇪" -> {
                val now = System.currentTimeMillis()
                if (now - lastShiftClickTime < 400) {
                    isCapsLock = !isCapsLock
                    isShifted = isCapsLock
                } else {
                    if (isCapsLock) {
                        isCapsLock = false
                        isShifted = false
                    } else {
                        isShifted = !isShifted
                    }
                }
                lastShiftClickTime = now
                renderKeyboard()
            }
            "?123" -> {
                currentMode = Mode.SYMBOLS_1
                renderKeyboard()
            }
            "=\\<" -> {
                currentMode = Mode.SYMBOLS_2
                renderKeyboard()
            }
            "ABC" -> {
                currentMode = Mode.QWERTY
                renderKeyboard()
            }
            "SPACE" -> {
                saveCurrentWordIfAny()
                ic.commitText(" ", 1)
                currentWordBuffer.clear()
                updateCandidateStrip(emptyList())
            }
            "↵" -> {
                saveCurrentWordIfAny()
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                currentWordBuffer.clear()
                updateCandidateStrip(emptyList())
            }
            else -> {
                val isUpper = (isShifted || isCapsLock)
                val baseChar = if (currentMode == Mode.QWERTY && isUpper && label.length == 1) {
                    label.uppercase(Locale.ROOT)
                } else {
                    label
                }

                val transformedChar = if (currentMode == Mode.QWERTY && baseChar.length == 1 && baseChar[0].isLetter()) {
                    applyFontStyle(baseChar[0], currentFontStyleIndex)
                } else {
                    baseChar
                }

                ic.commitText(transformedChar, 1)

                if (baseChar.length == 1 && baseChar[0].isLetter()) {
                    currentWordBuffer.append(baseChar)
                    val suggestions = predictWords(currentWordBuffer.toString())
                    updateCandidateStrip(suggestions)
                } else {
                    currentWordBuffer.clear()
                    updateCandidateStrip(emptyList())
                }

                if (isShifted && !isCapsLock) {
                    isShifted = false
                    renderKeyboard()
                }
            }
        }
    }

    private fun saveCurrentWordIfAny() {
        val word = currentWordBuffer.toString().trim()
        if (word.length > 1) {
            userLearnedWords.add(word)
        }
    }

    private fun predictWords(prefix: String): List<String> {
        if (prefix.isBlank()) return emptyList()
        val lowerPrefix = prefix.lowercase(Locale.ROOT)

        val candidates = (userLearnedWords.reversed() + defaultVocabulary).distinct()
        val matched = candidates.filter { it.lowercase(Locale.ROOT).startsWith(lowerPrefix) && it.lowercase(Locale.ROOT) != lowerPrefix }
        val isCap = prefix[0].isUpperCase()

        val list = mutableListOf<String>()
        for (w in matched.take(6)) {
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

    private fun applyFontStyle(char: Char, styleIndex: Int): String {
        if (!char.isLetter()) return char.toString()
        val isUpper = char.isUpperCase()
        val offset = if (isUpper) char - 'A' else char - 'a'

        return when (styleIndex) {
            0 -> char.toString()
            1 -> String(Character.toChars(if (isUpper) 0x1D400 + offset else 0x1D41A + offset))
            2 -> if (char == 'h') "ℎ" else String(Character.toChars(if (isUpper) 0x1D434 + offset else 0x1D44E + offset))
            3 -> String(Character.toChars(if (isUpper) 0x1D468 + offset else 0x1D482 + offset))
            4 -> String(Character.toChars(if (isUpper) 0x1D670 + offset else 0x1D68A + offset))
            5 -> String(Character.toChars(if (isUpper) 0x1D504 + offset else 0x1D51E + offset))
            6 -> String(Character.toChars(if (isUpper) 0x1D56C + offset else 0x1D586 + offset))
            7 -> String(Character.toChars(if (isUpper) 0x1D49C + offset else 0x1D4B6 + offset))
            8 -> String(Character.toChars(if (isUpper) 0x1D4D0 + offset else 0x1D4EA + offset))
            9 -> String(Character.toChars(if (isUpper) 0x1D538 + offset else 0x1D552 + offset))
            10 -> String(Character.toChars(if (isUpper) 0x24B6 + offset else 0x24D0 + offset))
            11 -> String(Character.toChars(if (isUpper) 0x1F150 + offset else 0x1F165 + offset))
            12 -> String(Character.toChars(if (isUpper) 0x1F130 + offset else 0x1F145 + offset))
            13 -> String(Character.toChars(if (isUpper) 0x1F170 + offset else 0x1F185 + offset))
            14 -> {
                val smallCaps = "ᴀʙᴄᴅᴇғɢʜɪᴊᴋʟᴍɴᴏᴘǫʀsᴛᴜᴠᴡxʏᴢ"
                if (offset in 0..25) smallCaps[offset].toString() else char.toString()
            }
            15 -> "$char "
            16 -> {
                val flipped = "ɐqɔpǝɟɓɥᴉɾʞlɯudodbɹsʇnʌʍxʎz"
                if (offset in 0..25) flipped[offset].toString() else char.toString()
            }
            17 -> "${char}\u0332"
            18 -> "${char}\u0336"
            19 -> "${char}⚡"
            else -> char.toString()
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
