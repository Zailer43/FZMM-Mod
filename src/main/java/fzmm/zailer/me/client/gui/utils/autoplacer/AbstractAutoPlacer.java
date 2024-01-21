package fzmm.zailer.me.client.gui.utils.autoplacer;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.playerstatue.PlayerStatuePlacerScreen;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class AbstractAutoPlacer extends BaseFzmmScreen {
    private static final String MAIN_LAYOUT_ID = "main-layout";
    private static final String EXECUTE_ID = "execute";
    private static final String CANCEL_ID = "cancel";
    private static final String LOADING_BAR_ID = "loading-bar";
    private static final String LOADING_LABEL_ID = "loading-label";
    private static final String INFO_LABELS_ID = "info-labels";
    protected FlowLayout loadingBarLayout;
    protected LabelComponent loadingLabel;
    protected ButtonComponent cancelButton;


    public AbstractAutoPlacer(String screenPath, String baseScreenTranslationKey, @Nullable Screen parent) {
        super(screenPath, baseScreenTranslationKey, parent);
    }

    @Override
    protected void setupButtonsCallbacks(FlowLayout rootComponent) {
        assert this.client != null;
        assert this.client.player != null;

        FlowLayout mainLayout = rootComponent.childById(FlowLayout.class, MAIN_LAYOUT_ID);
        checkNull(mainLayout, "flow-layout", MAIN_LAYOUT_ID);

        ButtonComponent executeButton = rootComponent.childById(ButtonComponent.class, EXECUTE_ID);
        checkNull(executeButton, "button", EXECUTE_ID);
        executeButton.setMessage(Text.translatable(BaseFzmmScreen.getOptionBaseTranslationKey(this.baseScreenTranslationKey) + "execute"));

        this.cancelButton = rootComponent.childById(ButtonComponent.class, CANCEL_ID);
        checkNull(this.cancelButton, "button", CANCEL_ID);
        this.cancelButton.onPress(buttonComponent -> this.close());

        this.loadingBarLayout = rootComponent.childById(FlowLayout.class, LOADING_BAR_ID);
        checkNull(this.loadingBarLayout, "flow-layout", LOADING_BAR_ID);

        this.loadingLabel = rootComponent.childById(LabelComponent.class, LOADING_LABEL_ID);
        checkNull(this.loadingLabel, "label", LOADING_LABEL_ID);

        FlowLayout infoLabels = rootComponent.childById(FlowLayout.class, INFO_LABELS_ID);
        checkNull(infoLabels, "flow-layout", INFO_LABELS_ID);

        infoLabels.children(this.getInfoLabels());

        executeButton.onPress(buttonComponent -> this.execute());
    }

    protected abstract List<Component> getInfoLabels();

    public void execute() {
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

            List<ItemStack> items = this.getItems();
            int containerItemsSize = items.size();
            for (int i = 0; i < containerItemsSize; i++) {
                ItemStack itemStack = items.get(i);
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
                FzmmUtils.updateHand(this.getFinalStack());

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

    protected abstract ItemStack getFinalStack();

    protected abstract List<ItemStack> getItems();

    protected abstract boolean isActive();

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        if (this.isActive()) {
            return false;
        }

        return super.shouldCloseOnEsc();
    }
}
