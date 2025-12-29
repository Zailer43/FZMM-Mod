package fzmm.zailer.me.client.gui.head_gallery.filter;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.logic.api.ApiResponse;
import fzmm.zailer.me.client.logic.minecraft_heads.IMchMatcher;
import fzmm.zailer.me.client.logic.minecraft_heads.model.MchCategory;
import fzmm.zailer.me.client.logic.minecraft_heads.model.MchTier;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class CategoryFilter extends AbstractFilter {

    public CategoryFilter(Consumer<IMchMatcher> applyOption) {
        super(applyOption);
    }

    @Override
    public CompletableFuture<ApiResponse<List<IMchMatcher>>> initOptions() {
        List<IMchMatcher> result = new ArrayList<>();
        result.add(MchCategory.ALL);
        result.addAll(FzmmClient.MCH_RESOURCES.categories());

        return ApiResponse.futureOf(result);
    }

    @Override
    protected Text filterText() {
        return Text.translatable("fzmm.gui.headGallery.button.categories");
    }

    @Override
    public Text optionText(IMchMatcher option) {
        return FzmmClient.MCH_RESOURCES.categoryText(option);
    }

    @Override
    protected Text missingOptions() {
        return Text.translatable("fzmm.gui.headGallery.snack_bar.error.message");
    }

    @Override
    public int permissionRequired() {
        return MchTier.CATEGORY_GENERAL_REQUEST;
    }
}
