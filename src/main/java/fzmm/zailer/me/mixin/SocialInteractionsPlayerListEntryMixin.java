package fzmm.zailer.me.mixin;

import com.google.common.collect.ImmutableList;
import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.itemgroup.Icon;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListEntry;
import net.minecraft.client.gui.screen.multiplayer.SocialInteractionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@Mixin(SocialInteractionsPlayerListEntry.class)
public abstract class SocialInteractionsPlayerListEntryMixin {
    private static final Text GIVE_HEAD_TEXT = Text.translatable("fzmm.gui.button.giveHead");

    @Mutable
    @Shadow
    @Final
    private List<ClickableWidget> buttons;
    @Shadow
    @Final
    private MinecraftClient client;
    @Shadow private @Nullable ButtonWidget hideButton;
    private ButtonWidget giveHeadButton;
    private ItemStack headStack;
    private Icon icon;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void addGiveHeadButton(MinecraftClient client, SocialInteractionsScreen parent, UUID uuid, String name, Supplier<Identifier> skinTexture, boolean reportable, CallbackInfo ci) {
        assert this.client.player != null;
        PlayerListEntry playerListEntry = this.client.player.networkHandler.getPlayerListEntry(uuid);
        if (playerListEntry == null)
            return;
        this.headStack = HeadBuilder.of(playerListEntry.getProfile());
        this.icon = Icon.of(this.headStack);

        this.giveHeadButton = ButtonWidget.builder(Text.literal(""), button -> FzmmUtils.giveItem(this.headStack))
                .dimensions(0, 0, 20, 20)
                .tooltip(Tooltip.of(GIVE_HEAD_TEXT))
                .build();

        if (this.buttons instanceof ImmutableList<ClickableWidget>)
            this.buttons = new ArrayList<>(this.buttons);

        this.buttons.add(this.giveHeadButton);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        if (this.giveHeadButton == null)
            return;

        this.giveHeadButton.setX(x + (entryWidth - this.giveHeadButton.getWidth() - 4) + (this.hideButton == null ? 0 : -48));
        this.giveHeadButton.setY(y + (entryHeight - this.giveHeadButton.getHeight()) / 2);
        this.giveHeadButton.render(context, mouseX, mouseY, tickDelta);
        this.icon.render(context, this.giveHeadButton.getX() + 2, this.giveHeadButton.getY() + 1, mouseX, mouseY, tickDelta);
    }
}
