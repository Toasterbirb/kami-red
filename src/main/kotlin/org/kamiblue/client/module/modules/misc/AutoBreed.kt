package org.kamiblue.client.module.modules.misc

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

internal object Nametags : Module(
    name = "AutoBreed",
    description = "Feeds food to animals nearby",
    category = Category.MISC
) {
    private val cow by setting("Cow", true)
    private val sheep by setting("Sheep", true)
    private val chicken by setting("Chicken", true)
    private val range by setting("Range", 2.0f, 1.0f..6.0f, 0.5f)
    private val breedDelay by setting("Breed Delay", 5, 0..10, 1)

    private val breedTimer = TickTimer(TimeUnit.TICKS)

    init {
        safeListener<TickEvent.ClientTickEvent> {
            if (breedTimer.tick(breedDelay)) {
                world.loadedEntityList.asSequence()
                .filter(::isValidEntity)
                .minByOrNull { player.getDistance(it) }
                ?.let {
                    if (player.getDistance(it) < range) {
                        playerController.interactWithEntity(player, it, EnumHand.MAIN_HAND)
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