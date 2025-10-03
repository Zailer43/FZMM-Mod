package fzmm.zailer.me.client.gui.encrypt_book.components;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.client.gui.components.extend.EContainers;
import fzmm.zailer.me.client.gui.components.extend.EStyles;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.snack_bar.BaseSnackBarComponent;
import fzmm.zailer.me.client.gui.encrypt_book.translation_file_saver.ITranslationFileSaver;
import fzmm.zailer.me.client.gui.encrypt_book.translation_file_saver.TranslationCreateResourcePack;
import fzmm.zailer.me.client.gui.encrypt_book.translation_file_saver.TranslationUpdateResourcePack;
import fzmm.zailer.me.client.gui.encrypt_book.translation_file_saver.TranslationWriteLang;
import fzmm.zailer.me.client.logic.enycrpt_book.TranslationEncryptProfile;
import fzmm.zailer.me.utils.SnackBarManager;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.OverlayContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.List;

public class DecryptorSaverOverlay extends OverlayContainer<EFlowLayout> {
    private static final int WIDTH = 350;
    private ITranslationFileSaver selectedSaver;

    public DecryptorSaverOverlay(TranslationEncryptProfile selectedProfile) {
        super(EContainers.verticalFlow(Sizing.fixed(WIDTH), Sizing.content()));

        this.addComponents(selectedProfile);
        this.child.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        this.child.gap(12);
        this.child.padding(Insets.of(6));
        this.child.surface(this.child.styledPanel());
    }

    //TODO: import from clipboard button (and export to clipboard button in profile component)
    //TODO: get all config profiles checkbox
    protected void addComponents(TranslationEncryptProfile selectedProfile) {
        //title
        LabelComponent label = EComponents.label(Text.translatable("fzmm.gui.encryptbook.getDecryptor.title"));
        label.horizontalSizing(Sizing.expand(100));

        // options
        FlowLayout optionsLayout = EContainers.verticalFlow(Sizing.expand(100), Sizing.content());

        List<ITranslationFileSaver> options = List.of(
                new TranslationCreateResourcePack(),
                new TranslationUpdateResourcePack(),
                new TranslationWriteLang()
        );

        for (var option : options) {
            optionsLayout.child(Components.button(option.getMessage(), optionButton -> {
                        for (var optionComponent : optionsLayout.children()) {
                            if (optionComponent instanceof ButtonComponent buttonComponent) {
                                buttonComponent.active = buttonComponent != optionButton;
                            }
                        }
                        this.selectedSaver = option;
                    }).renderer(EStyles.DEFAULT_FLAT_BUTTON).horizontalSizing(Sizing.expand(100))
            );
        }

        // default
        ((ButtonComponent) optionsLayout.children().get(0)).onPress();

        // bottom buttons
        FlowLayout buttonLayout = EContainers.horizontalFlow(Sizing.expand(100), Sizing.fixed(20));

        buttonLayout.child(Components.button(Text.translatable("gui.done"), buttonComponent -> {
                    this.execute(selectedSaver, selectedProfile);
                    this.remove();
                }).positioning(Positioning.relative(0, 0))
                .horizontalSizing(Sizing.fixed(100)));

        buttonLayout.child(Components.button(Text.translatable("fzmm.gui.button.cancel"), buttonComponent -> this.remove())
                .positioning(Positioning.relative(100, 0))
                .horizontalSizing(Sizing.fixed(100))
        );

        this.child.child(label);
        this.child.child(optionsLayout);
        this.child.child(buttonLayout);
    }

    protected void execute(ITranslationFileSaver translationFileSaver, TranslationEncryptProfile profile) {
        translationFileSaver.save(profile)
                .exceptionally(throwable -> {
                    MinecraftClient.getInstance().execute(() -> {
                        FzmmClient.LOGGER.error("[GetDecryptorOverlay] Failed to get decryptor", throwable);

                        SnackBarManager.getInstance().add(BaseSnackBarComponent.builder(SnackBarManager.ENCRYPTOR_SAVE_ID)
                                .keepOnLimit()
                                .backgroundColor(EStyles.ALERT_ERROR_COLOR)
                                .mediumTimer()
                                .title(Text.translatable("fzmm.gui.encryptbook.getDecryptor.snack_bar.error"))
                                .startTimer()
                                .build()
                        );
                    });

                    return true;
                }).thenAccept(cancelled -> {
                    if (cancelled) {
                        return;
                    }
                    SnackBarManager.getInstance().add(BaseSnackBarComponent.builder(SnackBarManager.ENCRYPTOR_SAVE_ID)
                            .backgroundColor(EStyles.ALERT_SUCCESS_COLOR)
                            .mediumTimer()
                            .title(Text.translatable("fzmm.gui.encryptbook.getDecryptor.snack_bar.success"))
                            .startTimer()
                            .build()
                    );
                });
    }
}
