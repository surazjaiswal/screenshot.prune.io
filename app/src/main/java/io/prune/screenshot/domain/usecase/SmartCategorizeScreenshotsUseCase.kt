package io.prune.screenshot.domain.usecase

import io.prune.screenshot.domain.model.Screenshot
import io.prune.screenshot.domain.model.SmartCategory

/**
 * Applies lightweight heuristics to generate smart labels for screenshots.
 *
 * The heuristics prefer simple filename-based rules so the logic remains
 * deterministic and easy to evolve (or swap out for ML-kit later).
 */
class SmartCategorizeScreenshotsUseCase {

    operator fun invoke(screenshots: List<Screenshot>): List<Screenshot> =
        screenshots.map { screenshot ->
            val category = classify(screenshot)
            if (category == screenshot.smartCategory) {
                screenshot
            } else {
                screenshot.copy(smartCategory = category)
            }
        }

    private fun classify(screenshot: Screenshot): SmartCategory {
        val candidates = buildString {
            appendLine(screenshot.displayName)
            appendLine(screenshot.appName)
        }.lowercase()

        return when {
            containsAny(candidates, memeKeywords) -> SmartCategory.Meme
            containsAny(candidates, receiptKeywords) -> SmartCategory.Receipt
            containsAny(candidates, chatKeywords) -> SmartCategory.Chat
            screenshot.sizeBytes >= LARGE_FILE_THRESHOLD_BYTES -> SmartCategory.Other
            else -> SmartCategory.Unknown
        }
    }

    private fun containsAny(source: String, keywords: Set<String>): Boolean =
        keywords.any { keyword -> keyword in source }

    private companion object {
        const val LARGE_FILE_THRESHOLD_BYTES: Long = 2_500_000 // ~2.5 MB

        val memeKeywords = setOf(
            "meme", "lol", "funny", "reaction", "sticker"
        )
        val receiptKeywords = setOf(
            "receipt", "invoice", "bill", "payment", "order", "purchase"
        )
        val chatKeywords = setOf(
            "chat", "conversation", "whatsapp", "telegram", "messenger", "sms"
        )
    }
}

