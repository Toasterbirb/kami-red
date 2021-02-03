package org.kamiblue.client.event.events

import org.kamiblue.client.event.Cancellable
import org.kamiblue.client.event.Event
import org.kamiblue.client.event.ICancellable
import net.minecraft.entity.Entity

open class EntityCollisionEvent(
    val entity: Entity,
    var x: Double,
    var y: Double,
    var z: Double
) : Event, ICancellable by Cancellable()