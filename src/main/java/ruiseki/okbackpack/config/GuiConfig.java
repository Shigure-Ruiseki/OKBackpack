package ruiseki.okbackpack.config;

import net.minecraft.client.gui.GuiScreen;

import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.SimpleGuiConfig;

import ruiseki.okbackpack.Reference;

public class GuiConfig extends SimpleGuiConfig {

    public GuiConfig(GuiScreen parent) throws ConfigException {
        super(parent, Reference.MOD_ID, Reference.MOD_NAME);
    }
}
