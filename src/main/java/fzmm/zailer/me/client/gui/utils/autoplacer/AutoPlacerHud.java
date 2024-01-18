package fzmm.zailer.me.client.gui.utils.autoplacer;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.playerstatue.PlayerStatuePlacerScreen;
import io.wispforest.owo.ui.base.BaseComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.hud.Hud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class AutoPlacerHud {
    public static boolean isHudActive = false;
    private static final Identifier HUD_IDENTIFIER = new Identifier(FzmmClient.MOD_ID, "auto_placer_hud");
    private static final List<Activation> activationList = new ArrayList<>();

    private static void addHud(Screen screen, List<Requirement> requirements) {
        if (isHudActive)
            return;

        List<Requirement> allRequirements = getRequirements(requirements);

        // Activate the HUD, as due to errors in the owo-lib,
        // it is added or removed incorrectly if the HUD is hidden.
        // If the HUD is activated immediately after the change, it has no effect,
        // as owo-lib makes the HUD change when an event is called.
        MinecraftClient.getInstance().options.hudHidden = false;
        Hud.add(HUD_IDENTIFIER, () -> {
            isHudActive = true;
            FlowLayout mainLayout = Containers.verticalFlow(Sizing.fill(70), Sizing.fill(70));

            LabelComponent titleLabel = Components.label(Text.translatable("fzmm.gui.autoPlacer.title"));
            titleLabel.positioning(Positioning.relative(50, 0));

            LabelComponent requirementLabel = Components.label(Text.translatable("fzmm.gui.autoPlacer.label.requirement"));
            LabelComponent currentRequirementLabel = Components.label(Text.empty());
            FlowLayout requirementLayout = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
            requirementLayout.child(requirementLabel);
            requirementLayout.child(currentRequirementLabel);
            requirementLayout.gap(4);
            requirementLayout.horizontalAlignment(HorizontalAlignment.CENTER);

            LabelComponent sneakLabel = Components.label(Text.translatable("fzmm.gui.autoPlacer.label.sneakInfo"));
            LabelComponent cancelLabel = Components.label(Text.translatable("fzmm.gui.autoPlacer.label.cancel", FzmmClient.OPEN_MAIN_GUI_KEYBINDING.getBoundKeyLocalizedText()));

            FlowLayout bottomTextLayout = Containers.verticalFlow(Sizing.fill(100), Sizing.content());
            bottomTextLayout.positioning(Positioning.relative(0, 100));
            bottomTextLayout.gap(4);
            bottomTextLayout.child(cancelLabel);
            bottomTextLayout.child(sneakLabel);
            Component eventComponent = new BaseComponent() {

                @Override
                public void draw(OwoUIDrawContext context, int mouseX, int mouseY, float partialTicks, float delta) {
                    for (var requirement : allRequirements) {
                        if (!requirement.predicate.get()) {
                            currentRequirementLabel.text(requirement.text);
                            return;
                        }
                    }

                    isHudActive = false;
                    MinecraftClient.getInstance().setScreen(screen);

                    // Activate the HUD, as due to errors in the owo-lib,
                    // it is added or removed incorrectly if the HUD is hidden.
                    // If the HUD is activated immediately after the change, it has no effect,
                    // as owo-lib makes the HUD change when an event is called.
                    MinecraftClient.getInstance().options.hudHidden = false;
                    Hud.remove(HUD_IDENTIFIER);
                }
            };


            eventComponent.sizing(Sizing.fixed(1), Sizing.fixed(1));
            mainLayout.child(eventComponent)
                    .child(titleLabel)
                    .child(requirementLayout)
                    .child(bottomTextLayout)
                    .padding(Insets.of(16))
                    .surface(Surface.VANILLA_TRANSLUCENT)
                    .alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                    .positioning(Positioning.relative(50, 50));

            return mainLayout;
        });
    }

    private static List<Requirement> getRequirements(List<Requirement> requirements) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;

        List<Requirement> allRequirements = new ArrayList<>(requirements);

        allRequirements.add(new Requirement(() -> client.crosshairTarget != null && client.crosshairTarget.getType() == BlockHitResult.Type.BLOCK,
                Text.translatable("fzmm.gui.autoPlacer.label.baseRequirement.noBlock")));

        allRequirements.add(new Requirement(() -> client.player.isOnGround(),
                Text.translatable("fzmm.gui.autoPlacer.label.baseRequirement.isNotInGround")));
        return allRequirements;
    }

    public static boolean check(ItemStack stack) {
        for (var activation : activationList) {
            if (activation.predicate.test(stack)) {
                addHud(activation.screenGetter.apply(stack), activation.requirements);
                return true;
            }
        }
        return false;
    }
    
    public static void addActivation(Activation activateAutoPlacerRequirement) {
        activationList.add(activateAutoPlacerRequirement);
    }
    
    public static void init() {
        addActivation(PlayerStatuePlacerScreen.getActivation());
    }

    public static void removeHud() {
        isHudActive = false;
        // Activate the HUD, as due to errors in the owo-lib,
        // it is added or removed incorrectly if the HUD is hidden.
        // If the HUD is activated immediately after the change, it has no effect,
        // as owo-lib makes the HUD change when an event is called.
        MinecraftClient.getInstance().options.hudHidden = false;
        Hud.remove(HUD_IDENTIFIER);
    }

    public record Requirement(Supplier<Boolean> predicate, Text text) {

    }
    
    public record Activation(Predicate<ItemStack> predicate, Function<ItemStack, Screen> screenGetter, List<Requirement> requirements) {
    }
}
