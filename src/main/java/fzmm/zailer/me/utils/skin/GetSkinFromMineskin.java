package fzmm.zailer.me.utils.skin;

import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.utils.HeadUtils;
import fzmm.zailer.me.utils.ImageUtils;
import net.minecraft.item.ItemStack;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class GetSkinFromMineskin extends GetSkinDecorator {
    private BufferedImage skin;

    public GetSkinFromMineskin(GetSkinDecorator getSkinDecorator) {
        super(getSkinDecorator);
    }

    public GetSkinFromMineskin() {
        super(null);
    }

    public void setSkin(BufferedImage skin) {
        this.skin = skin;
    }

    public GetSkinFromMineskin setCacheSkin(String playerName) {
        try {
            new GetSkinFromCache().getSkin(playerName).ifPresent(bufferedImage -> this.skin = bufferedImage);
        } catch (IOException e) {
            FzmmClient.LOGGER.error("[GetSkinFromMineskin] Failed to get skin for player " + playerName, e);
        }
        return this;
    }

    @Override
    public Optional<BufferedImage> getSkin(String playerName) throws IOException {
        if (this.skin == null) {
            FzmmClient.LOGGER.warn("[GetSkinFromMineskin] No skin found for player " + playerName);
            return super.getSkin(playerName);
        }

        CompletableFuture<HeadUtils> completableFuture = new HeadUtils().uploadHead(this.skin, playerName);
        AtomicBoolean successful = new AtomicBoolean(false);
        AtomicReference<String> url = new AtomicReference<>("");

        completableFuture.thenAcceptAsync(headUtils -> {
            url.set(headUtils.getUrl());
            successful.set(headUtils.isSkinGenerated());
        });

        return successful.get() ? ImageUtils.getImageFromUrl(url.get()) : super.getSkin(playerName);
    }

    @Override
    public Optional<ItemStack> getHead(String playerName) {
        if (this.skin == null) {
            FzmmClient.LOGGER.warn("[GetSkinFromMineskin] No skin found for player " + playerName);
            return super.getHead(playerName);
        }

        try {
            HeadUtils headUtils = new HeadUtils().uploadHead(this.skin, playerName).get();
            HeadBuilder builder = HeadBuilder.builder()
                    .headName(playerName)
                    .skinValue(headUtils.getSkinValue())
                    .signature(headUtils.getSignature());

            return headUtils.isSkinGenerated() ? Optional.of(builder.get()) : super.getHead(playerName);
        } catch (InterruptedException | ExecutionException e) {
            FzmmClient.LOGGER.error("Failed to upload head for player '{}'", playerName, e);
        }
        return super.getHead(playerName);
    }
}
