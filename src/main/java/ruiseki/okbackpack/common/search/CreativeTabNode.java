package ruiseki.okbackpack.common.search;

final class CreativeTabNode implements SearchNode {

    private final String tab;

    CreativeTabNode(String tab) {
        this.tab = tab;
    }

    @Override
    public boolean matches(ItemStackKey k) {
        if (k.getStack() == null || k.getStack()
            .getItem() == null) return false;
        return k.getStack()
            .getItem()
            .getCreativeTab()
            .getTabLabel()
            .toLowerCase()
            .contains(tab);
    }
}
