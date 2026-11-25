package ruiseki.okbackpack.api;

public interface ITintable {

    String MAIN_COLOR = "MainColor";
    String ACCENT_COLOR = "AccentColor";

    int getMainColor();

    int getAccentColor();

    void setColors(int mainColor, int accentColor);
}
