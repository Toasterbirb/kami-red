package me.zeroeightsix.kami.gui.hudgui.elements.world

import me.zeroeightsix.kami.event.SafeClientEvent
import me.zeroeightsix.kami.gui.hudgui.LabelHud
import me.zeroeightsix.kami.manager.managers.FriendManager
import me.zeroeightsix.kami.module.modules.combat.AntiBot
import me.zeroeightsix.kami.setting.GuiConfig.setting
import me.zeroeightsix.kami.util.color.ColorGradient
import me.zeroeightsix.kami.util.color.ColorHolder
import me.zeroeightsix.kami.util.color.DyeColors
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.MobEffects
import org.kamiblue.commons.utils.MathUtils

object TextRadar : LabelHud(
    name = "TextRadar",
    category = Category.WORLD,
    description = "List of players nearby"
) {

    private val health by setting("Health", true)
    private val ping by setting("Ping", false)
    private val combatPotion by setting("CombatPotion", true)
    private val distance by setting("Distance", true)
    private val friend by setting("Friend", true)
    private val range by setting("Range", 64, 16..256, 2)

    private val healthColorGradient = ColorGradient(
        0.0f to ColorHolder(180, 20, 20),
        10.0f to ColorHolder(240, 220, 20),
        20.0f to ColorHolder(20, 232, 20)
    )

    private val pingColorGradient = ColorGradient(
        0f to ColorHolder(101, 101, 101),
        0.1f to ColorHolder(20, 232, 20),
        20f to ColorHolder(20, 232, 20),
        150f to ColorHolder(20, 232, 20),
        300f to ColorHolder(150, 0, 0)
    )

    override fun SafeClientEvent.updateText() {
        world.playerEntities.asSequence()
            .filter { it != null && !it.isDead && it.health > 0.0f }
            .filter { it != player && it != mc.renderViewEntity }
            .filter { !AntiBot.isBot(it) }
            .filter { friend || !FriendManager.isFriend(it.name) }
            .map { it to player.getDistance(it) }
            .filter { it.second <= range }
            .sortedBy { it.second }
            .forEach {
                addHealth(it.first)
                addName(it.first)
                addPing(it.first)
                addPotion(it.first)
                addDist(it.second)
                displayText.currentLine++
            }
    }

    private fun addHealth(player: EntityPlayer) {
        if (health) {
            val hp = MathUtils.round(player.health, 1).toString()
            displayText.add(hp, healthColorGradient.get(player.health))
        }
    }

    private fun addName(player: EntityPlayer) {
        val color = if (FriendManager.isFriend(player.name)) DyeColors.GREEN.color else primaryColor
        displayText.add(player.name, color)
    }

    private fun SafeClientEvent.addPing(player: EntityPlayer) {
        if (ping) {
            val ping = connection.getPlayerInfo(player.name)?.responseTime ?: 0
            val color = pingColorGradient.get(ping.toFloat())
            displayText.add("${ping}ms", color)
        }
    }

    private fun addPotion(player: EntityPlayer) {
        if (combatPotion) {
            if (player.isPotionActive(MobEffects.WEAKNESS)) displayText.add("W", secondaryColor)
            if (player.isPotionActive(MobEffects.STRENGTH)) displayText.add("S", secondaryColor)
        }
    }

    private fun addDist(distIn: Float) {
        if (distance) {
            val dist = MathUtils.round(distIn, 1)
            displayText.add(dist.toString(), secondaryColor)
        }
    }
}