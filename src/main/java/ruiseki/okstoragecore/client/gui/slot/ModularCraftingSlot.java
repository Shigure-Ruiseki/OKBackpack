package ruiseki.okstoragecore.client.gui.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.stats.AchievementList;

import ruiseki.okcore.helper.ItemHelpers;
import ruiseki.okstoragecore.client.gui.widget.StoragePanel;

public class ModularCraftingSlot extends ModularResultSlot {

    private int amountCrafted;

    public ModularCraftingSlot(StoragePanel storagePanel, int upgradeIndex) {
        super(storagePanel, upgradeIndex);
    }

    /**
     * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg. Returns the new
     * stack.
     */
    @Override
    public ItemStack decrStackSize(int amount) {
        this.amountCrafted += Math.min(amount, ItemHelpers.getCount(getStack()));
        return super.decrStackSize(amount);
    }

    /**
     * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood. Typically increases an
     * internal count then calls onCrafting(item).
     */
    @Override
    public void onCrafting(ItemStack stack, int amount) {
        this.amountCrafted += amount;
        this.onCrafting(stack);
    }

    @Override
    public void onCraftShiftClick(EntityPlayer player, ItemStack stack) {
        if (!ItemHelpers.isEmpty(stack)) {
            player.dropPlayerItemWithRandomChoice(stack, false);
        }
    }

    /**
     * the itemStack passed in is the output - ie, iron ingots, and pickaxes, not ore and wood.
     */
    @Override
    public void onCrafting(ItemStack stack) {
        if (ItemHelpers.isEmpty(stack)) return;

        EntityPlayer player = getPlayer();
        stack.onCrafting(player.worldObj, player, this.amountCrafted);
        this.amountCrafted = 0;

        Item item = ItemHelpers.getItem(stack);

        if (item == Item.getItemFromBlock(Blocks.crafting_table)) {
            player.addStat(AchievementList.buildWorkBench, 1);
        }

        if (item instanceof ItemPickaxe) {
            player.addStat(AchievementList.buildPickaxe, 1);

            if (((ItemPickaxe) item).func_150913_i() != Item.ToolMaterial.WOOD) {
                player.addStat(AchievementList.buildBetterPickaxe, 1);
            }
        }

        if (item == Item.getItemFromBlock(Blocks.furnace)) {
            player.addStat(AchievementList.buildFurnace, 1);
        }

        if (item instanceof ItemHoe) {
            player.addStat(AchievementList.buildHoe, 1);
        }

        if (item == Items.bread) {
            player.addStat(AchievementList.makeBread, 1);
        }

        if (item == Items.cake) {
            player.addStat(AchievementList.bakeCake, 1);
        }

        if (item instanceof ItemSword) {
            player.addStat(AchievementList.buildSword, 1);
        }

        if (item == Item.getItemFromBlock(Blocks.enchanting_table)) {
            player.addStat(AchievementList.enchantments, 1);
        }

        if (item == Item.getItemFromBlock(Blocks.bookshelf)) {
            player.addStat(AchievementList.bookcase, 1);
        }
    }
}
