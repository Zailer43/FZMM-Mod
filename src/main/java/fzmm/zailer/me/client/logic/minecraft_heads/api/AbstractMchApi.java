package fzmm.zailer.me.client.logic.minecraft_heads.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.extend.EStyles;
import fzmm.zailer.me.client.gui.components.snack_bar.BaseSnackBarComponent;
import fzmm.zailer.me.client.logic.api.ApiResponse;
import fzmm.zailer.me.client.logic.api.IApiRateLimited;
import fzmm.zailer.me.client.logic.api.IApiRemote;
import fzmm.zailer.me.client.logic.minecraft_heads.MinecraftHeadsResources;
import fzmm.zailer.me.utils.SnackBarManager;
import io.wispforest.owo.ui.core.Sizing;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractMchApi implements IApiRemote, IApiRateLimited {
    public static final String URL = "https://minecraft-heads.com";
    public static final String APP_UUID = "cb0d7713-66a6-4a3c-bd5a-0e2b050ffd4a";
    protected static long nextAllowedTimestamp = 0L;

    @Override
    public String baseApiUrl() {
        return URL + "/api/";
    }

    @Override
    public String buildApiUrl(String route, String... parameters) {
        StringBuilder paramBuilder = new StringBuilder();
        paramBuilder.append("?app_uuid=").append(APP_UUID);
        paramBuilder.append(this.buildParameters(parameters));

        if (MinecraftHeadsResources.isDebug()) {
            paramBuilder.append("&demo=true");
        }

        return this.baseApiUrl() + route + paramBuilder;
    }

    @Override
    public void parseSuccess(ApiResponse<?> response) {
        response.json().ifPresent(jsonObject -> {
            FzmmClient.MCH_RESOURCES.license(jsonObject);
            response.success(!jsonObject.has("error"));
        });
    }

    @Override
    public HttpRequestBase requestOf(String url) {
        HttpGet result = new HttpGet(url);
        if (!FzmmClient.CONFIG.minecraftHeads.apiKey().isBlank()) {
            result.addHeader("api-key", FzmmClient.CONFIG.minecraftHeads.apiKey());
        }
        return result;
    }

    @Override
    public <T> ApiResponse<T> parseModel(Function<JsonObject, T> parser, ApiResponse<T> response) {
        if (!response.isSuccess()) return response;
        JsonObject jsonObject = response.json().orElseThrow();
        if (!jsonObject.has("data") || !jsonObject.get("data").isJsonArray()) return response;

        response.data(parser.apply(jsonObject));
        return response;
    }

    public <T> List<T> parse(JsonObject json, Function<JsonObject, T> parser) {
        return json.get("data").getAsJsonArray().asList()
                .parallelStream()
                .map(JsonElement::getAsJsonObject)
                .map(parser)
                .collect(Collectors.toCollection(ObjectArrayList::new));
    }

    @Override
    public void processResponse(ApiResponse<?> response, boolean isError) {
        IApiRateLimited.super.processResponse(response, isError);
    }

    @Override
    public void logWarnings(String route, ApiResponse<?> response) {
        MutableText warnings = Text.empty();
        if (!response.isSuccess() || response.statusType() != 2) {
            response.json().ifPresent(jsonObject -> {
                if (jsonObject.has("error")) {
                    warnings.append(Text.literal(jsonObject.get("error").getAsString()));
                }
            });
        } else {
            this.warningsToMessage(this.extractWarnings(response, this::parseMessage, "warnings"))
                    .ifPresent(s -> warnings.append(Text.literal(s)));

            if (FzmmClient.CONFIG.minecraftHeads.apiKey().isBlank()) {
                FzmmClient.LOGGER.warn("[MchApi] Missing API Key, skipping snack bar of warnings: {}", warnings.getString());
                return;
            }
        }

        if (warnings.getString().isEmpty()) return;

        FzmmClient.LOGGER.warn("[MchApi] API Warning(s): {}", warnings.getString());
        MinecraftClient.getInstance().execute(() -> SnackBarManager.getInstance().add(BaseSnackBarComponent.builder(SnackBarManager.HEAD_GALLERY_WARNING_ID)
                .backgroundColor(EStyles.ALERT_WARNING_COLOR)
                .title(Text.translatable("fzmm.gui.headGallery.snack_bar.warnings.title"))
                .details(warnings)
                .expandDetails()
                .closeButton()
                .sizing(Sizing.fixed(200), Sizing.content())
                .keepOnLimit()
                .build()
        ));
    }

    protected Optional<String> parseMessage(JsonElement jsonElement) {
        return Optional.of(jsonElement.getAsString());
    }

    @Override
    public long nextAllowed() {
        return nextAllowedTimestamp;
    }

    @Override
    public long defaultDelayMillis() {
        return 1500L;
    }

    @Override
    public void applyDelay(long timestamp) {
        nextAllowedTimestamp = timestamp;
    }

    @Override
    public @Nullable Long parseDelayFromResponse(ApiResponse<?> response) {
        return this.defaultDelayMillis();
    }
}
