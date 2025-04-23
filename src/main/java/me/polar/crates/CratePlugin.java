package me.polar.crates;

import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.PaperCommandManager;
import lombok.Getter;
import me.polar.crates.command.CrateCommand;
import me.polar.crates.data.Crate;
import me.polar.crates.listener.CrateListener;
import me.polar.crates.loader.CrateLoader;
import me.polar.crates.manager.CrateManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.infusedpvp.commons.utils.StringUtil;

import java.util.ArrayList;

@Getter
public class CratePlugin extends JavaPlugin {

    private CrateManager crateManager;
    private CrateLoader crateLoader;
    private PaperCommandManager commandManager;

    public static CratePlugin getInstance() {
        return getPlugin(CratePlugin.class);
    }

    public static String prefix(String string, Object... arguments) {
        return StringUtil.color("&3&lCrates &8» &7" + StringUtil.colorFormat(string, arguments));
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        crateManager = new CrateManager();
        crateLoader = new CrateLoader(crateManager);

        if (crateLoader.loadCrates(this)) {
            Bukkit.getLogger().info("[Crates] Crates have all successfully loaded!");
        } else {
            Bukkit.getLogger().info("[Crates] There was an issue loading crates!");
        }

        registerCommands();
        registerListeners();
    }

    @Override
    public void onDisable() {
        crateLoader.saveCrates(this);
    }

    private void registerCommands() {
        commandManager = new PaperCommandManager(this);

        commandManager.getCommandContexts().registerContext(Crate.class, context -> {
            String arg = context.popFirstArg();
            Crate crate = crateManager.getCrates().get(arg.toLowerCase());

            if (crate != null) {
                return crate;
            }

            throw new InvalidCommandArgument(prefix("&cThe crate '{0}' does not exist.", arg));
        });

        commandManager.getCommandCompletions()
                .registerCompletion("crates", c -> new ArrayList<>(crateManager.getCrates().keySet()));

        commandManager.registerCommand(new CrateCommand(crateManager));
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new CrateListener(crateManager), this);
    }
}
