package fzmm.zailer.me.client.gui.head_gallery.filter;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.client.gui.components.extend.EContainers;
import fzmm.zailer.me.client.gui.components.extend.EStyles;
import fzmm.zailer.me.client.gui.components.extend.component.EButtonComponent;
import fzmm.zailer.me.client.gui.components.extend.component.ELabelComponent;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.snack_bar.BaseSnackBarComponent;
import fzmm.zailer.me.client.gui.components.snack_bar.ISnackBarComponent;
import fzmm.zailer.me.client.logic.api.ApiResponse;
import fzmm.zailer.me.client.logic.minecraft_heads.IMchMatcher;
import fzmm.zailer.me.client.logic.minecraft_heads.model.MchTier;
import fzmm.zailer.me.utils.SnackBarManager;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.UIComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public abstract class AbstractFilter implements Serializable {
    private final Consumer<IMchMatcher> applyOption;
    @Nullable
    protected EFlowLayout optionsLayout = null;
    @Nullable
    private ButtonComponent selectedOptionButton = null;
    private boolean hasPermission = false;

    public AbstractFilter(Consumer<IMchMatcher> applyOption) {
        this.applyOption = applyOption;
    }

    public void hasPermission(boolean hasPermission) {
        this.hasPermission = hasPermission;
    }

    protected abstract Component filterText();

    public Component buttonText() {
        if (this.hasPermission) {
            return this.filterText();
        } else {
            MchTier tier = MchTier.minTierRequired(this.permissionRequired());
            return Component.translatable("fzmm.gui.headGallery.tier.missingPermission", this.filterText(), tier.message());
        }
    }

    public void initLayout(EFlowLayout filterLayout, IMchMatcher selectedOption) {
        filterLayout.clearChildren();
        this.optionsLayout = EContainers.verticalFlow(Sizing.expand(100), Sizing.content());

        if (this.hasPermission) {
            filterLayout.child(this.parametersLayout());
            if (this.loadOptionsAutomatically()) {
                this.initOptionsAsync(selectedOption);
            }
        } else {
            MchTier tier = MchTier.minTierRequired(this.permissionRequired());
            ELabelComponent label = EComponents.label(
                    Component.translatable("fzmm.gui.headGallery.tier.missingPermission.details", tier.message())
                            .withStyle(ChatFormatting.RED)
            );
            this.optionsLayout.child(label.horizontalSizing(Sizing.expand(100)).margins(Insets.of(2)));
        }

        filterLayout.child(this.optionsLayout);
    }

    protected boolean loadOptionsAutomatically() {
        return true;
    }

    public abstract CompletableFuture<ApiResponse<List<IMchMatcher>>> initOptions();

    protected void initOptionsAsync(@Nullable IMchMatcher selectedOption) {
        this.initOptions().whenComplete((response, throwable) -> {
            if (throwable != null || response == null || !response.isSuccess()) {
                FzmmClient.LOGGER.error("[AbstractFilter] Unexpected error while loading options", throwable);
                ISnackBarComponent snackBar = BaseSnackBarComponent.builder(SnackBarManager.HEAD_GALLERY_ID)
                        .backgroundColor(EStyles.ALERT_LOADING_COLOR)
                        .title(Component.translatable("fzmm.gui.headGallery.snack_bar.error.title"))
                        .details(Component.translatable("fzmm.gui.headGallery.snack_bar.error.message"))
                        .build();

                Minecraft.getInstance().execute(() -> SnackBarManager.getInstance().add(snackBar));
                return;
            }

            this.initOptions(response, selectedOption);
        });
    }

    protected void initOptions(ApiResponse<List<IMchMatcher>> response, @Nullable IMchMatcher selectedOption) {
        Optional<List<IMchMatcher>> filtersOptional = response.data();
        if (this.optionsLayout == null || filtersOptional.isEmpty()) return;
        this.optionsLayout.clearChildren();
        this.selectedOptionButton = EComponents.button(Component.empty());
        List<UIComponent> buttonList = new ArrayList<>();

        for (var option : filtersOptional.get()) {
            EButtonComponent button = this.optionButton(option);
            buttonList.add(button);

            if (option.equals(selectedOption)) {
                button.onPress();
            }
        }

        if (buttonList.isEmpty()) { // options can be missing in collections or maybe internet issue
            this.optionsLayout.child(
                    EComponents.label(this.missingOptions())
                            .horizontalSizing(Sizing.expand(100))
                            .margins(Insets.of(2))
            );
        } else {
            this.optionsLayout.children(buttonList);
        }
    }

    protected abstract Component missingOptions();

    protected EFlowLayout parametersLayout() {
        return EContainers.horizontalFlow(Sizing.expand(100), Sizing.content());
    }

    private EButtonComponent optionButton(IMchMatcher value) {
        EButtonComponent result = EComponents.button(this.optionText(value));

        result.onPress(button -> {
                    this.selectedOptionButton = button;
                    this.applyOption.accept(value);
                }).renderer(EStyles.DEFAULT_FLAT_BUTTON)
                .sizing(Sizing.fill(100), Sizing.fixed(16));

        return result;
    }

    public Component optionText(IMchMatcher option) {
        return Component.literal(option.name());
    }

    public void optionsEnabled(boolean active) {
        if (this.optionsLayout == null) return;

        for (var child : this.optionsLayout.children()) {
            if (child instanceof ButtonComponent button) {
                button.active(active);
            }
        }

        if (this.selectedOptionButton != null) { // some filters have no default selected option
            this.selectedOptionButton.active(false);
        }
    }

    public abstract int permissionRequired();
}
