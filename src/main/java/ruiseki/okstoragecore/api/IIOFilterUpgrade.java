package ruiseki.okstoragecore.api;

import java.util.Optional;

import ruiseki.okstoragecore.upgrades.FilterLogic;

public interface IIOFilterUpgrade {

    Optional<FilterLogic> getInputFilter();

    Optional<FilterLogic> getOutputFilter();
}
