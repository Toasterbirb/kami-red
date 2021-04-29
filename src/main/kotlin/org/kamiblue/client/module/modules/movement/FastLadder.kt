package org.kamiblue.client.module.modules.movement

import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.manager.managers.TimerManager.modifyTimer
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.MovementUtils.isMoving
import org.kamiblue.client.util.combat.CrystalUtils.getPlacePos
import org.kamiblue.client.util.threads.safeListener

class FastLadder {
    internal object FastLadder : Module(
        name = "FastLadder",
        category = Category.MOVEMENT,
        description = "Climb ladders faster when jumping in them"
    ) {
        private val mode by setting("Mode", Mode.TIMER)
        private val speed by setting("Speed", 1.5f, 0.5f..5.00f, 0.1f)
        private val preventFalling by setting("Prevent falling", false)

        private enum class Mode {
            TIMER, VELOCITY
        }

        init {
            onDisable {
                modifyTimer(50f)
            }

            safeListener<TickEvent.ClientTickEvent> {
                if (player.isOnLadder && player.isMoving && player.motionY != 0.00)
                {
                    if (player.motionX == 0.00 || player.motionZ == 0.00)
                    {
                        if (preventFalling)
                        {
                            player.motionX = 0.00
                            player.motionZ = 0.00
                        }

                        if (mode == Mode.TIMER && player.movementInput.jump)
                        {
                            modifyTimer(50f / speed)
                        }
                        else if (mode == Mode.VELOCITY)
                        {
                            if (player.movementInput.jump)
                                player.motionY = speed.toDouble() / 5.00
                            else if (!player.onGround && player.motionY < -0.05)
                                player.motionY = -(speed.toDouble() / 5.00)
                        }
                    }
                    else
                    {
                        player.motionY = 0.00
                    }
                }
                else if (mode == Mode.TIMER && player.isOnLadder)
                {
                    modifyTimer(50f)
                }
            }
        }
    }
}