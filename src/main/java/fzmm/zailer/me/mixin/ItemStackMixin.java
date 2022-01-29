package fzmm.zailer.me.mixin;

import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow @Nullable public abstract NbtCompound getNbt();

    @Shadow public abstract boolean hasNbt();

    @Inject(method = "getTooltip", at = @At("RETURN"), cancellable = true)
    public void getTooltip(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir) {
        if (this.hasNbt() && context.isAdvanced()) {
            NbtCompound nbt = this.getNbt();
            assert nbt != null;
            List<String> tags = nbt.getKeys().stream().toList();
            int tagsSize = tags.size();
            MutableText loreText = new LiteralText("Tags: ");
            List<Text> list = cir.getReturnValue();


            list.remove(list.size() - 1);

            for (int i = 0; i != tagsSize; i++) {
                loreText.append(tags.get(i) + (i == tagsSize - 1 ? "" : ", "));
                if (i % 3 == 2) {
                    list.add(loreText.setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)));
                    loreText = new LiteralText("");
                }
            }

            if (!loreText.getString().isEmpty())
                list.add(loreText.setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)));

            MutableText lengthText = new LiteralText(FzmmUtils.getNbtLengthInKB(nbt));
            list.add(lengthText.setStyle(Style.EMPTY.withColor(Formatting.GRAY)));

            cir.setReturnValue(list);
        }
    }
}
