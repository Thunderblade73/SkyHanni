package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.CollectionUtils.split
import at.hannibal2.skyhanni.utils.StringUtils.equalSizeInFont
import at.hannibal2.skyhanni.utils.renderables.Renderable

object ColourDisplay {

    // TODO add full colour syntax

    val text = """
        &0§0 Black§r
        &1§1 Dark Blue§r
        &2§2 Dark Green§r
        &3§3 Dark Aqua§r
        &4§4 Dark Red§r
        &5§5 Dark Purple§r
        &6§6 Gold§r
        &7§7 Gray§r 
        &8§8 Dark Gray§r
        &9§9 Blue§r
        &a§a Green§r
        &b§b Aqua§r
        &c§c Red§r
        &d§d Purple§r
        &e§e Yellow§r
        &f§f White§r
        &z§z Chroma§r
        &Z§Z SBA Chroma§r
        &k§k Magic§r
        &l§l Bold§r
        &m§m Striketrhough§r
        &n§n Underline§r
        &o§o Italic§r
    """.trimIndent().split('\n')

    val renderable = text.map { Renderable.string(it) }

    val renderableTable = Renderable.table(renderable.split(), xPadding = 2)

    fun onCommand() {
        ChatUtils.chat(ChatUtils.separator)
        ChatUtils.chat(
            text.equalSizeInFont(true).split().zipWithNext { a1, b1 ->
                a1.zip(b1) { a2, b2 ->
                    "$a2$b2"
                }
            }.flatten().joinToString("\n") { it.trimEnd('.') },
            prefix = false,
        )
        ChatUtils.chat(ChatUtils.separator)
    }
}
