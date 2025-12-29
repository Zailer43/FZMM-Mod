package fzmm.zailer.me.client.gui.head_gallery.controller;

import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.client.gui.components.extend.component.EItemComponent;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.logic.minecraft_heads.model.MchHead;
import fzmm.zailer.me.config.FzmmConfig;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.List;

public abstract class AbstractGalleryContent {
    protected List<MchHead> entries = List.of();
    protected double itemScale;
    protected Display display;

    protected AbstractGalleryContent() {
        FzmmConfig config = FzmmClient.CONFIG;
        var displayConfig = config.headGallery.display;
        this.display = new Display(
                displayConfig.enabled(),
                displayConfig.tags(),
                displayConfig.publishedAt(),
                displayConfig.id(),
                config.colors.headGalleryName().rgb(),
                config.colors.headGalleryLore().rgb()
        );
        this.itemScale = config.headGallery.itemScale();
    }

    public abstract void configureComponent(EFlowLayout rootComponent);

    public ItemStack toStack(MchHead head) {
        return toStack(head, this.display);
    }

    public static ItemStack toStack(MchHead head, Display display) {
        ItemStack stack = head.toStack();
        if (!display.enabled) return stack;

        String translationHey = "fzmm.item.headGallery.heads.";
        DisplayBuilder builder = DisplayBuilder.of(stack);
        // use getString() to get the translation and make string visible to people without the mod
        builder.setName(Component.translatable(translationHey + "name", head.name()).getString(), display.nameColor);

        if (display.tags && head.tags().isPresent() && head.tags().get().length > 0) {
            builder.addLore(Component.translatable(translationHey + "tags.title").getString(), display.loreColor);
            for (var tag : head.tags().get()) {
                builder.addLore(Component.translatable(translationHey + "tags.tag", tag.name()).getString(), display.loreColor);
            }
        }

        if (display.publishedAt && head.publishedAt() != null) {
            String dateString = head.publishedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            builder.addLore(Component.translatable(translationHey + "publishedAt", dateString).getString(), display.loreColor);
        }

        if (display.id && head.id() != null) {
            builder.addLore(Component.translatable(translationHey + "id", head.id()).getString(), display.loreColor);
        }

        return builder.get();
    }

    public EItemComponent toComponent(MchHead head) {
        EItemComponent itemComponent = EComponents.itemGive(this.toStack(head));
        itemComponent.sizing(Sizing.fixed((int) (this.itemScale * 16.0d)));

        if (!this.display.enabled) {
            itemComponent.setTooltipFromStack(false);
            itemComponent.tooltip(Component.literal(head.name()));
        }

        return itemComponent;
    }

    public abstract void apply(List<MchHead> heads);

    public void itemScale(double value) {
        this.itemScale = value;
    }

    public void display(Display value) {
        this.display = value;
    }

    public record Display(boolean enabled, boolean tags, boolean publishedAt, boolean id, int nameColor, int loreColor) implements Serializable {
    }
}
