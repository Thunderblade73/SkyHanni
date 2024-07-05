package at.hannibal2.skyhanni.features.misc

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.data.IslandType
import at.hannibal2.skyhanni.data.mob.Mob
import at.hannibal2.skyhanni.events.EntityClickEvent
import at.hannibal2.skyhanni.skyhannimodule.SkyHanniModule
import at.hannibal2.skyhanni.utils.ChatUtils
import at.hannibal2.skyhanni.utils.ConditionalUtils.transformIf
import at.hannibal2.skyhanni.utils.EntityUtils.getSkinTexture
import at.hannibal2.skyhanni.utils.KeyboardManager.isKeyHeld
import at.hannibal2.skyhanni.utils.LorenzUtils
import at.hannibal2.skyhanni.utils.LorenzUtils.isInIsland
import at.hannibal2.skyhanni.utils.MobUtils.mob
import com.google.gson.Gson
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.passive.EntityVillager
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import org.lwjgl.input.Keyboard
import java.io.File

@SkyHanniModule
object CopyNPC {

    private val config get() = SkyHanniMod.feature.dev.mobDebug

    private val gson = Gson()

    /** Escapes characters correctly. Does not add "" */
    private val String.jsonSave get() = gson.toJson(this).drop(1).dropLast(1)

    @SubscribeEvent
    fun onEntityClick(event: EntityClickEvent) {
        if (!isEnabled()) return
        val mob = (event.clickedEntity as? EntityLivingBase)?.mob?.takeIf { it.mobType == Mob.Type.DISPLAY_NPC } ?: return
        val playerSkin: String
        val playerId: String
        when (val entity = mob.baseEntity) {
            is EntityPlayer -> {
                playerSkin = entity.gameProfile?.getSkinTexture() ?: ""
                playerId = entity.gameProfile?.id?.toString() ?: ""
            }

            is EntityVillager -> {
                playerSkin =
                    "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzgyMmQ4ZTc1MWM4ZjJmZDRjODk0MmM0NGJkYjJmNWNhNGQ4YWU4ZTU3NWVkM2ViMzRjMThhODZlOTNiIn19fQ\u003d\u003d"
                playerId = "c9540683-51e4-3942-ad17-4f2c3f3ae4b7"
            }

            else -> return
        }
        val inRift = IslandType.THE_RIFT.isInIsland()
        val name = "${mob.armorStand?.displayName?.formattedText ?: mob.name} (${if (inRift) "Rift" else ""}NPC)"
        val id = "${mob.name.uppercase().replace("[ .]".toRegex(), "_")}${if (inRift) "RIFT_" else ""}_NPC"
        val position = mob.baseEntity.position
        val json = """
            {
              "itemid": "minecraft:skull",
              "displayname": "${name.jsonSave}",
              "nbttag": "{HideFlags:254,SkullOwner:{Id:\"${playerId.jsonSave}\",Properties:{textures:[0:{Value:\"${playerSkin.jsonSave}\"}]},Name:\"${playerId.jsonSave}\"},display:{Lore:[0:\"\"],Name:\"${name.jsonSave}\"},ExtraAttributes:{id:\"${id.jsonSave}\"}}",
              "damage": 3,
              "lore" : [
                ""
              ],
              "internalname": "${id.jsonSave}",
              "clickcommand": "",
              "x": ${position.x},
              "y": ${position.y + 1},
              "z": ${position.z},
              "island": "${LorenzUtils.skyBlockIsland.neuName}"
            }
        """.trimIndent()
        val file = File(config.copyNpcPath.transformIf({ isNotEmpty() }, { "$this/" }) + "${id}.json")
        if (file.exists()) {
            ChatUtils.userError("File '${file.absolutePath}' already exists")
            return
        }
        file.createNewFile()
        file.writeText(json)
        ChatUtils.chat("Saved '$id' to '${file.absolutePath}' successfully")
    }

    private fun isEnabled() = config.copyNpc != Keyboard.KEY_NONE && config.copyNpc.isKeyHeld()
}
