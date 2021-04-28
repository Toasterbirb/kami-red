package org.kamiblue.client.module.modules.movement

import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.MovementUtils.isMoving
import org.kamiblue.client.util.TickTimer
import org.kamiblue.client.util.TimeUnit
import org.kamiblue.client.util.threads.safeListener

internal object BunnyHop : Module(
    name = "BunnyHop",
    category = Category.MOVEMENT,
    description = "Automatically jumps if possible"
) {
    private val delay = setting("Tick Delay", 10, 0..40, 1)
    private val requireMovement by setting("Toggle when moving", true)

    private val timer = TickTimer(TimeUnit.TICKS)

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (player.isInWater || player.isInLava) player.motionY = 0.1
            else if (player.onGround && timer.tick(delay.value.toLong()))
            {
                if (!requireMovement)
                    player.jump()
                else
                {
                    if (player.isSprinting)
                        player.jump()
                }
            }
        }
    }
}