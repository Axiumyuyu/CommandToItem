package me.axiumyu.utlis

import io.papermc.paper.registry.RegistryAccess.registryAccess
import io.papermc.paper.registry.RegistryKey.ENCHANTMENT
import me.axiumyu.CommandToItem.Companion.mm
import me.axiumyu.utlis.Legacy2MiniMessage.replaceColor
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import kotlin.collections.get
import kotlin.random.Random

object ItemParser {

    fun parseItem(path: String, config: FileConfiguration, plugin: JavaPlugin): ItemStack {
        val cName = config.getString("$path.name")?:"empty name"
        val cType = config.getString("$path.item")?:"STONE"
        val cLore = config.getStringList("$path.lore")
        val cItemFlags = config.getStringList("$path.itemflags")

        // material
        val type = Material.getMaterial(cType) ?: Material.STONE

        val itemStack = ItemStack(type)
        itemStack.editMeta {
            it.lore(cLore.map<String, Component> { mm.deserialize(it.replaceColor()) })
            it.displayName(mm.deserialize(cName.replaceColor()))

            // custom model data
            if (config.contains("$path.custommodeldata")) {
                it.setCustomModelData(config.getInt("$path.custommodeldata", 0))
            }

            // attribute modifiers
            if (config.contains("$path.attributemodifiers")) {
                val cAttributeModifiers = config.getMapList("$path.attributemodifiers")
                for (attr in cAttributeModifiers) {
                    val cAttribute = attr["attribute"] as String
                    val attribute = Attribute.entries.find { it.toString() == cAttribute } ?: continue

                    val configurationSection = attr["modifier"] as Map<*, *>

                    val cNameSpace =
                        configurationSection["namespace"] as String? ?: "unknown:unknown${Random.nextInt(99999)}"
                    val cModifierOperation = configurationSection["operation"] as String? ?: "ADD_NUMBER"
                    val cAmount = configurationSection["amount"] as Double? ?: 1.0

                    val cEquipmentSlotGroup = configurationSection["equipmentslot"] as String? ?: "ANY"
                    val split = cNameSpace.split(":")
                    val namespace = NamespacedKey(split[0], split[1])
                    val equipmentSlotGroup = EquipmentSlotGroup.getByName(cEquipmentSlotGroup)!!
                    val operation = AttributeModifier.Operation.valueOf(cModifierOperation)


                    val modifier = AttributeModifier(namespace, cAmount, operation, equipmentSlotGroup)
                    it.addAttributeModifier(attribute, modifier)
                }
            }

            // item flags
            if (config.isSet("$path.itemflags")) {
                cItemFlags.forEach { itemFlag ->
                    runCatching { it.addItemFlags(ItemFlag.valueOf(itemFlag)) }.getOrDefault(ItemFlag.HIDE_DESTROYS)
                }
            }
            // unbreakable
            it.isUnbreakable = config.getBoolean("$path.unbreakable", false)

            // max stack size
            if (config.isSet("$path.maxstack")) {
                if (config.getInt("$path.maxstack") <= 99 && config.getInt("$path.maxstack") > 0) {
                    it.setMaxStackSize(config.getInt("$path.maxstack"))
                }
            }

            // enchantments
            if (config.isSet("$path.enchantments")) {
                for (key in config.getStringList("$path.enchantments")) {
                    val split = key.split(":")
                    if (split.size < 2) {
                        plugin.logger.warning("Enchantment does not follow format {namespace}:{name}:{level} : $key")
                        continue
                    }
                    val namespacedKey = NamespacedKey(split[0], split[1])
                    val enchantment =
                        registryAccess().getRegistry(ENCHANTMENT).get(namespacedKey) ?: Enchantment.PROTECTION
                    val level = split[2].toIntOrNull() ?: 1
                    it.addEnchant(enchantment, level, true)
                }
            }
        }
        return itemStack
    }
}

