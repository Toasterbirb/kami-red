package org.kamiblue.client.module.modules.render

import org.kamiblue.client.command.CommandManager
import org.kamiblue.client.event.events.BaritoneCommandEvent
import org.kamiblue.client.event.events.ConnectionEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.BaritoneUtils
import org.kamiblue.client.util.text.MessageSendHelper

internal object Highwaybot : Module(
    name = "HighwayBot",
    description = "Starts building the highway NE gang style",
    category = Category.HIGHWAY
) {
    init {
        onEnable {
            Start()
        }

        onDisable {
            Stop()
            BaritoneUtils.cancelEverything()
        }
    }

    private fun Start() {
        MessageSendHelper.sendBaritoneCommand("highwaybuild")
    }

    private fun Stop() {
        MessageSendHelper.sendBaritoneCommand("highwaystop")
    }
}
