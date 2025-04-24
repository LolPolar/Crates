package me.polar.crates.menu;

import lombok.Getter;
import me.polar.crates.data.Crate;
import me.polar.crates.data.CrateReward;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.infusedpvp.commons.item.ItemBuilder;
import org.infusedpvp.commons.utils.StringUtil;
import org.infusedpvp.commons.utils.inventory.ClickableItem;
import org.infusedpvp.commons.utils.inventory.SmartInventory;
import org.infusedpvp.commons.utils.inventory.content.InventoryContents;
import org.infusedpvp.commons.utils.inventory.content.InventoryProvider;
import org.infusedpvp.commons.utils.inventory.content.Pagination;
import org.infusedpvp.commons.utils.inventory.content.SlotIterator;

import java.util.Comparator;

@Getter
public class CratePreviewMenu implements InventoryProvider {

    private final Crate crate;
    private final SmartInventory inventory;

    public CratePreviewMenu(Crate crate) {
        this.crate = crate;
        this.inventory = SmartInventory.builder()
                .id("crate-preview")
                .provider(this)
                .size(5, 9)
                .title(StringUtil.color("Previewing Crate: " + crate.getInternalName()))
                .build();
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        contents.fillBorders(ClickableItem.empty(new ItemBuilder(Material.STAINED_GLASS_PANE).durability(7)));

        Pagination pagination = contents.pagination();
        pagination.setItems(crate.getCrateRewards().stream()
                .sorted(Comparator.comparing(CrateReward::getChance))
                .map(reward -> {
                    ItemBuilder builder = new ItemBuilder(reward.getItemStack())
                            .clearEnchantments()
                            .clearLore()
                            .lore("",
                                    "&fChance: &b??.??%",
                                    "&fAmount: &b" + reward.getAmount(),
                                    "");

                    return ClickableItem.empty(builder);
                }).toArray(ClickableItem[]::new));

        pagination.setItemsPerPage(27);
        pagination.addToIterator(contents.newIterator(SlotIterator.Type.HORIZONTAL, 1, 0));

        contents.set(4, 2, ClickableItem.of(
                new ItemBuilder(Material.PAPER)
                        .name("&cPrevious Page")
                        .lore("&7Click me to go to the previous page!"),
                event -> {
                    if (pagination.isFirst()) {
                        player.sendMessage("&cThis is the first page.");
                        return;
                    }
                    inventory.open(player, pagination.previous().getPage());
                }));

        contents.set(4, 6, ClickableItem.of(
                new ItemBuilder(Material.PAPER)
                        .name("&aNext Page")
                        .lore("&7Click me to go to the next page!"),
                event -> {
                    if (pagination.isLast()) {
                        player.sendMessage("&cThis is the last page.");
                        return;
                    }
                    inventory.open(player, pagination.next().getPage());
                }));
    }

}
