package me.axiumyu

import me.axiumyu.commands.BaseCommand
import me.axiumyu.events.UseItem
import me.axiumyu.utlis.ItemParser
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin


class CommandToItem : JavaPlugin() {
    companion object {
        @JvmField
        val cItems = mutableListOf<CItem>()

        @JvmField
        val mm = MiniMessage.miniMessage()

        lateinit var configuration: FileConfiguration
    }

    override fun onEnable() {
        configuration=this.config
        saveDefaultConfig()

        server.getPluginCommand("commandtoitem")?.setExecutor(BaseCommand(this))
        server.pluginManager.registerEvents(UseItem(), this)

        reloadConfig()
    }

    override fun reloadConfig() {
        super.reloadConfig()

        cItems.clear()
        config.getConfigurationSection("items")?.getKeys(false)?.forEach {
            val ist = ItemParser.parseItem(config.getString("items.$it")!!.uppercase(),config, this)

            val consume = config.getBoolean("items.$it.on-use.consume", config.getBoolean("items.$it.consume", true))

            val commands : List<String> = if (config.contains("items.$it.on-use.commands")) {
                config.getStringList("items.$it.on-use.commands")
            } else {
                config.getStringList("items.$it.commands")
            }

            val messages : List<String> = if (config.contains("items.$it.on-use.messages")) {
                config.getStringList("items.$it.on-use.messages")
            } else {
                config.getStringList("items.$it.messages")
            }

            val cooldown = config.getInt("items.$it.on-use.cooldown", config.getInt("items.$it.cooldown", 0))

            val sound : String = config.getString("items.$it.on-use.sound", config.getString("items.$it.sound", "NONE"))?:""

            val permissionRequired = config.getBoolean("items.$it.options.permission-required", false)
            val maxStack = config.getInt("items.$it.options.max-stack", 64)

            cItems.add(CItem(it.replace(" ", "_"), ist, commands, messages, consume, cooldown, sound, permissionRequired,maxStack))
        }
    }
}

