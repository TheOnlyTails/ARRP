package net.devtech.arrp.json.loot;

import com.google.gson.*;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.map.MapIcon;
import net.minecraft.loot.LootTableRange;
import net.minecraft.loot.LootTableRanges;
import net.minecraft.loot.UniformLootTableRange;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.function.ApplyBonusLootFunction;
import net.minecraft.loot.function.CopyNameLootFunction;
import net.minecraft.loot.function.CopyNbtLootFunction;
import net.minecraft.loot.function.ExplorationMapLootFunction;
import net.minecraft.loot.operator.BoundedIntUnaryOperator;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.StructureFeature;

import java.lang.reflect.Type;
import java.util.*;

@SuppressWarnings("ConstantConditions")
public class JFunction implements Cloneable {
    private final JsonObject properties = new JsonObject();
    private Identifier function;
    private List<JCondition> conditions;
    private JsonSerializationContext jsonSerializationContext;

    /**
     * @see JLootTable#function(Identifier)
     */
    public JFunction(Identifier function) {
        this.function = function;
    }

    /**
     * @deprecated use {@link JLootTable#function(Identifier)}
     */
    public JFunction(String function) {
        this.properties.addProperty("function", function);
    }

    /**
     * Adds a new property to the function.
     *
     * @param key      sets the key to be used in the property.
     * @param property sets the property's value.
     * @return the function with the property added.
     * @deprecated use {@link #add(String, JsonElement)}
     */
    @Deprecated
    public JFunction add(String key, Object property) {
        this.properties.addProperty(key, property.toString());
        return this;
    }

    /**
     * Adds a new property to the function.
     *
     * @param key      sets the key to be used in the property.
     * @param property sets the property's value.
     * @return the function with the property added.
     */
    public JFunction add(String key, JsonElement property) {
        this.properties.add(key, property);
        return this;
    }

    public JFunction setCount(LootTableRange countRange) {
        this.properties.add("count", LootTableRanges.toJson(countRange, jsonSerializationContext));
        return this;
    }

    public JFunction enchantWithLevels(LootTableRange range, boolean treasureEnchantmentsAllowed) {
        this.properties.add("levels", LootTableRanges.toJson(range, jsonSerializationContext));
        this.properties.addProperty("treasure", treasureEnchantmentsAllowed);
        return this;
    }

    public JFunction enchantRandomly(List<Enchantment> enchantments) {
        if (!enchantments.isEmpty()) {
            JsonArray jsonArray = new JsonArray();

            enchantments.forEach(enchantment -> {
                Identifier identifier = Registry.ENCHANTMENT.getId(enchantment);
                if (identifier == null) {
                    throw new IllegalArgumentException("Don't know how to serialize enchantment " + enchantment);
                }

                jsonArray.add(new JsonPrimitive(identifier.toString()));
            });


            this.properties.add("enchantments", jsonArray);
        }

        return this;
    }

    public JFunction setNbtLoot(CompoundTag tag) {
        this.properties.addProperty("tag", tag.toString());
        return this;
    }

    public JFunction lootingEnchant(UniformLootTableRange countRange, int limit) {
        this.properties.add("count", jsonSerializationContext.serialize(countRange));
        if (limit > 0) this.properties.add("limit", jsonSerializationContext.serialize(limit));
        return this;
    }

    public JFunction setDamage(UniformLootTableRange durabilityRange) {
        this.properties.add("count", jsonSerializationContext.serialize(durabilityRange));
        return this;
    }

    public JFunction setAttributes(List<SetAttributesAttribute> attributes) {
        JsonArray jsonAttributes = new JsonArray();

        attributes.forEach(attribute ->
                jsonAttributes.add(attribute.serialize(jsonSerializationContext))
        );

        this.properties.add("modifiers", jsonAttributes);

        return this;
    }

