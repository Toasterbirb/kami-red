package org.kamiblue.client.gui.hudgui.elements.highway

import net.minecraft.util.math.Vec3d
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.gui.hudgui.LabelHud
import org.kamiblue.client.util.BaritoneUtils
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

internal object HighwaySpeed : LabelHud(
    name = "HighwaySpeed",
    category = Category.HIGHWAY,
    description = "Highway building speed"
) {
    private val showDistance by setting("Show distance", true)
    private val showDistanceLeft by setting("Show distance left", true)
    private val showTime by setting("Show elapsed time", true)
    private val showTimeLeft by setting("Show time left", true)
    private val showBlocksPlaced by setting("Show placed blocks", true)
    private val showBlocksLeft by setting("Show blocks left", true)

    private var building = false
    private var startPos = Vec3d(0.00, 0.00, 0.00)
    private var startTime = Calendar.getInstance().timeInMillis

    private var targetPos = Vec3d(30000000.00, 100.00, -30000000.00)



    override fun SafeClientEvent.updateText() {
        val entity = mc.renderViewEntity ?: player
        val pos = entity.positionVector

        if (!building)
        {
            val process = BaritoneUtils.primary?.pathingControlManager?.mostRecentInControl()?.orElse(null) ?: return

            if (process.displayName() == "Building highway")
            {
                startPos = pos
                startTime = Calendar.getInstance().timeInMillis
                building = true
            }
        }

        // Check if building highway
        if (building)
        {
            // Delta for walked distance
            val deltaX = pos.x - startPos.x
            val deltaZ = pos.z - startPos.z

            // Delta for distance left
            val deltaLeftX = targetPos.x - pos.x
            val deltaLeftZ = targetPos.z - pos.z

            val distance = sqrt((deltaX.pow(2) + deltaZ.pow(2))).roundToInt()
            val distanceLeft = sqrt((deltaLeftX.pow(2) + deltaLeftZ.pow(2))).roundToInt()
            val elapsedTime = (Calendar.getInstance().timeInMillis - startTime) / 1000
            var elapsedTimeString = "n/a"

            if (elapsedTime > 3600)
            {
                elapsedTimeString = "${elapsedTime / 3600} hours"
            }
            else if (elapsedTime > 60)
            {
                elapsedTimeString = "${elapsedTime / 60} minutes"
            }
            else
            {
                elapsedTimeString = "$elapsedTime seconds"
            }

            var timeLeft: Long = 0
            var timeLeftString = ""

            if (distance > 0 && elapsedTime > 0)
            {
                timeLeft = distanceLeft / distance * elapsedTime / 60 / 60

                if (timeLeft / 24 > 365)
                {
                    val years: Float = timeLeft / 24f / 365f
                    timeLeftString = "$timeLeft hours ($years years)"
                }
                else
                {
                    timeLeftString = "$timeLeft hours (${timeLeft / 24} days)"
                }
            }

            if (showDistance)
            {
                displayText.add("Distance moved:", secondaryColor)
                displayText.addLine("$distance")
            }

            if (showDistanceLeft)
            {
                displayText.add("Distance left:", secondaryColor)
                displayText.addLine("$distanceLeft")
            }

            if (showTime)
            {
                displayText.add("Time:", secondaryColor)
                displayText.addLine("$elapsedTimeString")
            }

            if (showTimeLeft)
            {
                displayText.add("Time left:", secondaryColor)
                displayText.addLine("$timeLeftString")
            }

            if (showBlocksPlaced)
            {
                displayText.add("Blocks placed:", secondaryColor)
                displayText.addLine("${(deltaX * 5).roundToInt()}")
            }

            if (showBlocksLeft)
            {
                displayText.add("Missing blocks:", secondaryColor)
                displayText.addLine("${(deltaLeftX * 5).roundToInt()}")
            }
        }
    }
}
