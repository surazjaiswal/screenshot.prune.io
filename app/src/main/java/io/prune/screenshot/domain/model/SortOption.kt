package io.prune.screenshot.domain.model

/**
 * Sorting options available to the user.
 */
enum class SortOption(val displayName: String) {
    NewestFirst("Newest first"),
    OldestFirst("Oldest first"),
    LargestFirst("Largest first"),
    SmallestFirst("Smallest first");

    override fun toString(): String = displayName
}

