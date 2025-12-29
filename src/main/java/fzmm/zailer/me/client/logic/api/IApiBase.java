package fzmm.zailer.me.client.logic.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface IApiBase {

    void processResponse(ApiResponse<?> response, boolean isError);

    void logWarnings(String route, ApiResponse<?> response);

    default Optional<String> warningsToMessage(List<String> warnings) {
        if (warnings.isEmpty()) return Optional.empty();

        StringBuilder result = new StringBuilder();
        Iterator<String> warningsIterator = warnings.iterator();
        while (warningsIterator.hasNext()) {
            result.append("- ").append(warningsIterator.next());

            if (warningsIterator.hasNext()) {
                result.append("\n");
            }
        }

        return Optional.of(result.toString());
    }

    default List<String> extractWarnings(ApiResponse<?> response, Function<JsonElement, Optional<String>> parser, String key) {
        Optional<JsonObject> jsonOptional = response.json();
        if (jsonOptional.isEmpty()) return List.of();
        JsonObject json = jsonOptional.get();

        List<String> result = new ArrayList<>();
        if ((!json.has(key)) || (!json.get(key).isJsonArray()) || json.get(key).getAsJsonArray().isEmpty()) {
            return result;
        }

        for (var jsonElement : json.get(key).getAsJsonArray()) {
            parser.apply(jsonElement).ifPresent(result::add);
        }

        return result;
    }

    <T> ApiResponse<T> parseModel(Function<JsonObject, T> parser, ApiResponse<T> response);

}
