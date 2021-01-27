package me.zeroeightsix.kami.module.modules.client

import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.util.AsyncCachedValue
import me.zeroeightsix.kami.util.TimeUnit
import me.zeroeightsix.kami.util.graphics.font.KamiFontRenderer
import me.zeroeightsix.kami.util.threads.onMainThread
import java.awt.GraphicsEnvironment
import java.util.*
import kotlin.collections.HashMap

internal object CustomFont : Module(
    name = "CustomFont",
    description = "Use the better font instead of the stupid Minecraft font",
    showOnArray = false,
    category = Category.CLIENT,
    enabledByDefault = true
) {
    private const val DEFAULT_FONT_NAME = "Lato"

    val fontName = setting("FontName", DEFAULT_FONT_NAME, consumer = { prev, value ->
        getMatchingFontName(value) ?: getMatchingFontName(prev) ?: DEFAULT_FONT_NAME
    })
    private val sizeSetting = setting("Size", 1.0f, 0.5f..2.0f, 0.05f)
    private val gapSetting = setting("Gap", 0.0f, -10f..10f, 0.5f)
    private val lineSpaceSetting = setting("LineSpace", 0.0f, -10f..10f, 0.05f)
    private val baselineOffsetSetting = setting("BaselineOffset", 0.0f, -10.0f..10.0f, 0.05f)
    private val lodBiasSetting = setting("LodBias", 0.0f, -10.0f..10.0f, 0.05f)

    val isDefaultFont get() = fontName.value.equals(DEFAULT_FONT_NAME, true)
    val size get() = sizeSetting.value * 0.15f
    val gap get() = gapSetting.value * 0.5f - 0.8f
    val lineSpace get() = size * (lineSpaceSetting.value * 0.05f + 0.77f)
    val lodBias get() = lodBiasSetting.value * 0.25f - 0.5f
    val baselineOffset get() = baselineOffsetSetting.value * 2.0f - 4.5f

    /** Available fonts on the system */
    val availableFonts : Map<String, String> by AsyncCachedValue(5L, TimeUnit.SECONDS) {
        HashMap<String, String>().apply {
            val environment = GraphicsEnvironment.getLocalGraphicsEnvironment()

            environment.availableFontFamilyNames.forEach {
                this[it.toLowerCase(Locale.ROOT)] = it
            }

            environment.allFonts.forEach {
                this[it.name.toLowerCase(Locale.ROOT)] = it.family
            }
        }
    }

    private fun getMatchingFontName(name: String): String? {
        return if (name.equals(DEFAULT_FONT_NAME, true)) DEFAULT_FONT_NAME
        else availableFonts[name.toLowerCase(Locale.ROOT)]
    }

    init {
        fontName.listeners.add {
            onMainThread { KamiFontRenderer.reloadFonts() }
        }
    }
}