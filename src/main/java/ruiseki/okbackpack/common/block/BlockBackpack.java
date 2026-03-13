package ruiseki.okbackpack.common.block;

import static com.gtnewhorizon.gtnhlib.client.model.ModelISBRH.JSON_ISBRH_ID;
import static ruiseki.okbackpack.common.block.BackpackWrapper.ACCENT_COLOR;
import static ruiseki.okbackpack.common.block.BackpackWrapper.MAIN_COLOR;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.factory.PlayerInventoryGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.gtnewhorizon.gtnhlib.blockstate.core.BlockPropertyTrait;
import com.gtnewhorizon.gtnhlib.blockstate.core.BlockState;
import com.gtnewhorizon.gtnhlib.blockstate.properties.DirectionBlockProperty;
import com.gtnewhorizon.gtnhlib.blockstate.registry.BlockPropertyRegistry;
import com.gtnewhorizon.gtnhlib.client.model.color.IBlockColor;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.Getter;
import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.client.renderer.IBaubleRender;
import ruiseki.okbackpack.client.renderer.IItemJSONRender;
import ruiseki.okbackpack.client.renderer.JsonModelISBRH;
import ruiseki.okbackpack.client.renderer.RenderHelpers;
import ruiseki.okbackpack.common.entity.EntityBackpack;
import ruiseki.okcore.block.BlockOK;
import ruiseki.okcore.enums.EnumDye;
import ruiseki.okcore.helper.ItemNBTHelpers;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okcore.item.ItemBlockBauble;

public class BlockBackpack extends BlockOK implements IBlockColor {

    @Getter
    private final int backpackSlots;
    @Getter
    private final int upgradeSlots;

