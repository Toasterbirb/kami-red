package org.kamiblue.client.module.modules.combat

import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import org.kamiblue.client.event.events.ConnectionEvent
import org.kamiblue.client.module.Category
import org.kamiblue.client.module.Module
import org.kamiblue.client.util.TickTimer
import org.kamiblue.client.util.TimeUnit
import org.kamiblue.client.util.text.MessageSendHelper
import org.kamiblue.client.util.text.MessageSendHelper.sendServerMessage
import org.kamiblue.client.util.threads.safeListener
import org.kamiblue.commons.extension.synchronized
import org.kamiblue.event.listener.listener
import java.util.*
import kotlin.collections.LinkedHashMap

internal object AutoEZ : Module(
    name = "AutoEZ",
    category = Category.COMBAT,
    description = "Sends an insult in chat after killing someone"
) {
    private val detectMode = setting("Detect Mode", DetectMode.HEALTH)
    private val messageMode = setting("Message Mode", MessageMode.ONTOP)
    private val customText = setting("Custom Text", "unchanged")

    private enum class DetectMode {
        BROADCAST, HEALTH
    }

    @Suppress("UNUSED")
    enum class MessageMode(val text: String) {
        GG("gg, \$NAME"),
        ONTOP("KAMI BLUE on top! ez \$NAME"),
        EZD("You just got ez'd \$NAME"),
        EZ_HYPIXEL("\$HYPIXEL_MESSAGE \$NAME"),
        NAENAE("You just got naenae'd by kami blue plus, \$NAME"),
        CUSTOM("");
    }

    private val hypixelCensorMessages = arrayOf(
        "Hey Helper, how play game?",
        "Hello everyone! I am an innocent player who loves everything Hypixel.",
        "Please go easy on me, this is my first game!",
        "I like long walks on the beach and playing Hypixel",
        "Anyone else really like Rick Astley?",
        "Wait... This isn't what I typed!",
        "Plz give me doggo memes!",
        "You’re a great person! Do you want to play some Hypixel games with me?",
        "Welcome to the hypixel zoo!",
        "If the Minecraft world is infinite, how is the sun spinning around it?",
        "Your clicks per second are godly. ",
        "Maybe we can have a rematch?",
        "Pineapple doesn't go on pizza!",
        "ILY <3",
        "I heard you like Minecraft, so I built a computer in Minecraft in your Minecraft so you can Minecraft while you Minecraft",
        "Why can't the Ender Dragon read a book? Because he always starts at the End.",
        "I sometimes try to say bad things then this happens ",
        "Your personality shines brighter than the sun.",
        "You are very good at the game friend.",
        "I like pasta, do you prefer nachos?",
        "In my free time I like to watch cat videos on youtube",
        "I heard you like minecraft, so I built a computer so you can minecraft, while minecrafting in your minecraft.",
        "I like pineapple on my pizza",
        "You're a great person! Do you want to play some Hypixel games with me?",
        "I had something to say, then I forgot it.",
        "Hello everyone! I’m an innocent player who loves everything Hypixel.",
        "I like Minecraft pvp but you are truly better than me!",
        "Behold, the great and powerful, my magnificent and almighty nemesis!",
        "When nothing is right, go left.",
        "Let’s be friends instead of fighting okay?",
        "Your Clicks per second are godly.",
        "If the world in Minecraft is infinite how can the sun revolve around it?",
        "Blue is greenier than purple for sure",
        "I sometimes try to say bad things and then this happens :(",
        "I have really enjoyed playing with you! <3",
        "What can’t the Ender Dragon read a book? Because he always starts at the End.",
        "You are very good at this game friend.",
        "I like to eat pasta, do you prefer nachos?",
        "Sometimes I sing soppy, love songs in the car.",
        "I love the way your hair glistens in the light",
        "When I saw the guy with a potion I knew there was trouble brewing.",
        "I enjoy long walks on the beach and playing Hypixel",
        "I need help, teach me how to play!",
        "What happens if I add chocolate milk to macaroni and cheese?",
        "Can you paint with all the colors of the wind"
    ) // Got these from the forums, kinda based -humboldt123 

    private val timer = TickTimer(TimeUnit.SECONDS)
    private val attackedPlayers = LinkedHashMap<EntityPlayer, Int>().synchronized() // <Player, Last Attack Time>

    init {
        safeListener<ClientChatReceivedEvent> {
            if (detectMode.value != DetectMode.BROADCAST || player.isDead || player.health <= 0.0f) return@safeListener

            val message = it.message.unformattedText
            if (!message.contains(player.name, true)) return@safeListener

            for (player in attackedPlayers.keys) {
                if (!message.contains(player.name, true)) continue
                sendEzMessage(player)
                break // Break right after removing so we don't get exception
            }
        }

        safeListener<TickEvent.ClientTickEvent> { event ->
            if (event.phase != TickEvent.Phase.END) return@safeListener

            if (player.isDead || player.health <= 0.0f) {
                attackedPlayers.clear()
                return@safeListener
            }

            // Update attacked Entity
            val attacked = player.lastAttackedEntity
            if (attacked is EntityPlayer && !attacked.isDead && attacked.health > 0.0f) {
                attackedPlayers[attacked] = player.lastAttackedEntityTime
            }

            // Remove players if they are out of world or we haven't attack them again in 100 ticks (5 seconds)
            attackedPlayers.entries.removeIf { !it.key.isAddedToWorld || player.ticksExisted - it.value > 100 }

            // Check death
            if (detectMode.value == DetectMode.HEALTH) {
                for (player in attackedPlayers.keys) {
                    if (!player.isDead && player.health > 0.0f) continue
                    sendEzMessage(player)
                    break // Break right after removing so we don't get exception
                }
            }

            // Send custom message type help message
            sendHelpMessage()
        }

        // Clear the map on disconnect
        listener<ConnectionEvent.Disconnect> {
            attackedPlayers.clear()
        }
    }

    private fun sendHelpMessage() {
        if (messageMode.value == MessageMode.CUSTOM && customText.value == "unchanged" && timer.tick(5L)) { // 5 seconds delay
            MessageSendHelper.sendChatMessage("$chatName In order to use the custom $name, " +
                "please change the CustomText setting in ClickGUI, " +
                "with '&7\$NAME&f' being the username of the killed player")
        }
    }

    private fun sendEzMessage(player: EntityPlayer) {
        val text = (if (messageMode.value == MessageMode.CUSTOM) customText.value else messageMode.value.text)
            .replace("\$NAME", player.name).replace("\$HYPIXEL_MESSAGE", hypixelCensorMessages.random())
        sendServerMessage(text)
        attackedPlayers.remove(player)
    }
}
