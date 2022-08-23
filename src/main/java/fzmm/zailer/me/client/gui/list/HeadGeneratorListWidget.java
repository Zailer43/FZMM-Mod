package fzmm.zailer.me.client.gui.list;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import fzmm.zailer.me.client.gui.HeadGeneratorScreen;
import fzmm.zailer.me.client.logic.HeadGenerator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class HeadGeneratorListWidget extends ElementListWidget<HeadGeneratorListEntry> {
    private final HeadGeneratorScreen parent;
    private final List<HeadGeneratorListEntry> headTextures = Lists.newArrayList();

    public HeadGeneratorListWidget(HeadGeneratorScreen parent, MinecraftClient client, int width, int height, int top, int bottom, int itemHeight) {
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

        for (var headName : this.parent.getHeadNames()) {
            BufferedImage customHeadTexture = HeadGenerator.getTexture(headName);
            BufferedImage headTexture = new HeadGenerator(skinBase).addTexture(customHeadTexture).getHeadTexture();
            if (headTexture != null)
                this.headTextures.add(new HeadGeneratorListEntry(this, this.client, headName, headTexture));
        }

        this.headTextures.sort((player1, player2) -> player1.getName().compareToIgnoreCase(player2.getName()));
        this.replaceEntries(this.headTextures);
        this.setScrollAmount(this.getScrollAmount());
    }

    public void filter(String search) {
        List<HeadGeneratorListEntry> entries = new ArrayList<>(this.headTextures);

        this.filterHeads(entries, search);
        entries.sort((player1, player2) -> player1.getName().compareToIgnoreCase(player2.getName()));

        this.replaceEntries(entries);
        this.setScrollAmount(this.getScrollAmount());
    }

    public boolean isEmpty() {
        return this.headTextures.isEmpty();
    }

    private void filterHeads(List<HeadGeneratorListEntry> entries, String search) {
        if (!search.isEmpty()) {
            entries.removeIf((texture) -> !texture.getName().toLowerCase().contains(search));
            this.replaceEntries(entries);
        }
    }

    public String getPlayerName() {
        return this.parent.getPlayerName();
    }

    public void setDelay() {
        for (int i = 5; i != 0; i--) {
            this.updateList(false, "Wait... " + i);
            LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(1));
        }

        this.updateList(true, "Give");
    }

    public void updateList(boolean enabled, String buttonName) {
        Text message = Text.of(buttonName);
        for (var entry : this.headTextures) {
            entry.setEnabled(enabled);
            entry.setButtonName(message);
        }
    }
}