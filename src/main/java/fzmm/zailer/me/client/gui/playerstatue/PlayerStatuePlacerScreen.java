package fzmm.zailer.me.client.gui.playerstatue;

import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.utils.autoplacer.AutoPlacerHud;
import fzmm.zailer.me.client.logic.playerStatue.PlayerStatue;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.InventoryUtils;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

public class PlayerStatuePlacerScreen extends BaseFzmmScreen {
    public static boolean isActive = false;
    private static final String MAIN_LAYOUT_ID = "main-layout";
    private static final String PLACE_PLAYER_STATUE_ID = "place-player-statue";
    private static final String CANCEL_ID = "cancel";
    private static final String LOADING_BAR_ID = "loading-bar";
    private static final String LOADING_LABEL_ID = "loading-label";
    private static final String INFO_LABELS_ID = "info-labels";
    private final ItemStack playerStatueStack;
    private final List<ItemStack> containerItems;
    private FlowLayout loadingBarLayout;
    private LabelComponent loadingLabel;
    private ButtonComponent cancelButton;

    public PlayerStatuePlacerScreen(ItemStack playerStatueStack) {
        super("utils/player_statue_placer", "playerStatuePlacer", null);
        this.playerStatueStack = playerStatueStack;
        this.containerItems = InventoryUtils.getItemsFromContainer(this.playerStatueStack);
    }

    public static AutoPlacerHud.Activation getActivation() {
        Predicate<ItemStack> predicate = itemStack -> !PlayerStatuePlacerScreen.isActive &&
                itemStack.getItem() instanceof BlockItem blockItem &&
                blockItem.getBlock() instanceof BlockWithEntity &&
                PlayerStatue.isPlayerStatue(itemStack);

        List<AutoPlacerHud.Requirement> requirements = new ArrayList<>();
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;

        requirements.add(new AutoPlacerHud.Requirement(() -> {
            float yaw = MathHelper.wrapDegrees(client.player.getYaw());
            return yaw > 80 && yaw < 110;
        }, Text.translatable("fzmm.gui.playerStatuePlacer.label.requirement.invalidYaw")));

        return new AutoPlacerHud.Activation(predicate, PlayerStatuePlacerScreen::new, requirements);
    }

    @Override
    protected void setupButtonsCallbacks(FlowLayout rootComponent) {
        assert this.client != null;
        assert this.client.player != null;

        FlowLayout mainLayout = rootComponent.childById(FlowLayout.class, MAIN_LAYOUT_ID);
        checkNull(mainLayout, "flow-layout", MAIN_LAYOUT_ID);

        ButtonComponent placePlayerStatueButton = rootComponent.childById(ButtonComponent.class, PLACE_PLAYER_STATUE_ID);
        checkNull(placePlayerStatueButton, "button", PLACE_PLAYER_STATUE_ID);

        this.cancelButton = rootComponent.childById(ButtonComponent.class, CANCEL_ID);
        checkNull(this.cancelButton, "button", CANCEL_ID);
        this.cancelButton.onPress(buttonComponent -> this.close());

        this.loadingBarLayout = rootComponent.childById(FlowLayout.class, LOADING_BAR_ID);
        checkNull(this.loadingBarLayout, "flow-layout", LOADING_BAR_ID);

        this.loadingLabel = rootComponent.childById(LabelComponent.class, LOADING_LABEL_ID);
        checkNull(this.loadingLabel, "label", LOADING_LABEL_ID);

        FlowLayout infoLabels = rootComponent.childById(FlowLayout.class, INFO_LABELS_ID);
        checkNull(infoLabels, "flow-layout", INFO_LABELS_ID);

        infoLabels.child(Components.label(this.playerStatueStack.getName()));

        for (var text : DisplayBuilder.of(this.playerStatueStack).getLoreText()) {
            infoLabels.child(Components.label(text));
        }

        placePlayerStatueButton.onPress(buttonComponent -> this.placePlayerStatue());
    }

    public void placePlayerStatue() {
        assert this.client != null;
        assert this.client.player != null;
        this.client.execute(() -> {
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            PlayerStatuePlacerScreen.isActive = true;
            this.cancelButton.active = false;

            SimpleOption<Boolean> sneakToggled = this.client.options.getSneakToggled();
            boolean isSneakToggled = sneakToggled.getValue();
            sneakToggled.setValue(true);
            this.client.options.sneakKey.setPressed(true);

            int containerItemsSize = this.containerItems.size();
            for (int i = 0; i < containerItemsSize; i++) {
                ItemStack itemStack = this.containerItems.get(i);
                int index = i;

                scheduler.schedule(() -> {
                    FzmmUtils.updateHand(itemStack);
                    this.client.doItemUse();

                    int percent = (int) (((index + 1) / (float) containerItemsSize) * 100);
                    this.loadingBarLayout.horizontalSizing(Sizing.fill(percent));
                    this.loadingLabel.text(Text.literal(percent + "%"));
                }, (i + 1) * 500L, TimeUnit.MILLISECONDS);
            }

            scheduler.schedule(() -> {
                FzmmUtils.updateHand(this.playerStatueStack);

                PlayerStatuePlacerScreen.isActive = false;
                this.cancelButton.active = true;
                Text backText = Text.translatable("fzmm.gui.button.back");
                this.cancelButton.setMessage(backText);
                this.cancelButton.horizontalSizing(Sizing.fixed(this.client.textRenderer.getWidth(backText) + BaseFzmmScreen.BUTTON_TEXT_PADDING));

                sneakToggled.setValue(isSneakToggled);
                this.client.options.sneakKey.setPressed(false);
            }, (containerItemsSize + 2) * 500L, TimeUnit.MILLISECONDS);

            scheduler.shutdown();
        });
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        if (isActive) {
            return false;
        }

        return super.shouldCloseOnEsc();
    }
}
