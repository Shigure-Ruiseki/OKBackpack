package ruiseki.okstoragecore.modcompat.nei;

import java.util.ArrayList;

import codechicken.nei.PositionedStack;
import codechicken.nei.api.IStackPositioner;

public class StoragePositioner implements IStackPositioner {

    @Override
    public ArrayList<PositionedStack> positionStacks(ArrayList<PositionedStack> ai) {
        return ai;
    }
}
