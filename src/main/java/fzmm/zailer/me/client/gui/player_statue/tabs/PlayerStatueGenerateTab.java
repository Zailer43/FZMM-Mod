package fzmm.zailer.me.client.gui.player_statue.tabs;

import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.image.ImageButtonComponent;
import fzmm.zailer.me.client.gui.components.image.ImageMode;
import fzmm.zailer.me.client.gui.components.image.ImageStatus;
import fzmm.zailer.me.client.gui.components.row.image.ImageRows;
import fzmm.zailer.me.client.gui.components.row.image.ImageRowsElements;
import fzmm.zailer.me.client.gui.options.HorizontalDirectionOption;
import fzmm.zailer.me.client.gui.player_statue.PlayerStatueScreen;
import fzmm.zailer.me.client.gui.utils.InvisibleEntityWarning;
import fzmm.zailer.me.client.logic.head_generator.model.InternalModels;
import fzmm.zailer.me.client.logic.history.IMemento;
import fzmm.zailer.me.client.logic.player_statue.PlayerStatue;
import fzmm.zailer.me.client.logic.player_statue.StatuePart;
import fzmm.zailer.me.utils.ImageUtils;
import fzmm.zailer.me.utils.ItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.joml.Vector3f;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;

public class PlayerStatueGenerateTab implements IPlayerStatueTab, IMemento {
    private static final ImageStatus INVALID_SKIN_SIZE = new ImageStatus("error.title", "error.details.playerStatue.invalidSkinSize", true);
    public static boolean active = false;
    private ImageRowsElements skinElements;
    private Button executeButton;

    @Override
    public String getId() {
        return "generate";
    }

    @Override
    public void setupComponents(EFlowLayout rootComponent) {
        this.skinElements = ImageRows.setup(rootComponent, "skin", "skin-source", ImageMode.NAME);
        this.executeButton = rootComponent.childById(Button.class, PlayerStatueScreen.EXECUTE_ID);

        ImageButtonComponent skinButton = this.skinElements.imageButton();
        skinButton.setImageLoadedEvent(this::skinCallback);
        skinButton.setButtonCallback(skinOptional -> {
            this.executeButton.active = this.canExecute();
            if (skinOptional.isEmpty()) return;

            BufferedImage skin = skinOptional.get();
            if (skin.getWidth() == 64 && skin.getHeight() == 32) {
                skinButton.setImage(InternalModels.OLD_FORMAT_TO_NEW_FORMAT.getHeadSkin(skin, ImageUtils.hasUnusedPixel(skin)));
            }
        });
    }


    @Override
    public void execute(HorizontalDirectionOption direction, float x, float y, float z, String name) {
        if (!this.canExecute()) return;

        Optional<BufferedImage> image = this.skinElements.imageButton().getImage();
        if (image.isEmpty()) return;

        active = true;
        this.executeButton.active = false;
        Vector3f pos = new Vector3f(x, y, z);

        PlayerStatue statue = new PlayerStatue(image.get(), name, pos, direction);
        statue.generateStatues().whenComplete((unused, throwable) -> Minecraft.getInstance().execute(() -> {
            active = false;
            this.executeButton.active = true;
            if (throwable instanceof CancellationException || throwable instanceof CompletionException) return;

            ItemUtils.give(statue.getStatueInContainer());
            InvisibleEntityWarning.add(true, true, Component.translatable("fzmm.snack_bar.entityDifficultToRemove.entity.playerStatue"), StatuePart.PLAYER_STATUE_TAG);
        }));
    }

    @Override
    public boolean canExecute() {
        return this.canExecute(this.skinElements.imageButton().hasImage());
    }

    public boolean canExecute(boolean hasImage) {
        return hasImage && !active;
    }

    public ImageStatus skinCallback(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        if (!(width == 64 && height == 32) && !(width == 64 && height == 64) && !(width == 128 && height == 128))
            return INVALID_SKIN_SIZE;

        return ImageStatus.IMAGE_LOADED;
    }

    @Override
    public void backup(ObjectOutputStream output) throws IOException {
        output.writeObject(this.skinElements.valueField().getValue());
        output.writeObject(this.skinElements.mode().get());
    }

    @Override
    public void restore(ObjectInputStream input) throws IOException, ClassNotFoundException {
        this.skinElements.valueField().text((String) input.readObject());
        this.skinElements.imageModeButtons().get((ImageMode) input.readObject()).onPress();
    }
}
