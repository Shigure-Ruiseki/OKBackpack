package ruiseki.okbackpack.common.search;

import java.util.List;

final class AndNode implements SearchNode {

    private final List<SearchNode> children;

    AndNode(List<SearchNode> children) {
        this.children = children;
    }

    @Override
    public boolean matches(ItemStackKey k) {
        for (SearchNode n : children) {
            if (!n.matches(k)) return false;
        }
        return true;
    }
}
