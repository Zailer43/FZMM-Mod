package fzmm.zailer.me.client.gui.main;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.FzmmIcons;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.EncryptBookScreen;
import fzmm.zailer.me.client.gui.converters.ConvertersScreen;
import fzmm.zailer.me.client.gui.headgallery.HeadGalleryScreen;
import fzmm.zailer.me.client.gui.headgenerator.HeadGeneratorScreen;
import fzmm.zailer.me.client.gui.HistoryScreen;
import fzmm.zailer.me.client.gui.imagetext.ImagetextScreen;
import fzmm.zailer.me.client.gui.item_editor.base.ItemEditorBaseScreen;
import fzmm.zailer.me.client.gui.playerstatue.PlayerStatueScreen;
import fzmm.zailer.me.client.gui.textformat.TextFormatScreen;
import io.wispforest.owo.config.ui.ConfigScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.TextureComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

public class MainScreen extends BaseFzmmScreen {

    public MainScreen(@Nullable Screen parent) {
        super("main", "main", parent);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void setupButtonsCallbacks(FlowLayout rootComponent) {
        rootComponent.childById(ButtonComponent.class, "config-button")
                .onPress(button -> this.client.setScreen(ConfigScreen.create(FzmmClient.CONFIG, this)));

        Map<String, ButtonData> openScreenButtons = Map.of(
                "imagetext-button", new ButtonData(() -> new ImagetextScreen(this), 16, 0, false),
                "textFormat-button", new ButtonData(() -> new TextFormatScreen(this), 16, 16, false),
                "playerStatue-button", new ButtonData(() -> new PlayerStatueScreen(this), 16, 32, false),
                "encryptbook-button", new ButtonData(() -> new EncryptBookScreen(this), 16, 48, false),
                "headGenerator-button", new ButtonData(() -> new HeadGeneratorScreen(this), 16, 64, false),
                "converters-button", new ButtonData(() -> new ConvertersScreen(this), 16, 80, false),
                "history-button", new ButtonData(() -> new HistoryScreen(this), 16, 96, false),
                "headGallery-button", new ButtonData(() -> new HeadGalleryScreen(this), 16, 112, false),
                "itemEditor-button", new ButtonData(() -> new ItemEditorBaseScreen(this), 16, 128, true)
        );

        for (var key : openScreenButtons.keySet()) {
            FlowLayout buttonLayout = rootComponent.childById(FlowLayout.class, key);
            ButtonData data = openScreenButtons.get(key);
            if (buttonLayout != null) {
                buttonLayout.mouseDown().subscribe((mouseX, mouseY, button1) -> {
                    this.client.setScreen(data.screen.get());
                    return true;
                });
                buttonLayout.cursorStyle(CursorStyle.HAND);
                Surface defaultSurface = Surface.flat(0x40000000).and(Surface.outline(0x40FFFFFF));
                buttonLayout.surface(defaultSurface);
                buttonLayout.mouseEnter().subscribe(() -> buttonLayout.surface(Surface.flat(0x20004000).and(Surface.outline(0x40008000))));
                buttonLayout.mouseLeave().subscribe(() -> buttonLayout.surface(defaultSurface));

                FlowLayout iconLayout = Containers.horizontalFlow(Sizing.fixed(32), Sizing.fixed(32));
                int iconSize = 32;
                if (data.hasHandItem) {
                    ItemStack handStack = this.client.player.getMainHandStack();
                    handStack = handStack.isEmpty() ? Items.DIAMOND_SWORD.getDefaultStack() : handStack;
                    iconLayout.child(Components.item(handStack).sizing(Sizing.fixed(32), Sizing.fixed(32)));
                    iconSize = 16;
                }

                TextureComponent textureComponent = Components.texture(FzmmIcons.TEXTURE, data.iconU, data.iconV, 16, 16, 256, 256);
                textureComponent.sizing(Sizing.fixed(iconSize), Sizing.fixed(iconSize));
                textureComponent.positioning(Positioning.relative(100, 100));
                textureComponent.zIndex(200);
                iconLayout.child(textureComponent);

                buttonLayout.child(iconLayout);
            }
        }
    }

    private record ButtonData(Supplier<Screen> screen, int iconU, int iconV, boolean hasHandItem) {
    }
}