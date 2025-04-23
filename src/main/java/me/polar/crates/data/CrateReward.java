package me.polar.crates.data;

import lombok.Data;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.infusedpvp.commons.item.ItemBuilder;

import java.util.List;

@Data
public class CrateReward {

    private final String internalName;
    private final ItemStack itemStack;
    private final List<String> commands;
    private final int amount;
    private final double chance;

    public static CrateReward deserialize(String internalName, ConfigurationSection section) {
        String materialName = section.getString("material");
        int data = section.getInt("data", 0);
        String displayName = section.getString("display-name", "");
        int amount = section.getInt("amount", 1);
        List<String> lore = section.getStringList("lore");
        List<String> commands = section.getStringList("commands");
        double chance = section.getDouble("chance");

        Material material = Material.matchMaterial(materialName);
        if (material == null) return null;

        ItemStack item = new ItemBuilder(material, (byte) data)
                .name(displayName)
                .lore(lore);

        return new CrateReward(internalName, item, commands, amount, chance);
    }

}
