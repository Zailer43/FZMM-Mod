package fzmm.zailer.me.client.gui.imagetext.tabs;

import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.ContextMenuButton;
import fzmm.zailer.me.client.gui.components.extend.EStyles;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.imagetext.algorithms.IImagetextAlgorithm;
import fzmm.zailer.me.client.gui.options.LoreOption;
import fzmm.zailer.me.client.logic.history.IMemento;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import fzmm.zailer.me.utils.ItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

public class ImagetextLoreTab implements IImagetextTab, IImagetextTooltip, IMemento {
    private ContextMenuButton loreModeButton;
    private LoreOption loreMode;

    @Override
    public void build(IImagetextAlgorithm algorithm, ImagetextLogic logic, ImagetextData data, boolean isExecute) {
        logic.buildImagetext(algorithm, data);
    }

    @Override
    public void execute(ImagetextLogic logic) {
        ItemStack stack = this.getStack(this.loreMode);
        List<Component> imagetext = logic.text();

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
                dropdownComponent.button(Component.translatable(option.getTranslationKey()), dropdownButton -> {
                    this.updateLoreMode(option);
                    dropdownButton.remove();
                });
            }
        });
        this.updateLoreMode(LoreOption.ADD);
    }

    private void updateLoreMode(LoreOption mode) {
        this.loreMode = mode;
        this.loreModeButton.setMessage(Component.translatable(this.loreMode.getTranslationKey()));
    }


    @Override
    public Component getTooltip(ImagetextLogic logic) {
        ItemStack stack = this.getStack(this.loreMode);
        int loreSize = stack.getComponents()
                .getOrDefault(DataComponents.LORE, ItemLore.EMPTY).lines().size() + logic.height();

        MutableComponent currentLore = Component.literal(String.valueOf(loreSize));
        if (loreSize > ItemLore.MAX_LINES) {
           currentLore.setStyle(currentLore.getStyle().withColor(EStyles.TEXT_ERROR_COLOR.rgb()));
        }

        return Component.translatable("fzmm.gui.imagetext.tab.lore.tooltip", currentLore, ItemLore.MAX_LINES);
    }

    private ItemStack getStack(LoreOption option) {
        assert Minecraft.getInstance().player != null;
        ItemStack stack = ItemUtils.from(InteractionHand.MAIN_HAND);

        if (stack.isEmpty()) {
            stack = ItemUtils.from(FzmmClient.CONFIG.imagetext.defaultItem()).getDefaultInstance();
        }

        return switch (option) {
            case ADD -> stack;
            case REPLACE -> {
                stack.update(DataComponents.LORE, null, component -> new ItemLore(List.of()));
                yield stack;
            }
        };
    }

    @Override
    public void backup(ObjectOutputStream output) throws IOException {
        output.writeObject(this.loreMode);
    }

    @Override
    public void restore(ObjectInputStream input) throws IOException, ClassNotFoundException {
        this.loreMode = (LoreOption) input.readObject();
        this.updateLoreMode(this.loreMode);
    }
}
