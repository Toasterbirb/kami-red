package me.zeroeightsix.kami.command.commands

import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.command.syntax.ChunkBuilder
import me.zeroeightsix.kami.module.MacroManager
import me.zeroeightsix.kami.util.Macro.*
import me.zeroeightsix.kami.util.MessageSendHelper.*
import me.zeroeightsix.kami.util.Wrapper

/**
 * @author dominikaaaa
 */
class MacroCommand : Command("macro", ChunkBuilder().append("key|list").append("clear|message/command").build(), "m") {
    override fun call(args: Array<out String?>) {
        val rKey = args[0]
        val macro = args[1]
        val key = Wrapper.getKey(rKey)

        if (key == 0 && !rKey.equals("list", true)) {
            sendErrorMessage("Unknown key '&7$rKey&f'!")
            return
        }

        when {
            args[0] == null -> { /* key */
                sendWarningMessage("$chatLabel You must include the key you want to bind it to. Left alt is &7lmenu&f and left Control is &7lctrl&f. You cannot bind the &7meta&f key.")
                return
            }
            args[0] == "list" -> {
                sendChatMessage("You have the following macros: ")
                for ((key1, value) in MacroManager.macros) {
                    sendChatMessage(Wrapper.getKeyName(key1.toInt()) + ": $value")
                }
                return
            }
            args[1] == null -> { /* message */
                if (getMacrosForKey(key) == null) {
                    sendChatMessage("'&7$rKey&f' has no macros")
                    return
                }
                // TODO: empty check doesn't work idk
                sendChatMessage("'&7$rKey&f' has the following macros: ")
                sendStringChatMessage(getMacrosForKey(key).toTypedArray(), false)
                return
            }
            args[1] == "clear" -> {
                removeMacro(key.toString())
                sendChatMessage("Cleared macros for '&7$rKey&f'")
                return
            }
            args[2] != null -> { /* some random 3rd argument which shouldn't exist */
                sendWarningMessage("$chatLabel Your macro / command must be inside quotes, as 1 argument in the command. Example: &7" + getCommandPrefix() + label + " R \";set AutoSpawner debug toggle\"")
                return
            }
            else -> {
                addMacroToKey(key.toString(), macro)
                sendChatMessage("Added macro '&7$macro&f' for key '&7$rKey&f'")
            }
        }
    }
}