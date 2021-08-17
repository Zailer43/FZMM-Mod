package fzmm.zailer.me.mixin;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow @Nullable public abstract NbtCompound getTag();

    @Inject(method = "getTooltip", at = @At("RETURN"), cancellable = true)
    public void getTooltip(PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir) {
        if (this.getTag() != null && context.isAdvanced()) {

            List<String> stringList = new ArrayList<>();
            final String[] string = {Formatting.DARK_GRAY + "Tag's: "};
            Set<String> tags = this.getTag().getKeys();
            AtomicInteger i = new AtomicInteger(0);
            int tagsSize = tags.size();

            tags.forEach(element -> {
                string[0] += element;
                i.getAndIncrement();
                if (i.get() != tagsSize) string[0] += ", ";
                if (i.get() % 3 == 0 && i.get() != tagsSize) {
                    stringList.add(string[0]);
                    string[0] = Formatting.DARK_GRAY + "";
                } else if (i.get() == tagsSize) {
                    stringList.add(string[0]);
                }

            });

            List<Text> list = cir.getReturnValue();
            for (int a = 0; a != stringList.size(); a++)
                list.add(new LiteralText(stringList.get(a)));

            cir.setReturnValue(list);
        }
    }
}
