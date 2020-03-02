package me.zeroeightsix.kami.module.modules.misc;

import me.zero.alpine.listener.EventHandler;
import me.zero.alpine.listener.Listener;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.event.events.PacketEvent;
import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.server.SPacketChunkData;

/***
 * Kill mode
 * @author Fums
 * @coauthor S-B99
 * Updated by S-B99 on 19/12/19
 */
/***
 * Packet mode
 * @author cats
 * Updated by cats on 02/12/19
 */
@Module.Info(name = "AntiChunkBan", description = "Prevents being kicked by overloaded chunks", category = Module.Category.MISC, showOnArray = Module.ShowOnArray.OFF)
public class AntiChunkBan extends Module {

    private static long startTime = 0;
    private Setting<ModeThing> modeThing = register(Settings.e("Mode", ModeThing.PACKET));
    private Setting<Float> delayTime = register(Settings.f("Kill Delay", 10));
    private Setting<Boolean> disable = register(Settings.b("Disable After Kill", false));
    private Setting<Boolean> warn = register(Settings.b("Warning", true));

    private enum ModeThing {
        PACKET, KILL, BOTH
    }

    public void onEnable() {
        if (mc.player == null) return;
        Command.sendChatMessage(this.getChatName() + " Note: this disables chunks loading in. If you want to be able to play normally you have to disable it");
    }

    @Override
    public void onUpdate() {
        if (mc.player == null) return;

        if (modeThing.getValue().equals(ModeThing.KILL) || modeThing.getValue().equals(ModeThing.BOTH)) {
            if (Minecraft.getMinecraft().getCurrentServerData() != null) {
                if (startTime == 0) startTime = System.currentTimeMillis();
                if (startTime + delayTime.getValue() <= System.currentTimeMillis()) {
                    if (Minecraft.getMinecraft().getCurrentServerData() != null) {
                        Minecraft.getMinecraft().playerController.connection.sendPacket(new CPacketChatMessage("/kill"));
                    }
                    if (mc.player.getHealth() <= 0) {
                        mc.player.respawnPlayer();
                        mc.displayGuiScreen(null);
                        if (disable.getValue()) {
                            this.disable();
                        }
                    }
                    startTime = System.currentTimeMillis();
                }
            }
        }
    }

    @EventHandler
    Listener<PacketEvent.Receive> receiveListener = new Listener<>(event -> {
        if (modeThing.getValue().equals(ModeThing.PACKET) || modeThing.getValue().equals(ModeThing.BOTH)) {
            if (mc.player == null) return;
            if (event.getPacket() instanceof SPacketChunkData) {
                event.cancel();
            }
        }
    });
}