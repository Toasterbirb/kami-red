package me.zeroeightsix.kami.manager.mangers

import me.zero.alpine.listener.EventHandler
import me.zero.alpine.listener.EventHook
import me.zero.alpine.listener.Listener
import me.zeroeightsix.kami.command.Command
import me.zeroeightsix.kami.manager.Manager
import me.zeroeightsix.kami.util.Macro
import me.zeroeightsix.kami.util.text.MessageSendHelper
import net.minecraftforge.fml.common.gameevent.InputEvent
import org.lwjgl.input.Keyboard

/**
 * @author dominikaaaa
 * Created by dominikaaaa on 04/05/20
 */
@Suppress("UNUSED_PARAMETER")
object MacroManager : Manager() {

    @EventHandler
    private val onKeyInput = Listener(EventHook { event: InputEvent.KeyInputEvent ->
        sendMacro(Keyboard.getEventKey())
    })

    /**
     * Reads macros from KAMIBlueMacros.json into the macros Map
     */
    fun loadMacros(): Boolean {
        return Macro.readFileToMemory()
    }

    /**
     * Saves macros from the macros Map into KAMIBlueMacros.json
     */
    fun saveMacros(): Boolean {
        return Macro.writeMemoryToFile()
    }

    /**
     * Sends the message or command, depending on which one it is
     * @param keyCode int keycode of the key the was pressed
     */
    fun sendMacro(keyCode: Int) {
        val macrosForThisKey = Macro.getMacrosForKey(keyCode) ?: return
        for (currentMacro in macrosForThisKey) {
            if (currentMacro!!.startsWith(Command.getCommandPrefix())) { // this is done instead of just sending a chat packet so it doesn't add to the chat history
                MessageSendHelper.sendKamiCommand(currentMacro, false) // ie, the false here
            } else {
                MessageSendHelper.sendServerMessage(currentMacro)
            }
        }
    }
}