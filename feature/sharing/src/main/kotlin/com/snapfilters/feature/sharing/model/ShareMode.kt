package com.snapfilters.feature.sharing.model

/**
 * ShareMode represents the two deliberately distinct sharing paths
 * defined in PRD v3.0 Chapter 17 (FR-SHR-02).
 */
enum class ShareMode(val displayName: String, val description: String) {
    PHOTO(
        displayName = "Share Photo",
        description = "Photo: optimised for chat apps."
    ),
    ORIGINAL_FILE(
        displayName = "Share Original File",
        description = "Original file: full quality, larger size. The receiving app may compress this image."
    )
}

object ShareCopyStrings {
    // Approved copy per Chapter 17
    const val PHOTO_EXPLANATION = "Photo: optimised for chat apps."
    const val ORIGINAL_EXPLANATION = "Original file: full quality, larger size."
    const val RECEIVER_DISCLAIMER = "The receiving app may compress this image."

    // Banned claims per FR-SHR-03
    val FORBIDDEN_CLAIMS = listOf(
        "Lossless sharing to WhatsApp",
        "WhatsApp keeps your original quality",
        "Sent in HD"
    )
}
