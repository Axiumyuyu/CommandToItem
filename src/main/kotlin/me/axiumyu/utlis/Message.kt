package me.axiumyu.utlis

import me.axiumyu.CommandToItem.Companion.configuration
import me.axiumyu.utlis.Legacy2MiniMessage.replaceColor

enum class Message(val id: String, val msg: String) {
    FULL_INV("full-inv", "&c%player% doesn't have enough space in their inventory!"),
    GIVE_ITEM("give-item", "&6Given &e%player% &6%amount% %item%&6."),
    RECEIVE_ITEM("receive-item", "&6You have been given %amount% %item%&6."),
    RECEIVE_ITEM_INVENTORY_FULL("receive-item-inventory-full", "&6You have been given %item%&6, but it was dropped at your feet because your inventory is full."),
    COOLDOWN("cooldown", "&6You have been given %given_amount% %item%&6, but %dropped_amount% dropped at your feet because your inventory is full."),
    NO_PERMISSION("no-permission", "&cYou cannot use this item."),
    ITEM_LIST("item-list", "&6Items: &e%items%"),
    RELOAD("reload", "&7CommandToItem has been reloaded"),
    PLAYER_NOT_FOUND("player-not-found", "&cThe specified player could not be found."),
    ITEM_NOT_FOUND("item-not-found", "&cThe item &4%item%&c could not be found."),
    ITEM_LIMITS("item-limits", "&cPlease enter an amount between &4%min%&c and &4%max%&c.");

    companion object{
        @JvmStatic
        fun getMessage(message: Message): String {
            return (configuration.getString("messages.${message.id}", message.msg)?.replaceColor()) ?: ""
        }
    }
}