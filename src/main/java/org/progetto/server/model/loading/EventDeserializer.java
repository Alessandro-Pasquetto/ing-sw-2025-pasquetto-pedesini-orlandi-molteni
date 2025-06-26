package org.progetto.server.model.loading;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.progetto.server.model.components.*;
import org.progetto.server.model.events.*;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * Deserializer for loading EventCard objects from JSON file
 *
 * @author Lorenzo
 */
public class EventDeserializer implements JsonDeserializer<EventCard> {

    @Override
    public EventCard deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

        JsonObject jsonObject = json.getAsJsonObject();
        CardType type = CardType.valueOf(jsonObject.get("type").getAsString());
        int level = jsonObject.get("level").getAsInt();
        String imgSrc = jsonObject.get("imgSrc").getAsString();

        switch (type) {

            case METEORSRAIN:
                ArrayList<Projectile> meteors = context.deserialize(jsonObject.get("meteors"), new TypeToken<ArrayList<Projectile>>() {}.getType());
                return new MeteorsRain(type, level, imgSrc, meteors);

            case OPENSPACE:
                return new OpenSpace(type, level, imgSrc);

            case SLAVERS:
                int firePower = jsonObject.get("firePowerRequired").getAsInt();
                int penaltyCrew = jsonObject.get("penaltyCrew").getAsInt();
                int penaltyDays = jsonObject.get("penaltyDays").getAsInt();
                int rewardCredits = jsonObject.get("rewardCredits").getAsInt();
                return new Slavers(type, level, imgSrc, firePower, penaltyCrew, penaltyDays, rewardCredits);

            case SMUGGLERS:
                firePower = jsonObject.get("firePowerRequired").getAsInt();
                int penaltyBoxes = jsonObject.get("penaltyBoxes").getAsInt();
                penaltyDays = jsonObject.get("penaltyDays").getAsInt();
                ArrayList<Box> rewardBoxes = context.deserialize(jsonObject.get("rewardBoxes"), new TypeToken<ArrayList<Box>>() {}.getType());
                return new Smugglers(type, level, imgSrc, firePower, penaltyBoxes, penaltyDays, rewardBoxes);

            case LOSTSTATION:
                int requiredCrew = jsonObject.get("requiredCrew").getAsInt();
                rewardBoxes = context.deserialize(jsonObject.get("rewardBoxes"), new TypeToken<ArrayList<Box>>() {}.getType());
                penaltyDays = jsonObject.get("penaltyDays").getAsInt();
                return new LostStation(type, level, imgSrc, requiredCrew, rewardBoxes, penaltyDays);

            case EPIDEMIC:
                return new Epidemic(type, level, imgSrc);

            case PIRATES:
                int firePowerRequired = jsonObject.get("firePowerRequired").getAsInt();
                penaltyDays = jsonObject.get("penaltyDays").getAsInt();
                rewardCredits = jsonObject.get("rewardCredits").getAsInt();
                ArrayList<Projectile> shots = context.deserialize(jsonObject.get("shots"), new TypeToken<ArrayList<Projectile>>() {}.getType());
                return new Pirates(type, level, imgSrc, firePowerRequired, penaltyDays, rewardCredits, shots);

            case STARDUST:
                return new Stardust(type, level, imgSrc);

            case PLANETS:
                ArrayList<ArrayList<Box>> rewardsForPlanets = context.deserialize(jsonObject.get("rewardsForPlanets"), new TypeToken<ArrayList<ArrayList<Box>>>() {}.getType());
                penaltyDays = jsonObject.get("penaltyDays").getAsInt();
                return new Planets(type, level, imgSrc, rewardsForPlanets, penaltyDays);

            case LOSTSHIP:
                penaltyCrew = jsonObject.get("penaltyCrew").getAsInt();
                rewardCredits = jsonObject.get("rewardCredits").getAsInt();
                penaltyDays = jsonObject.get("penaltyDays").getAsInt();
                return new LostShip(type, level, imgSrc, penaltyCrew, rewardCredits, penaltyDays);

            case BATTLEZONE:
                ArrayList<ConditionPenalty> couples = deserializeConditionPenalties(jsonObject.get("couples").getAsJsonArray(), context);
                return new Battlezone(type, level, imgSrc, couples);
        }

        return null;
    }

    // Helper method to manually deserialize the ConditionPenalty list
    private ArrayList<ConditionPenalty> deserializeConditionPenalties(JsonArray couplesArray, JsonDeserializationContext context) {
        ArrayList<ConditionPenalty> conditionPenalties = new ArrayList<>();

        for (JsonElement coupleElement : couplesArray) {
            JsonObject coupleObject = coupleElement.getAsJsonObject();

            ConditionType conditionType = ConditionType.valueOf(coupleObject.get("type").getAsString());
            JsonObject penaltyObject = coupleObject.getAsJsonObject("penalty");

            Penalty penalty = deserializePenalty(penaltyObject, context);

            conditionPenalties.add(new ConditionPenalty(conditionType, penalty));
        }

        return conditionPenalties;
    }

    // Helper method to manually deserialize the Penalty object
    private Penalty deserializePenalty(JsonObject penaltyObject, JsonDeserializationContext context) {
        PenaltyType penaltyType = PenaltyType.valueOf(penaltyObject.get("type").getAsString());
        int neededAmount = penaltyObject.get("neededAmount").getAsInt();
        ArrayList<Projectile> shots = context.deserialize(penaltyObject.get("shots"), new TypeToken<ArrayList<Projectile>>() {}.getType());

        return new Penalty(penaltyType, neededAmount, shots);
    }
}