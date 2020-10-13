package me.zeroeightsix.kami.util.graphics.font

import java.awt.Font

object TextProperties {
    @Suppress("UNUSED")
    enum class Style(val code: String, val codeChar: Char, val fontPath: String, val styleConst: Int) {
        REGULAR("§r", 'r', "/assets/kamiblue/fonts/Source_Sans_Pro/SourceSansPro-SemiBold.ttf", Font.PLAIN),
        BOLD("§l", 'l', "/assets/kamiblue/fonts/Source_Sans_Pro/SourceSansPro-Black.ttf", Font.BOLD),
        ITALIC("§o", 'o', "/assets/kamiblue/fonts/Source_Sans_Pro/SourceSansPro-SemiBoldItalic.ttf", Font.ITALIC)
    }

    @Suppress("UNUSED")
    enum class HAlign(val multiplier: Float) {
        LEFT(0f),
        CENTER(0.5f),
        RIGHT(1f)
    }

    @Suppress("UNUSED")
    enum class VAlign(val multiplier: Float) {
        TOP(0f),
        CENTER(0.5f),
        BOTTOM(1f)
    }
}