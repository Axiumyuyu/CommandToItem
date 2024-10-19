package me.axiumyu

import com.google.common.io.ByteStreams
import me.axiumyu.commands.BaseCommand
import me.axiumyu.events.UseItem
import me.axiumyu.utlis.ItemParser
import me.axiumyu.utlis.LegacyColor2MiniMessage.replaceColor
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class CommandToItem : JavaPlugin() {

    val items = mutableListOf<Item>()
//
    override fun onEnable() {
        val directory = File(dataFolder.toString())
        if (!directory.exists() && !directory.isDirectory) {
            directory.mkdir()
        }

        val config = File(dataFolder, "config.yml")
        if (!config.exists()) {
            try {
                config.createNewFile()
                CommandToItem::class.java.classLoader.getResourceAsStream("config.yml")?.use { input ->
                    FileOutputStream(config).use { output ->
                        ByteStreams.copy(input, output)
                    }
                } ?: run {
                    logger.severe("Failed to create config.yml.")
                    logger.severe("please delete the CommandToItem directory and try RESTARTING (not reloading).")
                }
            } catch (e: IOException) {
                logger.severe("Failed to create config.yml.")
                e.printStackTrace()
                logger.severe("please delete the CommandToItem directory and try RESTARTING (not reloading).")
            }
        }

        server.getPluginCommand("commandtoitem")?.setExecutor(BaseCommand(this))
        server.pluginManager.registerEvents(UseItem(this), this)

        reloadConfig()
    }

    fun getMessage(message: Message): String {
        return config.getString("messages.${message.id}", message.def)?.replaceColor()?:""
    }

    override fun reloadConfig() {
        super.reloadConfig()

        items.clear()
        config.getConfigurationSection("items")?.getKeys(false)?.forEach { s ->
            val ist = ItemParser.parseItem(config.getString("items.$s")!!.uppercase(),config, this)

            val consume = config.getBoolean("items.$s.on-use.consume", config.getBoolean("items.$s.consume", true))

            val commands : List<String> = if (config.contains("items.$s.on-use.commands")) {
                config.getStringList("items.$s.on-use.commands")
            } else {
                config.getStringList("items.$s.commands")
            }

            val messages : List<String> = if (config.contains("items.$s.on-use.messages")) {
                config.getStringList("items.$s.on-use.messages")
            } else {
                config.getStringList("items.$s.messages")
            }

            val cooldown = config.getInt("items.$s.on-use.cooldown", config.getInt("items.$s.cooldown", 0))

            val sound : String = config.getString("items.$s.on-use.sound", config.getString("items.$s.sound", "NONE"))?:""

            val permissionRequired = config.getBoolean("items.$s.options.permission-required", false)

            items.add(Item(s.replace(" ", "_"), ist, commands, messages, consume, cooldown, sound, permissionRequired))
        }
    }

    enum class Message(val id: String, val def: String) {
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
        ITEM_LIMITS("item-limits", "&cPlease enter an amount between &4%min%&c and &4%max%&c.")
    }
}

