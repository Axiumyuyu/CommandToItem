package me.axiumyu.events


import me.axiumyu.CommandToItem.Companion.cItems
import me.axiumyu.CommandToItem.Companion.mm
import me.axiumyu.CItem
import me.axiumyu.utlis.Legacy2MiniMessage.replaceColor
import me.axiumyu.utlis.Message
import me.axiumyu.utlis.Message.Companion.getMessage
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

object UseItem : Listener {

    //
    @EventHandler
    fun onPlayerUse(event: PlayerInteractEvent) {
        val player: Player = event.player
        if (event.item != null && event.item?.type != Material.AIR) {
            val mainHandItem : ItemStack = player.inventory.itemInMainHand

            val cItem: CItem? = cItems.find { it.compare(mainHandItem) }

            if (cItem == null) return
            event.isCancelled = true

            if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) {
                return
            }
            if (cItem.isOnCooldown(event.player)) {
                getMessage(Message.COOLDOWN).let {
                    event.player.sendMessage(
                        mm.deserialize(it.replace("%cooldown%", cItem.getCooldownFor(event.player).toString()).replaceColor())
                    )
                }
                return
            }
            if (cItem.permissionRequired && !event.player.hasPermission("commandtoitem.use.${cItem.id}")) {
                event.player.sendMessage(mm.deserialize(getMessage(Message.NO_PERMISSION).replaceColor()))
                return
            }
            if (cItem.consumed) {
                player.inventory.itemInMainHand.amount--
            }

            cItem.executeUseActions(player)

        }
    }
}

