package org.kamiblue.client.module.modules.misc

import net.minecraft.init.Items
import net.minecraft.util.EnumHand
import net.minecraft.entity.Entity
import net.minecraft.entity.passive.*
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.SafeClientEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.TickTimer
import org.kamiblue.client.util.TimeUnit
import org.kamiblue.client.util.threads.safeListener

internal object AutoShearer : Module(
    name = "AutoShearer",
    description = "Shears all sheep nearby",
    category = Category.MISC
) {
    private val range by setting("Range", 2.0f, 1.0f..10.0f, 0.5f)
    private val shearDelay by setting("Shear Delay", 5, 1..10, 1)

    private val shearTimer = TickTimer(TimeUnit.TICKS)

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (shearTimer.tick(shearDelay) && player.heldItemMainhand.item == Items.SHEARS) {
                val entitylist = world.loadedEntityList;
            
                for (e in entitylist) {
                    if (e != player && isValidEntity(e)) {
                        if (player.getDistance(e) <= range) {                            
                            playerController.interactWithEntity(player, e, EnumHand.MAIN_HAND)
                        }
                    }
                }
            }
        }
        
    }

    private fun isValidEntity(entity: Entity): Boolean {
        return entity is EntityAnimal && !entity.isChild // FBI moment
            && (entity is EntitySheep)
    }
}