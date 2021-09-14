package fzmm.zailer.me.config;

import fzmm.zailer.me.client.gui.imagetext.ImagetextLogic;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

@Config(name = "fzmm")
public class FzmmConfig implements ConfigData {

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.TransitiveObject
    public General general = new General();

    @ConfigEntry.Category("encodebook")
    @ConfigEntry.Gui.TransitiveObject
    public Encodebook encodebook = new Encodebook();

    public static class General {
        public boolean forceInvisibleItemFrame = false;
        public boolean giveClientSideItem = false;
        public String loreColorPickBlock = "19b2ff";
        public String mineSkinApiKey = "";
        public boolean removeFacingState = false;
        public boolean disableItalic = true;
        public String defaultImagetextBookMessage = "Hover over this message to see an image";
    }

    public static class Encodebook {
        public short messageLength = 255;
        public String padding = "1234567890qwertyuiopsdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM_,.";
        public String translationKey = "secret_mc_";
        public String defaultBookMessage = "Hello world";
        public String bookTitle = "Encode book (%s)";
        public String separatorMessage = "-----";
        @ConfigEntry.Gui.Tooltip()
        public int asymmetricEncodeKey = 0;
    }

    public static void init() {
        AutoConfig.register(FzmmConfig.class, GsonConfigSerializer::new);
    }

    public static FzmmConfig get() {
        return AutoConfig.getConfigHolder(FzmmConfig.class).getConfig();
    }
}