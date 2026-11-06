
package ruiseki.okstoragecore.upgrades.stack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.gtnewhorizon.gtnhlib.util.data.ItemMeta;

import cpw.mods.fml.common.registry.GameRegistry;

public class StackUpgradeConfig {

    private final Supplier<List<String>> nonStackableItemsSupplier;

    private List<String> cachedEntries;

    private final Set<Item> allMetadataItems = new HashSet<>();
    private final Set<ItemMeta> metadataItems = new HashSet<>();

    public StackUpgradeConfig(Supplier<List<String>> nonStackableItemsSupplier) {
        this.nonStackableItemsSupplier = nonStackableItemsSupplier;
    }

    private void updateCache() {
        List<String> entries = nonStackableItemsSupplier.get();

        if (entries == null) {
            entries = Collections.emptyList();
        }

        if (entries.equals(cachedEntries)) {
            return;
        }

        Set<Item> newAllMetadataItems = new HashSet<>();
        Set<ItemMeta> newMetadataItems = new HashSet<>();

        for (String entry : entries) {
            if (entry == null) {
                continue;
            }

            String[] parts = entry.trim()
                .split(":", -1);

            if (parts.length < 2 || parts.length > 3) {
                continue;
            }

            Item item = GameRegistry.findItem(parts[0], parts[1]);

            if (item == null) {
                continue;
            }

            if (parts.length == 2 || "*".equals(parts[2])) {
                newAllMetadataItems.add(item);
                continue;
            }

            try {
                int meta = Integer.parseInt(parts[2]);
                newMetadataItems.add(new ItemMeta(item, meta));
            } catch (NumberFormatException ignored) {
                // Invalid metadata
            }
        }

        newMetadataItems.removeIf(itemMeta -> newAllMetadataItems.contains(itemMeta.getItem()));

        allMetadataItems.clear();
        allMetadataItems.addAll(newAllMetadataItems);

        metadataItems.clear();
        metadataItems.addAll(newMetadataItems);

        cachedEntries = new ArrayList<>(entries);
    }

    public boolean canStackItem(ItemStack stack) {
        if (stack == null || stack.getItem() == null) {
            return false;
        }

        updateCache();

        Item item = stack.getItem();

        if (allMetadataItems.contains(item)) {
            return false;
        }

        return !metadataItems.contains(new ItemMeta(stack));
    }

    public void clearCache() {
        cachedEntries = null;
        allMetadataItems.clear();
        metadataItems.clear();
    }
}
