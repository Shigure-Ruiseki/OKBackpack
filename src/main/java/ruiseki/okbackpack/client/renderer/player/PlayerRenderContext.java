package ruiseki.okbackpack.client.renderer.player;

public class PlayerRenderContext {

    private boolean renderHelmet = true;
    private boolean renderCape = true;
    private boolean renderItem = true;

    public boolean renderHelmet() {
        return renderHelmet;
    }

    public boolean renderCape() {
        return renderCape;
    }

    public boolean renderItem() {
        return renderItem;
    }

    public void setRenderHelmet(boolean value) {
        this.renderHelmet = value;
    }

    public void setRenderCape(boolean value) {
        this.renderCape = value;
    }

    public void setRenderItem(boolean value) {
        this.renderItem = value;
    }
}
