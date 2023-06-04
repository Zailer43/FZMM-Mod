package fzmm.zailer.me.client.gui.components.image.source;

import fzmm.zailer.me.client.gui.utils.selectItem.RequestedItem;
import fzmm.zailer.me.client.gui.utils.selectItem.SelectItemScreen;
import fzmm.zailer.me.utils.HeadUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class PlayerHeadSource implements IInteractiveImageLoader {
    private BufferedImage image;
    private Consumer<BufferedImage> consumer;
    private Screen previousScreen;

    public PlayerHeadSource() {
        this.image = null;
    }

    @Override
    public void execute(Consumer<BufferedImage> consumer) {
        this.image = null;
        this.consumer = consumer;
        MinecraftClient client = MinecraftClient.getInstance();

        this.previousScreen = client.currentScreen;
        RequestedItem requestedItem = new RequestedItem(
                itemStack -> itemStack.getItem() == Items.PLAYER_HEAD,
                this::setImage,
                List.of(Items.PLAYER_HEAD.getDefaultStack()),
                Items.PLAYER_HEAD.getName(),
                true
        );
        client.setScreen(new SelectItemScreen(this.previousScreen, requestedItem));
    }

    @Override
    public Optional<BufferedImage> getImage() {
        return Optional.ofNullable(this.image);
    }

    @Override
    public boolean hasTextField() {
        return false;
    }

    private void setImage(ItemStack head) {
        Optional<BufferedImage> skinOptional;

        try {
            skinOptional = HeadUtils.getSkin(head);
        } catch (IOException e) {
            skinOptional = Optional.empty();
        }

        skinOptional.ifPresent(this::setImage);
    }

    @Override
    public void setImage(BufferedImage image) {
        this.image = image;
        this.consumer.accept(this.image);

        MinecraftClient.getInstance().setScreen(this.previousScreen);
        this.previousScreen = null;
    }

}
