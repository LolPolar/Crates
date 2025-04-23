package me.polar.crates.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import com.infusedpvp.commons.nbtapi.NBTItem;
import lombok.RequiredArgsConstructor;
import me.polar.crates.CratePlugin;
import me.polar.crates.data.Crate;
import me.polar.crates.manager.CrateManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.infusedpvp.commons.utils.inventory.InvUtil;

import java.util.stream.IntStream;

@CommandAlias("crate")
@CommandPermission("crate.admin")
@RequiredArgsConstructor
public class CrateCommand extends BaseCommand {

    private final CrateManager crateManager;

    @Subcommand("givekey")
    @CommandCompletion("@players @crates")
    public void onGiveKey(CommandSender sender, OnlinePlayer target, String internalName, @Default("1") int amount) {
        Crate crate = crateManager.getCrates().get(internalName);
        if (crate == null || crate.getCrateKey() == null) {
            sender.sendMessage(CratePlugin.prefix("&cThis crate doesn't exist or the key is broken!"));
            return;
        }

        ItemStack key = crate.getCrateKey().clone();
        IntStream.range(0, amount).forEach(i -> InvUtil.addItems(target.getPlayer(), key));

        sender.sendMessage(CratePlugin.prefix("&7You have given {0} &f{1} {2} &7crate keys.", target.getPlayer().getDisplayName(), amount, crate.getDisplayName()));
        target.getPlayer().sendMessage(CratePlugin.prefix("&7You received &f{0}x {1} &7crate keys!", amount, crate.getDisplayName()));
    }

    @Subcommand("giveplacer")
    @CommandCompletion("@players @crates")
    public void onGivePlacer(Player sender, String internalName) {
        Crate crate = crateManager.getCrates().get(internalName);
        if (crate == null) {
            sender.sendMessage(CratePlugin.prefix("&cThis crate doesn't exist!"));
            return;
        }

        ItemStack placer = new ItemStack(crate.getMaterial());
        NBTItem nbt = new NBTItem(placer);
        nbt.setString("cratePlacer1", internalName);
        placer = nbt.getItem();

        sender.getInventory().addItem(placer);
    }

    @Subcommand("reload")
    public void onReload(CommandSender sender) {
        CratePlugin.getInstance().getCrateLoader().loadCrates(CratePlugin.getInstance());
        sender.sendMessage(CratePlugin.prefix("&aCrates have been reloaded."));
    }

}
