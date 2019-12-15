package me.zeroeightsix.kami.module.modules.bewwawho.player;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import net.minecraft.network.play.client.CPacketAnimation;

/**
 * Made by FINZ0
 */

@Module.Info(name = "NoSwing", category = Module.Category.PLAYER, description = "Cancels server and client swinging packets")
public class
NoSwing extends Module {

    @EventHandler
    private Listener<PacketEvent.Send> listener = new Listener<>(event -> {
        if (event.getPacket() instanceof CPacketAnimation) {
            event.cancel();
        }
    });

}
