package fzmm.zailer.me.client.gui.headgenerator;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import fzmm.zailer.me.client.logic.HeadGenerator;
import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.util.List;

public class HeadLayersListWidget extends ElementListWidget<HeadLayerEntry> {
    private HeadLayerEntry baseSkin;
    private final List<HeadLayerEntry> layers = Lists.newArrayList();
    private Identifier mergedHeadIdentifier;
    private BufferedImage mergedHeadImage;

    public HeadLayersListWidget(MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
        super(client, width, height, top, bottom, itemHeight);
        this.setRenderBackground(false);
        this.setRenderHorizontalShadows(false);
        this.mergedHeadIdentifier = null;
        this.mergedHeadImage = null;
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        double d = this.client.getWindow().getScaleFactor();
        RenderSystem.enableScissor((int) ((double) this.getRowLeft() * d), (int) ((double) (this.height - this.bottom) * d), (int) ((double) (this.getScrollbarPositionX() + 6) * d), (int) ((double) (this.height - (this.height - this.bottom) - this.top - 4) * d));
        super.render(matrices, mouseX, mouseY, delta);
        RenderSystem.disableScissor();
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

    @Override
    protected int getScrollbarPositionX() {
        return this.getRowLeft() + this.getRowWidth();
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
}