package ruiseki.okbackpack.common.init;

import static ruiseki.okbackpack.common.init.TierRegistries.DIAMOND;
import static ruiseki.okbackpack.common.init.TierRegistries.GOLD;
import static ruiseki.okbackpack.common.init.TierRegistries.IRON;
import static ruiseki.okbackpack.common.init.TierRegistries.LEATHER;
import static ruiseki.okbackpack.common.init.TierRegistries.OBSIDIAN;

import java.util.function.Supplier;

import net.minecraft.block.Block;

import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.tier.TierRegistry;
import ruiseki.okbackpack.common.block.BlockBackpack;
import ruiseki.okbackpack.common.block.BlockSleepingBag;
import ruiseki.okcore.block.IBlock;
import ruiseki.okcore.registries.DeferredRegister;
import ruiseki.okcore.registries.RegistryObject;
import ruiseki.okcore.tag.Registries;

public final class OKBackpackBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, Reference.MOD_ID);

    public static final RegistryObject<Block> BACKPACK_BASE = register(
        "leather_backpack",
        () -> new BlockBackpack(TierRegistry.getTier(LEATHER)));

    public static final RegistryObject<Block> BACKPACK_IRON = register(
        "iron_backpack",
        () -> new BlockBackpack(TierRegistry.getTier(IRON)));

    public static final RegistryObject<Block> BACKPACK_GOLD = register(
        "gold_backpack",
        () -> new BlockBackpack(TierRegistry.getTier(GOLD)));

    public static final RegistryObject<Block> BACKPACK_DIAMOND = register(
        "diamond_backpack",
        () -> new BlockBackpack(TierRegistry.getTier(DIAMOND)));

    public static final RegistryObject<Block> BACKPACK_OBSIDIAN = register(
        "obsidian_backpack",
        () -> new BlockBackpack(TierRegistry.getTier(OBSIDIAN)));

    public static final RegistryObject<Block> SLEEPING_BAG = register("sleeping_bag", BlockSleepingBag::new);

    private static RegistryObject<Block> register(String name, Supplier<IBlock> blockSupplier) {
        return register(name, () -> true, blockSupplier);
    }

    private static RegistryObject<Block> register(String name, Supplier<Boolean> configCondition,
        Supplier<IBlock> blockSupplier) {
        if (!configCondition.get()) {
            return RegistryObject.empty();
        }

        return BLOCKS.register(
            name,
            () -> blockSupplier.get()
                .get());
    }

    public static void register() {
        BLOCKS.register();
    }

    private OKBackpackBlocks() {}
}
