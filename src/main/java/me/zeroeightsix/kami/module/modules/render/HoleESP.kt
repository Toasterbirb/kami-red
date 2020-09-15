package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.event.events.RenderEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.ModuleManager
import me.zeroeightsix.kami.module.modules.combat.CrystalAura
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.BlockUtils.surroundOffset
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.ESPRenderer
import me.zeroeightsix.kami.util.graphics.GeometryMasks
import net.minecraft.init.Blocks
import net.minecraft.util.math.BlockPos
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.ceil

@Module.Info(
        name = "HoleESP",
        category = Module.Category.RENDER,
        description = "Show safe holes for crystal pvp"
)
object HoleESP : Module() {
    private val renderDistance = register(Settings.floatBuilder("RenderDistance").withValue(8.0f).withRange(0.0f, 32.0f).build())
    private val filled = register(Settings.b("Filled", true))
    private val outline = register(Settings.b("Outline", true))
    private val r1 = register(Settings.integerBuilder("Red(Obby)").withMinimum(0).withValue(208).withMaximum(255).withVisibility { shouldAddObby() }.build())
    private val g1 = register(Settings.integerBuilder("Green(Obby)").withMinimum(0).withValue(144).withMaximum(255).withVisibility { shouldAddObby() }.build())
    private val b1 = register(Settings.integerBuilder("Blue(Obby)").withMinimum(0).withValue(255).withMaximum(255).withVisibility { shouldAddObby() }.build())
    private val r2 = register(Settings.integerBuilder("Red(Bedrock)").withMinimum(0).withValue(144).withMaximum(255).withVisibility { shouldAddBedrock() }.build())
    private val g2 = register(Settings.integerBuilder("Green(Bedrock)").withMinimum(0).withValue(144).withMaximum(255).withVisibility { shouldAddBedrock() }.build())
    private val b2 = register(Settings.integerBuilder("Blue(Bedrock)").withMinimum(0).withValue(255).withMaximum(255).withVisibility { shouldAddBedrock() }.build())
    private val aFilled = register(Settings.integerBuilder("FilledAlpha").withMinimum(0).withValue(31).withMaximum(255).withVisibility { filled.value }.build())
    private val aOutline = register(Settings.integerBuilder("OutlineAlpha").withMinimum(0).withValue(127).withMaximum(255).withVisibility { outline.value }.build())
    private val renderMode = register(Settings.e<Mode>("Mode", Mode.BLOCK_HOLE))
    private val holeType = register(Settings.e<HoleType>("HoleType", HoleType.BOTH))

    private val safeHoles = ConcurrentHashMap<BlockPos, ColorHolder>()

    private enum class Mode {
        BLOCK_HOLE, BLOCK_FLOOR, FLAT
    }

    private enum class HoleType {
        OBBY, BEDROCK, BOTH
    }

    private fun shouldAddObby(): Boolean {
        return holeType.value == HoleType.OBBY || holeType.value == HoleType.BOTH
    }

    private fun shouldAddBedrock(): Boolean {
        return holeType.value == HoleType.BEDROCK || holeType.value == HoleType.BOTH
    }

    override fun onUpdate() {
        safeHoles.clear()
        val range = ceil(renderDistance.value).toInt()
        val crystalAura = ModuleManager.getModuleT(CrystalAura::class.java)!!
        val blockPosList = crystalAura.getSphere(CrystalAura.getPlayerPos(), range.toFloat(), range, false, true, 0)
        for (pos in blockPosList) {
            if (mc.world.getBlockState(pos).block != Blocks.AIR// block gotta be air
                    || mc.world.getBlockState(pos.up()).block != Blocks.AIR // block 1 above gotta be air
                    || mc.world.getBlockState(pos.up().up()).block != Blocks.AIR) continue // block 2 above gotta be air

            var isSafe = true
            var isBedrock = true
            for (offset in surroundOffset) {
                val block = mc.world.getBlockState(pos.add(offset)).block
                if (block !== Blocks.BEDROCK && block !== Blocks.OBSIDIAN && block !== Blocks.ENDER_CHEST && block !== Blocks.ANVIL) {
                    isSafe = false
                    break
                }
                if (block !== Blocks.BEDROCK) {
                    isBedrock = false
                }
            }

            if (isSafe) {
                if (!isBedrock && shouldAddObby()) {
                    safeHoles[pos] = ColorHolder(r1.value, g1.value, b1.value)
                }
                if (isBedrock && shouldAddBedrock()) {
                    safeHoles[pos] = ColorHolder(r2.value, g2.value, b2.value)
                }
            }
        }
    }

    override fun onWorldRender(event: RenderEvent) {
        if (mc.player == null || safeHoles.isEmpty()) return
        val side = if (renderMode.value != Mode.FLAT) GeometryMasks.Quad.ALL
        else GeometryMasks.Quad.DOWN
        val renderer = ESPRenderer()
        renderer.aFilled = if (filled.value) aFilled.value else 0
        renderer.aOutline = if (outline.value) aOutline.value else 0
        for ((pos, colour) in safeHoles) {
            val renderPos = if (renderMode.value == Mode.BLOCK_FLOOR) pos.down() else pos
            renderer.add(renderPos, colour, side)
        }
        renderer.render(true)
    }
}