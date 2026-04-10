package ruiseki.okbackpack.common.search;

import net.minecraft.item.Item;

final class ModNode implements SearchNode {

    private final String mod;

    ModNode(String mod) {
        this.mod = mod;
    }

    @Override
    public boolean matches(ItemStackKey k) {
        if (k.getStack() == null || k.getStack()
            .getItem() == null) return false;
        String name = Item.itemRegistry.getNameForObject(
            k.getStack()
                .getItem());
        if (name == null) return false;
        int idx = name.indexOf(':');
        String modId = idx >= 0 ? name.substring(0, idx) : name;
        return modId.toLowerCase()
            .contains(mod);
    }
}
