package fzmm.zailer.me.client.gui.components.image.source;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.utils.select_item.RequestedItem;
import fzmm.zailer.me.client.gui.utils.select_item.SelectItemScreen;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.HeadUtils;
import fzmm.zailer.me.utils.skin.CacheSkinGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class PlayerHeadSource implements IInteractiveImageLoader {
    @Nullable
    private BufferedImage image;
    private Consumer<BufferedImage> consumer;
    private BaseFzmmScreen previousScreen;

    public PlayerHeadSource() {
        this.image = null;
    }

    @Override
    public void execute(Consumer<BufferedImage> consumer) {
        if (this.image != null) {
            this.image.flush();
        }
        this.image = null;
        this.consumer = consumer;
        Minecraft client = Minecraft.getInstance();

        this.previousScreen = client.screen instanceof BaseFzmmScreen baseScreen ? baseScreen : null;
        RequestedItem requestedItem = new RequestedItem(
                itemStack -> itemStack.getItem() == Items.PLAYER_HEAD,
                this::setImage,
                List.of(Items.PLAYER_HEAD.getDefaultInstance()),
                Items.PLAYER_HEAD.getName(),
                false
        );
        FzmmUtils.setScreen(new SelectItemScreen(this.previousScreen, requestedItem));
    }

    @Override
    public Optional<BufferedImage> getImage() {
        return Optional.ofNullable(this.image);
    }

    @Override
    public boolean hasTextField() {
        return false;
    }

    private void setImage(@Nullable ItemStack head) {
        if (head == null) {
            this.setImage((BufferedImage) null);
            return;
        }

        HeadUtils.getSkinTextures(head).whenComplete((skinOptional, throwable) -> Minecraft.getInstance().execute(() -> {
            if (throwable != null || skinOptional.isEmpty()) {
                this.setImage((BufferedImage) null);
            } else {
                this.setImage(new CacheSkinGetter().getSkin(skinOptional.get()).orElse(null));
            }
        }));
    }

    public void setImage(BufferedImage image) {
        if (this.image != null) {
            this.image.flush();
        }
        FzmmUtils.setScreen(this.previousScreen);
        this.previousScreen = null;

        this.image = image;
        this.consumer.accept(this.image);
    }

}
