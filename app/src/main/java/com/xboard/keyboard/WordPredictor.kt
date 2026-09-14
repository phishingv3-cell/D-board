package com.xboard.keyboard

import java.util.Locale

class WordPredictor {

    private val commonWords = listOf(
        "the", "be", "to", "of", "and", "a", "in", "that", "have", "i",
        "it", "for", "not", "on", "with", "he", "as", "you", "do", "at",
        "this", "but", "his", "by", "from", "they", "we", "say", "her", "she",
        "or", "an", "will", "my", "one", "all", "would", "there", "their", "what",
        "so", "up", "out", "if", "about", "who", "get", "which", "go", "me",
        "when", "make", "can", "like", "time", "no", "just", "him", "know", "take",
        "people", "into", "year", "your", "good", "some", "could", "them", "see",
        "keyboard", "android", "phone", "message", "smart", "quick", "thanks", "let's"
    )

    private val nextWordMap = mapOf(
        "" to listOf("I", "Let's", "The"),
        "i" to listOf("am", "will", "have"),
        "you" to listOf("are", "can", "have"),
        "let's" to listOf("go", "do", "meet"),
        "the" to listOf("best", "new", "first")
    )

    fun getSuggestions(textBeforeCursor: String?): List<String> {
        if (textBeforeCursor.isNullOrBlank()) {
            return listOf("I", "Let's", "The")
        }

        val trimmed = textBeforeCursor.trimEnd()
        val words = trimmed.split(Regex("\\s+"))
        val endsWithSpace = textBeforeCursor.endsWith(" ")

        if (endsWithSpace) {
            val lastWord = words.lastOrNull()?.lowercase(Locale.ROOT) ?: ""
            return nextWordMap[lastWord] ?: listOf("the", "and", "to")
        }

        val currentPrefix = words.lastOrNull()?.lowercase(Locale.ROOT) ?: ""
        val matches = commonWords.filter { it.startsWith(currentPrefix) && it != currentPrefix }

        val isCapital = words.lastOrNull()?.firstOrNull()?.isUpperCase() == true

        if (matches.isEmpty()) {
            val fallback = if (isCapital) currentPrefix.replaceFirstChar { it.uppercase() } else currentPrefix
            return listOf(fallback, currentPrefix.uppercase(Locale.ROOT), currentPrefix + "s")
        }

        return matches.take(3).map {
            if (isCapital) it.replaceFirstChar { char -> char.uppercase() } else it
        }
    }
}
