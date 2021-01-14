package me.zeroeightsix.kami.module.modules.render

import me.zeroeightsix.kami.module.Category
import me.zeroeightsix.kami.module.Module

internal object ExtraTab : Module(
    name = "ExtraTab",
    description = "Expands the player tab menu",
    category = Category.RENDER
) {
    private val tabSize = setting("MaxPlayers", 265, 80..400, 5)

    fun <E> subList(list: List<E>, fromIndex: Int, toIndex: Int): List<E> {
        return list.subList(fromIndex, if (isEnabled) tabSize.value.coerceAtMost(list.size) else toIndex)
    }
}