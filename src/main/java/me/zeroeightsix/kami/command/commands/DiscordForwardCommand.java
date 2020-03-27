package me.zeroeightsix.kami.command.commands;

import me.zeroeightsix.kami.KamiMod;
import me.zeroeightsix.kami.command.Command;
import me.zeroeightsix.kami.command.syntax.ChunkBuilder;
import me.zeroeightsix.kami.module.ModuleManager;
import me.zeroeightsix.kami.module.modules.chat.DiscordForward;

/**
 * @author S-B99
 * Created by S-B99 on 26/03/20
 */
public class DiscordForwardCommand extends Command {
    public DiscordForwardCommand() {
        super("discordforward", new ChunkBuilder().append("webhook url").append("avatar url").build(), "webhook");
    }

    @Override
    public void call(String[] args) {
        DiscordForward df = (DiscordForward) ModuleManager.getModuleByName("DiscordForward");
        if (args[0] != null && !args[0].equals("")) {
            df.url.setValue(args[0]);
            Command.sendChatMessage(df.getChatName() + "Set URL to \"" + args[0] + "\"!");
        } else if (args[0] == null) {
            Command.sendErrorMessage(df.getChatName() + "Error: you must specify a URL or \"\" for the first parameter when running the command");
        }

        if (args[1] == null) return;
        if (!args[1].equals("")) {
            df.avatar.setValue(args[1]);
            Command.sendChatMessage(df.getChatName() + "Set Avatar to \"" + args[1] + "\"!");
        } else {
            df.avatar.setValue(KamiMod.GITHUB_LINK + "raw/assets/assets/icons/kami.png");
            Command.sendChatMessage(df.getChatName() + "Reset Avatar!");
        }
    }
}
