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
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@Mixin(SocialInteractionsPlayerListEntry.class)
public abstract class SocialInteractionsPlayerListEntryMixin {
    @Unique
    private static final Text GIVE_HEAD_TEXT = Text.translatable("fzmm.gui.button.giveHead");

    @Mutable
    @Shadow
    @Final
    private List<ClickableWidget> buttons;
    @Shadow
    @Final
    private MinecraftClient client;
    @Shadow private @Nullable ButtonWidget hideButton;
    @Unique
    private ButtonWidget fzmm$giveHeadButton;
    @Unique
    private ItemStack fzmm$headStack;
    @Unique
    private Icon fzmm$icon;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void fzmm$addGiveHeadButton(MinecraftClient client, SocialInteractionsScreen parent, UUID uuid, String name, Supplier<Identifier> skinTexture, boolean reportable, CallbackInfo ci) {
        assert this.client.player != null;
        PlayerListEntry playerListEntry = this.client.player.networkHandler.getPlayerListEntry(uuid);
        if (playerListEntry == null)
            return;
        this.fzmm$headStack = HeadBuilder.of(playerListEntry.getProfile());
        this.fzmm$icon = Icon.of(this.fzmm$headStack);

        this.fzmm$giveHeadButton = ButtonWidget.builder(Text.literal(""), button -> FzmmUtils.giveItem(this.fzmm$headStack))
                .dimensions(0, 0, 20, 20)
                .tooltip(Tooltip.of(GIVE_HEAD_TEXT))
                .build();

        if (this.buttons instanceof ImmutableList<ClickableWidget>)
            this.buttons = new ArrayList<>(this.buttons);

        this.buttons.add(this.fzmm$giveHeadButton);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void fzmm$render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci) {
        if (this.fzmm$giveHeadButton == null)
            return;

        this.fzmm$giveHeadButton.setX(x + (entryWidth - this.fzmm$giveHeadButton.getWidth() - 4) + (this.hideButton == null ? 0 : -48));
        this.fzmm$giveHeadButton.setY(y + (entryHeight - this.fzmm$giveHeadButton.getHeight()) / 2);
        this.fzmm$giveHeadButton.render(context, mouseX, mouseY, tickDelta);
        this.fzmm$icon.render(context, this.fzmm$giveHeadButton.getX() + 2, this.fzmm$giveHeadButton.getY() + 1, mouseX, mouseY, tickDelta);
    }
}
