package me.zeroeightsix.kami.module.modules.movement

import me.zeroeightsix.kami.KamiMod
import me.zeroeightsix.kami.module.Module
import me.zeroeightsix.kami.module.modules.client.InfoOverlay
import me.zeroeightsix.kami.setting.Settings
import me.zeroeightsix.kami.util.MathsUtils
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.init.Items
import net.minecraft.inventory.ClickType

/**
 * Created by Dewy on the 4th of April, 2020
 */
// The code here is terrible. Not proud of it. TODO: Make this not suck.
@Module.Info(
        name = "ElytraReplace",
        description = "Automatically swap and replace your chestplate and elytra.",
        category = Module.Category.MOVEMENT
)
class ElytraReplace : Module() {
    private val threshold = register(Settings.integerBuilder("Broken %").withRange(1, 100).withValue(7).build())
    private val inventoryMode = register(Settings.b("Inventory", false))
    private val elytraFlightCheck = register(Settings.b("ElytraFlight Check", true))
    private var currentlyMovingElytra = false
    private var currentlyMovingChestplate = false
    private var elytraCount = 0

    override fun onUpdate() {
        if (!inventoryMode.value && mc.currentScreen is GuiContainer) {
            return
        }

        elytraCount = InfoOverlay.getItems(Items.ELYTRA) + InfoOverlay.getArmor(Items.ELYTRA)
        val chestplateCount = InfoOverlay.getItems(Items.DIAMOND_CHESTPLATE) + InfoOverlay.getArmor(Items.DIAMOND_CHESTPLATE)

        if (currentlyMovingElytra) {
            mc.playerController.windowClick(0, 6, 0, ClickType.PICKUP, mc.player)
            currentlyMovingElytra = false
            return
        }

        if (currentlyMovingChestplate) {
            mc.playerController.windowClick(0, 6, 0, ClickType.PICKUP, mc.player)
            currentlyMovingChestplate = false
            return
        }

        if (onGround()) {
            var slot = -420

            if (chestplateCount == 0) {
                return
            }

            if (mc.player.inventory.armorInventory[2].isEmpty()) {
                for (i in 0..44) {
                    if (mc.player.inventory.getStackInSlot(i).getItem() === Items.DIAMOND_CHESTPLATE) {
                        slot = i
                        break
                    }
                }
                mc.playerController.windowClick(0, if (slot < 9) slot + 36 else slot, 0, ClickType.PICKUP, mc.player)
                currentlyMovingElytra = true
                return
            }

            if (!(mc.player.inventory.armorInventory[2].getItem() === Items.DIAMOND_CHESTPLATE)) {
                for (i in 0..44) {
                    if (mc.player.inventory.getStackInSlot(i).getItem() === Items.DIAMOND_CHESTPLATE) {
                        slot = i
                        break
                    }
                }
                mc.playerController.windowClick(0, if (slot < 9) slot + 36 else slot, 0, ClickType.PICKUP, mc.player)
                mc.playerController.windowClick(0, 6, 0, ClickType.PICKUP, mc.player)
                mc.playerController.windowClick(0, if (slot < 9) slot + 36 else slot, 0, ClickType.PICKUP, mc.player)
                return
            }
        }

        if (!onGround() && passElytraFlightCheck()) {
            var slot = -420

            if (elytraCount == 0) {
                return
            }

            // if there's no elytra, or it's broken
            if (mc.player.inventory.armorInventory[2].isEmpty() || isBrokenArmor(2)) {
                for (i in 0..44) {
                    if (mc.player.inventory.getStackInSlot(i).getItem() === Items.ELYTRA && !isBroken(i)) {
                        slot = i
                        break
                    }
                }
                mc.playerController.windowClick(0, if (slot < 9) slot + 36 else slot, 0, ClickType.PICKUP, mc.player)
                currentlyMovingElytra = true
                return
            }

            // if it's not an elytra, or it's a broken elytra
            if (!(mc.player.inventory.armorInventory[2].getItem() === Items.ELYTRA) || (mc.player.inventory.armorInventory[2].getItem() === Items.ELYTRA && isBrokenArmor(2))) {
                for (i in 0..44) {
                    if (mc.player.inventory.getStackInSlot(i).getItem() === Items.ELYTRA && !isBroken(i)) {
                        slot = i
                        break
                    }
                }
                mc.playerController.windowClick(0, if (slot < 9) slot + 36 else slot, 0, ClickType.PICKUP, mc.player)
                mc.playerController.windowClick(0, 6, 0, ClickType.PICKUP, mc.player)
                mc.playerController.windowClick(0, if (slot < 9) slot + 36 else slot, 0, ClickType.PICKUP, mc.player)
            }
        }
    }

    override fun getHudInfo(): String {
        return elytraCount.toString()
    }

    private fun onGround(): Boolean {
        return mc.player.onGround
    }

    private fun isBroken(i : Int): Boolean { // (100 * damage / max damage) >= (100 - 70)
        return if (mc.player.inventory.getStackInSlot(i).maxDamage == 0) {
            false
        } else {
            100 * mc.player.inventory.getStackInSlot(i).getItemDamage() / mc.player.inventory.getStackInSlot(i).maxDamage > MathsUtils.reverseNumber(threshold.value, 1, 100)
        }
    }

    private fun isBrokenArmor(i : Int): Boolean { // (100 * damage / max damage) >= (100 - 70)
        return if (mc.player.inventory.armorInventory[i].maxDamage == 0) {
            false
        } else {
            100 * mc.player.inventory.armorInventory[i].getItemDamage() / mc.player.inventory.armorInventory[i].maxDamage > MathsUtils.reverseNumber(threshold.value, 1, 100)
        }
    }

    private fun passElytraFlightCheck(): Boolean {
        return if (elytraFlightCheck.value && KamiMod.MODULE_MANAGER.isModuleEnabled(ElytraFlight::class.java)) {
            true
        } else !elytraFlightCheck.value
    }
}