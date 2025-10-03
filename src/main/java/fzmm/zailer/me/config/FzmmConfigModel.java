package fzmm.zailer.me.config;

import fzmm.zailer.me.client.gui.imagetext.ImagetextScreen;
import fzmm.zailer.me.client.logic.enycrpt_book.TranslationEncryptProfile;
import io.wispforest.owo.config.annotation.*;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.*;

@SuppressWarnings("unused")
@Modmenu(modId = "fzmm")
@Config(name = "fzmm", wrapperName = "FzmmConfig")
public class FzmmConfigModel {

    @Nest
    @Expanded
    public GeneralNest general = new GeneralNest();
    @Nest
    public ColorsNest colors = new ColorsNest();
    @Nest
    public GuiStyleNest guiStyle = new GuiStyleNest();

    @Nest
    @SectionHeader("externalAPIs")
    public MineskinNest mineskin = new MineskinNest();
    @Nest
    public HeadGalleryNest headGallery = new HeadGalleryNest();

    @SectionHeader("gui")
    @Nest
    public ImagetextNest imagetext = new ImagetextNest();
    @Nest
    public TextFormatNest textFormat = new TextFormatNest();
    @Nest
    public PlayerStatueNest playerStatue = new PlayerStatueNest();
    @Nest
    public EncryptbookNest encryptbook = new EncryptbookNest();
    @Nest
    public HeadGeneratorNest headGenerator = new HeadGeneratorNest();
    @Nest
    public HistoryNest history = new HistoryNest();
    @Nest
    public ItemEditorBannerNest itemEditorBanner = new ItemEditorBannerNest();

    public static class GeneralNest {
        public boolean disableItalic = true;
        public boolean forceInvisibleItemFrame = false;
        public boolean giveClientSide = false;
        public boolean showSymbolButton = true;
        public boolean showItemSize = true;
        public boolean checkValidCodec = true;
        public boolean giveItemSizeLimit = true;
        public boolean minimizeHeadTexturesTag = true;
        public boolean removeViaVersionTags = true;
    }

    public static class GuiStyleNest {
        // the GUI is not well adapted for white mode, it looks quite ugly because
        // when changing the text to black, it does not look good when it has
        // no background, and it lacks application in some parts like in the dropdowns,
        // besides it looks inconsistent because the buttons have white text
//        public boolean darkMode = true;
        public boolean oldBackground = false;
        public boolean persistentScrollbar = true;
    }

    public static class ItemEditorBannerNest {
        @RangeConstraint(min = 0, max = 300, decimalPlaces = 0)
        public int maxUndo = 75;
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
        @RangeConstraint(min = 0.0f, max = ImagetextScreen.MAX_SIMILARITY_THRESHOLD, decimalPlaces = 1)
        public double defaultSimilarityThreshold = 2.5d;

        public static boolean predicateItem(String value) {
            return FzmmConfigModel.predicateItem(value);
        }
    }

    public static class TextFormatNest {
        @PredicateConstraint("predicateItem")
        public String defaultItem = Items.NAME_TAG.toString();
        @RangeConstraint(min = 0.001f, max = 0.1f, decimalPlaces = 3)
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
        public String defaultBookMessage = "Hello world";
        public String defaultBookTitle = "Encode book (%s)";
        public String padding = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_,.";
        public String separatorMessage = "-----";

        @ExcludeFromScreen
        public List<TranslationEncryptProfileModel> profiles = new ArrayList<>(
                List.of(new TranslationEncryptProfileModel())
        );
    }

    public static class ColorsNest {
        public Color imagetextHologram = Color.ofRgb(0xF1C232);
        public Color imagetextMessages = Color.ofRgb(0x71C29F);
        public Color playerStatue = Color.ofRgb(0xCB347D);
        public Color usefulBlockStates = Color.ofRgb(0x66F5B7);
        public Color headGalleryName = Color.ofRgb(0x50AF70);
        public Color headGalleryTags = Color.ofRgb(0x74D02F);
        @ExcludeFromScreen // owo-lib won't let me make Color lists
        public List<Color> favoriteColors = new ArrayList<>();
    }

    public static class HeadGalleryNest {
        @RestartRequired
        public boolean cacheCategories = true;
        public boolean setStyleToHeads = true;
        @RangeConstraint(min = 1, max = 2500)
        public int maxHeadsPerPage = 300;
        @RangeConstraint(min = 1, max = 3, decimalPlaces = 1)
        public double itemScale = 1.5d;
    }

    public static class HeadGeneratorNest {
        @ExcludeFromScreen
        public Set<String> favoriteSkins = new HashSet<>();
        public boolean forcePreEditNoneInModels = true;
    }

    public static class HistoryNest {
        @Hook
        public int maxItemHistory = 100;
        @Hook
        public int maxHeadHistory = 100;
        public boolean automaticallyRecoverScreens = true;
    }

    public static final class TranslationEncryptProfileModel {
        public int seed = 0;
        @RangeConstraint(min = 1, max = TranslationEncryptProfile.MAX_LENGTH)
        public int length = 255;
        public String key = "secret_mc_%s";
        public int asymmetricValue = 0;
        @RangeConstraint(min = 1, max = TranslationEncryptProfile.ALGORITHM_VERSION)
        public int algorithmVersion = TranslationEncryptProfile.ALGORITHM_VERSION;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TranslationEncryptProfileModel that = (TranslationEncryptProfileModel) o;
            return seed == that.seed &&
                    length == that.length &&
                    asymmetricValue == that.asymmetricValue &&
                    algorithmVersion == that.algorithmVersion &&
                    Objects.equals(key, that.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(seed, length, key, asymmetricValue, algorithmVersion);
        }
    }

    @SuppressWarnings("unused")
    public static boolean predicateItem(String value) {
        return Registries.ITEM.getEntry(Identifier.of(value)).isPresent();
    }
}