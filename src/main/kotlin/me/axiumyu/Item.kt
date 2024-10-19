package me.axiumyu
import io.papermc.paper.command.brigadier.argument.ArgumentTypes.gameMode
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Bat
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.awt.Component

import java.util.UUID
import java.util.concurrent.TimeUnit

class Item(val id: String, val itemStack: ItemStack,val commands: List<String>,val messages: List<String>,val consumed: Boolean,val cooldown: Int,val sound: String,val permissionRequired: Boolean) {

    private val cooldowns: HashMap<UUID, Long> = HashMap()

    fun executeUseActions(player: Player) {
        playSounds(player)
        executeCommands(player)
        putOnCooldown(player)
        sendMessages(player)
    }

    fun executeCommands(player: Player) {
        for (command in commands) {
            var cmd = command.replace("%player%", player.name)
            cmd = PlaceholderAPI.setPlaceholders(player, cmd)
            if (cmd.contains("executeas:player")) {
                cmd = cmd.replace("executeas:player ", "").replace("executeas:player", "")
                Bukkit.getServer().dispatchCommand(player, cmd)
            } else if (cmd.contains("executeas:console")) {
                cmd = cmd.replace("executeas:console ", "").replace("executeas:console", "")
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().consoleSender, cmd)
            } else {
                Bukkit.getServer().dispatchCommand(Bukkit.getServer().consoleSender, cmd)
            }
        }
    }

    fun playSounds(player: Player) {
        if (sound.isEmpty()) return
        try {
            player.playSound(player.location, Sound.valueOf(sound), 1.0f, 1.0f)
        } catch (_: Exception) {
            // doesn't exist
        }

    }
//
    fun sendMessages(player: Player) {
        for (message in messages) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message
                    .replace("%player%", player.name)
                    .replace("%item%", itemStack.displayName().toString())
                    .replace("%cooldown%", getCooldownFor(player).toString())))
        }
    }

    fun isOnCooldown(player: Player): Boolean {
        if (cooldown <= 0 && cooldowns.containsKey(player.uniqueId)) {
            val timeNow = System.currentTimeMillis()
            val timeExpire = cooldowns[player.uniqueId]!!
            return timeExpire > timeNow
        } else return false
    }

    fun getCooldownFor(player: Player): Long {
        if (cooldowns.containsKey(player.uniqueId)) {
            val timeNow = System.currentTimeMillis()
            val timeExpire = cooldowns[player.uniqueId]!!
            val diff = timeExpire - timeNow

            return TimeUnit.SECONDS.convert(diff, TimeUnit.MILLISECONDS)
        } else return 0
    }

    fun putOnCooldown(player: Player) {
        if (cooldown <= 0) return

        val timeExpire = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(cooldown.toLong(), TimeUnit.SECONDS)
        cooldowns[player.uniqueId] = timeExpire
    }

    fun compare(to: ItemStack): Boolean {
        return itemStack.isSimilar(to)
    }
}
