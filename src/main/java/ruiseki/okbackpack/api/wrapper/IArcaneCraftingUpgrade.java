package ruiseki.okbackpack.api.wrapper;

import java.util.Map;

public interface IArcaneCraftingUpgrade extends ICraftingUpgrade {

    String ARCANE_ASPECTS_TAG = "ArcaneAspects";
    String WAND_SLOT_TAG = "WandSlot";

    int WAND_SLOT_INDEX = 10;

    Map<String, Integer> getRequiredAspects();

    void setRequiredAspects(Map<String, Integer> aspects);

    boolean hasWand();

    void setHasWand(boolean hasWand);

    String getMissingResearch();

    void setMissingResearch(String researchKey);

    String getMissingResearchName();

    void setMissingResearchName(String name);
}
