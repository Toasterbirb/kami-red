package org.kamiblue.client.module.modules.render

import net.minecraft.init.Blocks
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.BaritoneUtils
import org.kamiblue.client.util.MovementUtils.isMoving
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.client.manager.managers.TimerManager.modifyTimer
import org.kamiblue.client.util.items.block

internal object Highwaybot : Module(
    name = "HighwayBot",
    description = "Starts building the highway NE gang style",
    category = Category.HIGHWAY
) {
    private val timerBuild by setting("Timer build", false)
    private val timerModifier by setting("Multiplier", 1.2f, 0.5f..10.0f, 0.1f, { timerBuild == true })

    init {
        onEnable {
            Start()
        }

        onDisable {
            Stop()
            BaritoneUtils.cancelEverything()
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (timerBuild && !player.isMoving && !player.isSprinting) {
                if (player.heldItemMainhand.item.block == Blocks.OBSIDIAN)
                {
                    modifyTimer(50f / timerModifier)
                }
            }
            else
            {
                modifyTimer(50f)
            }
        }
    }

    private fun Start() {
        MessageSendHelper.sendBaritoneCommand("highwaybuild")
    }

    private fun Stop() {
        MessageSendHelper.sendBaritoneCommand("highwaystop")
    }
}
