package ruiseki.okbackpack.common.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.Explosion;
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
import com.gtnewhorizon.gtnhlib.blockstate.properties.DirectionBlockProperty;
import com.gtnewhorizon.gtnhlib.blockstate.registry.BlockPropertyRegistry;
import com.gtnewhorizon.gtnhlib.client.model.color.BlockColor;
import com.gtnewhorizon.gtnhlib.client.model.color.IBlockColor;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.Getter;
import ruiseki.okbackpack.OKBCreativeTab;
import ruiseki.okbackpack.Reference;
import ruiseki.okbackpack.api.wrapper.IAdminProtectable;
import ruiseki.okbackpack.api.wrapper.IBatteryUpgrade;
import ruiseki.okbackpack.client.renderer.BackpackContentHandler;
import ruiseki.okbackpack.client.renderer.JsonModelISBRH;
import ruiseki.okbackpack.client.renderer.RenderHelpers;
import ruiseki.okbackpack.client.renderer.player.IArmorRender;
import ruiseki.okbackpack.client.renderer.player.IBaubleRender;
import ruiseki.okbackpack.client.renderer.player.PlayerRenderContext;
import ruiseki.okbackpack.common.entity.EntityBackpack;
import ruiseki.okbackpack.compat.Mods;
import ruiseki.okcore.block.BlockOK;
import ruiseki.okcore.energy.IOKEnergyItem;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okcore.item.ItemBlockBauble;

