package fzmm.zailer.me.client.gui.encrypt_book.translation_file_saver;

import fzmm.zailer.me.client.logic.enycrpt_book.TranslationEncryptProfile;
import fzmm.zailer.me.client.logic.resource_pack.ResourcePackWriter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class TranslationCreateResourcePack implements ITranslationFileSaver {
    @Override
    public Text getMessage() {
        return Text.translatable("fzmm.gui.resourcePackBuilder.option.newResourcePack");
    }

    @Override
    public CompletableFuture<Boolean> save(TranslationEncryptProfile profile) {
        return CompletableFuture.supplyAsync(() -> {
            String resourcePackPath = TinyFileDialogs.tinyfd_saveFileDialog(
                    "Save resource pack",
                    MinecraftClient.getInstance().getResourcePackDir().resolve("resourcepack.zip").toString(),
                    null,
                    null
            );
            boolean cancelled = resourcePackPath == null;

            if (!cancelled) {
                this.updateResourcePack(profile, Path.of(resourcePackPath)).join();
            }

            return cancelled;
        }, Util.getMainWorkerExecutor());
    }

    private CompletableFuture<Void> updateResourcePack(TranslationEncryptProfile profile, Path resourcePackPath) {
        return new ResourcePackWriter()
                .fileName(resourcePackPath.getFileName().toString())
                .description("Decrypt to " + profile.translationKey())
                .file(EN_US_LANG_PATH, profile.toJson(), false)
                .write();
    }
}
