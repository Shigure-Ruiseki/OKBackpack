package ruiseki.okbackpack.common.search;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.oredict.OreDictionary;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public final class ItemStackKey {

    public final Item item;
    public final int meta;
    public final NBTTagCompound tag;

    public final int hash;

    private final int tagHash;

    @SideOnly(Side.CLIENT)
    private String displayNameLower;

    @SideOnly(Side.CLIENT)
    private List<String> tooltipLower;

    @SideOnly(Side.CLIENT)
    private String creativeTab;

    @SideOnly(Side.CLIENT)
    private int[] oreIds;

    private ItemStackKey(Item item, int meta, NBTTagCompound tag) {
        this.item = item;
        this.meta = meta;

        if (tag != null && tag.hasNoTags()) {
            tag = null;
        }

        this.tag = tag != null ? (NBTTagCompound) tag.copy() : null;

        this.tagHash = this.tag != null ? this.tag.hashCode() : 0;
        this.hash = computeHash();
    }

    static ItemStackKey of(ItemStack stack) {
        if (stack == null) throw new IllegalArgumentException("stack");

        return new ItemStackKey(
            stack.getItem(),
            stack.getItemDamage(),
            stack.hasTagCompound() ? stack.getTagCompound() : null);
    }

    private int computeHash() {
        int h = Item.getIdFromItem(item);
        h = 31 * h + meta;
        h = 31 * h + tagHash;
        return h;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemStackKey other)) return false;
        if (hash != other.hash) return false;

        if (item != other.item) return false;
        if (meta != other.meta) return false;

        if (tag == null) return other.tag == null;
        if (other.tag == null) return false;

        return tag.equals(other.tag);
    }

    public ItemStack toStack() {
        return toStack(1);
    }

    public ItemStack toStack(int count) {
        ItemStack stack = new ItemStack(item, count, meta);
        if (tag != null) {
            stack.setTagCompound((NBTTagCompound) tag.copy());
        }
        return stack;
    }

    @SideOnly(Side.CLIENT)
    public String getDisplayName() {
        if (displayNameLower == null) {
            displayNameLower = toStack().getDisplayName()
                .toLowerCase();
        }
        return displayNameLower;
    }

    @SideOnly(Side.CLIENT)
    public String getModId() {
        String name = Item.itemRegistry.getNameForObject(item);
        if (name == null) return "";
        int idx = name.indexOf(':');
        return idx >= 0 ? name.substring(0, idx) : name;
    }

    @SideOnly(Side.CLIENT)
    public List<String> getTooltipLower() {
        if (tooltipLower != null) return tooltipLower;

        tooltipLower = Collections.emptyList();

        if (Minecraft.getMinecraft() == null || Minecraft.getMinecraft().thePlayer == null) {
            return tooltipLower;
        }

        ItemStack stack = toStack();
        List<String> raw = stack.getTooltip(Minecraft.getMinecraft().thePlayer, false);

        List<String> list = new ArrayList<>(raw.size());
        for (String s : raw) {
            list.add(s.toLowerCase());
        }

        tooltipLower = list;
        return tooltipLower;
    }

    @SideOnly(Side.CLIENT)
    public String getCreativeTab() {
        if (creativeTab != null) return creativeTab;

        creativeTab = item.getCreativeTab() != null ? item.getCreativeTab()
            .getTabLabel()
            .toLowerCase() : "";
        return creativeTab;
    }

    @SideOnly(Side.CLIENT)
    public int[] getOreIds() {
        if (oreIds == null) {
            oreIds = OreDictionary.getOreIDs(toStack());
        }
        return oreIds;
    }

    public void write(PacketBuffer buf) throws IOException {
        buf.writeVarIntToBuffer(Item.getIdFromItem(item));
        buf.writeVarIntToBuffer(meta);

        if (tag != null) {
            buf.writeBoolean(true);
            buf.writeNBTTagCompoundToBuffer(tag);
        } else {
            buf.writeBoolean(false);
        }
    }

    public static ItemStackKey read(PacketBuffer buf) throws IOException {
        Item item = Item.getItemById(buf.readVarIntFromBuffer());
        int meta = buf.readVarIntFromBuffer();

        NBTTagCompound tag = null;
        if (buf.readBoolean()) {
            tag = buf.readNBTTagCompoundFromBuffer();
        }

        return new ItemStackKey(item, meta, tag);
    }
}
