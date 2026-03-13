package ruiseki.okbackpack.common.item.wrapper;

public interface IUpgrade {

    String TAB_STATE_TAG = "TabState";

    void setTabOpened(boolean opened);

    boolean isTabOpened();

    String getSettingLangKey();
}
