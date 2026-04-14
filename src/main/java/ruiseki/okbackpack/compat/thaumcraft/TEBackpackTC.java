package ruiseki.okbackpack.compat.thaumcraft;

import cpw.mods.fml.common.Optional;
import ruiseki.okbackpack.common.block.TEBackpack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;

@Optional.Interface(iface = "thaumcraft.api.aspects.IAspectContainer", modid = "Thaumcraft")
public class TEBackpackTC extends TEBackpack implements IAspectContainer {

    @Override
    @Optional.Method(modid = "Thaumcraft")
    public AspectList getAspects() {
        return ThaumcraftHelpers.getWandAspects(getWrapper());
    }

    @Override
    @Optional.Method(modid = "Thaumcraft")
    public void setAspects(AspectList aspects) {}

    @Override
    @Optional.Method(modid = "Thaumcraft")
    public boolean doesContainerAccept(Aspect aspect) {
        return ThaumcraftHelpers.doesWandAcceptAspect(getWrapper(), aspect);
    }

    @Override
    @Optional.Method(modid = "Thaumcraft")
    public int addToContainer(Aspect aspect, int amount) {
        int leftover = ThaumcraftHelpers.addAspectToWands(getWrapper(), aspect, amount);
        if (leftover < amount) markDirty();
        return leftover;
    }

    @Override
    @Optional.Method(modid = "Thaumcraft")
    public boolean takeFromContainer(Aspect aspect, int amount) {
        return false;
    }

    @Override
    @Deprecated
    @Optional.Method(modid = "Thaumcraft")
    public boolean takeFromContainer(AspectList aspects) {
        return false;
    }

    @Override
    @Optional.Method(modid = "Thaumcraft")
    public boolean doesContainerContainAmount(Aspect aspect, int amount) {
        return ThaumcraftHelpers.getWandAspectAmount(getWrapper(), aspect) >= amount;
    }

    @Override
    @Deprecated
    @Optional.Method(modid = "Thaumcraft")
    public boolean doesContainerContain(AspectList aspects) {
        if (aspects == null) return false;
        for (Aspect a : aspects.getAspects()) {
            if (a != null && !doesContainerContainAmount(a, aspects.getAmount(a))) return false;
        }
        return true;
    }

    @Override
    @Optional.Method(modid = "Thaumcraft")
    public int containerContains(Aspect aspect) {
        return ThaumcraftHelpers.getWandAspectAmount(getWrapper(), aspect);
    }
}
