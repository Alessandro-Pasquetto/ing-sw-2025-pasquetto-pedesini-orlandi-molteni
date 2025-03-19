package org.progetto.server.model.loadClasses;

import com.google.gson.*;
import org.progetto.server.model.components.*;

import java.lang.reflect.Type;

public class ComponentDeserializer implements JsonDeserializer<Component> {
    @Override
    public Component deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();
        ComponentType type = ComponentType.valueOf(jsonObject.get("type").getAsString());
        String imgSrc = jsonObject.get("imgSrc").getAsString();
        int[] connections = context.deserialize(jsonObject.get("connections"), int[].class);
        int capacity;
        boolean isRed;

        switch (type) {

            case BATTERY_STORAGE:
                capacity = jsonObject.get("capacity").getAsInt();
                return new BatteryStorage(type, connections, imgSrc, capacity);

            case HOUSING_UNIT:
                capacity = jsonObject.get("capacity").getAsInt();
                return new HousingUnit(type, connections, imgSrc, capacity);

            case BOX_STORAGE:
                capacity = jsonObject.get("capacity").getAsInt();
                isRed = jsonObject.get("isRed").getAsBoolean();
                return new BoxStorage(type,connections,imgSrc,capacity,isRed);

            case RED_BOX_STORAGE:
                capacity = jsonObject.get("capacity").getAsInt();
                isRed = jsonObject.get("isRed").getAsBoolean();
                return new BoxStorage(type,connections,imgSrc,capacity,isRed);
        }

        return new Component(type, connections, imgSrc);
    }
}
