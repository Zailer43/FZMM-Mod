package fzmm.zailer.me.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.*;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.config.hotkeys.Hotkeys;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.File;

public class Configs implements IConfigHandler
{
    private static final String CONFIG_FILE_NAME = FzmmClient.MOD_ID + ".json";

    public static class Generic {
        public static final ConfigBoolean FORCE_INVISIBLE_ITEM_FRAME = new ConfigBoolean("forceInvisibleItemFrame", false, "");
        public static final ConfigBoolean GIVE_CLIENT_SIDE = new ConfigBoolean("giveClientSide", false, "");
        public static final ConfigBoolean DISABLE_ITALIC = new ConfigBoolean("disableItalic", true, "");
        public static final ConfigBoolean PRESERVE_IMAGE_ASPECT_RATIO_IN_IMAGETEXT = new ConfigBoolean("preserveImageAspectRatioInImagetext", true, "");
        public static final ConfigString MINESKIN_API_KEY = new ConfigString("mineskinApiKey", "", "");
        public static final ConfigString DEFAULT_IMAGETEXT_BOOK_MESSAGE = new ConfigString("defaultImagetextBookMessage", "Hover over this message to see an image", "");
        public static final ConfigString DEFAULT_GRADIENT_ITEM = new ConfigString("defaultGradientItem", Items.PAPER.toString(), "");
        public static final ConfigString DEFAULT_IMAGETEXT_ITEM = new ConfigString("defaultImagetextItem", Items.PAINTING.toString(), "");
        public static final ConfigString PLAYER_STATUE_DEFAULT_CONTAINER = new ConfigString("playerStatueDefaultContainer", Items.WHITE_SHULKER_BOX.toString(), "");

        public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
                FORCE_INVISIBLE_ITEM_FRAME,
                GIVE_CLIENT_SIDE,
                DISABLE_ITALIC,
                PRESERVE_IMAGE_ASPECT_RATIO_IN_IMAGETEXT,
                MINESKIN_API_KEY,
                DEFAULT_IMAGETEXT_BOOK_MESSAGE,
                DEFAULT_GRADIENT_ITEM,
                DEFAULT_IMAGETEXT_ITEM,
                PLAYER_STATUE_DEFAULT_CONTAINER
        );
    }

    public static class Colors {
        public static final ConfigColor USEFUL_BLOCK_STATES = new ConfigColor("usefulBlockStates", "#66F5B7", "");
        public static final ConfigColor PLAYER_STATUE = new ConfigColor("playerStatue", "#CB347D", "");
        public static final ConfigColor IMAGETEXT_MESSAGES = new ConfigColor("imagetextMessages", "#71C29F", "");
        public static final ConfigColor IMAGETEXT_HOLOGRAM = new ConfigColor("imagetextHologram", "#796957", "");
        public static final ConfigColor ITEM_FRAME_HOTKEY = new ConfigColor("itemFrameHotkey", "#BB82B7", "");

        public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
                USEFUL_BLOCK_STATES,
                PLAYER_STATUE,
                IMAGETEXT_MESSAGES,
                IMAGETEXT_HOLOGRAM,
                ITEM_FRAME_HOTKEY
        );
    }

    public static class Encryptbook {
        public static final ConfigInteger MESSAGE_MAX_LENGTH = new ConfigInteger("messageMaxLength", 255, 0, 0x1ff, "");
        public static final ConfigInteger ASYMMETRIC_ENCRYPT_KEY = new ConfigInteger("asymmetricEncryptKey", 0, -0xffff, 0xffff, "");
        public static final ConfigString PADDING = new ConfigString("padding", "1234567890qwertyuiopsdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM_,.", "");
        public static final ConfigString TRANSLATION_KEY_PREFIX = new ConfigString("translationKeyPrefix", "secret_mc_", "");
        public static final ConfigString DEFAULT_BOOK_MESSAGE = new ConfigString("defaultBookMessage", "Hello world", "");
        public static final ConfigString DEFAULT_BOOK_TITLE = new ConfigString("bookTitle", "Encode book (%s)", "");
        public static final ConfigString SEPARATOR_MESSAGE = new ConfigString("separatorMessage", "-----", "");

        public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
                MESSAGE_MAX_LENGTH,
                ASYMMETRIC_ENCRYPT_KEY,
                PADDING,
                TRANSLATION_KEY_PREFIX,
                DEFAULT_BOOK_MESSAGE,
                DEFAULT_BOOK_TITLE,
                SEPARATOR_MESSAGE
        );
    }

    public static void loadFromFile()
    {
        File configFile = new File(FileUtils.getConfigDirectory(), CONFIG_FILE_NAME);

        if (configFile.exists() && configFile.isFile() && configFile.canRead())
        {
            JsonElement element = JsonUtils.parseJsonFile(configFile);

            if (element != null && element.isJsonObject())
            {
                JsonObject root = element.getAsJsonObject();

                ConfigUtils.readConfigBase(root, "Generic", Generic.OPTIONS);
                ConfigUtils.readConfigBase(root, "Colors", Colors.OPTIONS);
                ConfigUtils.readConfigBase(root, "Hotkeys", Hotkeys.HOTKEY_LIST);
                ConfigUtils.readConfigBase(root, "Encryptbook", Encryptbook.OPTIONS);
            }
        }
    }

    public static void saveToFile()
    {
        File dir = FileUtils.getConfigDirectory();

        if ((dir.exists() && dir.isDirectory()) || dir.mkdirs())
        {
            JsonObject root = new JsonObject();

            ConfigUtils.writeConfigBase(root, "Generic", Generic.OPTIONS);
            ConfigUtils.writeConfigBase(root, "Colors", Colors.OPTIONS);
            ConfigUtils.writeConfigBase(root, "Hotkeys", Hotkeys.HOTKEY_LIST);
            ConfigUtils.writeConfigBase(root, "Encryptbook", Encryptbook.OPTIONS);

            JsonUtils.writeJsonToFile(root, new File(dir, CONFIG_FILE_NAME));
        }
    }

    @Override
    public void load()
    {
        loadFromFile();
    }

    @Override
    public void save()
    {
        saveToFile();
    }

    public static Item getConfigItem(ConfigString config) {
        try {
            return Registry.ITEM.get(new Identifier(config.getStringValue()));
        } catch (Exception ignored) {
            return Registry.ITEM.get(new Identifier(config.getDefaultStringValue()));
        }
    }

    @SuppressWarnings("unchecked")
    public static void setComments() {
        setComments("generic", Generic.OPTIONS);
        setComments("colors", Colors.OPTIONS);
        setComments("hotkeys", (ImmutableList<IConfigBase>) ((Object) Hotkeys.HOTKEY_LIST));
        setComments("encryptbook", Encryptbook.OPTIONS);
    }

    private static void setComments(String commentKey, ImmutableList<IConfigBase> configs) {
        String commentBase = "fzmm.gui.configGui." + commentKey + ".comment.";
        for (IConfigBase config : configs) {
            if (config instanceof ConfigBase<?> configBase) {
                configBase.setComment(commentBase + configBase.getName());
            }
        }
    }
}