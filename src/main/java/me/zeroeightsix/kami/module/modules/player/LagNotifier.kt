package me.zeroeightsix.kami.module.modules.player

import me.zeroeightsix.kami.event.events.ConnectionEvent
import me.zeroeightsix.kami.event.events.PacketEvent
import me.zeroeightsix.kami.event.events.RenderOverlayEvent
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.process.PauseProcess.pauseBaritone
import me.zeroeightsix.kami.process.PauseProcess.unpauseBaritone
import me.zeroeightsix.kami.setting.ModuleConfig.setting
import me.zeroeightsix.kami.util.*
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.graphics.font.FontRenderAdapter
import me.zeroeightsix.kami.util.math.Vec2f
import me.zeroeightsix.kami.util.text.MessageSendHelper
import me.zeroeightsix.kami.util.threads.safeListener
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.network.play.server.SPacketPlayerPosLook
import net.minecraft.util.math.Vec3d
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.commons.utils.MathUtils
import org.kamiblue.event.listener.listener
import org.lwjgl.opengl.GL11.glColor4f

/**
 * Thanks Brady and cooker and leij for helping me not be completely retarded
 */
object LagNotifier : Module(
    name = "LagNotifier",
    description = "Displays a warning when the server is lagging",
    category = Category.PLAYER
) {
    private val detectRubberBand by setting("DetectRubberBand", true)
    private val pauseBaritone by setting("PauseBaritone", true)
    val pauseTakeoff by setting("PauseElytraTakeoff", true)
    val pauseAutoWalk by setting("PauseAutoWalk", true)
    private val feedback by setting("PauseFeedback", true, { pauseBaritone })
    private val timeout by setting("Timeout", 3.5f, 0.0f..10.0f, 0.5f)

    private val pingTimer = TickTimer(TimeUnit.SECONDS)
    private var lastPacketTimer = TickTimer()
    private var lastRubberBandTimer = TickTimer()
    private var text = ""

    var paused = false; private set

    init {
        onDisable {
            unpause()
        }

        listener<RenderOverlayEvent> {
            if (text.isBlank()) return@listener

            val resolution = ScaledResolution(mc)
            val posX = resolution.scaledWidth / 2.0f - FontRenderAdapter.getStringWidth(text) / 2.0f
            val posY = 80.0f / resolution.scaleFactor

            /* 80px down from the top edge of the screen */
            FontRenderAdapter.drawString(text, posX, posY, color = ColorHolder(255, 33, 33))
            glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        }

        safeListener<TickEvent.ClientTickEvent> {
            if ((mc.currentScreen != null && mc.currentScreen !is GuiChat) || mc.isIntegratedServerRunning) {
                if (mc.isIntegratedServerRunning) unpause()
                text = ""
            } else {
                val timeoutMillis = (timeout * 1000.0f).toLong()
                when {
                    lastPacketTimer.tick(timeoutMillis, false) -> {
                        if (pingTimer.tick(1L)) WebUtils.update()
                        text = if (WebUtils.isInternetDown) "Your internet is offline! "
                        else "Server Not Responding! "

                        text += timeDifference(lastPacketTimer.time)
                        pause()
                    }
                    detectRubberBand && !lastRubberBandTimer.tick(timeoutMillis, false) -> {
                        text = "RubberBand Detected! ${timeDifference(lastRubberBandTimer.time)}"
                        pause()
                    }
                    else -> {
                        unpause()
                        mc.currentScreen
                    }
                }
            }
        }

        safeListener<PacketEvent.Receive>(2000) {
            lastPacketTimer.reset()

            if (!detectRubberBand || it.packet !is SPacketPlayerPosLook) return@safeListener

            val dist = Vec3d(it.packet.x, it.packet.y, it.packet.z).subtract(player.positionVector).length()
            val rotationDiff = Vec2f(it.packet.yaw, it.packet.pitch).minus(Vec2f(player)).length()

            if (dist in 0.5..64.0 || rotationDiff > 1.0) lastRubberBandTimer.reset()
        }

        listener<ConnectionEvent.Connect> {
            lastPacketTimer.reset(5000L)
            lastRubberBandTimer.reset(5000L)
        }
    }

    private fun pause() {
        if (!paused && pauseBaritone && feedback) {
            MessageSendHelper.sendBaritoneMessage("Paused due to lag!")
        }

        pauseBaritone()
        paused = true
    }

    private fun unpause() {
        if (paused && pauseBaritone && feedback) {
            MessageSendHelper.sendBaritoneMessage("Unpaused!")
        }

        unpauseBaritone()
        paused = false
        text = ""
    }

    private fun timeDifference(timeIn: Long) = MathUtils.round((System.currentTimeMillis() - timeIn) / 1000.0, 1)
}
