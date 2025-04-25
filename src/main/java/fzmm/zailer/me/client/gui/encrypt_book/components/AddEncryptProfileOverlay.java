package fzmm.zailer.me.client.gui.encrypt_book.components;

import fzmm.zailer.me.client.gui.components.extend.component.EBooleanButton;
import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.client.gui.components.extend.EContainers;
import fzmm.zailer.me.client.gui.components.extend.EStyles;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.encrypt_book.EncryptBookScreen;
import fzmm.zailer.me.client.logic.enycrpt_book.TranslationEncryptProfile;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.OverlayContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class AddEncryptProfileOverlay extends OverlayContainer<EFlowLayout> {
    private static final int WIDTH = 350;

    public AddEncryptProfileOverlay(Consumer<TranslationEncryptProfile> onAdd) {
        super(EContainers.verticalFlow(Sizing.fixed(WIDTH), Sizing.content()));

        this.addComponents(onAdd);
        this.child.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        this.child.gap(12);
        this.child.padding(Insets.of(6));
        this.child.surface(this.child.styledPanel());
        this.zIndex(300);
    }

    @SuppressWarnings("UnstableApiUsage")
    protected void addComponents(Consumer<TranslationEncryptProfile> onAdd) {
        //title
        LabelComponent label = EComponents.label(Text.translatable("fzmm.gui.encryptbook.addProfile.title"));
        label.horizontalSizing(Sizing.expand(100));

        // options
        FlowLayout optionsLayout = EContainers.verticalFlow(Sizing.expand(100), Sizing.content());

        ConfigTextBox seedComponent = new ConfigTextBox();
        ConfigTextBox keyComponent = new ConfigTextBox();
        ConfigTextBox lengthComponent = new ConfigTextBox();
        ConfigTextBox asymmetricComponent = new ConfigTextBox();
        EBooleanButton oldAlgorithmComponent = new EBooleanButton();

        LabelComponent duplicatedKeyLabel = EComponents.label(Text.empty());
        Text duplicateKeyText = Text.translatable("fzmm.gui.encryptbook.addProfile.key.duplicated");

        ButtonComponent randomAsymmetric = Components.button(Text.translatable("fzmm.gui.button.random"), buttonComponent -> {
            int value = new Random(Util.getEpochTimeMs()).nextInt();
            asymmetricComponent.text(String.valueOf(value));
        });

        // configure options
        List<TranslationEncryptProfile> profiles = EncryptBookScreen.getProfiles();
        String defaultKey = "secret_mc_%s";
        int defaultSeed = 0;

        for (int i = 0; i != profiles.size() + 1; i++) {
            if (!this.isDuplicatedKey(profiles, i, defaultKey)) {
                defaultSeed = i;
                break;
            }
        }
        seedComponent.configureForNumber(Integer.class);
        seedComponent.text(String.valueOf(defaultSeed));
        seedComponent.onChanged().subscribe(value -> {
            if (this.isDuplicatedKey(profiles, (int) seedComponent.parsedValue(), keyComponent.getText())) {
                duplicatedKeyLabel.text(duplicateKeyText);
                keyComponent.setEditableColor(keyComponent.invalidColor());
            } else {
                duplicatedKeyLabel.text(Text.empty());
                keyComponent.setEditableColor(keyComponent.validColor());
            }
        });

        keyComponent.applyPredicate(key -> {
            int seed = (int) seedComponent.parsedValue();
            boolean isDuplicated = this.isDuplicatedKey(profiles, seed, key);

            duplicatedKeyLabel.text(isDuplicated ? duplicateKeyText : Text.empty());

            return !isDuplicated;
        });
        keyComponent.text(defaultKey);

        lengthComponent.configureForNumber(Integer.class);
        lengthComponent.applyPredicate(s -> {
            try {
                var value = Double.parseDouble(s);
                return value >= 1 && value <= TranslationEncryptProfile.MAX_LENGTH;
            } catch (NumberFormatException nfe) {
                return false;
            }
        });
        lengthComponent.text("255");

        asymmetricComponent.configureForNumber(Integer.class);
        asymmetricComponent.text("0");

        oldAlgorithmComponent.enabled(false);

        // add options
        String baseKey = "fzmm.gui.encryptbook.addProfile.";
        optionsLayout.child(this.getRow(baseKey + "seed", seedComponent));
        optionsLayout.child(this.getRow(baseKey + "key", keyComponent, duplicatedKeyLabel));
        optionsLayout.child(this.getRow(baseKey + "length", lengthComponent));
        optionsLayout.child(this.getRow(baseKey + "asymmetric", asymmetricComponent, randomAsymmetric));
        optionsLayout.child(this.getRow(baseKey + "oldAlgorithm", oldAlgorithmComponent));

        // bottom buttons
        FlowLayout buttonLayout = EContainers.horizontalFlow(Sizing.expand(100), Sizing.fixed(20));

        buttonLayout.child(Components.button(Text.translatable("fzmm.gui.encryptbook.addProfile.done"), buttonComponent -> {
                    TranslationEncryptProfile profile = new TranslationEncryptProfile(
                            (int) seedComponent.parsedValue(),
                            (int) lengthComponent.parsedValue(),
                            keyComponent.getText(),
                            (int) asymmetricComponent.parsedValue(),
                            oldAlgorithmComponent.enabled() ? 1 : TranslationEncryptProfile.ALGORITHM_VERSION
                    );
                    onAdd.accept(profile);
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

    private Component getRow(String translationKey, Component... components) {
        components[0].horizontalSizing(Sizing.fixed(100));
        List<Component> componentList = new ArrayList<>();
        componentList.add(EComponents.label(Text.translatable(translationKey))
                .horizontalSizing(Sizing.fixed(100))
                .tooltip(Text.translatable(translationKey + ".tooltip"))
        );
        componentList.addAll(List.of(components));

        return EContainers.horizontalFlow(Sizing.content(), Sizing.content())
                .hoveredSurface(EStyles.DEFAULT_HOVERED)
                .children(componentList)
                .gap(4)
                .verticalAlignment(VerticalAlignment.CENTER)
                .horizontalSizing(Sizing.expand(100));
    }

    private boolean isDuplicatedKey(List<TranslationEncryptProfile> profiles, int seed, String key) {
        String translationKey = TranslationEncryptProfile.translationKey(key, seed);
        return profiles.stream().anyMatch(profile -> profile.translationKey().equals(translationKey));
    }
}
