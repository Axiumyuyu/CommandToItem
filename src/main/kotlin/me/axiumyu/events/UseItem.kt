package me.axiumyu.events


import me.axiumyu.CommandToItem
import me.axiumyu.CommandToItem.Message
import me.axiumyu.Item
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class UseItem(private val plugin: CommandToItem) : Listener {
//
    @EventHandler
    fun onPlayerUse(event: PlayerInteractEvent) {
        val player: Player = event.player
        if (event.item != null && event.item?.type != Material.AIR) {
            val itemInHand: ItemStack = player.inventory.itemInMainHand

            val item: Item? = plugin.items.find { it.compare(itemInHand) }

            if (item == null) return
            event.isCancelled = true

            if (event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK) {
                return
            }
            if (item.isOnCooldown(event.player)) {
                plugin.getMessage(Message.COOLDOWN).let {
                    event.player.sendMessage(
                        it.replace("%cooldown%", item.getCooldownFor(event.player).toString())
                    )
                }
                return
            }
            if (item.permissionRequired && !event.player.hasPermission("commandtoitem.use.${item.id}")) {
                event.player.sendMessage(plugin.getMessage(Message.NO_PERMISSION))
                return
            }
            if (item.consumed) {
                player.inventory.itemInMainHand.amount--
            }

            item.executeUseActions(player)

        }
    }
}

