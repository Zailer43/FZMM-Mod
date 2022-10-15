//package fzmm.zailer.me.client.gui;
//
//import fi.dy.masa.malilib.config.IConfigBase;
//import fi.dy.masa.malilib.config.options.ConfigInteger;
//import fi.dy.masa.malilib.config.options.ConfigOptionList;
//import fi.dy.masa.malilib.config.options.ConfigString;
//import fi.dy.masa.malilib.gui.GuiBase;
//import fi.dy.masa.malilib.gui.button.ButtonBase;
//import fi.dy.masa.malilib.gui.button.ButtonGeneric;
//import fi.dy.masa.malilib.gui.button.IButtonActionListener;
//import fi.dy.masa.malilib.util.StringUtils;
//import fzmm.zailer.me.client.gui.enums.Buttons;
//import fzmm.zailer.me.client.FzmmIcons;
//import fzmm.zailer.me.client.gui.enums.options.DirectionOption;
//import fzmm.zailer.me.client.gui.enums.options.SkinOption;
//import fzmm.zailer.me.client.gui.interfaces.IScreenTab;
//import fzmm.zailer.me.client.gui.interfaces.ITabListener;
//import fzmm.zailer.me.utils.ImageOption;
//import fzmm.zailer.me.client.gui.wrapper.OptionWrapper;
//import fzmm.zailer.me.client.logic.playerStatue.PlayerStatue;
//import fzmm.zailer.me.utils.FzmmUtils;
//import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
//import net.minecraft.client.MinecraftClient;
//import net.minecraft.client.gui.screen.ConfirmLinkScreen;
//import net.minecraft.client.gui.screen.Screen;
//import net.minecraft.client.network.ClientPlayerEntity;
//import net.minecraft.item.ItemStack;
//import net.minecraft.util.Util;
//import net.minecraft.util.math.Vec3f;
//import net.minecraft.world.World;
//
//import java.awt.image.BufferedImage;
//import java.util.ArrayList;
//import java.util.List;
//
//public class PlayerStatueScreen extends GuiOptionsBase {
//    private static Thread CREATE_THREAD = new Thread(null, PlayerStatueScreen::createPlayerStatue, "Fzmm: Player Statue");
//    private static final ImageOption.ImageStatus OLD_SKIN_FORMAT_NOT_SUPPORTED = new ImageOption.ImageStatus("playerStatue.oldSkinFormatNotSupported", FzmmIcons.ERROR);
//    private static ButtonGeneric executeButton;
//    private static ButtonGeneric lastStatueButton;
//    private static PlayerStatue statue = null;
//    private static PlayerStatue lastStatueGenerated;
//    private static PlayerStatueGuiTab tab = PlayerStatueGuiTab.CREATE;
//    private final ConfigOptionList configDirectionOption;
//    private final ConfigInteger configPosX;
//    private final ConfigInteger configPosY;
//    private final ConfigInteger configPosZ;
//    private final ImageOption configSkin;
//    private final ConfigString configName;
//
//    public PlayerStatueScreen(Screen parent) {
//        super("playerStatue", parent);
//
//        MinecraftClient mc = MinecraftClient.getInstance();
//        ClientPlayerEntity player = mc.player;
//        assert player != null;
//
//        this.configDirectionOption = new ConfigOptionList("direction", DirectionOption.getPlayerDirection(), this.commentBase + "direction");
//        this.configPosX = new ConfigInteger("x", player.getBlockX(), -World.HORIZONTAL_LIMIT, World.HORIZONTAL_LIMIT, this.commentBase + "coordinates");
//        this.configPosY = new ConfigInteger("y", player.getBlockY(), -0xffff, 0xffff, this.commentBase + "coordinates");
//        this.configPosZ = new ConfigInteger("z", player.getBlockZ(), -World.HORIZONTAL_LIMIT, World.HORIZONTAL_LIMIT, this.commentBase + "coordinates");
//        this.configSkin = new ImageOption("skin", "", SkinOption.NAME, this.commentBase + "skin");
//        this.configName = new ConfigString("statueName", "", this.commentBase + "statueName");
//
//        this.configSkin.setValueChangeCallback(this::skinLoadCallback);
//    }
//
//    @Override
//    public void initGui() {
//        super.initGui();
//
//        this.createTabs(PlayerStatueGuiTab.values(), new TabButtonListener(this));
//
//        executeButton = Buttons.EXECUTE.get(20, this.height - 40, ScreenConstants.NORMAL_BUTTON_WIDTH);
//        executeButton.setEnabled(!CREATE_THREAD.isAlive());
//
//        lastStatueButton = Buttons.PLAYER_STATUE_LAST_GENERATED.get(22 + executeButton.getWidth(), this.height - 40, -1);
//        lastStatueButton.setEnabled(lastStatueGenerated != null);
//
//        ButtonGeneric faqButton = Buttons.FAQ.getToLeft(this.width - 20, 20);
//
//        this.addButton(executeButton, this::executeButtonListener);
//        this.addButton(lastStatueButton, (button, mouseButton) -> FzmmUtils.giveItem(lastStatueGenerated.getStatueInContainer()));
//        this.addButton(faqButton, new FaqButtonListener());
//    }
//
//    @Override
//    public List<ConfigOptionWrapper> getConfigs() {
//        List<IConfigBase> options = new ArrayList<>();
//
//        options.add(this.configDirectionOption);
//        options.add(this.configPosX);
//        options.add(this.configPosY);
//        options.add(this.configPosZ);
//        options.add(this.configName);
//
//        List<ConfigOptionWrapper> optionsWrapper = OptionWrapper.createFor(options);
//        this.addTabOptions(optionsWrapper);
//
//        return optionsWrapper;
//
//    }
//
//    @Override
//    public boolean isTab(IScreenTab tab) {
//        return PlayerStatueScreen.tab == tab;
//    }
//
//    private void addTabOptions(List<ConfigOptionWrapper> list) {
//        List<IConfigBase> options = new ArrayList<>();
//
//        if (tab == PlayerStatueGuiTab.CREATE) {
//            options.add(this.configSkin);
//            list.add(new OptionWrapper(""));
//            list.add(new OptionWrapper(tab.translationKey));
//        }
//
//        list.addAll(OptionWrapper.createFor(options));
//    }
//
//    private enum PlayerStatueGuiTab implements IScreenTab {
//        CREATE("create"),
//        UPDATE("update");
//
//        static final String BASE_KEY = "fzmm.gui.playerStatue.";
//
//        private final String translationKey;
//
//        PlayerStatueGuiTab(String translationKey) {
//            this.translationKey = BASE_KEY + translationKey;
//        }
//
//        public String getDisplayName() {
//            return StringUtils.translate(this.translationKey);
//        }
//    }
//
//    private static class TabButtonListener implements ITabListener {
//        private final IScreenTab tab;
//        private final PlayerStatueScreen parent;
//
//        private TabButtonListener(IScreenTab tab, PlayerStatueScreen parent) {
//            this.tab = tab;
//            this.parent = parent;
//        }
//
//        private TabButtonListener(PlayerStatueScreen gui) {
//            this(null, gui);
//        }
//
//        @Override
//        public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
//            if (this.tab == null)
//                return;
//
//            PlayerStatueScreen.tab = (PlayerStatueGuiTab) this.tab;
//
//            this.parent.reload();
//        }
//
//        @Override
//        public ITabListener of(IScreenTab tab) {
//            return new TabButtonListener(tab, this.parent);
//        }
//
//        @Override
//        public GuiOptionsBase getParent() {
//            return this.parent;
//        }
//    }
//
//    public void executeButtonListener(ButtonBase button, int mouseButton) {
//        DirectionOption direction = (DirectionOption) PlayerStatueScreen.this.configDirectionOption.getOptionListValue();
//        int x = this.configPosX.getIntegerValue();
//        int y = this.configPosY.getIntegerValue();
//        int z = this.configPosZ.getIntegerValue();
//        String name = this.configName.getStringValue();
//        Vec3f pos = new Vec3f(x, y, z);
//
//        if (tab == PlayerStatueGuiTab.CREATE) {
//            if (PlayerStatueScreen.this.configSkin.hasNoImage())
//                return;
//
//            BufferedImage skin = this.configSkin.getImage();
//            statue = new PlayerStatue(skin, name, pos, direction);
//
//            CREATE_THREAD = new Thread(null, PlayerStatueScreen::createPlayerStatue, "Fzmm: Player Statue");
//            CREATE_THREAD.start();
//        } else {
//            MinecraftClient client = MinecraftClient.getInstance();
//            assert client.player != null;
//
//            ItemStack statue = PlayerStatue.updateStatue(client.player.getMainHandStack(), pos, direction, name);
//            FzmmUtils.giveItem(statue);
//        }
//    }
//
//    private class FaqButtonListener implements IButtonActionListener, BooleanConsumer {
//        private static final String FAQ_URL = "https://github.com/Zailer43/FZMM-Mod/wiki/FAQ-Player-Statue";
//
//        @Override
//        public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
//            GuiBase.openGui(new ConfirmLinkScreen(this, FAQ_URL, true));
//        }
//
//        @Override
//        public void accept(boolean bl) {
//            if (bl) {
//				Util.getOperatingSystem().open(FAQ_URL);
//			}
//
//			GuiBase.openGui(PlayerStatueScreen.this);
//        }
//    }
//
//    private static void createPlayerStatue() {
//        new Thread(() -> {
//            if (statue == null)
//                return;
//            executeButton.setEnabled(false);
//
//            lastStatueGenerated = statue.generateStatues();
//
//            lastStatueButton.setEnabled(true);
//            FzmmUtils.giveItem(lastStatueGenerated.getStatueInContainer());
//            statue = null;
//
//            executeButton.setEnabled(true);
//        }).start();
//    }
//
//    private void skinLoadCallback(ImageOption option) {
//        if (option.hasNoImage())
//            return;
//
//        BufferedImage image = option.getImage();
//        assert image != null;
//        int width = image.getWidth();
//        int height = image.getHeight();
//        if (!(width == 64 && height == 64) && !(width == 128 && height == 128)) {
//            option.setStatus(OLD_SKIN_FORMAT_NOT_SUPPORTED);
//            option.setImage(null);
//        }
//    }
//}
