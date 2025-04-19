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
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.logic.head_generator.model.InternalModels;
import fzmm.zailer.me.client.logic.player_statue.PlayerStatue;
import fzmm.zailer.me.client.logic.player_statue.StatuePart;
import fzmm.zailer.me.utils.ImageUtils;
import fzmm.zailer.me.utils.ItemUtils;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.joml.Vector3f;

import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class PlayerStatueGenerateTab implements IPlayerStatueTab {
    private static final ImageStatus INVALID_SKIN_SIZE = new ImageStatus("error.title", "error.details.playerStatue.invalidSkinSize", true);
    private static CompletableFuture<Void> CREATE_COMPLETABLE_FUTURE = null;
    private ImageRowsElements skinElements;
    private ButtonWidget executeButton;

    @Override
    public String getId() {
        return "generate";
    }

    @Override
    public void setupComponents(EFlowLayout rootComponent) {
        this.skinElements = ImageRows.setup(rootComponent, "skin", "skin-source", ImageMode.NAME);
        this.executeButton = rootComponent.childById(ButtonWidget.class, PlayerStatueScreen.EXECUTE_ID);

        ImageButtonComponent skinButton = this.skinElements.imageButton();
        skinButton.setImageLoadedEvent(this::skinCallback);
        skinButton.setButtonCallback(skinOptional -> {
            this.executeButton.active = this.canExecute();
            if (skinOptional.isEmpty()) {
                return;
            }

            BufferedImage skin = skinOptional.get();
            if (skin.getWidth() == 64 && skin.getHeight() == 32) {
                skinButton.setImage(InternalModels.OLD_FORMAT_TO_NEW_FORMAT.getHeadSkin(skin, ImageUtils.hasUnusedPixel(skin)));
            }
        });
    }


    @Override
    public void execute(HorizontalDirectionOption direction, float x, float y, float z, String name) {
        if (!this.canExecute()) {
            return;
        }

        Optional<BufferedImage> image = this.skinElements.imageButton().getImage();

        if (image.isEmpty()) {
            return;
        }

        CREATE_COMPLETABLE_FUTURE = CompletableFuture.runAsync(() -> {
            this.executeButton.active = false;


            Vector3f pos = new Vector3f(x, y, z);

            ItemStack statueGenerated = new PlayerStatue(image.get(), name, pos, direction)
                    .generateStatues()
                    .getStatueInContainer();

            ItemUtils.give(statueGenerated);
            InvisibleEntityWarning.add(true, true, Text.translatable("fzmm.snack_bar.entityDifficultToRemove.entity.playerStatue"), StatuePart.PLAYER_STATUE_TAG);

            this.executeButton.active = true;
            CREATE_COMPLETABLE_FUTURE = null;
        });
    }

    @Override
    public boolean canExecute() {
        return this.canExecute(this.skinElements.imageButton().hasImage());
    }

    public boolean canExecute(boolean hasImage) {
        return hasImage && CREATE_COMPLETABLE_FUTURE == null;
    }

    public ImageStatus skinCallback(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        if (!(width == 64 && height == 32) && !(width == 64 && height == 64) && !(width == 128 && height == 128))
            return INVALID_SKIN_SIZE;

        return ImageStatus.IMAGE_LOADED;
    }

    @Override
    public IMementoObject createMemento() {
        return new GenerateMementoTab(this.skinElements.valueField().getText(), this.skinElements.mode().get());
    }

    @Override
    public void restoreMemento(IMementoObject mementoTab) {
        GenerateMementoTab memento = (GenerateMementoTab) mementoTab;
        this.skinElements.valueField().text(memento.skinRowValue);
        this.skinElements.imageModeButtons().get(memento.sourceType).onPress();
    }

    private record GenerateMementoTab(String skinRowValue, ImageMode sourceType) implements IMementoObject {
    }
}
