package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.setting.Settings

@Module.Info(
        name = "ExtraTab",
        description = "Expands the player tab menu",
        category = Module.Category.RENDER
)
object ExtraTab : Module() {
    val offload = register(Settings.b("ReduceLag", true))
    val tabSize = register(Settings.integerBuilder("MaxPlayers").withMinimum(1).withValue(230).build())
}