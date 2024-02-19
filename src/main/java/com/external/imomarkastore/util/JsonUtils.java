package com.external.imomarkastore.util;

import com.google.gson.JsonElement;
import lombok.NoArgsConstructor;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class JsonUtils {

    public static void extractMessageIds(JsonElement jsonElement, List<Integer> messageIds) {
        if (jsonElement.isJsonObject()) {
            final var jsonObject = jsonElement.getAsJsonObject();
            jsonObject.keySet()
                    .forEach(key -> extractMessageIds(jsonObject.get(key), messageIds));
        } else if (jsonElement.isJsonArray()) {
            jsonElement.getAsJsonArray()
                    .forEach(jsonArrayEntry -> extractMessageIds(jsonArrayEntry, messageIds));
        } else if (jsonElement.isJsonPrimitive()) {
            messageIds.add(jsonElement.getAsInt());
        }
    }
}
