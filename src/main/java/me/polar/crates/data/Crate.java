package me.polar.crates.data;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import com.massivecraft.factions.zcore.nbtapi.NBTItem;
import lombok.Data;
import me.polar.crates.CratePlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.infusedpvp.commons.utils.StringUtil;

import java.util.List;

@Data
public class Crate {

    private final String internalName;
    private final String displayName;
    private final Material material;
    private final List<String> hologramLines;
    private final List<CrateReward> crateRewards;
    private final int rewardsPerCrate;
    private ItemStack crateKey;
    private Location location;

    private Hologram hologram;

    public void setupCrate() {
        if (location != null) {
            hologram = HologramsAPI.createHologram(CratePlugin.getInstance(), location.clone().add(0.5, 2.4, 0.5));
            hologramLines.stream().map(StringUtil::color).forEach(string -> hologram.appendTextLine(string));
            location.getBlock().setMetadata("crate", new FixedMetadataValue(CratePlugin.getInstance(), internalName));
        }

        if (crateKey != null) {
            NBTItem nbtItem = new NBTItem(crateKey.clone());
            nbtItem.setString("crateKey", internalName);
            crateKey = nbtItem.getItem();
        }
    }

    public void removeCrate() {
        if (location != null) {
            if (location.getBlock().hasMetadata("crate")) {
                location.getBlock().removeMetadata("crate", CratePlugin.getInstance());
            }

            if (hologram != null) {
                hologram.delete();
                hologram = null;
            }

            location = null;
        }
    }

}