    private final static DirectionBlockProperty property = new DirectionBlockProperty() {

        @Override
        public String getName() {
            return "facing";
        }

        @Override
        public boolean hasTrait(BlockPropertyTrait trait) {
            return switch (trait) {
                case SupportsWorld, WorldMutable, StackMutable, SupportsStacks -> true;
                default -> false;
            };
        }

        @Override
        public ForgeDirection getValue(IBlockAccess world, int x, int y, int z) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TEBackpack tile) {
                return tile.getFacing();
            }
            return ForgeDirection.NORTH;
        }

        @Override
        public void setValue(World world, int x, int y, int z, ForgeDirection value) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TEBackpack tile) {
                tile.setFacing(value);
            }
        }

        @Override
        public ForgeDirection getValue(ItemStack stack) {
            return ForgeDirection.NORTH;
        }
    };

    public BlockBackpack(String name, int backpackSlots, int upgradeSlots) {
        super(name, TEBackpack.class, Material.cloth);
        setStepSound(soundTypeCloth);
        setHardness(1f);
        this.backpackSlots = backpackSlots;
        this.upgradeSlots = upgradeSlots;
        this.isFullSize = this.isOpaque = false;
    }

    @Override
    public int getRenderType() {
        return JSON_ISBRH_ID;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemIn) {
        super.onBlockPlacedBy(world, x, y, z, player, itemIn);
        int heading = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        ForgeDirection facing = getDirectionForHeading(heading);
        BlockState state = BlockPropertyRegistry.getBlockState(world, x, y, z);
        state.setPropertyValue(property, facing);
        state.place(world, x, y, z);
        state.close();
    }

    private ForgeDirection getDirectionForHeading(int heading) {
        return switch (heading) {
            case 0 -> ForgeDirection.SOUTH;
            case 1 -> ForgeDirection.EAST;
            case 2 -> ForgeDirection.NORTH;
            case 3 -> ForgeDirection.WEST;
            default -> ForgeDirection.NORTH;
        };
    }

    @Override
    protected Class<? extends ItemBlock> getItemBlockClass() {
        return ItemBackpack.class;
    }

    @Override
    protected void registerComponent() {
        BlockPropertyRegistry.registerProperty(this, property);
        BlockPropertyRegistry.registerProperty(Item.getItemFromBlock(this), property);
    }

    @Override
    protected void registerBlock() {
        GameRegistry.registerBlock(this, this.getItemBlockClass(), this.name);
    }

    @Override
    public int colorMultiplier(@Nullable ItemStack stack, int tintIndex) {
        if (stack == null) return -1;
        NBTTagCompound tag = ItemNBTHelpers.getNBT(stack);
        int main = tag.hasKey(MAIN_COLOR) ? tag.getInteger(MAIN_COLOR) : 0xFFCC613A;
        int accent = tag.hasKey(ACCENT_COLOR) ? tag.getInteger(ACCENT_COLOR) : 0xFF622E1A;

        if (tintIndex == 0) {
            return EnumDye.rgbToAbgr(main);
        }
        if (tintIndex == 1) {
            return EnumDye.rgbToAbgr(accent);
        }
        return -1;
    }

    @Override
    public int colorMultiplier(@Nullable IBlockAccess world, int x, int y, int z, int tintIndex) {
        if (world == null) return -1;
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TEBackpack backpack) {
            if (tintIndex == 0) {
                return EnumDye.rgbToAbgr(backpack.getMainColor());
            }
            if (tintIndex == 1) {
                return EnumDye.rgbToAbgr(backpack.getAccentColor());
            }
        }
        return -1;
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        return new TEBackpack(backpackSlots, upgradeSlots);
    }

    @Override
    public boolean shouldDropInventory(World world, int x, int y, int z) {
        return false;
    }

    @Override
    public boolean onBlockActivated(World worldIn, int x, int y, int z, EntityPlayer player, int side, float subX,
        float subY, float subZ) {
        TileEntity te = worldIn.getTileEntity(x, y, z);
        if (te instanceof TEBackpack backpack) {
            return backpack.onBlockActivated(worldIn, player, ForgeDirection.getOrientation(side), subX, subY, subZ);
        }
        return super.onBlockActivated(worldIn, x, y, z, player, side, subX, subY, subZ);
    }

    public static class ItemBackpack extends ItemBlockBauble
        implements IGuiHolder<PlayerInventoryGuiData>, IBaubleRender, IItemJSONRender {

        @Getter
        private int backpackSlots = 27;
        @Getter
        private int upgradeSlots = 1;

        public ItemBackpack(Block block) {
            super(block);
            if (block instanceof BlockBackpack backpack) {
                this.backpackSlots = backpack.getBackpackSlots();
                this.upgradeSlots = backpack.getUpgradeSlots();
            }
        }

        @Override
        public String getItemStackDisplayName(ItemStack stack) {
            if (stack.hasTagCompound() && stack.getTagCompound()
                .hasKey("display", 10)) {
                return stack.getTagCompound()
                    .getCompoundTag("display")
                    .getString("Name");
            }
            return super.getItemStackDisplayName(stack);
        }

        @Override
        public String[] getBaubleTypes(ItemStack itemstack) {
            return new String[] { "body" };
        }

        @Override
        public boolean isValidArmor(ItemStack stack, int armorType, Entity entity) {
            return armorType == 1;
        }

        @Override
        public boolean hasCustomEntity(ItemStack stack) {
            return true;
        }

        @Override
        public Entity createEntity(World world, Entity location, ItemStack stack) {
            BackpackWrapper handler = new BackpackWrapper(stack, this);
            return new EntityBackpack(world, location, stack, handler);
        }

        @Override
        public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isHeld) {
            super.onUpdate(stack, world, entity, slot, isHeld);
            if (!world.isRemote && stack != null) {
                if (!stack.hasTagCompound()) {
                    BackpackWrapper cap = new BackpackWrapper(stack, this);
                    cap.writeToItem();
                    stack.setTagCompound(cap.getTagCompound());
                }
            }
        }

        @Override
        public void onCreated(ItemStack stack, World world, EntityPlayer player) {
            super.onCreated(stack, world, player);
            if (!world.isRemote && stack != null) {
                if (!stack.hasTagCompound()) {
                    BackpackWrapper cap = new BackpackWrapper(stack, this);
                    cap.writeToItem();
                    stack.setTagCompound(cap.getTagCompound());
                }
            }
        }

        @Override
        public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
            float hitX, float hitY, float hitZ) {

            if (player.isSneaking() && stack != null && stack.getTagCompound() != null) {
                return super.onItemUse(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
            }

            return false;
        }

        @Override
        public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
            if (!world.isRemote && stack != null && stack.getTagCompound() != null) {
                BackpackWrapper cap = new BackpackWrapper(stack, this);
                if (cap.canPlayerAccess(player.getUniqueID())) {
                    GuiFactories.playerInventory()
                        .openFromMainHand(player);
                }
            }
            return stack;
        }

        @Override
        public ModularPanel buildUI(PlayerInventoryGuiData data, PanelSyncManager syncManager, UISettings settings) {
            ItemStack stack = data.getUsedItemStack();
            BackpackWrapper cap = new BackpackWrapper(stack, this);
            return new BackpackGuiHolder.ItemStackGuiHolder(cap).buildUI(data, syncManager, settings);
        }

        @Override
        public ModularScreen createScreen(PlayerInventoryGuiData data, ModularPanel mainPanel) {
            return new ModularScreen(Reference.MOD_ID, mainPanel);
        }

        @Override
        @SideOnly(Side.CLIENT)
        public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean flag) {
            list.add(LangHelpers.localize("tooltip.backpack.inventory_size", backpackSlots));
            list.add(LangHelpers.localize("tooltip.backpack.upgrade_slots_size", upgradeSlots));
            if (GuiScreen.isShiftKeyDown()) {
                BackpackWrapper cap = new BackpackWrapper(stack, this);
                list.add(LangHelpers.localize("tooltip.backpack.stack_multiplier", cap.getTotalStackMultiplier(), "x"));
            }
            super.addInformation(stack, player, list, flag);
        }

        @Override
        public void onPlayerBaubleRender(ItemStack stack, RenderPlayerEvent event, RenderHelpers.RenderType type) {
            if (stack == null || type != RenderHelpers.RenderType.BODY) {
                return;
            }

            GL11.glPushMatrix();
            GL11.glTranslatef(0f, 0.3f, 0.3f);
            RenderHelpers.rotateIfSneaking(event.entityPlayer);
            JsonModelISBRH.INSTANCE.renderToEntity(stack);
            GL11.glPopMatrix();

        }

        @Override
        public void onArmorRender(ItemStack stack, RenderPlayerEvent event, RenderHelpers.RenderType type) {
            if (stack == null || type != RenderHelpers.RenderType.BODY) {
                return;
            }

            GL11.glPushMatrix();
            GL11.glTranslatef(0f, 0.3f, 0.3f);
            RenderHelpers.rotateIfSneaking(event.entityPlayer);
            JsonModelISBRH.INSTANCE.renderToEntity(stack);
            GL11.glPopMatrix();

        }
    }
}
