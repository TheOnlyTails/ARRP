package net.devtech.arrp.json.loot;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.loot.UniformLootTableRange;
import net.minecraft.loot.context.LootContext;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.predicate.entity.DamageSourcePredicate;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.lang.reflect.Type;
import java.util.Map;

@SuppressWarnings("unused")
public class JCondition implements Cloneable {
    private final Identifier condition;
    private JsonObject parameters;
    /**
     * Only use a class that ends with "Predicate" and come from vanilla, i. e. {@link ItemPredicate}, {@link
     * StatePredicate}
     */
    private JsonElement predicate;
    private JsonSerializationContext jsonSerializationContext;

    /**
     * @see JLootTable#predicate(String)
     * @deprecated use {@link #JCondition(Identifier)}
     */
    public JCondition(String condition) {
        this.condition = new Identifier(condition);
    }

    public JCondition(Identifier condition) {
        this.condition = condition;
    }

    // public JCondition set(JsonObject parameters) {
    //     parameters.addProperty("condition", this.condition.toString());
    //     this.parameters = parameters;
    //     return this;
    // }

    /**
     * "or"'s the conditions together
     */
    public JCondition alternative(JCondition... conditions) {
        getOrCreateParameters();

        this.parameters.add("terms", jsonSerializationContext.serialize(conditions));
        return this;
    }

    public JCondition blockStateProperties(Identifier block, StatePredicate properties) {
        getOrCreateParameters();

        this.parameters.addProperty("block", block.toString());
        this.parameters.add("properties", properties.toJson());

        return this;
    }

    public JCondition damageSourceProperties(DamageSourcePredicate predicate) {
        getOrCreateParameters();

        this.parameters.add("predicate", predicate.toJson());
        return this;
    }

    public JCondition inverted(JCondition condition) {
        getOrCreateParameters();

        this.parameters.add("term", jsonSerializationContext.serialize(condition));
        return this;
    }

    public JCondition killedByPlayer() {
        return this;
    }

    public JCondition randomChance(float chance) {
        getOrCreateParameters();

        this.parameters.addProperty("chance", chance);
        return this;
    }

    public JCondition randomChanceWithLooting(float chance, float lootingMultiplier) {
        getOrCreateParameters();

        this.parameters.addProperty("chance", chance);
        this.parameters.addProperty("looting_multiplier", lootingMultiplier);
        return this;
    }

    public JCondition reference(Identifier name) {
        getOrCreateParameters();

        this.parameters.addProperty("name", name.toString());
        return this;
    }

    public JCondition tableBonus(Identifier enchantment, float... chances) {
        getOrCreateParameters();

        this.parameters.addProperty("enchantment", enchantment.toString());
        this.parameters.add("chances", jsonSerializationContext.serialize(chances));

        return this;
    }

    public JCondition timeCheck(long period, UniformLootTableRange value) {
        getOrCreateParameters();

        this.parameters.addProperty("period", period);
        this.parameters.add("value", this.jsonSerializationContext.serialize(value));

        return this;
    }

    public JCondition weatherCheck(boolean raining, boolean thundering) {
        getOrCreateParameters();

        this.parameters.addProperty("raining", raining);
        this.parameters.addProperty("thundering", thundering);
        return this;
    }

    public JCondition damageSource(DamageSourcePredicate predicate) {
        getOrCreateParameters();

        this.parameters.add("predicate", predicate.toJson());
        return this;
    }

    public JCondition entityScores(Map<String, UniformLootTableRange> scores, LootContext.EntityTarget target) {
        JsonObject jsonScores = new JsonObject();

        scores.forEach((key, value) -> jsonScores.add(key, jsonSerializationContext.serialize(value)));

        getOrCreateParameters();

        this.parameters.add("scores", jsonScores);
        this.parameters.add("entity", jsonSerializationContext.serialize(target));

        return this;
    }

    public JCondition matchTool(ItemPredicate predicate) {
        this.predicate = predicate.toJson();

        return this;
    }

    public JCondition locationCheck(LocationPredicate predicate, BlockPos offset) {
        this.predicate = predicate.toJson();

        getOrCreateParameters();

        if (offset.getX() != 0) this.parameters.addProperty("offsetX", offset.getX());
        if (offset.getY() != 0) this.parameters.addProperty("offsetY", offset.getY());
        if (offset.getZ() != 0) this.parameters.addProperty("offsetZ", offset.getZ());

        return this;
    }

    public void getOrCreateParameters() {
        if (this.parameters == null) {
            this.parameters = new JsonObject();
        }
    }

    private void getJsonSerializationContext(JsonSerializationContext receivedContext) {
        this.jsonSerializationContext = receivedContext;
    }

    @Override
    public JCondition clone() {
        try {
            return (JCondition) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    public static class Serializer implements JsonSerializer<JCondition> {
        @Override
        public JsonElement serialize(JCondition src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject finalCondition = new JsonObject();

            finalCondition.add("condition", context.serialize(src.condition));

            if (src.parameters != null) {
                src.parameters.entrySet()
                        .forEach(entry -> finalCondition.add(entry.getKey(), entry.getValue()));
            }

            finalCondition.add("predicate", src.predicate);

            return finalCondition;
        }
    }
}
