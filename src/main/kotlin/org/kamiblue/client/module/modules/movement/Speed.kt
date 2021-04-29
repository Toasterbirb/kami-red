package org.kamiblue.client.module.modules.movement

import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.manager.managers.TimerManager.modifyTimer
import org.kamiblue.client.manager.managers.TimerManager.resetTimer
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.MovementUtils.isMoving
import org.kamiblue.client.util.MovementUtils.setSpeed
import org.kamiblue.client.util.TickTimer
import org.kamiblue.client.util.TimeUnit
import org.kamiblue.client.util.threads.safeListener
import java.util.*
import kotlin.math.sin

internal object Speed : Module(
    name = "Speed",
    category = Category.MOVEMENT,
    description = "Increased speed"
) {
    private val mode by setting("Mode", Mode.LOWHOP)

    private val delay = setting("Tick Delay", 2, 0..10, 1, { mode == Mode.LOWHOP })
    private val boost by setting("Boost", 1f, 1f..2f, 0.1f, { mode == Mode.LOWHOP })
    private val fallSpeed by setting("Fall Speed", 0.3f, 0.1f..0.5f, 0.1f, { mode == Mode.LOWHOP })
    private val timerBoost by setting("Timer", 1.2f, 0.5f..5.0f, 0.1f, { mode == Mode.TIMER })

    private enum class Mode {
        LOWHOP, TIMER
    }

    private val timer = TickTimer(TimeUnit.TICKS)

    init {
        onEnable {
            // Disable Jesus module to prevent lag
            Jesus.disable()
        }

        safeListener<TickEvent.ClientTickEvent> {
            if (mode == Mode.LOWHOP)
            {
                if (player.isInWater || player.isInLava) return@safeListener
                else if (player.onGround && timer.tick(delay.value.toLong()))
                {
                    modifyTimer(50.0f / boost)
                    player.jump()
                    player.motionY -= fallSpeed

                    if (timer.tick(delay.value.toLong()))
                    {
                        modifyTimer(50f)
                    }
                }
            }
            else if (mode == Mode.TIMER)
            {
                if (player.isMoving && !player.movementInput.jump && player.motionY >= -0.1)
                {
                    modifyTimer(50f / timerBoost)
                }
                else
                {
                    modifyTimer(50f)
                }
            }
        }
    }
}