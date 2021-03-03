package org.kamiblue.client.util

import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.init.MobEffects
import net.minecraft.util.MovementInput
import org.kamiblue.client.event.SafeClientEvent
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

object MovementUtils {
    private val mc = Minecraft.getMinecraft()

    val isInputting
        get() = mc.player?.movementInput?.let {
            it.moveForward != 0.0f || it.moveStrafe != 0.0f
        } ?: false

    val Entity.isMoving get() = speed > 0.0001
    val Entity.speed get() = hypot(motionX, motionZ)
    val Entity.realSpeed get() = hypot(posX - prevPosX, posZ - prevPosZ)

    /* totally not taken from elytrafly */
    fun SafeClientEvent.calcMoveYaw(yawIn: Float = mc.player.rotationYaw, moveForward: Float = roundedForward, moveString: Float = roundedStrafing): Double {
        var strafe = 90 * moveString
        strafe *= if (moveForward != 0F) moveForward * 0.5F else 1F

        var yaw = yawIn - strafe
        yaw -= if (moveForward < 0F) 180 else 0

        return Math.toRadians(yaw.toDouble())
    }

    private val roundedForward get() = getRoundedMovementInput(mc.player.movementInput.moveForward)
    private val roundedStrafing get() = getRoundedMovementInput(mc.player.movementInput.moveStrafe)

    private fun getRoundedMovementInput(input: Float) = when {
        input > 0f -> 1f
        input < 0f -> -1f
        else -> 0f
    }

    fun SafeClientEvent.setSpeed(speed: Double) {
        val yaw = calcMoveYaw()
        player.motionX = -sin(yaw) * speed
        player.motionZ = cos(yaw) * speed
    }

    fun SafeClientEvent.applySpeedPotionEffects(speed: Double) =
        player.getActivePotionEffect(MobEffects.SPEED)?.let {
            speed * (1.0 + (it.amplifier + 1) * 0.2)
        } ?: speed

    fun MovementInput.resetMove() {
        moveForward = 0.0f
        moveStrafe = 0.0f
        forwardKeyDown = false
        backKeyDown = false
        leftKeyDown = false
        rightKeyDown = false
    }

    fun MovementInput.resetJumpSneak() {
        jump = false
        sneak = false
    }
}