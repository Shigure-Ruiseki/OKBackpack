package ruiseki.okbackpack.upgrades.toolswapper;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

import org.jetbrains.annotations.Nullable;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.AtomicDouble;
import com.gtnewhorizon.gtnhlib.blockstate.core.BlockState;

import ruiseki.okbackpack.api.IAttackEntityResponseUpgrade;
import ruiseki.okbackpack.api.IBlockClickResponseUpgrade;
import ruiseki.okbackpack.api.IBlockToolSwapUpgrade;
import ruiseki.okbackpack.api.IEntityToolSwapUpgrade;
import ruiseki.okbackpack.backpack.ItemBackpack;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.helper.BlockStateHelpers;
import ruiseki.okcore.helper.Helpers;
import ruiseki.okcore.helper.InventoryHelpers;
import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okcore.helper.NBTHelpers;
import ruiseki.okcore.item.handler.IItemHandlerSimpleInserter;
import ruiseki.okstoragecore.api.IStorageWrapper;
import ruiseki.okstoragecore.upgrades.FilterLogic;
import ruiseki.okstoragecore.upgrades.IFilteredUpgrade;
import ruiseki.okstoragecore.upgrades.UpgradeWrapperBase;

public class ToolSwapperUpgradeWrapper extends UpgradeWrapperBase<ToolSwapperUpgradeWrapper, ItemToolSwapperUpgrade>
    implements IBlockClickResponseUpgrade, IAttackEntityResponseUpgrade, IBlockToolSwapUpgrade, IEntityToolSwapUpgrade,
    IFilteredUpgrade {

    private static final LoadingCache<ItemStack, Boolean> isToolCache = CacheBuilder.newBuilder()
        .expireAfterAccess(1, TimeUnit.MINUTES)
        .build(new CacheLoader<>() {

            @Override
            public Boolean load(ItemStack key) {
                return canPerformToolAction(key);
            }
        });
    private final FilterLogic filterLogic;
    private final int slotCount;
    @Nullable
    private ResourceLocation toolCacheFor = null;
    private final Queue<ItemStack> toolCache = new LinkedList<>();

    private Block lastMinedBlock = Blocks.air;

    protected ToolSwapperUpgradeWrapper(IStorageWrapper storageWrapper, ItemStack upgrade,
        Consumer<ItemStack> upgradeSaveHandler) {
        super(storageWrapper, upgrade, upgradeSaveHandler);
        slotCount = upgradeItem.getSlotCount();
        filterLogic = new FilterLogic(upgrade, upgradeSaveHandler, upgradeItem.getSlotCount());
    }

    public int getSlotCount() {
        return slotCount;
    }

    @Override
    public boolean onBlockClick(EntityPlayer player, BlockPos pos) {
        ToolSwapMode toolSwapMode = getToolSwapMode();
        if (player.capabilities.isCreativeMode || toolSwapMode == ToolSwapMode.NO_SWAP) {
            return false;
        }

        ItemStack mainHandItem = player.getHeldItem();
        if (ItemHelpers.isEmpty(mainHandItem) || ItemHelpers.getItem(mainHandItem) instanceof ItemBackpack
            || (toolSwapMode == ToolSwapMode.ONLY_TOOLS && isSword(mainHandItem))
            || (!isSword(mainHandItem) && isNotTool(mainHandItem))
            || !filterLogic.matchesFilter(mainHandItem)) {
            return false;
        }
        BlockState state = BlockStateHelpers.getState(player.worldObj, pos);
        Block block = pos.getBlock(player.worldObj);

        double mainToolSpeed = 0;
        if (isGoodAtBreakingBlock(state, mainHandItem)) {
            if (lastMinedBlock == block) {
                return true;
            }
            mainToolSpeed = mainHandItem.func_150997_a(block);
        }

        lastMinedBlock = block;

        return tryToSwapTool(player, state, mainToolSpeed, mainHandItem);
    }

    private boolean tryToSwapTool(EntityPlayer player, BlockState state, double mainHandItemSpeed,
        ItemStack mainHandItem) {
        AtomicReference<ItemStack> selectedTool = new AtomicReference<>(ItemHelpers.EMPTY);
        AtomicInteger selectedSlot = new AtomicInteger(-1);
        AtomicDouble bestSpeed = new AtomicDouble(mainHandItemSpeed);
        IItemHandlerSimpleInserter backpackInventory = storageWrapper.getInventoryHandler();
        InventoryHelpers.iterate(backpackInventory, (slot, stack) -> {
            if (ItemHelpers.isEmpty(stack)) {
                return;
            }
            if (isAllowedAndGoodAtBreakingBlock(state, stack)) {
                float destroySpeed = stack.func_150997_a(state.getBlock());
                if (bestSpeed.get() < destroySpeed) {
                    bestSpeed.set(destroySpeed);
                    selectedSlot.set(slot);
                    selectedTool.set(stack);
                }
            }
        });
        ItemStack tool = selectedTool.get();
        if (!ItemHelpers.isEmpty(tool) && hasSpaceInBackpackOrCanPlaceInTheSlotOfSwappedTool(
            backpackInventory,
            mainHandItem,
            tool,
            selectedSlot.get())) {
            player.inventory.setInventorySlotContents(
                player.inventory.currentItem,
                backpackInventory.extractItem(selectedSlot.get(), 1, false));
            backpackInventory.insertItem(mainHandItem, false);
            return true;
        }

        return false;
    }

    private boolean hasSpaceInBackpackOrCanPlaceInTheSlotOfSwappedTool(IItemHandlerSimpleInserter backpackInventory,
        ItemStack mainHandItem, ItemStack tool, int selectedSlot) {
        return (ItemHelpers.isEmpty(backpackInventory.insertItem(mainHandItem, true)))
            || (ItemHelpers.getCount(tool) == 1 && backpackInventory.isItemValid(selectedSlot, mainHandItem));
    }

    private boolean isAllowedAndGoodAtBreakingBlock(BlockState block, ItemStack stack) {
        return filterLogic.matchesFilter(stack) && isGoodAtBreakingBlock(block, stack);
    }

    private boolean isGoodAtBreakingBlock(BlockState state, ItemStack stack) {
        if (ItemHelpers.isEmpty(stack)) return false;

        String harvestTool = state.getBlock()
            .getHarvestTool(state.getBlockMeta(0));

        if (harvestTool != null && !harvestTool.isEmpty()) {
            if (!stack.getItem()
                .getToolClasses(stack)
                .contains(harvestTool)) {
                return false;
            }

            if (stack.getItem()
                .getHarvestLevel(stack, harvestTool)
                < state.getBlock()
                    .getHarvestLevel(state.getBlockMeta(0))) {
                return false;
            }
        }

        return stack.func_150997_a(state.getBlock()) > 1.5F;
    }

    @Override
    public boolean onAttackEntity(AttackEntityEvent event) {
        EntityPlayer player = event.entityPlayer;
        if (!shouldSwapWeapon()) {
            return false;
        }

        ItemStack mainHandItem = player.getHeldItem();
        if (isSword(mainHandItem)) {
            return true;
        }

        if (ItemHelpers.isEmpty(mainHandItem) || ItemHelpers.getItem(mainHandItem) instanceof ItemBackpack
            || isNotTool(mainHandItem)
            || !filterLogic.matchesFilter(mainHandItem)) {
            return false;
        }

        return tryToSwapInWeapon(player, mainHandItem);
    }

    private boolean isNotTool(ItemStack stack) {
        return !isToolCache.getUnchecked(stack);
    }

    private static boolean canPerformToolAction(ItemStack stack) {
        return canPerformToolAction(stack, "axe") || canPerformToolAction(stack, "hoe")
            || canPerformToolAction(stack, "pickaxe")
            || canPerformToolAction(stack, "shovel")
            || (!ItemHelpers.isEmpty(stack) && ItemHelpers.getItem(stack) instanceof ItemShears);
    }

    private static boolean canPerformToolAction(ItemStack stack, String toolClass) {
        if (ItemHelpers.isEmpty(stack)) return false;
        return ItemHelpers.getItem(stack)
            .getToolClasses(stack)
            .contains(toolClass);
    }

    private boolean isSword(ItemStack stack) {
        if (ItemHelpers.isEmpty(stack)) return false;
        return ItemHelpers.getItem(stack) instanceof ItemSword;
    }

    private boolean tryToSwapInWeapon(EntityPlayer player, ItemStack mainHandItem) {
        AtomicReference<ItemStack> bestAxe = new AtomicReference<>(ItemHelpers.EMPTY);
        AtomicDouble bestAxeDamage = new AtomicDouble(0);
        AtomicReference<ItemStack> bestSword = new AtomicReference<>(ItemHelpers.EMPTY);
        AtomicDouble bestSwordDamage = new AtomicDouble(0);

        updateBestWeapons(bestAxe, bestAxeDamage, bestSword, bestSwordDamage, mainHandItem);

        IItemHandlerSimpleInserter backpackInventory = storageWrapper.getInventoryForUpgradeProcessing();
        InventoryHelpers.iterate(backpackInventory, (slot, stack) -> {
            if (filterLogic.matchesFilter(stack)) {
                updateBestWeapons(bestAxe, bestAxeDamage, bestSword, bestSwordDamage, stack);
            }
        });

        if (!ItemHelpers.isEmpty(bestSword.get())) {
            return swapWeapon(player, mainHandItem, backpackInventory, bestSword.get());
        } else if (!ItemHelpers.isEmpty(bestAxe.get())) {
            return swapWeapon(player, mainHandItem, backpackInventory, bestAxe.get());
        }
        return false;
    }

    private void updateBestWeapons(AtomicReference<ItemStack> bestAxe, AtomicDouble bestAxeDamage,
        AtomicReference<ItemStack> bestSword, AtomicDouble bestSwordDamage, ItemStack stack) {
        if (ItemHelpers.isEmpty(stack)) return;

        Multimap<String, AttributeModifier> modifiers = stack.getAttributeModifiers();
        Collection<AttributeModifier> damageModifiers = modifiers
            .get(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName());
        if (damageModifiers.isEmpty()) return;

        double damageValue = 0.0D;
        for (AttributeModifier modifier : damageModifiers) {
            damageValue += modifier.getAmount();
        }

        if (canPerformToolAction(stack, "axe")) {
            if (damageValue > bestAxeDamage.get()) {
                bestAxe.set(stack);
                bestAxeDamage.set(damageValue);
            }
        } else if (isSword(stack) && damageValue > bestSwordDamage.get()) {
            bestSword.set(stack);
            bestSwordDamage.set(damageValue);
        }
    }

    private static float getAttackDamage(ItemStack stack) {
        Multimap<String, AttributeModifier> attributes = stack.getAttributeModifiers();
        Collection<AttributeModifier> modifiers = attributes
            .get(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName());
        float damage = 0F;
        for (AttributeModifier modifier : modifiers) {
            damage += (float) modifier.getAmount();
        }
        return damage;
    }

    private boolean swapWeapon(EntityPlayer player, ItemStack mainHandItem,
        IItemHandlerSimpleInserter backpackInventory, ItemStack sword) {
        if (sword == mainHandItem) {
            return true;
        }

        InventoryHelpers.extractFromInventory(sword, backpackInventory, false);
        if (ItemHelpers.isEmpty(backpackInventory.insertItem(mainHandItem, true))) {
            player.inventory.setInventorySlotContents(player.inventory.currentItem, sword);
            backpackInventory.insertItem(mainHandItem, false);
            return true;
        } else {
            backpackInventory.insertItem(sword, false);
            return false;
        }
    }

    @Override
    public boolean hideSettingsTab() {
        return !upgradeItem.hasSettingsTab();
    }

    @Override
    public FilterLogic getFilterLogic() {
        return filterLogic;
    }

    public boolean shouldSwapWeapon() {
        return NBTHelpers.getBoolean(upgrade, "shouldSwapWeapon")
            .orElse(true);
    }

    public void setSwapWeapon(boolean shouldSwapWeapon) {
        NBTHelpers.setBoolean(upgrade, "shouldSwapWeapon", shouldSwapWeapon);
        save();
    }

    public ToolSwapMode getToolSwapMode() {
        return NBTHelpers.getEnumConstant(upgrade, "toolSwapMode", ToolSwapMode::fromName)
            .orElse(ToolSwapMode.ANY);
    }

    public void setToolSwapMode(ToolSwapMode toolSwapMode) {
        NBTHelpers.setEnumConstant(upgrade, "toolSwapMode", toolSwapMode);
        save();
    }

    @Override
    public boolean onEntityInteract(World world, Entity entity, EntityPlayer player) {
        if (!upgradeItem.shouldSwapToolOnKeyPress()) {
            return false;
        }
        return tryToSwapTool(player, stack -> itemWorksOnEntity(stack, entity), Helpers.getLocation(entity));
    }

    private boolean itemWorksOnEntity(ItemStack stack, Entity entity) {
        if (ItemHelpers.isEmpty(stack)) return false;
        return isShearableEntity(entity, stack) && isShearsItem(stack);
    }

    @Override
    public boolean onBlockInteract(World world, BlockPos pos, BlockState blockState, EntityPlayer player) {
        if (!upgradeItem.shouldSwapToolOnKeyPress()) {
            return false;
        }
        return tryToSwapTool(
            player,
            stack -> itemWorksOnBlock(world, pos, blockState, player, stack),
            Helpers.getLocation(blockState.getBlock()));
    }

    private boolean tryToSwapTool(EntityPlayer player, Predicate<ItemStack> isToolValid,
        @Nullable ResourceLocation targetRegistryName) {
        ItemStack mainHandStack = player.getHeldItem();
        if (mainHandStack.getItem() instanceof ItemBackpack) {
            return false;
        }
        if (toolCacheFor == null || !toolCacheFor.equals(targetRegistryName)) {
            toolCache.clear();
            toolCacheFor = targetRegistryName;
        }

        boolean itemInHandIsValid = isToolValid.test(mainHandStack);
        IItemHandlerSimpleInserter backpackInventory = storageWrapper.getInventoryForUpgradeProcessing();
        if (itemInHandIsValid && toolCache.stream()
            .noneMatch(st -> ItemHelpers.areItemsEqualIgnoreDurability(st, mainHandStack))) {
            toolCache.offer(mainHandStack);
        }
        ItemStack tool = findToolToSwap(backpackInventory, isToolValid);

        if (ItemHelpers.isEmpty(tool)) {
            return false;
        }

        tool = ItemHelpers.split(ItemHelpers.copy(tool), 1);

        if ((ItemHelpers.getCount(tool) == 1
            || ItemHelpers.isEmpty(backpackInventory.insertItem(mainHandStack, true)))) {
            player.inventory.setInventorySlotContents(
                player.inventory.currentItem,
                InventoryHelpers.extractFromInventory(tool, backpackInventory, false));
            backpackInventory.insertItem(mainHandStack, false);
            toolCache.offer(tool);
        }
        return true;
    }

    private ItemStack findToolToSwap(IItemHandlerSimpleInserter backpackInventory, Predicate<ItemStack> isValidTool) {
        Set<ItemStack> alreadyGivenBefore = new HashSet<>();
        AtomicReference<ItemStack> toolFound = new AtomicReference<>(ItemHelpers.EMPTY);
        InventoryHelpers.iterate(backpackInventory, (slot, stack) -> {
            if (ItemHelpers.isEmpty(stack)) {
                return;
            }

            if (!hasEquivalentItem(toolCache, stack)) {
                if (isValidTool.test(stack)) {
                    toolFound.set(stack);
                }
            } else {
                alreadyGivenBefore.add(stack);
            }
        }, () -> !ItemHelpers.isEmpty(toolFound.get()));

        if (ItemHelpers.isEmpty(toolFound.get()) && !alreadyGivenBefore.isEmpty()) {
            while (toolCache.peek() != null) {
                ItemStack itemStack = toolCache.poll();
                if (hasEquivalentItem(alreadyGivenBefore, itemStack)) {
                    toolFound.set(itemStack);
                    break;
                }
            }
        }
        return toolFound.get();
    }

    private boolean hasEquivalentItem(Collection<ItemStack> alreadyGivenBefore, ItemStack stack) {
        for (ItemStack givenTool : alreadyGivenBefore) {
            if (ItemHelpers.areItemsEqualIgnoreDurability(givenTool, stack)) {
                return true;
            }
        }

        return false;
    }

    private boolean itemWorksOnBlock(World world, BlockPos pos, BlockState blockState, EntityPlayer player,
        ItemStack stack) {
        if (ItemHelpers.isEmpty(stack)) return false;

        Block block = blockState.getBlock();
        int metadata = world.getBlockMetadata(pos.getX(), pos.getY(), pos.getZ());

        if (isShearInteractionBlock(world, pos, stack, block) && isShearsItem(stack)) {
            return true;
        }

        String harvestTool = block.getHarvestTool(metadata);
        if (harvestTool == null || harvestTool.isEmpty()) {
            return false;
        }
        return canPerformToolAction(stack, harvestTool) && ItemHelpers.getItem(stack)
            .getHarvestLevel(stack, harvestTool) >= block.getHarvestLevel(metadata);
    }

    private boolean isShearsItem(ItemStack stack) {
        return stack.getItem() instanceof ItemShears;
    }

    private boolean isShearInteractionBlock(World world, BlockPos pos, ItemStack stack, Block block) {
        return (block instanceof IShearable shearable
            && shearable.isShearable(stack, world, pos.getX(), pos.getY(), pos.getZ()));
    }

    private boolean isShearableEntity(Entity entity, ItemStack stack) {
        return entity instanceof IShearable shearable
            && shearable.isShearable(stack, entity.worldObj, (int) entity.posX, (int) entity.posY, (int) entity.posZ);
    }

    @Override
    public boolean canProcessBlockInteract() {
        return upgradeItem.shouldSwapToolOnKeyPress();
    }

    @Override
    public boolean canProcessEntityInteract() {
        return upgradeItem.shouldSwapToolOnKeyPress();
    }
}
