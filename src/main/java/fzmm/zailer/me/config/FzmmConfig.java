package fzmm.zailer.me.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

import java.awt.*;

@Config(name = "fzmm")
public class FzmmConfig implements ConfigData {

    @ConfigEntry.Category("general")
    @ConfigEntry.Gui.TransitiveObject
    public General general = new General();

    @ConfigEntry.Category("encodebook")
    @ConfigEntry.Gui.TransitiveObject
    public Encodebook encodebook = new Encodebook();

    @ConfigEntry.Category("imagetext")
    @ConfigEntry.Gui.TransitiveObject
    public Imagetext imagetext = new Imagetext();

    public static class General {
        public boolean forceInvisibleItemFrame = false;
        public boolean giveClientSideItem = false;
        public String loreColorPickBlock = "19b2ff";
        public String mineSkinApiKey = "";
        public boolean removeFacingState = false;
    }

    public static class Encodebook {
        public int messageLength = 255;
        public String myRandom = "1234567890abcdefqwrtyuiopsghjklzxvnmQWERTYUIOPASDFGHJKLZXCVBNM_,.";
        public String translationKey = "secret_mc_";
        public String defaultBookMessage = "Hello world";
        public String bookTitle = "Encode book (%s)";
        public String separatorMessage = "-----";
        @ConfigEntry.Gui.Tooltip()
        public long endToEndEncodeKey = 0;
    }

    public static class Imagetext {
        public ImagetextScale imagetextScale = ImagetextScale.DEFAULT;
        public String defaultBookMessage = "Pon el cursor encima de este mensaje para ver una imagen";
    }

    public static void init() {
        AutoConfig.register(FzmmConfig.class, GsonConfigSerializer::new);
    }

    public static FzmmConfig get() {
        return AutoConfig.getConfigHolder(FzmmConfig.class).getConfig();
    }

    public enum ImagetextScale {
        DEFAULT(Image.SCALE_DEFAULT),
        FAST(Image.SCALE_FAST),
        SMOOTH(Image.SCALE_SMOOTH),
        REPLICATE(Image.SCALE_REPLICATE),
        AREA_AVERAGING(Image.SCALE_AREA_AVERAGING);

        public int value;
        ImagetextScale(int value){
            this.value=value;
        }
    }
}