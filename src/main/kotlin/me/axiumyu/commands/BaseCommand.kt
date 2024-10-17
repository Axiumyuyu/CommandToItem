package me.axiumyu.commands

import me.axiumyu.CommandToItem
import me.axiumyu.Item
import org.bukkit.Bukkit
import org.bukkit.ChatColor.*
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.StringUtil

class BaseCommand(private val plugin: CommandToItem) : CommandExecutor, TabCompleter {

    private var nameCache: MutableList<String> = mutableListOf()
    private val maxAllowedItems = Int.MAX_VALUE

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<out String>): Boolean {
        if (cmd.label.equals("commandtoitem", ignoreCase = true)) {
            if (args.isNotEmpty()) {
                when (args[0]) {
                    "list" -> {
                        val ids = plugin.items.map { it.id }
                        sender.sendMessage(
                            plugin.getMessage(CommandToItem.Message.ITEM_LIST)
                                .replace("%items%", ids.joinToString(", "))
                        )
                        return true
                    }

                    "reload" -> {
                        plugin.reloadConfig()
                        refreshNameCache()
                        sender.sendMessage(plugin.getMessage(CommandToItem.Message.RELOAD))
                        return true
                    }
                }

                val target: Player? = if (args.size >= 2) {
                    Bukkit.getServer().onlinePlayers.find { it.name.equals(args[1], ignoreCase = true) }
                } else {
                    null
                }

                if (target == null) {
                    sender.sendMessage(plugin.getMessage(CommandToItem.Message.PLAYER_NOT_FOUND))
                    return true
                }

                val amount = args.getOrNull(2)?.toIntOrNull() ?: 1

                val item = getItemByName(args[0])
                if (item == null) {
                    sender.sendMessage(
                        plugin.getMessage(CommandToItem.Message.ITEM_NOT_FOUND).replace("%item%", args[0])
                    )
                    return true
                }

                if (amount > maxAllowedItems || amount < 1) {
                    sender.sendMessage(
                        plugin.getMessage(CommandToItem.Message.ITEM_LIMITS).replace("%min%", "1")
                            .replace("%max%", maxAllowedItems.toString())
                    )
                    return true
                }

                val itemStack = item.itemStack

                if (!plugin.config.getBoolean("options.drop-if-full-inventory", false) && !canAddItems(
                        amount, target, itemStack
                    )) {
                    sender.sendMessage(
                        plugin.getMessage(CommandToItem.Message.FULL_INV).replace("%player%", target.name)
                    )
                    return true
                }

                var addedItems = 0
                repeat(amount) {
                    if (!canAddItems(1, target, itemStack)) {
                        target.world.dropItem(target.location, item.itemStack)
                    } else {
                        target.inventory.addItem(itemStack)
                        addedItems++
                    }
                }

                val lostItems = amount - addedItems
                if (plugin.config.getBoolean("options.show-receive-message", true)) {
                    if (lostItems > 0) {
                        target.sendMessage(
                            plugin.getMessage(CommandToItem.Message.RECEIVE_ITEM_INVENTORY_FULL)
                                .replace("%item%", item.itemStack.displayName().toString())
                                .replace("%given_amount%", amount.toString())
                                .replace("%dropped_amount%", lostItems.toString())
                        )
                    } else {
                        target.sendMessage(
                            plugin.getMessage(CommandToItem.Message.RECEIVE_ITEM)
                                .replace("%player%", target.name)
                                .replace("%item%", item.itemStack.displayName().toString())
                                .replace("%amount%", amount.toString())
                        )
                    }
                }

                sender.sendMessage(
                    plugin.getMessage(CommandToItem.Message.GIVE_ITEM)
                        .replace("%player%", target.name)
                        .replace("%item%", item.itemStack.displayName().toString())
                        .replace("%amount%", amount.toString())
                )
                return true
            }

            sender.sendMessage("${GOLD}${BOLD}Command To Item (ver ${plugin.pluginMeta.version})")
            sender.sendMessage("${GRAY}<> = required, [] = optional")
            sender.sendMessage("${YELLOW}/cti :${GRAY} view this menu")
            sender.sendMessage(
                "${YELLOW}/cti <item> <player> [amount] :${GRAY} give [amount] of <item> to <player> (or 1 if no amount specified)"
            )
            sender.sendMessage("${YELLOW}/cti list :${GRAY} list all items")
            sender.sendMessage("${YELLOW}/cti reload :${GRAY} reload the config.yml")
            return true
        }
        return true
    }

    override fun onTabComplete(
        sender: CommandSender, cmd: Command, alias: String, args: Array<out String>
    ): List<String>? {
        if (nameCache.isEmpty()) {
            refreshNameCache()
        }
        return when {
            args.size < 2 -> {
                val completions = mutableListOf<String>()
                val options = nameCache.toMutableList()
                options.add("reload")
                options.add("list")
                StringUtil.copyPartialMatches(args[0], nameCache, completions)
                completions.sorted()
            }

            args.size == 3 -> {
                val item = getItemByName(args[0])
                val amounts = mutableListOf<String>()
                if (item == null) return emptyList()

                val maxStackSize = item.itemStack.maxStackSize
                for (i in 1..maxStackSize) {
                    amounts.add(i.toString())
                }
                if (!amounts.contains("64")) amounts.add("64")

                val completions = mutableListOf<String>()
                StringUtil.copyPartialMatches(args[2], amounts, completions)
                completions.sorted()
            }

            else -> null
        }
    }

    private fun refreshNameCache() {
        nameCache = plugin.items.map { it.id }.toMutableList()
    }

    private fun getItemByName(name: String): Item? {
        return plugin.items.find { it.id == name }
    }

    private fun canAddItems(amount: Int, target: Player, itemToAdd: ItemStack): Boolean {
        var amountAbleToAdd = 0
        for (i in 0 until 36) {
            val usersItemStack = target.inventory.getItem(i)
            when {
                usersItemStack == null -> amountAbleToAdd += itemToAdd.maxStackSize
                usersItemStack.isSimilar(itemToAdd) -> amountAbleToAdd += itemToAdd.maxStackSize - usersItemStack.amount
            }
            if (amountAbleToAdd >= amount) break
        }
        return amountAbleToAdd >= amount
    }
}