    public JFunction setName(Text name, LootContext.EntityTarget entity) {
        if (name != null) {
            this.properties.add("name", Text.Serializer.toJsonTree(name));
        }

        if (entity != null) {
            this.properties.add("entity", jsonSerializationContext.serialize(entity));
        }

        return this;
    }

    public JFunction explorationMap(StructureFeature<?> destination,
                                    MapIcon.Type decoration,
                                    byte zoom,
                                    int searchRadius,
                                    boolean skipExistingChunks) {
        StructureFeature<?> buriedTreasure = StructureFeature.BURIED_TREASURE;
        MapIcon.Type defaultDecoration = MapIcon.Type.MANSION;

        if (!destination.equals(buriedTreasure))
            this.properties.add("destination", jsonSerializationContext.serialize(destination.getName()));

        if (decoration != ExplorationMapLootFunction.DEFAULT_DECORATION)
            this.properties.add("decoration", jsonSerializationContext.serialize(decoration.toString()
                    .toLowerCase(Locale.ROOT)));

        if (zoom != 2) this.properties.addProperty("zoom", zoom);

        if (searchRadius != 50) this.properties.addProperty("search_radius", searchRadius);

        if (!skipExistingChunks) this.properties.addProperty("skip_existing_chunks", skipExistingChunks);

        return this;
    }

    public JFunction setStewEffect(Map<StatusEffect, UniformLootTableRange> effects) {
        if (!effects.isEmpty()) {
            JsonArray jsonArray = new JsonArray();

            effects.keySet().forEach(effect -> {
                JsonObject jsonObject = new JsonObject();
                Identifier identifier = Registry.STATUS_EFFECT.getId(effect);
                if (identifier == null) {
                    throw new IllegalArgumentException("Don't know how to serialize mob effect " + effect);
                }

                jsonObject.add("type", new JsonPrimitive(identifier.toString()));
                jsonObject.add("duration", jsonSerializationContext.serialize(effects.get(effect)));
                jsonArray.add(jsonObject);
            });

            this.properties.add("effects", jsonArray);
        }

        return this;
    }

    public JFunction copyName(CopyNameLootFunction.Source source) {
        this.properties.addProperty("source", source.name);
        return this;
    }

    public JFunction setContents(List<JEntry> entries) {
        this.properties.add("entries", jsonSerializationContext.serialize(entries));
        return this;
    }

    public JFunction limitCount(BoundedIntUnaryOperator limit) {
        this.properties.add("limit", jsonSerializationContext.serialize(limit));
        return this;
    }

    public JFunction applyBonus(Enchantment enchantment, ApplyBonusLootFunction.Formula formula) {
        this.properties.addProperty("enchantment", Registry.ENCHANTMENT.getId(enchantment).toString());
        this.properties.addProperty("formula", formula.getId().toString());
        JsonObject jsonObject = new JsonObject();
        formula.toJson(jsonObject, jsonSerializationContext);

        if (jsonObject.size() > 0) {
            this.properties.add("parameters", jsonObject);
        }

        return this;
    }

    /**
     * Adds the set_loot_table function.
     *
     * @param id   the ID of the loot table.
     * @param seed the seed. set to 0L if you want it blank.
     * @return the function with the set_loot_table function's parameters added.
     */
    public JFunction setLootTable(Identifier id, long seed) {
        this.properties.addProperty("name", id.toString());
        if (seed != 0L) {
            this.properties.addProperty("seed", seed);
        }

        return this;
    }

    public JFunction addLore(boolean replace, List<Text> lore, LootContext.EntityTarget entity) {
        this.properties.addProperty("replace", replace);
        JsonArray jsonArray = new JsonArray();

        lore.stream().map(Text.Serializer::toJsonTree).forEach(jsonArray::add);

        this.properties.add("lore", jsonArray);

        if (entity != null) {
            this.properties.add("entity", jsonSerializationContext.serialize(entity));
        }

        return this;
    }

    public JFunction fillPlayerHead(LootContext.EntityTarget entity) {
        this.properties.add("entity", jsonSerializationContext.serialize(entity));
        return this;
    }

