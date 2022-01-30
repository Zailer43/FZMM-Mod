package fzmm.zailer.me.config;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fi.dy.masa.malilib.config.ConfigUtils;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.IConfigHandler;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigColor;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.config.options.ConfigString;
import fi.dy.masa.malilib.util.FileUtils;
import fi.dy.masa.malilib.util.JsonUtils;
import fzmm.zailer.me.config.hotkeys.Hotkeys;

import java.io.File;

public class Configs implements IConfigHandler
{
    private static final String CONFIG_FILE_NAME = "fzmm.json";

    public static class Generic {
        public static final ConfigBoolean FORCE_INVISIBLE_ITEM_FRAME = new ConfigBoolean("forceInvisibleItemFrame", false, "");
        public static final ConfigBoolean GIVE_CLIENT_SIDE = new ConfigBoolean("giveClientSide", false, "");
        public static final ConfigBoolean REMOVE_FACING_STATE = new ConfigBoolean("removeFacingStateOnPick", false, "");
        public static final ConfigBoolean DISABLE_ITALIC = new ConfigBoolean("disableItalic", true, "");
        public static final ConfigString MINESKIN_API_KEY = new ConfigString("mineskinApiKey", "", "");
        public static final ConfigString DEFAULT_IMAGETEXT_BOOK_MESSAGE = new ConfigString("defaultImagetextBookMessage", "Hover over this message to see an image", "");

        public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
                FORCE_INVISIBLE_ITEM_FRAME,
                GIVE_CLIENT_SIDE,
                REMOVE_FACING_STATE,
                DISABLE_ITALIC,
                MINESKIN_API_KEY,
                DEFAULT_IMAGETEXT_BOOK_MESSAGE
        );
    }

    public static class Colors {
        public static final ConfigColor LORE_PICK_BLOCK = new ConfigColor("lorePickBlock", "#FF19B2FF", "");
        public static final ConfigColor USEFUL_BLOCK_STATES = new ConfigColor("usefulBlockStates", "#FF66F5B7", "");
        public static final ConfigColor PLAYER_STATUE = new ConfigColor("playerStatue", "#FFCB347D", "");
        public static final ConfigColor IMAGETEXT_HOLOGRAM = new ConfigColor("imagetextHologram", "#FF796957", "");

        public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
                LORE_PICK_BLOCK,
                USEFUL_BLOCK_STATES,
                PLAYER_STATUE,
                IMAGETEXT_HOLOGRAM
        );
    }

    public static class Encodebook {
        public static final ConfigInteger MESSAGE_MAX_LENGTH = new ConfigInteger("messageMaxLength", 255, 0, 0x1ff, "");
        public static final ConfigInteger ASYMMETRIC_ENCODE_KEY = new ConfigInteger("asymmetricEncodeKey", 0, -0xffff, 0xffff, "");
        public static final ConfigString PADDING = new ConfigString("padding", "1234567890qwertyuiopsdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM_,.", "");
        public static final ConfigString TRANSLATION_KEY_PREFIX = new ConfigString("translationKeyPrefix", "secret_mc_", "");
        public static final ConfigString DEFAULT_BOOK_MESSAGE = new ConfigString("defaultBookMessage", "Hello world", "");
        public static final ConfigString DEFAULT_BOOK_TITLE = new ConfigString("bookTitle", "Encode book (%s)", "");
        public static final ConfigString SEPARATOR_MESSAGE = new ConfigString("separatorMessage", "-----", "");

        public static final ImmutableList<IConfigBase> OPTIONS = ImmutableList.of(
                MESSAGE_MAX_LENGTH,
                ASYMMETRIC_ENCODE_KEY,
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
                ConfigUtils.readConfigBase(root, "Encodebook", Encodebook.OPTIONS);
            }
        }
    }

    public static void saveToFile()
    {
        File dir = FileUtils.getConfigDirectory();

        if ((dir.exists() && dir.isDirectory()) || dir.mkdirs())
        {
            JsonObject root = new JsonObject();

            ConfigUtils.readConfigBase(root, "Generic", Generic.OPTIONS);
            ConfigUtils.readConfigBase(root, "Colors", Colors.OPTIONS);
            ConfigUtils.writeConfigBase(root, "Hotkeys", Hotkeys.HOTKEY_LIST);
            ConfigUtils.readConfigBase(root, "Encodebook", Encodebook.OPTIONS);

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
}