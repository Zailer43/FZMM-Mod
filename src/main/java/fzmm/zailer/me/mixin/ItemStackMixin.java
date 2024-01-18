package fzmm.zailer.me.mixin;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow
    public abstract ItemStack copy();

    @Inject(method = "getTooltip", at = @At("RETURN"))
    public void fzmm$getTooltip(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir) {
        if (!FzmmClient.CONFIG.general.showItemSize())
            return;

        List<Text> tooltipList = cir.getReturnValue();
        for (int i = tooltipList.size() - 1; i > 0; i--) {
            Text tooltipLine = tooltipList.get(i);
            if (tooltipLine.getContent() instanceof TranslatableTextContent translatableTooltipText && translatableTooltipText.getKey().equals("item.nbt_tags")) {
                tooltipList.add( i + 1, this.fzmm$getSizeMessage());
                return;
            }
        }
    }

    @Unique
    public Text fzmm$getSizeMessage() {
        ItemStack stack = this.copy();

        long itemSizeInBytes = FzmmUtils.getLengthInBytes(stack);

        return (itemSizeInBytes > 1023 ?
                Text.translatable("fzmm.item.tooltip.size.kilobytes", FzmmUtils.getLengthInKB(itemSizeInBytes)) :
                Text.translatable("fzmm.item.tooltip.size.bytes", itemSizeInBytes)
        ).setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY));
    }
}
