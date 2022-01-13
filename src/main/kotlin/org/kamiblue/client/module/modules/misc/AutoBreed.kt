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
import org.kamiblue.client.util.text.MessageSendHelper

internal object AutoBreed : Module(
    name = "AutoBreed",
    description = "Feeds food to animals nearby",
    category = Category.MISC
) {
    private val cow by setting("Cow", true)
    private val sheep by setting("Sheep", true)
    private val chicken by setting("Chicken", true)
    private val range by setting("Range", 2.0f, 1.0f..10.0f, 0.5f)
    private val breedDelay by setting("Breed Delay", 5, 1..10, 1)

    private val breedTimer = TickTimer(TimeUnit.TICKS)

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (breedTimer.tick(breedDelay) && player.heldItemMainhand.item == Items.WHEAT || player.heldItemMainhand.item == Items.WHEAT_SEEDS) {
                val entitylist = world.loadedEntityList;
            
                for (e in entitylist) {
                    if (player.getDistance(e) <= range) {
                        if (e != player && isValidEntity(e)) {
                            /* Check if the item in hand is correct */
                            if ((e is EntityCow || e is EntitySheep) && player.heldItemMainhand.item != Items.WHEAT) continue
                            if (e is EntityChicken && player.heldItemMainhand.item != Items.WHEAT_SEEDS) continue
                            
                            playerController.interactWithEntity(player, e, EnumHand.MAIN_HAND)
                        }
                    }
                }
            }
        }
        
    }

    private fun isValidEntity(entity: Entity): Boolean {
        return entity is EntityAnimal && !entity.isChild // FBI moment
            && (cow && entity is EntityCow
            || sheep && entity is EntitySheep
            || chicken && entity is EntityChicken)
    }
}