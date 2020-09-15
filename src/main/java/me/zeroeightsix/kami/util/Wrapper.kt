package me.zeroeightsix.kami.util

import me.zeroeightsix.kami.gui.kami.KamiGUI
import me.zeroeightsix.kami.gui.rgui.render.font.FontRenderer
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.world.World
import org.lwjgl.input.Keyboard

/**
 * Created by 086 on 11/11/2017.
 */
object Wrapper {
    @JvmStatic
    val fontRenderer: FontRenderer
        get() = KamiGUI.fontRenderer

    @JvmStatic
    val minecraft: Minecraft
        get() = Minecraft.getMinecraft()

    @JvmStatic
    val player: EntityPlayerSP?
        get() = minecraft.player

    @JvmStatic
    val world: World?
        get() = minecraft.world

    @JvmStatic
    fun getKey(keyname: String): Int {
        return Keyboard.getKeyIndex(keyname.toUpperCase())
    }

    fun getKeyName(keycode: Int): String {
        return Keyboard.getKeyName(keycode)
    }
}