public class BlockBackpack extends BlockOK {

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
        setCreativeTab(OKBCreativeTab.INSTANCE);
        this.backpackSlots = backpackSlots;
        this.upgradeSlots = upgradeSlots;
        this.isFullSize = this.isOpaque = false;
    }

    @Override
    public float getPlayerRelativeBlockHardness(EntityPlayer player, World world, int x, int y, int z) {
        if (hasAdminProtection(world, x, y, z) && !player.capabilities.isCreativeMode) {
            return -1.0f;
        }
        return super.getPlayerRelativeBlockHardness(player, world, x, y, z);
    }

    private boolean hasAdminProtection(World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TEBackpack backpack) {
            for (var entry : backpack.getWrapper()
                .gatherCapabilityUpgrades(IAdminProtectable.class)
                .values()) {
                if (entry.isAdmin()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public float getExplosionResistance(Entity exploder, World world, int x, int y, int z, double explosionX,
        double explosionY, double explosionZ) {
        if (hasAdminProtection(world, x, y, z)) {
            return Float.MAX_VALUE;
        }
        return super.getExplosionResistance(exploder, world, x, y, z, explosionX, explosionY, explosionZ);
    }

    @Override
    public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {
        if (hasAdminProtection(world, x, y, z)) {
            return;
        }
        super.onBlockExploded(world, x, y, z, explosion);
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemIn) {
        super.onBlockPlacedBy(world, x, y, z, player, itemIn);
        int heading = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
        ForgeDirection facing = getDirectionForHeading(heading);
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof TEBackpack backpack) {
            backpack.setFacing(facing);
            if (!world.isRemote) {
                backpack.getWrapper()
                    .forceStopAllJukeboxes(world, x + 0.5f, y + 0.5f, z + 0.5f);
            }
        }
    }

    private ForgeDirection getDirectionForHeading(int heading) {
        return switch (heading) {
            case 1 -> ForgeDirection.EAST;
            case 2 -> ForgeDirection.NORTH;
            case 3 -> ForgeDirection.WEST;
            default -> ForgeDirection.SOUTH;
        };
    }

    @Override
    protected Class<? extends ItemBlock> getItemBlockClass() {
        return ItemBackpack.class;
    }

    @Override
    protected void registerComponent() {

        BlockColor.registerBlockColors(new IBlockColor() {

            @Override
            public int colorMultiplier(@Nullable ItemStack stack, int tintIndex) {
                if (stack == null) return -1;

                BackpackWrapper wrapper = new BackpackWrapper(stack, backpackSlots, upgradeSlots);
                return switch (tintIndex) {
                    case 0 -> wrapper.getMainColor();
                    case 1 -> wrapper.getAccentColor();
                    default -> -1;
                };
            }

            @Override
            public int colorMultiplier(@Nullable IBlockAccess world, int x, int y, int z, int tintIndex) {
                if (world == null) return -1;

                TileEntity te = world.getTileEntity(x, y, z);
                if (!(te instanceof TEBackpack backpack)) return -1;

                return switch (tintIndex) {
                    case 0 -> backpack.getMainColor();
                    case 1 -> backpack.getAccentColor();
                    default -> -1;
                };
            }

        }, this);

        BlockPropertyRegistry.registerBlockItemProperty(this, property);
    }

    @Override
    public TileEntity createTileEntity(World world, int metadata) {
        TEBackpack backpack = new TEBackpack();
        BackpackWrapper wrapper = new BackpackWrapper(backpack, backpackSlots, upgradeSlots);
        backpack.setWrapper(wrapper);
        return backpack;
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
        implements IGuiHolder<PlayerInventoryGuiData>, IBaubleRender, IArmorRender, IOKEnergyItem {

        public int backpackSlots = 27;
        public int upgradeSlots = 1;

        public ItemBackpack(Block block) {
            super(block);
            if (block instanceof BlockBackpack backpack) {
                this.backpackSlots = backpack.getBackpackSlots();
                this.upgradeSlots = backpack.getUpgradeSlots();
            }
        }

        private @Nullable IBatteryUpgrade getBatteryUpgrade(ItemStack stack) {
            if (stack == null || !stack.hasTagCompound()) return null;
            if (!stack.getTagCompound()
                .hasKey(BackpackWrapper.BACKPACK_NBT)) return null;
            BackpackWrapper wrapper = new BackpackWrapper(stack, this);
            wrapper.readFromItem();
            var batteries = wrapper.gatherCapabilityUpgrades(IBatteryUpgrade.class);
            if (batteries.isEmpty()) return null;
            return batteries.values()
                .iterator()
                .next();
        }

        private @Nullable BackpackWrapper createWrapper(ItemStack stack) {
            if (stack == null || !stack.hasTagCompound()) return null;
            if (!stack.getTagCompound()
                .hasKey(BackpackWrapper.BACKPACK_NBT)) return null;
            BackpackWrapper wrapper = new BackpackWrapper(stack, this);
            wrapper.readFromItem();
            return wrapper;
        }

        @Override
        public int receiveEnergy(ItemStack container, int maxReceive, boolean simulate) {
            BackpackWrapper wrapper = createWrapper(container);
            if (wrapper == null) return 0;
            var batteries = wrapper.gatherCapabilityUpgrades(IBatteryUpgrade.class);
            if (batteries.isEmpty()) return 0;
            IBatteryUpgrade battery = batteries.values()
                .iterator()
                .next();
            int received = battery.receiveEnergy(maxReceive, simulate);
            if (!simulate && received > 0) {
                wrapper.writeToItem();
            }
            return received;
        }

        @Override
        public int extractEnergy(ItemStack container, int maxExtract, boolean simulate) {
            BackpackWrapper wrapper = createWrapper(container);
            if (wrapper == null) return 0;
            var batteries = wrapper.gatherCapabilityUpgrades(IBatteryUpgrade.class);
            if (batteries.isEmpty()) return 0;
            IBatteryUpgrade battery = batteries.values()
                .iterator()
                .next();
            int extracted = battery.extractEnergy(maxExtract, simulate);
            if (!simulate && extracted > 0) {
                wrapper.writeToItem();
            }
            return extracted;
        }

        @Override
        public int getEnergyStored(ItemStack container) {
            IBatteryUpgrade battery = getBatteryUpgrade(container);
            return battery != null ? battery.getEnergyStored() : 0;
        }

        @Override
        public void setEnergyStored(ItemStack container, int energy) {
            // Energy is managed by the battery upgrade internally
        }

        @Override
        public int getMaxEnergyStored(ItemStack container) {
            IBatteryUpgrade battery = getBatteryUpgrade(container);
            return battery != null ? battery.getMaxEnergyStored() : 0;
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
            BackpackWrapper wrapper = new BackpackWrapper(stack, this);
            if (!world.isRemote) {
                wrapper.writeAdditionalInfo(world, (float) location.posX, (float) location.posY, (float) location.posZ);
            }
            return new EntityBackpack(world, location, stack, wrapper);
        }

        @Override
        public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isHeld) {
            super.onUpdate(stack, world, entity, slot, isHeld);
            if (!world.isRemote && stack != null) {
                if (!stack.hasTagCompound()) {
                    stack.setTagCompound(new NBTTagCompound());
                }

                if (!stack.getTagCompound()
                    .hasKey(BackpackWrapper.BACKPACK_NBT)) {
                    BackpackWrapper wrapper = new BackpackWrapper(stack, this);
                    wrapper.writeToItem();
                }
            }
        }

        @Override
        public void onCreated(ItemStack stack, World world, EntityPlayer player) {
            super.onCreated(stack, world, player);
            if (!world.isRemote && stack != null) {
                if (!stack.hasTagCompound()) {
                    stack.setTagCompound(new NBTTagCompound());
                }

                if (!stack.getTagCompound()
                    .hasKey(BackpackWrapper.BACKPACK_NBT)) {
                    BackpackWrapper wrapper = new BackpackWrapper(stack, this);
                    wrapper.writeToItem();
                }
            }
        }

        @Override
        public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
            float hitX, float hitY, float hitZ) {

            if (player.isSneaking() && stack != null && !(world.getBlock(x, y, z) instanceof BlockBackpack)) {
                if (stack.getTagCompound() == null) {
                    new BackpackWrapper(stack, this).writeToItem();
                }
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
            // UUID - only show with F3+H and if uuid is stored in NBT
            if (flag) {
                NBTTagCompound tag = stack.getTagCompound();
                if (tag != null && tag.hasKey(BackpackWrapper.BACKPACK_NBT)) {
                    NBTTagCompound bpTag = tag.getCompoundTag(BackpackWrapper.BACKPACK_NBT);
                    if (bpTag.hasKey(BackpackWrapper.UUID_TAG, 8)) {
                        String uuid = bpTag.getString(BackpackWrapper.UUID_TAG);
                        list.add("\u00a78" + LangHelpers.localize("tooltip.backpack.contents.uuid", uuid));
                    }
                }
            }

            if (!GuiScreen.isShiftKeyDown()) {
                // "Press <Left Shift> to View Contents"
                String shiftKey = "\u00a7b" + LangHelpers.localize("tooltip.backpack.contents.shift")
                    + "\u00a7r\u00a77";
                list.add("\u00a77" + LangHelpers.localize("tooltip.backpack.contents.press_for_contents", shiftKey));
                if (Mods.CodeChickenCore.isLoaded()) {
                    BackpackContentHandler.reset();
                }
            } else if (Mods.CodeChickenCore.isLoaded()) {
                // Expanded tooltip with upgrade/inventory info
                BackpackWrapper wrapper = new BackpackWrapper(stack, this);
                BackpackContentHandler.prepareContents(wrapper);

                boolean hasUpgrades = !BackpackContentHandler.upgradeInfos.isEmpty();
                boolean hasContents = !BackpackContentHandler.sortedContents.isEmpty();

                if (!hasUpgrades && !hasContents) {
                    // Empty backpack
                    list.add("\u00a7e" + LangHelpers.localize("tooltip.backpack.contents.empty"));
                } else {
                    // Stack multiplier
                    double multiplier = wrapper.applyStackLimitModifiers();
                    if (multiplier > 1 && multiplier != Integer.MAX_VALUE) {
                        list.add(
                            "\u00a7a" + LangHelpers.localize(
                                "tooltip.backpack.contents.stack_multiplier",
                                String.format("%.1f", multiplier)));
                    } else if (multiplier == Integer.MAX_VALUE) {
                        list.add(
                            "\u00a7a" + LangHelpers.localize("tooltip.backpack.contents.stack_multiplier", "\u221E"));
                    }

                    if (hasUpgrades) {
                        list.addAll(BackpackContentHandler.upgradeTooltipLines);
                        list.add("\u00a7e" + LangHelpers.localize("tooltip.backpack.contents.upgrades"));
                        list.add(BackpackContentHandler.getUpgradeHandlerLine());
                    }

                    if (hasContents) {
                        list.add("\u00a7e" + LangHelpers.localize("tooltip.backpack.contents.inventory"));
                        list.add(BackpackContentHandler.getContentsHandlerLine());
                    }
                }
            }
            list.add(LangHelpers.localize("tooltip.backpack.inventory_size", backpackSlots));
            list.add(LangHelpers.localize("tooltip.backpack.upgrade_slots_size", upgradeSlots));
            super.addInformation(stack, player, list, flag);
        }

        @Override
        public void collectContext(ItemStack stack, EntityPlayer player, PlayerRenderContext context) {
            context.setRenderCape(false);
        }

        @Override
        public void render(ItemStack stack, EntityPlayer player, RenderPlayerEvent event,
            RenderHelpers.RenderType type) {
            if (stack == null || type != RenderHelpers.RenderType.BODY) return;
            GL11.glPushMatrix();
            GL11.glTranslatef(0f, 0.3f, 0.3f);
            JsonModelISBRH.INSTANCE.renderToEntity(stack);
            GL11.glPopMatrix();
        }
    }
}
