package ruiseki.backpack.backpack;

import java.util.List;
import java.util.function.IntSupplier;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.IFluidContainerItem;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.factory.GuiFactories;
import com.cleanroommc.modularui.factory.PlayerInventoryGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.gtnewhorizons.angelica.api.IDynamicLightProducer;

import cofh.api.energy.IEnergyContainerItem;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.experimental.Delegate;
import ruiseki.backpack.backpack.wrapper.BackpackWrapper;
import ruiseki.backpack.backpack.wrapper.IBackpackWrapper;
import ruiseki.backpack.capabilities.CapabilityBackpackWrapperConfig;
import ruiseki.backpack.helpers.InteractionHelpers;
import ruiseki.backpack.upgrades.everlasting.EntityEverlastingItem;
import ruiseki.backpack.upgrades.everlasting.ItemEverlastingUpgrade;
import ruiseki.okbackpack.api.tier.BackpackTier;
import ruiseki.okbackpack.api.tier.TierRegistry;
import ruiseki.okbackpack.client.renderer.JsonModelISBRH;
import ruiseki.okbackpack.client.renderer.RenderHelpers;
import ruiseki.okbackpack.client.renderer.player.IArmorRender;
import ruiseki.okbackpack.client.renderer.player.IBaubleRender;
import ruiseki.okbackpack.client.renderer.player.PlayerRenderContext;
import ruiseki.okbackpack.common.init.TierRegistries;
import ruiseki.okcore.capabilities.Capability;
import ruiseki.okcore.capabilities.ICapabilityProvider;
import ruiseki.okcore.datastructure.BlockPos;
import ruiseki.okcore.datastructure.LazyOptional;
import ruiseki.okcore.energy.capability.CapabilityEnergy;
import ruiseki.okcore.energy.capability.EmptyEnergyStorage;
import ruiseki.okcore.energy.component.EnergyContainerItemComponent;
import ruiseki.okcore.fluid.capability.CapabilityFluidHandler;
import ruiseki.okcore.fluid.capability.EmptyFluidHandlerItem;
import ruiseki.okcore.fluid.component.FluidContainerItemComponent;
import ruiseki.okcore.helper.CapabilityHelpers;
import ruiseki.okcore.helper.LangHelpers;
import ruiseki.okcore.item.ItemBlockBauble;
import ruiseki.okstoragecore.capabilities.CapabilityStorageWrapperConfig;
import ruiseki.okstoragecore.client.gui.tooltip.StorageContentHandler;
import ruiseki.okstoragecore.upgrades.ILightUpgrade;

