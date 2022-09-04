package fzmm.zailer.me.client.gui.headgenerator;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import fzmm.zailer.me.client.logic.HeadGenerator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class HeadListWidget extends ElementListWidget<HeadEntry> {
    private final HeadGeneratorScreen parent;
    private final List<HeadEntry> headTextures = Lists.newArrayList();

    public HeadListWidget(HeadGeneratorScreen parent, MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
        super(client, width, height, top, bottom, itemHeight);
        this.parent = parent;
        this.setRenderBackground(false);
        this.setRenderHorizontalShadows(false);
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        double d = this.client.getWindow().getScaleFactor();
        RenderSystem.enableScissor((int) ((double) this.getRowLeft() * d), (int) ((double) (this.height - this.bottom) * d), (int) ((double) (this.getScrollbarPositionX() + 6) * d), (int) ((double) (this.height - (this.height - this.bottom) - this.top - 4) * d));
        super.render(matrices, mouseX, mouseY, delta);
        RenderSystem.disableScissor();
    }

    public void updatePreview(BufferedImage skinBase) {
        this.headTextures.clear();
        if (skinBase == null)
            return;

        for (var headName : this.parent.getHeadNames()) {
            BufferedImage customHeadTexture = HeadGenerator.getTexture(headName);
            BufferedImage headTexture = new HeadGenerator(skinBase).addTexture(customHeadTexture).getHeadTexture();
            if (headTexture != null)
                this.headTextures.add(new HeadEntry(this, this.client, headName, headTexture, customHeadTexture));
        }

        this.headTextures.sort((player1, player2) -> player1.getName().compareToIgnoreCase(player2.getName()));
        this.replaceEntries(this.headTextures);
        this.setScrollAmount(this.getScrollAmount());
    }

    public void filter(String search) {
        List<HeadEntry> entries = new ArrayList<>(this.headTextures);

        this.filterHeads(entries, search);
        entries.sort((player1, player2) -> player1.getName().compareToIgnoreCase(player2.getName()));

        this.replaceEntries(entries);
        this.setScrollAmount(this.getScrollAmount());
    }

    public boolean isEmpty() {
        return this.headTextures.isEmpty();
    }

    private void filterHeads(List<HeadEntry> entries, String search) {
        if (!search.isEmpty()) {
            entries.removeIf((texture) -> !texture.getName().toLowerCase().contains(search));
            this.replaceEntries(entries);
        }
    }

    public List<ButtonWidget> getGiveButtons() {
        return this.headTextures.stream().map(HeadEntry::getGiveButton).toList();
    }

    public int size() {
        return this.children().size();
    }

    public void addLayer(HeadEntry headEntry) {
        this.parent.addLayer(headEntry);
    }

    @Override
    protected int getScrollbarPositionX() {
        return this.getRowLeft() + this.getRowWidth();
    }

    public void execute(BufferedImage image) {
        this.parent.execute(image);
    }
}