    public JFunction copyNbt(CopyNbtLootFunction.Source source, List<CopyNbtLootFunction.Operation> operations) {
        this.properties.addProperty("source", source.name);

        JsonArray jsonArray = new JsonArray();
        operations.stream().map(CopyNbtLootFunction.Operation::toJson).forEach(jsonArray::add);
        this.properties.add("ops", jsonArray);

        return this;
    }

    public JFunction copyState(Block block, Set<Property<?>> properties) {
        this.properties.addProperty("block", Registry.BLOCK.getId(block).toString());

        JsonArray jsonArray = new JsonArray();
        properties.forEach(property -> jsonArray.add(property.getName()));
        this.properties.add("properties", jsonArray);

        return this;
    }

    /**
     * Adds a condition to a function.
     *
     * @param condition the condition.
     * @return the function, with the condition added.
     * @deprecated misleading name. use {@link #condition(JCondition)}
     */
    @Deprecated
    public JFunction add(JCondition condition) {
        getOrCreateConditions();
        this.conditions.add(condition);
        return this;
    }

    /**
     * Adds a condition to a function.
     *
     * @param condition the condition.
     * @return the function, with the condition added.
     */
    public JFunction condition(JCondition condition) {
        getOrCreateConditions();
        this.conditions.add(condition);
        return this;
    }

    private void getJsonSerializationContext(JsonSerializationContext context) {
        this.jsonSerializationContext = context;
    }

    private void getOrCreateConditions() {
        if (this.conditions == null) {
            this.conditions = new ArrayList<>();
        }
    }

    @Override
    public JFunction clone() {
        try {
            return (JFunction) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    public static class Serializer implements JsonSerializer<JFunction> {
        @Override
        public JsonElement serialize(JFunction src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject finalFunction = new JsonObject();

            src.getJsonSerializationContext(context);

            finalFunction.add("function", context.serialize(src.function));

            if (src.conditions != null) {
                finalFunction.add("conditions", context.serialize(src.conditions));
            }

            return finalFunction;
        }
    }

    static class SetAttributesAttribute {
        private final String name;
        private final EntityAttribute attribute;
        private final EntityAttributeModifier.Operation operation;
        private final UniformLootTableRange amountRange;
        private final UUID id;
        private final EquipmentSlot[] slots;

        private SetAttributesAttribute(String name,
                                       EntityAttribute entityAttribute,
                                       EntityAttributeModifier.Operation operation,
                                       UniformLootTableRange amountRange,
                                       EquipmentSlot[] slots,
                                       UUID id) {
            this.name = name;
            this.attribute = entityAttribute;
            this.operation = operation;
            this.amountRange = amountRange;
            this.id = id;
            this.slots = slots;
        }

        private static String getNameOperation(EntityAttributeModifier.Operation operation) {
            switch (operation) {
                case ADDITION:
                    return "addition";
                case MULTIPLY_BASE:
                    return "multiply_base";
                case MULTIPLY_TOTAL:
                    return "multiply_total";
                default:
                    throw new IllegalArgumentException("Unknown operation " + operation);
            }
        }

        @SuppressWarnings("ConstantConditions")
        public JsonObject serialize(JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("name", this.name);
            jsonObject.addProperty("attribute", Registry.ATTRIBUTE.getId(this.attribute).toString());
            jsonObject.addProperty("operation", getNameOperation(this.operation));
            jsonObject.add("amount", context.serialize(this.amountRange));

            if (this.id != null) {
                jsonObject.addProperty("id", this.id.toString());
            }

            if (this.slots.length == 1) {
                jsonObject.addProperty("slot", this.slots[0].getName());
            } else {
                JsonArray jsonArray = new JsonArray();

                Arrays.stream(this.slots).forEach(slot ->
                        jsonArray.add(new JsonPrimitive(slot.getName()))
                );

                jsonObject.add("slot", jsonArray);
            }

            return jsonObject;
        }
    }
}
