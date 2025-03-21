package org.progetto.server.model.loadClasses;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.progetto.server.model.components.*;
import org.progetto.server.model.events.*;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class EventDeserializer implements JsonDeserializer<EventCard> {
    @Override
    public EventCard deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();
        CardType type = CardType.valueOf(jsonObject.get("type").getAsString());
        String imgSrc = jsonObject.get("imgSrc").getAsString();

        switch (type) {

            case METEORSRAIN:
                ArrayList<Projectile> meteors = context.deserialize(jsonObject.get("meteors"), new TypeToken<ArrayList<Projectile>>() {}.getType());
                return new MeteorsRain(type,imgSrc,meteors);

            case OPENSPACE:
                return new OpenSpace(type,imgSrc);

            case SLAVERS:
                int firePower = jsonObject.get("firePowerRequired").getAsInt();
                int penaltyCrew = jsonObject.get("penaltyCrew").getAsInt();
                int penaltyDays = jsonObject.get("penaltyDays").getAsInt();
                int rewardCredits = jsonObject.get("rewardCredits").getAsInt();
                return new Slavers(type,imgSrc,firePower,penaltyCrew,penaltyDays,rewardCredits);

            case SMUGGLERS:
                firePower = jsonObject.get("firePowerRequired").getAsInt();
                int penaltyBoxes = jsonObject.get("penaltyBoxes").getAsInt();
                penaltyDays = jsonObject.get("penaltyDays").getAsInt();
                ArrayList<Box> rewardBoxes = context.deserialize(jsonObject.get("rewardBoxes"), new TypeToken<ArrayList<Box>>() {}.getType());
                return new Smugglers(type,imgSrc,firePower,penaltyBoxes,penaltyDays,rewardBoxes);


            case LOSTSTATION:
                int requiredCrew = jsonObject.get("requiredCrew").getAsInt();
                rewardBoxes = context.deserialize(jsonObject.get("rewardBoxes"), new TypeToken<ArrayList<Box>>() {}.getType());
                penaltyDays = jsonObject.get("penaltyDays").getAsInt();
                return new LostStation(type,imgSrc,requiredCrew,rewardBoxes,penaltyDays);

            case SABOTAGE:
                return new Sabotage(type,imgSrc);

            case EPIDEMIC:
                return new Epidemic(type,imgSrc);

            case PIRATES:
                firePower = jsonObject.get("firePowerRequired").getAsInt();
                ArrayList<Projectile> penaltyShots = context.deserialize(jsonObject.get("penaltyShots"), new TypeToken<ArrayList<Projectile>>() {}.getType());
                penaltyDays = jsonObject.get("penaltyDays").getAsInt();
                rewardCredits = jsonObject.get("rewardCredits").getAsInt();
                return new Pirates(type,imgSrc,firePower,penaltyDays,rewardCredits,penaltyShots);

            case STARDUST:
                penaltyDays = jsonObject.get("penaltyDays").getAsInt();
                return new Stardust(type,imgSrc);

            case PLANETS:
                ArrayList<ArrayList<Box>> rewardsForPlanets = context.deserialize(jsonObject.get("rewardsForPlanets"), new TypeToken<ArrayList<ArrayList<Box>>>() {}.getType());
                penaltyDays = jsonObject.get("penaltyDays").getAsInt();
                return new Planets(type,imgSrc,rewardsForPlanets,penaltyDays);

            case LOSTSHIP:
                penaltyCrew = jsonObject.get("penaltyCrew").getAsInt();
                rewardCredits = jsonObject.get("rewardCredits").getAsInt();
                penaltyDays = jsonObject.get("penaltyDays").getAsInt();
                return new LostShip(type,imgSrc,penaltyCrew,rewardCredits,penaltyDays);

            case BATTLEZONE:
                ArrayList<ConditionPenalty> couples = context.deserialize(jsonObject.get("couples"), new TypeToken<ArrayList<ConditionPenalty>>() {}.getType());
                return new Battlezone(type,imgSrc,couples);

        }

        return null;
    }
}
