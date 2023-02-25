package fzmm.zailer.me.client.gui.headgallery.components;

import fzmm.zailer.me.client.gui.components.GiveItemComponent;
import net.minecraft.item.ItemStack;

import java.util.Set;

public class HeadGalleryItemComponent extends GiveItemComponent {

    private final String name;
    private final Set<String> tags;

    public HeadGalleryItemComponent(ItemStack stack, String name, Set<String> tags) {
        super(stack);
        this.name = name;
        this.tags = tags;
    }

    public boolean filter(Set<String> tags, String name) {
        boolean hasTag = tags.isEmpty() || this.tags.containsAll(tags);

        return hasTag && this.name.contains(name);
    }
}
