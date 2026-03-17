package ruiseki.okbackpack.common.search;

final class ModNode implements SearchNode {

    private final String mod;

    ModNode(String mod) {
        this.mod = mod;
    }

    @Override
    public boolean matches(ItemStackKey k) {
        return k.getModId()
            .toLowerCase()
            .contains(mod);
    }
}
