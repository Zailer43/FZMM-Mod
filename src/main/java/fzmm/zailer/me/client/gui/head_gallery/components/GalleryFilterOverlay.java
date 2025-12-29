package fzmm.zailer.me.client.gui.head_gallery.components;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.extend.component.ELabelComponent;
import fzmm.zailer.me.client.gui.utils.text_filter.AbstractTextFilter;
import fzmm.zailer.me.client.gui.utils.text_filter.FilterOverlay;
import fzmm.zailer.me.client.logic.minecraft_heads.model.MchTier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GalleryFilterOverlay extends FilterOverlay {
    protected final Map<AbstractTextFilter<?, ?>, Integer> filtersMap = new LinkedHashMap<>();

    public GalleryFilterOverlay(String baseTranslationKey, Consumer<String> callback) {
        super(baseTranslationKey, callback);
    }

    public void filters(Map<? extends AbstractTextFilter<?, ?>, Integer> filters, String searchText) {
        this.filtersMap.clear();
        this.filtersMap.putAll(filters);
        super.filters(filters.keySet(), searchText);
    }

    @Override
    protected boolean isActive(AbstractTextFilter<?, ?> filter) {
        return FzmmClient.MCH_RESOURCES.licenseDetected().hasPermission(this.filtersMap.get(filter));
    }

    @Override
    protected void filterText(AbstractTextFilter<?, ?> filter, ELabelComponent label, boolean active) {
        super.filterText(filter, label, active);
        if (active) return;

        int permission = this.filtersMap.get(filter);
        MchTier requiredLicense = MchTier.minTierRequired(permission);

        label.text(Component.translatable("fzmm.gui.headGallery.tier.missingPermission", label.text(), requiredLicense.message()));
        Component text = Component.translatable("fzmm.gui.headGallery.tier.missingPermission.details", MchTier.minTierRequired(MchTier.TAG_GENERAL_REQUEST).message());
        List<FormattedCharSequence> textList = Minecraft.getInstance().font.split(text, 250);
        label.tooltip(textList.stream().map(ClientTooltipComponent::create).collect(Collectors.toList()));
    }
}
