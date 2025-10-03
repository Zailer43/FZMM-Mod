package fzmm.zailer.me.client.gui.imagetext.tabs;

import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.ContextMenuButton;
import fzmm.zailer.me.client.gui.components.extend.EStyles;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.imagetext.algorithms.IImagetextAlgorithm;
import fzmm.zailer.me.client.gui.options.LoreOption;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import fzmm.zailer.me.utils.ItemUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import java.util.List;

public class ImagetextLoreTab implements IImagetextTab, IImagetextTooltip {
    private ContextMenuButton loreModeButton;
    private LoreOption loreMode;

    @Override
    public void build(IImagetextAlgorithm algorithm, ImagetextLogic logic, ImagetextData data, boolean isExecute) {
        logic.buildImagetext(algorithm, data);
    }

    @Override
    public void execute(ImagetextLogic logic) {
        ItemStack stack = this.getStack(this.loreMode);
        List<Text> imagetext = logic.text();

        DisplayBuilder display = DisplayBuilder.of(stack);
        display.addLore(imagetext).get();

        ItemUtils.give(display.get());
    }

    @Override
    public String getId() {
        return "lore";
    }

    @Override
    public void setupComponents(EFlowLayout rootComponent) {
        this.loreModeButton = rootComponent.childByIdOrThrow(ContextMenuButton.class, "loreMode");
        this.loreModeButton.setContextMenuOptions(dropdownComponent -> {
            for (var option : LoreOption.values()) {
                dropdownComponent.button(Text.translatable(option.getTranslationKey()), dropdownButton -> {
                    this.updateLoreMode(option);
                    dropdownButton.remove();
                });
            }
        });
        this.updateLoreMode(LoreOption.ADD);
    }

    private void updateLoreMode(LoreOption mode) {
        this.loreMode = mode;
        this.loreModeButton.setMessage(Text.translatable(this.loreMode.getTranslationKey()));
    }


    @Override
    public Text getTooltip(ImagetextLogic logic) {
        ItemStack stack = this.getStack(this.loreMode);
        int loreSize = stack.getComponents()
                .getOrDefault(DataComponentTypes.LORE, LoreComponent.DEFAULT).lines().size() + logic.height();

        MutableText currentLore = Text.literal(String.valueOf(loreSize));
        if (loreSize > LoreComponent.MAX_LORES) {
           currentLore.setStyle(currentLore.getStyle().withColor(EStyles.TEXT_ERROR_COLOR.rgb()));
        }

        return Text.translatable("fzmm.gui.imagetext.tab.lore.tooltip", currentLore, LoreComponent.MAX_LORES);
    }

    private ItemStack getStack(LoreOption option) {
        assert MinecraftClient.getInstance().player != null;
        ItemStack stack = ItemUtils.from(Hand.MAIN_HAND);

        if (stack.isEmpty()) {
            stack = ItemUtils.from(FzmmClient.CONFIG.imagetext.defaultItem()).getDefaultStack();
        }

        return switch (option) {
            case ADD -> stack;
            case REPLACE -> {
                stack.apply(DataComponentTypes.LORE, null, component -> new LoreComponent(List.of()));
                yield stack;
            }
        };
    }

    @Override
    public IMementoObject createMemento() {
        return new LoreMementoTab(this.loreMode);
    }

    @Override
    public void restoreMemento(IMementoObject mementoTab) {
        LoreMementoTab memento = (LoreMementoTab) mementoTab;
        this.updateLoreMode(memento.mode);
    }

    private record LoreMementoTab(LoreOption mode) implements IMementoObject {
    }
}
