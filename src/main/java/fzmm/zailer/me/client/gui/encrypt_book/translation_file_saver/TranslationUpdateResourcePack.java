package fzmm.zailer.me.client.gui.encrypt_book.translation_file_saver;

import fzmm.zailer.me.client.logic.enycrpt_book.TranslationEncryptProfile;
import fzmm.zailer.me.client.logic.resource_pack.ResourcePackWriter;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class TranslationUpdateResourcePack implements ITranslationFileSaver{
    @Override
    public Component getMessage() {
        return Component.translatable("fzmm.gui.resourcePackBuilder.option.updateResourcePack");
    }

    @Override
    public CompletableFuture<Boolean> save(TranslationEncryptProfile profile) {
        return CompletableFuture.supplyAsync(() -> {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                PointerBuffer filterPatterns = stack.mallocPointer(1);
                filterPatterns.put(stack.UTF8("*.zip"));
                filterPatterns.flip();

                String resourcePackPath = TinyFileDialogs.tinyfd_openFileDialog(
                        "Choose resource pack",
                        Minecraft.getInstance().getResourcePackDirectory().toString() + "/",
                        filterPatterns,
                        "Resource pack (ZIP)",
                        false
                );
                boolean cancelled = resourcePackPath == null;

                if (!cancelled) {
                    this.updateResourcePack(profile, Path.of(resourcePackPath)).join();
                }
                return cancelled;
            }
        }, Util.backgroundExecutor());
    }

    private CompletableFuture<Void> updateResourcePack(TranslationEncryptProfile profile, Path resourcePackPath) {
        return new ResourcePackWriter()
                .from(resourcePackPath)
                .fileName(resourcePackPath.getFileName().toString())
                .file(EN_US_LANG_PATH, profile.toJson(), true)
                .write();
    }
}
