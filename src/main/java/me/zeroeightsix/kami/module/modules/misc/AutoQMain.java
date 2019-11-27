package me.zeroeightsix.kami.module.modules.misc;

import me.zeroeightsix.kami.module.Module;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.setting.Setting;
import me.zeroeightsix.kami.setting.Settings;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.CPacketChatMessage;

/**
 * Created by d1gress/Qther on 24/11/2019.
 * Updated by @S-B99 on 25/11/19
 */
@Module.Info(name = "AutoQMain", description = "Automatically does \"/queue main\" every 7.1 minutes.", category = Module.Category.MISC)
public class AutoQMain extends Module {

    private Setting<Boolean> debug = register(Settings.b("Debug", true));

    private static long startTime = 0;
    private static long startTimeCon = 0;

    @Override
    public void onUpdate() {
        if (mc.player == null) return;

        if (Minecraft.getMinecraft().getCurrentServerData() == null || (Minecraft.getMinecraft().getCurrentServerData() !=null && !Minecraft.getMinecraft().getCurrentServerData().serverIP.equalsIgnoreCase("2b2t.org"))) {
            startTime = 0;
            if (startTimeCon == 0 || startTimeCon <= System.currentTimeMillis() - 600000) startTimeCon = System.currentTimeMillis();
            if (startTimeCon + 600000 <= System.currentTimeMillis()) {
                Command.sendWarningMessage("&cYou are not connected to 2b2t.org, so AutoQMain will not function on this server.");
                startTimeCon = System.currentTimeMillis();
            }
        }
        else {
            if (startTime == 0 || startTime <= System.currentTimeMillis() - 427000) startTime = System.currentTimeMillis();
            if (startTime + 426000 <= System.currentTimeMillis()) {
                if (debug.getValue()) {
                    Command.sendChatMessage("&7Run &b/queue main&7 at " + System.currentTimeMillis());
                }
                Minecraft.getMinecraft().playerController.connection.sendPacket(new CPacketChatMessage("/queue main"));
                Command.sendChatMessage("Sent \"/queue main\" at " + System.currentTimeMillis());
                startTime = System.currentTimeMillis();
            }
        }
    }
}
