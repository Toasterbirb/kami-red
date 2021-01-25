package me.zeroeightsix.kami.gui.hudgui.elements.client

import me.zeroeightsix.kami.gui.hudgui.HudElement
import me.zeroeightsix.kami.module.AbstractModule
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.setting.GuiConfig.setting
import me.zeroeightsix.kami.util.AsyncCachedValue
import me.zeroeightsix.kami.util.TickTimer
import me.zeroeightsix.kami.util.TimeUnit
import me.zeroeightsix.kami.util.TimedFlag
import me.zeroeightsix.kami.util.color.ColorConverter
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.AnimationUtils
import me.zeroeightsix.kami.util.graphics.VertexHelper
import me.zeroeightsix.kami.util.graphics.font.FontRenderAdapter
import me.zeroeightsix.kami.util.graphics.font.HAlign
import me.zeroeightsix.kami.util.graphics.font.TextComponent
import me.zeroeightsix.kami.util.graphics.font.VAlign
import me.zeroeightsix.kami.util.threads.safeAsyncListener
import net.minecraft.client.renderer.GlStateManager
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.commons.extension.sumByFloat
import org.kamiblue.commons.interfaces.DisplayEnum
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.max

object ModuleList : HudElement(
    name = "ModuleList",
    category = Category.CLIENT,
    description = "List of enabled modules",
    enabledByDefault = true
) {

    private val sortingMode = setting("SortingMode", SortingMode.LENGTH)
    private val showInvisible by setting("ShowInvisible", false)
    private val rainbow = setting("Rainbow", true)
    private val rainbowLength by setting("RainbowLength", 10.0f, 1.0f..20.0f, 0.5f, { rainbow.value })
    private val indexedHue by setting("IndexedHue", 0.5f, 0.0f..1.0f, 0.05f)
    private val primary by setting("PrimaryColor", ColorHolder(155, 144, 255), false)
    private val secondary by setting("SecondaryColor", ColorHolder(255, 255, 255), false)

    @Suppress("UNUSED")
    private enum class SortingMode(
        override val displayName: String,
        val comparator: Comparator<AbstractModule>
    ) : DisplayEnum {
        LENGTH("Length", compareByDescending { it.textLine.getWidth() }),
        ALPHABET("Alphabet", compareBy { it.name }),
        CATEGORY("Category", compareBy { it.category.ordinal })
    }

    private var cacheWidth = 20.0f
    private var cacheHeight = 20.0f
    override val hudWidth: Float get() = cacheWidth
    override val hudHeight: Float get() = cacheHeight

    private val sortedModuleListCache = AsyncCachedValue(5L, TimeUnit.SECONDS) {
        ModuleManager.modules.sortedWith(sortingMode.value.comparator)
    }

    private val sortedModuleList by sortedModuleListCache
    private val textLineMap = HashMap<AbstractModule, TextComponent.TextLine>()

    private val timer = TickTimer(TimeUnit.SECONDS)
    private var toggleMap = ModuleManager.modules
        .associateWith { TimedFlag(false) }

    init {
        safeAsyncListener<TickEvent.ClientTickEvent> { event ->
            if (event.phase != TickEvent.Phase.END) return@safeAsyncListener

            if (timer.tick(5L)) {
                toggleMap = ModuleManager.modules
                    .associateWith { toggleMap[it] ?: TimedFlag(false) }
            }

            for ((module, timedFlag) in toggleMap) {
                val state = module.isEnabled && (module.isVisible || showInvisible)
                if (timedFlag.value != state) timedFlag.value = state

                if (timedFlag.progress <= 0.0f) continue
                textLineMap[module] = module.newTextLine()
            }

            cacheWidth = sortedModuleList.maxOfOrNull {
                if (toggleMap[it]?.value == true) it.textLine.getWidth() + 4.0f
                else 20.0f
            }?.let {
                max(it, 20.0f)
            } ?: 20.0f

            cacheHeight = max(toggleMap.values.sumByFloat { it.displayHeight }, 20.0f)
        }
    }

    override fun renderHud(vertexHelper: VertexHelper) {
        super.renderHud(vertexHelper)
        GlStateManager.pushMatrix()

        GlStateManager.translate(width / scale * dockingH.multiplier, 0.0f, 0.0f)
        if (dockingV == VAlign.BOTTOM) {
            GlStateManager.translate(0.0f, height / scale - (FontRenderAdapter.getFontHeight() + 2.0f), 0.0f)
        }

        drawModuleList()

        GlStateManager.popMatrix()
    }

    private fun drawModuleList() {
        val primaryHsb = Color.RGBtoHSB(primary.r, primary.g, primary.b, null)
        val lengthMs = rainbowLength * 1000.0f
        val timedHue = System.currentTimeMillis() % lengthMs.toLong() / lengthMs

        var index = 0

        for (module in sortedModuleList) {
            val timedFlag = toggleMap[module] ?: continue
            val progress = timedFlag.progress

            if (progress <= 0.0f) continue

            GlStateManager.pushMatrix()

            val textLine = module.textLine
            val textWidth = textLine.getWidth()
            val animationXOffset = textWidth * dockingH.offset * (1.0f - progress)
            val stringPosX = textWidth * dockingH.multiplier
            val margin = 2.0f * dockingH.offset

            GlStateManager.translate(animationXOffset - stringPosX - margin, 0.0f, 0.0f)

            if (rainbow.value) {
                val hue = timedHue + indexedHue * 0.05f * index++
                val color = ColorConverter.hexToRgb(Color.HSBtoRGB(hue, primaryHsb[1], primaryHsb[2]))
                module.newTextLine(color).drawLine(progress, true, HAlign.LEFT, FontRenderAdapter.useCustomFont)
            } else {
                textLine.drawLine(progress, true, HAlign.LEFT, FontRenderAdapter.useCustomFont)
            }

            GlStateManager.popMatrix()
            var yOffset = timedFlag.displayHeight
            if (dockingV == VAlign.BOTTOM) yOffset *= -1.0f
            GlStateManager.translate(0.0f, yOffset, 0.0f)
        }
    }

    private val AbstractModule.textLine
        get() = textLineMap.getOrPut(this) {
            this.newTextLine()
        }

    private fun AbstractModule.newTextLine(color: ColorHolder = primary) =
        TextComponent.TextLine(" ").apply {
            add(TextComponent.TextElement(name, color))
            getHudInfo().let {
                if (it.isNotBlank()) add(TextComponent.TextElement(it, secondary))
            }
            if (dockingH == HAlign.RIGHT) reverse()
        }

    private val TimedFlag<Boolean>.displayHeight
        get() = (FontRenderAdapter.getFontHeight() + 2.0f) * progress

    private val TimedFlag<Boolean>.progress
        get() = if (value) {
            AnimationUtils.exponentInc(AnimationUtils.toDeltaTimeFloat(lastUpdateTime), 200.0f)
        } else {
            AnimationUtils.exponentDec(AnimationUtils.toDeltaTimeFloat(lastUpdateTime), 200.0f)
        }

    init {
        relativePosX = -2.0f
        relativePosY = 2.0f
        dockingH = HAlign.RIGHT

        sortingMode.listeners.add {
            sortedModuleListCache.update()
        }
    }

}