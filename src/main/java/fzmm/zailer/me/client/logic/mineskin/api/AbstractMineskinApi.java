package fzmm.zailer.me.client.logic.mineskin.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.client.gui.components.extend.EStyles;
import fzmm.zailer.me.client.gui.components.snack_bar.BaseSnackBarComponent;
import fzmm.zailer.me.client.gui.components.snack_bar.SnackBarBuilder;
import fzmm.zailer.me.client.logic.api.ApiResponse;
import fzmm.zailer.me.client.logic.api.IApiRateLimited;
import fzmm.zailer.me.client.logic.api.IApiRemote;
import fzmm.zailer.me.utils.SnackBarManager;
import io.wispforest.owo.config.ui.ConfigScreen;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public abstract class AbstractMineskinApi implements IApiRemote, IApiRateLimited {
    public static final String[] SKIP_WARNINGS = {
            "no_api_key" // can be annoying if you don't have a key
    };
    protected long nextAllowedTimestamp = 0L; // generate-queue has a slower rate limit than list-queue

    @Override
    public String baseApiUrl() {
        return "https://api.mineskin.org/v2/";
    }

    @Override
    public String buildApiUrl(String route, String... parameters) {
        return this.baseApiUrl() + route + this.buildParameters(parameters);
    }

    @Override
    public void logWarnings(String route, ApiResponse<?> response) {
        this.warningsToMessage(this.extractWarnings(response, this::parseMessage, "messages"))
                .ifPresent(s -> FzmmClient.LOGGER.info("[AbstractMineskinApi] API Info: {}", s));

        MinecraftClient.getInstance().execute(() -> {
            this.responseWarningsAlert(response).ifPresent(snackBar ->
                    SnackBarManager.getInstance().add(snackBar.build())
            );
            this.statusCodeAlert(response).ifPresent(snackBar ->
                    SnackBarManager.getInstance().add(snackBar.build())
            );
        });
    }

    @Override
    public void parseSuccess(ApiResponse<?> response) {
        Optional<JsonObject> jsonOptional = response.json();
        if (jsonOptional.isEmpty()) return;
        JsonObject jsonObject = jsonOptional.get();

        response.success(jsonObject.has("success") && jsonObject.get("success").getAsBoolean());
    }

    @Override
    public <T> ApiResponse<T> parseModel(Function<JsonObject, T> parser, ApiResponse<T> response) {
        if (!response.isSuccess()) return response;
        JsonObject jsonObject = response.json().orElseThrow();

        response.data(parser.apply(jsonObject));
        return response;
    }

    private Optional<SnackBarBuilder> responseWarningsAlert(ApiResponse<?> response) {
        MutableText warnings = Text.empty();
        this.warningsToMessage(this.extractWarnings(response, this::parseMessage, "errors")).ifPresent(warnings::append);
        this.warningsToMessage(this.extractWarnings(response, this::parseMessage, "warnings")).ifPresent(s -> {
            if (!warnings.getString().isEmpty()) {
                warnings.append(Text.literal("\n"));
            }
            warnings.append(Text.literal(s));
        });

        if (warnings.getString().isEmpty()) return Optional.empty();
        FzmmClient.LOGGER.warn("[AbstractMineskinApi] API Warning(s): {}", warnings.getString());
        return Optional.of(BaseSnackBarComponent.builder(SnackBarManager.MINESKIN_WARNINGS_ID)
                .backgroundColor(EStyles.ALERT_WARNING_COLOR)
                .title(Text.translatable("fzmm.gui.mineskin.snack_bar.warnings.title"))
                .details(warnings)
                .highTimer()
                .startTimer()
                .closeButton()
                .sizing(Sizing.fixed(200), Sizing.content())
                .keepOnLimit()
        );
    }

    protected Optional<String> parseMessage(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String code = jsonObject.get("code").getAsString();
        for (var skip : SKIP_WARNINGS) {
            if (skip.equals(code)) {
                FzmmClient.LOGGER.info("[AbstractMineskinApi] Skipping snack bar of warning: {}", code);
                return Optional.empty();
            }
        }

        return Optional.of(jsonObject.get("message").getAsString());
    }

    public Optional<SnackBarBuilder> statusCodeAlert(@Nullable ApiResponse<?> response) {
        int statusCode = response != null ? response.statusCode() : -1;
        int statusType = response != null ? response.statusType() : -1;
        if (statusType == 2) return Optional.empty();

        SnackBarBuilder snackBar = BaseSnackBarComponent.builder(SnackBarManager.MINESKIN_ID)
                .backgroundColor(EStyles.ALERT_ERROR_COLOR)
                .keepOnLimit()
                .highTimer()
                .startTimer()
                .closeButton();

        if (statusCode == 401) {
            snackBar.title(Text.translatable("fzmm.snack_bar.mineskin.error.invalidApiKey"))
                    .details(Text.translatable("fzmm.snack_bar.mineskin.error.invalidApiKey.description"))
                    .button(unused -> EComponents.button(Text.translatable("fzmm.gui.title.configs.icon")).onPress(button ->
                            MinecraftClient.getInstance().setScreen(ConfigScreen.create(FzmmClient.CONFIG, MinecraftClient.getInstance().currentScreen))
                    ));
        } else {
            String translationKey = "fzmm.gui.mineskin.snack_bar.error." + (statusType == 5 ? "external" : "internal");
            snackBar.title(Text.translatable(translationKey))
                    .details(Text.translatable(translationKey + ".description"));
        }

        return Optional.of(snackBar);
    }

    @Override
    public HttpRequestBase requestOf(String url) {
        HttpRequestBase result = this.getRequest(url);
        result.addHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
        result.addHeader("Accept", ContentType.APPLICATION_JSON.getMimeType());

        if (!FzmmClient.CONFIG.mineskin.apiKey().isBlank()) {
            result.addHeader("Authorization", "Bearer " + FzmmClient.CONFIG.mineskin.apiKey());
        }

        return result;
    }

    protected abstract HttpRequestBase getRequest(String url);

    @Override
    public long nextAllowed() {
        return this.nextAllowedTimestamp;
    }

    @Override
    public void applyDelay(long value) {
        this.nextAllowedTimestamp = value;
    }

    @Override
    @Nullable
    public Long parseDelayFromResponse(ApiResponse<?> response) {
        Optional<JsonObject> jsonOptional = response.json();
        if (jsonOptional.isEmpty()) return this.defaultDelayMillis();
        if (!jsonOptional.get().has("rateLimit")) return this.getWaitMillis();

        return jsonOptional.get().getAsJsonObject("rateLimit")
                .get("next").getAsJsonObject()
                .get("relative").getAsLong();
    }

    @Override
    public CompletableFuture<Void> prepare() {
        if (this.getWaitMillis() == 0) {
            return CompletableFuture.completedFuture(null);
        } else {
            return this.waitForNext();
        }
    }
}
