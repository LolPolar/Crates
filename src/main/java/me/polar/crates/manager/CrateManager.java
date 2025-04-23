package me.polar.crates.manager;

import com.massivecraft.factions.zcore.nbtapi.NBTItem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.polar.crates.data.Crate;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Getter
@Setter
public class CrateManager {

    private Map<String, Crate> crates = new HashMap<>();

    public Optional<Crate> getCrate(Block block) {
        if (!block.hasMetadata("crate")) {
            return Optional.empty();
        }
        String internalName = block.getMetadata("crate").get(0).asString();
        if (!crates.containsKey(internalName)) {
            return Optional.empty();
        }
        return Optional.of(crates.get(internalName));
    }

    public Optional<Crate> getCrate(ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack);
        if (!nbtItem.hasKey("crateKey")) {
            return Optional.empty();
        }
        String internalName = nbtItem.getString("crateKey");
        if (!crates.containsKey(internalName)) {
            return Optional.empty();
        }
        return Optional.of(crates.get(internalName));
    }

    public Optional<Crate> getCratePlacer(ItemStack itemStack) {
        NBTItem nbtItem = new NBTItem(itemStack);
        if (!nbtItem.hasKey("cratePlacer1")) {
            return Optional.empty();
        }
        String internalName = nbtItem.getString("cratePlacer1");
        if (!crates.containsKey(internalName)) {
            return Optional.empty();
        }
        return Optional.of(crates.get(internalName));
    }

}
