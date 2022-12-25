package fzmm.zailer.me.config;

import io.wispforest.owo.config.annotation.*;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("unused")
@Modmenu(modId = "fzmm")
@Config(name = "fzmm", wrapperName = "FzmmConfig")
public class FzmmConfigModel {


    @Nest
    public GeneralNest general = new GeneralNest();

    @Nest
    public ColorsNest colors = new ColorsNest();
    @Nest
    public EncryptbookNest encryptbook = new EncryptbookNest();
    @Nest
    public HeadGeneratorNest headGenerator = new HeadGeneratorNest();
    @Nest
    public ImagetextNest imagetext = new ImagetextNest();
    @Nest
    public MineskinNest mineskin = new MineskinNest();
    @Nest
    public PlayerStatueNest playerStatue = new PlayerStatueNest();
    @Nest
    public TextFormatNest textFormat = new TextFormatNest();

    public static class GeneralNest {
        public boolean disableItalic = true;
        public boolean forceInvisibleItemFrame = false;
        public boolean giveClientSide = false;
    }

    public static class MineskinNest {
        public String apiKey = "";
        public boolean publicSkins = false;
    }

    public static class ImagetextNest {
        public String defaultBookMessage = "Hover over this message to see an image";
        @PredicateConstraint("predicateItem")
        public String defaultItem = Items.PAPER.toString();
        public boolean defaultPreserveImageAspectRatio = true;
        @ExcludeFromScreen
        public int maxResolution = 127;

        public static boolean predicateItem(String value) {
            return FzmmConfigModel.predicateItem(value);
        }
    }

    public static class TextFormatNest {
        @PredicateConstraint("predicateItem")
        public String defaultItem = Items.NAME_TAG.toString();
        @RangeConstraint(min = 0.001f, max = 0.1f)
        public float minRainbowHueStep = 0.005f;
        @RangeConstraint(min = 0.01f, max = 0.99f)
        public float maxRainbowHueStep = 0.15f;

        public static boolean predicateItem(String value) {
            return FzmmConfigModel.predicateItem(value);
        }
    }

    public static class PlayerStatueNest {
        public boolean convertSkinWithAlexModelInSteveModel = true;
        @PredicateConstraint("predicateItem")
        public String defaultContainer = Items.WHITE_SHULKER_BOX.toString();

        public static boolean predicateItem(String value) {
            return FzmmConfigModel.predicateItem(value);
        }
    }

    public static class EncryptbookNest {
        public int asymmetricEncryptKey = 0;
        public String defaultBookMessage = "Hello world";
        public String defaultBookTitle = "Encode book (%s)";
        @RangeConstraint(min = 1, max = 512)
        public int maxMessageLength = 255;
        public String padding = "1234567890qwertyuiopsdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM_,.";
        public String separatorMessage = "-----";
        public String translationKeyPrefix = "secret_mc_";
    }

    public static class ColorsNest {
        @RegexConstraint("[0-9a-fA-F]{6,6}")
        public String imagetextHologram = "F1C232";
        @RegexConstraint("[0-9a-fA-F]{6,6}")
        public String imagetextMessages = "71C29F";
        @RegexConstraint("[0-9a-fA-F]{6,6}")
        public String itemFrameFromHotkey = "BB82B7";
        @RegexConstraint("[0-9a-fA-F]{6,6}")
        public String playerStatue = "CB347D";
        @RegexConstraint("[0-9a-fA-F]{6,6}")
        public String usefulBlockStates = "66F5B7";
    }

    public static class HeadGeneratorNest {
        @ExcludeFromScreen
        public Set<String> favoriteSkins = new HashSet<>();
    }

    @SuppressWarnings("unused")
    public static boolean predicateItem(String value) {
        return Registries.ITEM.getOrEmpty(new Identifier(value)).isPresent();
    }
}