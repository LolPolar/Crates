package me.polar.crates.loader;

import lombok.RequiredArgsConstructor;
import me.polar.crates.CratePlugin;
import me.polar.crates.data.Crate;
import me.polar.crates.data.CrateReward;
import me.polar.crates.manager.CrateManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.infusedpvp.commons.item.ItemBuilder;
import org.infusedpvp.commons.utils.LocUtil;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("ALL")
@RequiredArgsConstructor
public class CrateLoader {

    private final CrateManager crateManager;

    public static ItemStack deserializeKey(ConfigurationSection section) {
        Material material = Material.matchMaterial(section.getString("material"));
        if (material == null) return null;

        return new ItemBuilder(material, (byte) section.getInt("data", 0))
                .name(section.getString("display-name", ""))
                .lore(section.getStringList("lore"));
    }

    public boolean loadCrates(CratePlugin plugin) {
        crateManager.getCrates().clear();

        File cratesDir = new File(plugin.getDataFolder(), "crates");
        if (!cratesDir.exists()) cratesDir.mkdirs();

        File[] files = Optional.ofNullable(cratesDir.listFiles((dir, name) -> name.endsWith(".yml")))
                .orElse(new File[0]);

        if (files.length == 0) {
            createDefaultCrate(plugin);
            return false;
        }

        Stream.of(files).forEach(file -> {
            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
                String internalName = file.getName().replace(".yml", "");
                Material material = Material.matchMaterial(config.getString("material"));

                Crate crate = new Crate(
                        internalName.toLowerCase(),
                        config.getString("display-name"),
                        material == null ? Material.CHEST : material,
                        config.getStringList("hologram-lines"),
                        loadRewards(config.getConfigurationSection("rewards")),
                        config.getInt("rewards-per-crate")
                );

                Optional.ofNullable(config.getConfigurationSection("crate-key"))
                        .map(CrateLoader::deserializeKey)
                        .ifPresent(crate::setCrateKey);

                Optional.ofNullable(config.getString("location"))
                        .map(LocUtil::deserializeLocation)
                        .ifPresent(crate::setLocation);

                crate.setupCrate();

                crateManager.getCrates().put(internalName.toLowerCase(), crate);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load crate file: " + file.getName());
                e.printStackTrace();
            }
        });

        return true;
    }

    public void saveCrates(CratePlugin plugin) {
        File cratesDir = new File(plugin.getDataFolder(), "crates");
        if (!cratesDir.exists()) cratesDir.mkdirs();

        crateManager.getCrates().forEach((internalName, crate) -> {
            File file = new File(cratesDir, internalName + ".yml");
            YamlConfiguration config = new YamlConfiguration();

            config.set("material", crate.getMaterial().name());
            config.set("display-name", crate.getDisplayName());
            config.set("hologram-lines", crate.getHologramLines());
            config.set("rewards-per-crate", crate.getRewardsPerCrate());

            if (crate.getCrateKey() != null && crate.getCrateKey().getType() != Material.AIR) {
                ItemStack key = crate.getCrateKey();
                ItemBuilder keyBuilder = new ItemBuilder(key);
                config.set("crate-key.material", key.getType().name());
                config.set("crate-key.data", key.getDurability());
                config.set("crate-key.display-name", key.getItemMeta().hasDisplayName() ? key.getItemMeta().getDisplayName() : "");
                config.set("crate-key.lore", key.getItemMeta().hasLore() ? key.getItemMeta().getLore() : Collections.emptyList());
            }

            if (crate.getLocation() != null) {
                config.set("location", LocUtil.serializeLocation(crate.getLocation()));
            }

            ConfigurationSection rewardsSection = config.createSection("rewards");
            for (CrateReward reward : crate.getCrateRewards()) {
                ConfigurationSection rewardSection = rewardsSection.createSection(reward.getInternalName());
                ItemStack item = reward.getItemStack();

                rewardSection.set("material", item.getType().name());
                rewardSection.set("data", item.getDurability());
                rewardSection.set("display-name", item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : "");
                rewardSection.set("lore", item.getItemMeta().hasLore() ? item.getItemMeta().getLore() : Collections.emptyList());
                rewardSection.set("amount", reward.getAmount());
                rewardSection.set("chance", reward.getChance());
                rewardSection.set("commands", reward.getCommands());
            }

            try {
                config.save(file);
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to save crate file: " + file.getName());
                e.printStackTrace();
            }
        });
    }

    private void createDefaultCrate(CratePlugin plugin) {
        File exampleFile = new File(plugin.getDataFolder(), "crates/example.yml");
        if (!exampleFile.exists()) {
            YamlConfiguration exampleConfig = new YamlConfiguration();
            exampleConfig.set("material", "CHEST");
            exampleConfig.set("display-name", "&b&lExample");
            exampleConfig.set("hologram-lines", Arrays.asList("&7Right Click to open this crate.",
                    "&7Left Click to preview this crate."));
            exampleConfig.set("rewards-per-crate", 1);
            exampleConfig.set("crate-key.material", "TRIPWIRE_HOOK");
            exampleConfig.set("crate-key.display-name", "&b&lExample Crate Key");
            exampleConfig.set("crate-key.lore", Arrays.asList("&7Right Click to open the example crate."));

            ConfigurationSection rewardsSection = exampleConfig.createSection("rewards.example");
            rewardsSection.set("material", "DIAMOND");
            rewardsSection.set("display-name", "&b&lExample");
            rewardsSection.set("amount", 1);
            rewardsSection.set("chance", 50);
            rewardsSection.set("commands", Arrays.asList("bc %player% test"));

            try {
                exampleConfig.save(exampleFile);
                plugin.getLogger().info("Created example crate configuration: example.yml");
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to create example crate configuration.");
                e.printStackTrace();
            }
        }
    }

    private List<CrateReward> loadRewards(ConfigurationSection rewardsSection) {
        if (rewardsSection == null) return Collections.emptyList();

        return rewardsSection.getKeys(false).stream()
                .map(rewardsSection::getConfigurationSection)
                .filter(Objects::nonNull)
                .map(section -> CrateReward.deserialize(section.getName(), section))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
