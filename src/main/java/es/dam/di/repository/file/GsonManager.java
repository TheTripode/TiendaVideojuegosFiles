package es.dam.di.repository.file;

import com.google.gson.*;
import java.time.LocalDate;

// Clase base para poder tratar con archivos json
public class GsonManager {

    private static final Gson GSON = new GsonBuilder()
            //Código para poder guardar bien el LocalDate
            .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, typeOfT, context) ->
                    LocalDate.parse(json.getAsString()))
            .setPrettyPrinting() // para formato bonito
            .create();

    public static Gson getGson() {
        return GSON;
    }
}