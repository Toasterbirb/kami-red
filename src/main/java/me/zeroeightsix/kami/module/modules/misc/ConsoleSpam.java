package me.zeroeightsix.kami.module.modules.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.util.Wrapper;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUpdateSign;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.BlockPos;

/**
 * Created on 16 December by 0x2E | PretendingToCode
 */

@Module.Info(name = "ConsoleSpam", description = "Spams Spigot consoles by sending invalid UpdateSign packets", category = Module.Category.MISC)
public class ConsoleSpam extends Module {

    @Override
    public void onEnable() {
        Command.sendChatMessage(this.getChatName() + " Every time you right click a sign, a warning will appear in console.");
        Command.sendChatMessage(this.getChatName() + " Use an autoclicker to automate this process.");
    }

    @EventHandler
    public Listener<PacketEvent.Send> sendListener = new Listener<>(event -> {
        if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
            BlockPos location = ((CPacketPlayerTryUseItemOnBlock) event.getPacket()).getPos();

            Wrapper.getPlayer().connection.sendPacket(new CPacketUpdateSign(location, new TileEntitySign().signText));
        }
    });
}
