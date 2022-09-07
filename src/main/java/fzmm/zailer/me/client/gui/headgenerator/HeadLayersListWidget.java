package fzmm.zailer.me.client.gui.headgenerator;

import com.google.common.collect.Lists;
import fzmm.zailer.me.client.logic.HeadGenerator;
import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.util.List;

public class HeadLayersListWidget extends AbstractHeadList<HeadLayerEntry> {
    private HeadLayerEntry baseSkin;
    private final List<HeadLayerEntry> layers = Lists.newArrayList();
    private Identifier mergedHeadIdentifier;
    private BufferedImage mergedHeadImage;

    public HeadLayersListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
        super(client, width, height, top, bottom, itemHeight);
        this.mergedHeadIdentifier = null;
        this.mergedHeadImage = null;
    }

    public boolean isEmpty() {
        return this.layers.isEmpty();
    }

    public void add(HeadLayerEntry entry) {
        this.layers.add(entry);
        this.updateEntries();
        this.updateMergedHead();
    }

    public void setBaseSkin(BufferedImage baseSkin) {
        this.layers.clear();
        HeadLayerEntry entry = new HeadLayerEntry(this, this.client, "Base skin", baseSkin, baseSkin);
        entry.setEnabled(false);
        this.baseSkin = entry;
        this.updateEntries();
        this.updateMergedHead();
    }

    private void updateEntries() {
        List<HeadLayerEntry> entries = Lists.newArrayList();
        entries.add(this.baseSkin);
        entries.addAll(this.layers);
        this.replaceEntries(entries);
        this.setScrollAmount(this.getScrollAmount());
    }

    public void remove(HeadLayerEntry entry) {
        this.layers.remove(entry);
        this.updateEntries();
        this.updateMergedHead();
    }

    private List<BufferedImage> getHeadTextures() {
        return this.layers.stream().map(AbstractHeadListEntry::getHeadTexture).toList();
    }

    @Nullable
    public Identifier getMergedHeadIdentifier() {
        return this.mergedHeadIdentifier;
    }

    @Nullable
    public BufferedImage getMergedHeadImage() {
        return this.mergedHeadImage;
    }

    private void updateMergedHead() {
        BufferedImage mergedHead = new HeadGenerator(this.baseSkin.getHeadTexture())
                .merge(this.getHeadTextures())
                .getHeadTexture();
        this.mergedHeadImage = mergedHead;
        this.mergedHeadIdentifier = FzmmUtils.saveBufferedImageAsIdentifier(mergedHead);
    }

    public int getTop() {
        return this.top;
    }

    public int getBottom() {
        return this.bottom;
    }

    public BufferedImage getBaseSkin() {
        return this.baseSkin.getHeadTexture();
    }

}