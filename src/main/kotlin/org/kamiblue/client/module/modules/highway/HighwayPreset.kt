package org.kamiblue.client.module.modules.highway

import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.module.modules.combat.AutoLog
import org.kamiblue.client.module.modules.combat.CombatSetting
import org.kamiblue.client.module.modules.combat.KillAura
import org.kamiblue.client.module.modules.misc.AntiWeather
import org.kamiblue.client.module.modules.misc.AutoReconnect
import org.kamiblue.client.module.modules.misc.AutoRespawn
import org.kamiblue.client.module.modules.misc.AutoTool
import org.kamiblue.client.module.modules.movement.AntiHunger
import org.kamiblue.client.module.modules.movement.Velocity
import org.kamiblue.client.module.modules.player.AutoEat
import org.kamiblue.client.module.modules.player.InventoryManager
import org.kamiblue.client.module.modules.player.NoFall

internal object HighwayPreset : Module(
    name = "HighwayPreset",
    description = "Enables some useful modules for highway building",
    category = Category.HIGHWAY,
    showOnArray = false,
    enabledByDefault = false
) {
    init {
        onEnable {
            InventoryManager.enable()
            InventoryManager.setting("Refill Threshold", 32, 1..63, 1)
            AutoReconnect.enable()
            AutoRespawn.enable()
            AntiWeather.enable()
            AntiHunger.enable()
            AutoTool.enable()
            AutoLog.enable()
            AutoEat.enable()
            Velocity.enable()
            NoFall.enable()
            KillAura.enable()
            CombatSetting.passive.value = true
            CombatSetting.neutral.value = true
            CombatSetting.hostile.value = true

            this.disable()
        }
    }
}