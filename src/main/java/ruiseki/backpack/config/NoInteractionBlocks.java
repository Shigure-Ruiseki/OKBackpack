package ruiseki.backpack.config;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraftforge.oredict.OreDictionary;

import com.gtnewhorizon.gtnhlib.util.data.BlockMeta;

public class NoInteractionBlocks {

    private boolean initialized = false;
    private Set<BlockMeta> noInteractionBlocksSet;

    private final Supplier<String> supplier;

    public NoInteractionBlocks(Supplier<String> supplier) {
        this.supplier = supplier;
    }

    public boolean isBlockInteractionDisallowed(Block block, int meta) {
        if (!initialized) {
            loadDisallowedSet();
        }

        return noInteractionBlocksSet.contains(new BlockMeta(block, meta))
            || noInteractionBlocksSet.contains(new BlockMeta(block, OreDictionary.WILDCARD_VALUE));
    }

    private void loadDisallowedSet() {
        initialized = true;
        noInteractionBlocksSet = new HashSet<>();

        String config = supplier.get();

        if (config == null || config.trim()
            .isEmpty()) {
            return;
        }

        for (String entry : config.split(",")) {
            String[] parts = entry.trim()
                .split(":");

            if (parts.length < 2 || parts.length > 3) {
                continue;
            }

            String registryName = parts[0] + ":" + parts[1];

            Block block = (Block) Block.blockRegistry.getObject(registryName);

            if (block == null) {
                continue;
            }

            int meta = -1;

            if (parts.length == 3) {
                try {
                    meta = Integer.parseInt(parts[2]);
                } catch (NumberFormatException ignored) {
                    continue;
                }
            }

            noInteractionBlocksSet.add(new BlockMeta(block, meta));
        }
    }
}
