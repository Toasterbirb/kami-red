package me.zeroeightsix.kami.command

import me.zeroeightsix.kami.util.Wrapper
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.multiplayer.PlayerControllerMP
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.client.network.NetHandlerPlayClient
import org.kamiblue.command.ExecuteEvent
import org.kamiblue.command.IExecuteEvent

abstract class AbstractClientEvent {
    val mc = Wrapper.minecraft
    abstract val world: WorldClient?
    abstract val player: EntityPlayerSP?
    abstract val playerController: PlayerControllerMP?
    abstract val connection: NetHandlerPlayClient?
}

open class ClientEvent : AbstractClientEvent() {
    final override val world: WorldClient? = mc.world
    final override val player: EntityPlayerSP? = mc.player
    final override val playerController: PlayerControllerMP? = mc.playerController
    final override val connection: NetHandlerPlayClient? = mc.connection
}

open class SafeClientEvent(
    override val world: WorldClient,
    override val player: EntityPlayerSP,
    override val playerController: PlayerControllerMP,
    override val connection: NetHandlerPlayClient
) : AbstractClientEvent()

class ClientExecuteEvent(
    args: Array<String>
) : ClientEvent(), IExecuteEvent by ExecuteEvent(CommandManager, args)

class SafeExecuteEvent(
    world: WorldClient,
    player: EntityPlayerSP,
    playerController: PlayerControllerMP,
    connection: NetHandlerPlayClient,
    event: ClientExecuteEvent
) : SafeClientEvent(world, player, playerController, connection), IExecuteEvent by event

fun ClientEvent.toSafe() =
    if (world != null && player != null && playerController != null && connection != null) SafeClientEvent(world, player, playerController, connection)
    else null

fun ClientExecuteEvent.toSafe() =
    if (world != null && player != null && playerController != null && connection != null) SafeExecuteEvent(world, player, playerController, connection, this)
    else null