@Optional.Interface(iface = "com.gtnewhorizons.angelica.api.IDynamicLightProducer", modid = "angelica")
public class ItemBackpack extends ItemBlockBauble implements IGuiHolder<PlayerInventoryGuiData>, IEnergyContainerItem,
    IFluidContainerItem, IDynamicLightProducer, IBaubleRender, IArmorRender {

    @Delegate
    private final EnergyContainerItemComponent energyStorage = new EnergyContainerItemComponent();
    @Delegate
    private final FluidContainerItemComponent tank = new FluidContainerItemComponent();

    private String tierId = TierRegistries.LEATHER;
    private BackpackTier tier = null;
    private final IntSupplier numberOfSlots;
    private final IntSupplier numberOfUpgradeSlots;

    public ItemBackpack(Block block) {
        super(block);
        this.setMaxStackSize(1);
        if (block instanceof BlockBackpack backpack) {
            this.tierId = backpack.getTierId();
        }
        this.numberOfSlots = () -> this.getTier()
            .getBackpackSlots();
        this.numberOfUpgradeSlots = () -> this.getTier()
            .getUpgradeSlots();
    }

    public BackpackTier getTier() {
        if (this.tier == null) {
            this.tier = TierRegistry.getTier(tierId);
        }
        return this.tier;
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    private boolean hasEverlastingUpgrade(ItemStack stack) {
        return CapabilityHelpers.getCapability(stack, CapabilityBackpackWrapperConfig.CAPABILITY)
            .map(
                w -> !w.getUpgradeHandler()
                    .getTypeWrappers(ItemEverlastingUpgrade.TYPE)
                    .isEmpty())
            .orElse(false);
    }

    @Override
    public Entity createEntity(World world, Entity entity, ItemStack itemstack) {
        if (!(entity instanceof EntityItem itemEntity)) {
            return null;
        }
        UUIDDeduplicator.dedupeBackpackItemEntityInArea(itemEntity);
        return hasEverlastingUpgrade(itemstack) ? createEverlastingBackpack(world, itemEntity, itemstack) : null;
    }

    private EntityEverlastingItem createEverlastingBackpack(World world, EntityItem itemEntity, ItemStack itemstack) {
        return new EntityEverlastingItem(world, itemEntity, itemstack);
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {
        if (stack == null) return false;
        if (player.isSneaking()) {
            if (InteractionHelpers.tryInventoryInteraction(
                new BlockPos(x, y, z),
                world,
                stack,
                ForgeDirection.getOrientation(side),
                player)) {
                return true;
            }
            if (world.getBlock(x, y, z) instanceof BlockBackpack) return false;
            return super.onItemUse(stack, player, world, x, y, z, side, hitX, hitY, hitZ);
        }
        return false;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (!world.isRemote && stack != null) {
            GuiFactories.playerInventory()
                .openFromMainHand(player);
        }
        return stack;
    }

    public int getNumberOfSlots() {
        return numberOfSlots.getAsInt();
    }

    public int getNumberOfUpgradeSlots() {
        return numberOfUpgradeSlots.getAsInt();
    }

    @Override
    public boolean isValidArmor(ItemStack stack, int armorType, Entity entity) {
        return armorType == 1;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean flag) {
        if (!GuiScreen.isShiftKeyDown()) {
            // "Press <Left Shift> to View Contents"
            list.add(
                "§7" + LangHelpers.localize(
                    "tooltip.backpack.contents.press_for_contents",
                    LangHelpers.localize("tooltip.backpack.contents.shift")));
            StorageContentHandler.reset();
        } else {
            CapabilityHelpers.getCapability(stack, CapabilityStorageWrapperConfig.CAPABILITY)
                .ifPresent(wrapper -> { list.addAll(StorageContentHandler.buildExpandedTooltipLines(wrapper)); });
        }
        super.addInformation(stack, player, list, flag);
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new ICapabilityProvider() {

            private IBackpackWrapper wrapper = null;

            @Override
            public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability,
                @Nullable ForgeDirection forgeDirection) {

                this.initCap();
                if (capability == CapabilityBackpackWrapperConfig.CAPABILITY) {
                    return LazyOptional.of(() -> wrapper)
                        .cast();
                } else if (capability == CapabilityStorageWrapperConfig.CAPABILITY) {
                    return LazyOptional.of(() -> wrapper)
                        .cast();
                } else if (capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM) {
                    return wrapper.getFluidHandler()
                        .<LazyOptional<T>>map(
                            handler -> LazyOptional.of(() -> handler)
                                .cast())
                        .orElse(
                            LazyOptional.of(() -> EmptyFluidHandlerItem.INSTANCE)
                                .cast());
                } else if (capability == CapabilityEnergy.ENERGY) {
                    return wrapper.getEnergyStorage()
                        .<LazyOptional<T>>map(
                            storage -> LazyOptional.of(() -> storage)
                                .cast())
                        .orElse(
                            LazyOptional.of(() -> EmptyEnergyStorage.INSTANCE)
                                .cast());
                }
                return LazyOptional.empty();
            }

            private void initCap() {
                if (this.wrapper == null) {
                    this.wrapper = new BackpackWrapper(stack);
                }
            }
        };
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModularScreen createScreen(PlayerInventoryGuiData data, ModularPanel mainPanel) {
        return new BackpackGuiHolder.ItemStackGuiHolder().createScreen(data, mainPanel);
    }

    @Override
    public ModularPanel buildUI(PlayerInventoryGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return new BackpackGuiHolder.ItemStackGuiHolder().buildUI(data, syncManager, settings);
    }

    @Optional.Method(modid = "angelica")
    public int getLuminance(ItemStack stack) {
        return CapabilityHelpers.getCapability(stack, CapabilityStorageWrapperConfig.CAPABILITY)
            .map(wrapper -> {
                List<ILightUpgrade> lights = wrapper.getUpgradeHandler()
                    .getWrappersThatImplement(ILightUpgrade.class);
                if (lights.isEmpty()) return 0;
                int maxLight = 0;
                for (ILightUpgrade light : lights) {
                    if (light != null) {
                        maxLight = Math.max(maxLight, light.getLightLevel());
                    }
                }
                return maxLight;
            })
            .orElse(0);
    }

    @Override
    @Optional.Method(modid = "Baubles")
    public String[] getBaubleTypes(ItemStack itemstack) {
        return new String[] { "body" };
    }

    @Override
    public void collectContext(ItemStack stack, EntityPlayer player, PlayerRenderContext context) {
        context.setRenderCape(false);
    }

    @Override
    public void render(ItemStack stack, EntityPlayer player, RenderPlayerEvent event, RenderHelpers.RenderType type) {
        if (stack == null || type != RenderHelpers.RenderType.BODY) return;
        GL11.glPushMatrix();
        GL11.glTranslatef(0f, 0.3f, 0.3f);
        JsonModelISBRH.INSTANCE.renderToEntity(stack);
        GL11.glPopMatrix();
    }
}
