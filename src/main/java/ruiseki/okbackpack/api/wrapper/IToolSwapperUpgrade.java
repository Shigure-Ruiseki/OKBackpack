package ruiseki.okbackpack.api.wrapper;

public interface IToolSwapperUpgrade {

    String WEAPON_SWAP_TAG = "WeaponSwap";
    String TOOL_SWAP_TAG = "ToolSwap";

    WeaponSwapMode getWeaponSwapMode();

    void setWeaponSwapMode(WeaponSwapMode mode);

    ToolSwapMode getToolSwapMode();

    void setToolSwapMode(ToolSwapMode mode);

    enum WeaponSwapMode {
        SWAP_WEAPON,
        NO_SWAP_WEAPON;
    }

    enum ToolSwapMode {
        SWAP_TOOL,
        ONLY_TOOL_SWAP_TOOL,
        NO_SWAP_TOOL;
    }
}
