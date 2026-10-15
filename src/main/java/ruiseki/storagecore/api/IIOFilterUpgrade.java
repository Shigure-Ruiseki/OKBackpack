package ruiseki.storagecore.api;

import java.util.Optional;

import ruiseki.storagecore.upgrades.FilterLogic;

public interface IIOFilterUpgrade {

    Optional<FilterLogic> getInputFilter();

    Optional<FilterLogic> getOutputFilter();
}
