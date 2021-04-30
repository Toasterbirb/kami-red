package org.kamiblue.client.module.modules.movement

import net.minecraft.block.BlockCactus
import net.minecraft.init.Blocks
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.module.Module
import org.kamiblue.client.module.Category
import org.kamiblue.client.util.threads.safeListener
import java.util.*

class Hover {
    internal object Hover : Module(
        name = "Hover",
        category = Category.MOVEMENT,
        description = "Hover over the ground like a genie"
    ) {
        private val hoverScale by setting("Scale", 1.00, 0.1..10.00, 0.1)

        private val startTime = Calendar.getInstance().timeInMillis

        init {
            safeListener<TickEvent.ClientTickEvent> {
                val elapsedTime: Double = Calendar.getInstance().timeInMillis.toDouble() - startTime.toDouble()
                player.motionY = Math.sin(elapsedTime / (100.00 * hoverScale)) / 20.00
            }
        }
    }
}