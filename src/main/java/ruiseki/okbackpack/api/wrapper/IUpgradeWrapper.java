package ruiseki.okbackpack.api.wrapper;

public interface IUpgradeWrapper {

    String TAB_STATE_TAG = "TabState";

    void setTabOpened(boolean opened);

    boolean isTabOpened();

    String getSettingLangKey();
}
