package io.prune.screenshot.domain.model

/**
 * Lightweight smart labels generated from simple heuristics.
 */
enum class SmartCategory(val displayName: String) {
    Meme("Memes"),
    Receipt("Receipts"),
    Chat("Chats"),
    Other("Other"),
    Unknown("Unclassified")
}

