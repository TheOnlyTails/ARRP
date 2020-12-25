package net.devtech.arrp.json.loot;

import com.google.gson.*;
import net.minecraft.util.Identifier;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JEntry implements Cloneable {
    private List<JCondition> conditions;
    private List<JFunction> functions;
    private Identifier type;
    private Identifier name;
    private List<JEntry> children;
    private Boolean expand;
    private Integer weight;
    private Integer quality;

    /**
     * @see JLootTable#entry(Identifier)
     */
    public JEntry() {
    }

    /**
     * @see JLootTable#entry(Identifier)
     */
    public JEntry(Identifier type) {
        this.type = type;
    }

    public JEntry condition(JCondition condition) {
        if (this.conditions == null) {
            this.conditions = new ArrayList<>();
        }
        this.conditions.add(condition);
        return this;
    }

    /**
     * Sets the type of the entry.
     *
     * @param type the type.
     * @return the entry, with the type added.
     * @deprecated see {@link JEntry#JEntry(Identifier)}
     */
    @Deprecated
    public JEntry type(String type) {
        this.type = new Identifier(type);
        return this;
    }

    /**
     * Sets the type of the entry.
     *
     * @param type the type.
     * @return the entry, with the type added.
     * @deprecated see {@link JEntry#JEntry(Identifier)}
     */
    @Deprecated
    public JEntry type(Identifier type) {
        this.type = type;
        return this;
    }

    /**
     * Adds the name of the item of this entry.
     *
     * @param name the name of the item.
     * @return the entry with the name added.
     * @deprecated use {@link #name(Identifier)} instead.
     */
    @Deprecated
    public JEntry name(String name) {
        this.name = new Identifier(name);
        return this;
    }

    /**
     * Adds the name of the item of this entry.
     *
     * @param name the name of the item.
     * @return the entry with the name added.
     */
    public JEntry name(Identifier name) {
        this.name = name;
        return this;
    }

    /**
     * Creates a new child entry.
     *
     * @param child the child.
     * @return the entry, with the child added.
     * @deprecated use JEntry#child
     */
    @Deprecated
    public JEntry child(String child) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children.add(new JEntry().type("minecraft:item").name(child));
        return this;
    }

    /**
     * Creates a new child entry.
     *
     * @param child the child entry.
     * @return the entry, with the child added.
     */
    public JEntry child(JEntry child) {
        if (this.children == null) {
            this.children = new ArrayList<>();
        }
        this.children.add(child);
        return this;
    }

    public JEntry expand(Boolean expand) {
        this.expand = expand;
        return this;
    }

    public JEntry function(JFunction function) {
        if (this.functions == null) {
            this.functions = new ArrayList<>();
        }
        this.functions.add(function);
        return this;
    }

    public JEntry weight(Integer weight) {
        this.weight = weight;
        return this;
    }

    public JEntry quality(Integer quality) {
        this.quality = quality;
        return this;
    }

    @Override
    public JEntry clone() {
        try {
            return (JEntry) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    public static class Serializer implements JsonSerializer<JEntry> {
        @Override
        public JsonElement serialize(JEntry src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject finalEntry = new JsonObject();

            if (src.conditions != null) {
                JsonArray jsonConditions = new JsonArray();
                src.conditions.forEach(condition -> jsonConditions.add(context.serialize(condition)));
                finalEntry.add("conditions", jsonConditions);
            }

            if (src.functions != null) {
                JsonArray jsonConditions = new JsonArray();
                src.functions.forEach(function -> jsonConditions.add(context.serialize(function)));
                finalEntry.add("functions", jsonConditions);
            }

            if (src.type != null) {
                finalEntry.add("type", context.serialize(src.type));
            }

            if (src.name != null) {
                finalEntry.add("name", context.serialize(src.name));
            }

            if (src.children != null) {
                JsonArray jsonChildren = new JsonArray();
                src.children.forEach(child -> jsonChildren.add(context.serialize(child)));
                finalEntry.add("children", jsonChildren);
            }

            if (src.expand != null) {
                finalEntry.add("expand", context.serialize(src.expand));
            }

            if (src.weight != null) {
                finalEntry.add("weight", context.serialize(src.weight));
            }

            if (src.quality != null) {
                finalEntry.add("quality", context.serialize(src.quality));
            }

            return finalEntry;
        }
    }
}
