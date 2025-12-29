package fzmm.zailer.me.client.gui.head_gallery.filter;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.client.gui.components.extend.component.EButtonComponent;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.logic.api.ApiResponse;
import fzmm.zailer.me.client.logic.history.IMemento;
import fzmm.zailer.me.client.logic.minecraft_heads.IMchMatcher;
import fzmm.zailer.me.client.logic.minecraft_heads.model.MchTier;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class OtherCollectionFilter extends SelfCollectionFilter implements IMemento {
    private TextBoxComponent usernameField = null;

    public OtherCollectionFilter(Consumer<IMchMatcher> applyOption) {
        super(applyOption);
    }

    @Override
    public CompletableFuture<ApiResponse<List<IMchMatcher>>> initOptions() {
        if (this.usernameField == null || this.usernameField.getValue().isBlank()) return new CompletableFuture<>();

        return super.initOptions();
    }

    @Override
    protected EFlowLayout parametersLayout() {
        EFlowLayout result = super.parametersLayout();

        //TODO: add a button that open a overlay with saved users
        this.usernameField = UIComponents.textBox(Sizing.expand(100));
        this.usernameField.setMaxLength(16);
        this.usernameField.keyPress().subscribe(input -> {
            if (input.isConfirmation()) {
                this.initOptionsAsync(null);
                return true;
            }
            return false;
        });

        EButtonComponent button = EComponents.button(Component.translatable("fzmm.gui.headGallery.option.collection.other.fetchCollection"));

        result.verticalAlignment(VerticalAlignment.CENTER);
        button.onPress(buttonComponent -> this.initOptionsAsync(null));
        result.child(this.usernameField);
        result.child(button);

        return result;
    }

    @Override
    protected boolean loadOptionsAutomatically() {
        return false;
    }

    @Override
    protected Component filterText() {
        return Component.translatable("fzmm.gui.headGallery.button.collections.other");
    }

    @Override
    protected Component missingOptions() {
        return Component.translatable("fzmm.gui.headGallery.option.collection.other.missing");
    }

    @Override
    public int permissionRequired() {
        return MchTier.COLLECTION_PLAYERS_GENERAL_REQUEST;
    }

    @Override
    protected CompletableFuture<ApiResponse<List<IMchMatcher>>> collections() {
        return FzmmClient.MCH_RESOURCES.fetchCollectionFrom(this.usernameField.getValue());
    }

    @Override
    protected Component loadingAlertText() {
        return Component.translatable("fzmm.gui.headGallery.snack_bar.loading.collection.other", this.usernameField.getValue());
    }

    @Override
    public void backup(ObjectOutputStream output) throws IOException {
        output.writeObject(this.usernameField.getValue());
    }

    @Override
    public void restore(ObjectInputStream input) throws IOException, ClassNotFoundException {
        this.usernameField.text((String) input.readObject());
    }
}
