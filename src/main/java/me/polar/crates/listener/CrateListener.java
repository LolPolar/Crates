package me.polar.crates.listener;

import lombok.RequiredArgsConstructor;
import me.polar.crates.CratePlugin;
import me.polar.crates.data.Crate;
import me.polar.crates.data.CrateReward;
import me.polar.crates.menu.CratePreviewMenu;
import me.polar.crates.manager.CrateManager;
import me.polar.crates.util.RewardUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.infusedpvp.commons.item.ItemNames;

import java.util.Optional;
import java.util.stream.IntStream;

@RequiredArgsConstructor
public class CrateListener implements Listener {

    private final CrateManager crateManager;

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();

        if (item == null || item.getType() == Material.AIR) return;

        Optional<Crate> crateOpt = crateManager.getCratePlacer(item);
        if (!crateOpt.isPresent()) {
            return;
        }

        if (!player.isOp()) {
            event.setCancelled(true);
            return;
        }

        Crate crate = crateOpt.get();
        crate.setLocation(event.getBlock().getLocation());
        crate.setupCrate();

        player.sendMessage(CratePlugin.prefix("&7You have placed this {0} &7crate location!", crate.getDisplayName()));
        player.setItemInHand(null);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block == null || block.getType() == Material.AIR || !block.hasMetadata("crate")) return;

        Optional<Crate> crateOpt = crateManager.getCrate(block);
        if (!crateOpt.isPresent()) {
            return;
        }

        Crate crate = crateOpt.get();
        String action = event.getAction().name();
        event.setCancelled(true);

        if (player.isOp() && action.contains("LEFT") && player.isSneaking() && player.getGameMode() == GameMode.CREATIVE) {
            crate.removeCrate();
            player.sendMessage(CratePlugin.prefix("&7You have removed this {0} &7crate location!", crate.getDisplayName()));
            return;
        }

        if (action.contains("LEFT")) {
            new CratePreviewMenu(crate).getInventory().open(player);
            return;
        }

        if (action.contains("RIGHT")) {
            ItemStack hand = event.getItem();

            if (hand == null || hand.getType() == Material.AIR) {
                player.sendMessage(CratePlugin.prefix("&cYou must be using the {0} &ccrate key!", crate.getDisplayName()));
                return;
            }

            Optional<Crate> keyCrateOpt = crateManager.getCrate(hand);
            if (!keyCrateOpt.isPresent()) {
                player.sendMessage(CratePlugin.prefix("&cYou must be using the {0} &ccrate key!", crate.getDisplayName()));
                return;
            }

            if (!keyCrateOpt.get().getInternalName().equalsIgnoreCase(crate.getInternalName())) {
                player.sendMessage(CratePlugin.prefix("&cYou must be using the {0} &ccrate key!", crate.getDisplayName()));
                return;
            }

            if (hand.getAmount() > 1) {
                hand.setAmount(hand.getAmount() - 1);
            } else {
                player.setItemInHand(null);
            }

            giveReward(player, crate);
        }
    }

    private void giveReward(Player player, Crate crate) {
        IntStream.range(0, crate.getRewardsPerCrate()).forEach(i -> {
            CrateReward reward = RewardUtils.getRandomReward(crate.getCrateRewards());

            reward.getCommands().forEach(cmd ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd.replace("%player%", player.getName()))
            );

            player.sendMessage(CratePlugin.prefix("You have opened your {0} Crate&7 and received &f{1} {2}&7!",
                    crate.getDisplayName(),
                    reward.getAmount(),
                    ItemNames.lookup(reward.getItemStack())
            ));
        });
    }
}
