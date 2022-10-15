package fzmm.zailer.me.config;

import io.wispforest.owo.config.annotation.*;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@Modmenu(modId = "fzmm")
@Config(name = "fzmm", wrapperName = "FzmmConfig")
public class FzmmConfigModel {

    @Nest
    public GeneralNest general = new GeneralNest();
    @Nest
    public MineskinNest mineskin = new MineskinNest();
    @Nest
    public ImagetextNest imagetext = new ImagetextNest();
    @Nest
    public GradientNest gradient = new GradientNest();
    @Nest
    public PlayerStatueNest playerStatue = new PlayerStatueNest();
    @Nest
    public EncryptbookNest encryptbook = new EncryptbookNest();
    @Nest
    public ColorsNest colors = new ColorsNest();

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
        public String defaultBookMessage = "Hover over this detailsId to see an image";
        @PredicateConstraint("predicateItem")
        public String defaultItem = Items.PAPER.toString();
        public boolean defaultPreserveImageAspectRatio = true;

        public static boolean predicateItem(String value) {
            return FzmmConfigModel.predicateItem(value);
        }
    }

    public static class GradientNest {
        @PredicateConstraint("predicateItem")
        public String defaultItem = Items.NAME_TAG.toString();

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
        public int messageMaxLength = 255;
        public String padding = "1234567890qwertyuiopsdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM_,.";
        public String separatorMessage = "-----";
        public String translationKeyPrefix = "secret_mc_";
    }

    public static class ColorsNest {
        @RegexConstraint("[0-9a-fA-F]{6,6}")
        public String imagetextHologram = "796957";
        @RegexConstraint("[0-9a-fA-F]{6,6}")
        public String imagetextMessages = "71C29F";
        @RegexConstraint("[0-9a-fA-F]{6,6}")
        public String itemFrameFromHotkey = "BB82B7";
        @RegexConstraint("[0-9a-fA-F]{6,6}")
        public String playerStatue = "CB347D";
        @RegexConstraint("[0-9a-fA-F]{6,6}")
        public String usefulBlockStates = "66F5B7";
    }

    @SuppressWarnings("unused")
    public static boolean predicateItem(String value) {
        return Registry.ITEM.getOrEmpty(new Identifier(value)).isPresent();
    }
}