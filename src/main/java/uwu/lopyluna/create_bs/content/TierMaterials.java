package uwu.lopyluna.create_bs.content;

import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

public enum TierMaterials {
    NONE(0, 0, "", Items.AIR, false, false, null, 0, 0, SoundType.EMPTY),
    WOOD(1, 0, "Wooden", ItemTags.PLANKS, false, true, MapColor.WOOD, 2, 10, SoundType.WOOD),

    COPPER(2, 1, "Copper", Items.COPPER_INGOT, false, true, MapColor.TERRACOTTA_ORANGE, 3, 30, SoundType.COPPER),
    IRON(3, 1, "Iron", Items.IRON_INGOT, false, true, MapColor.COLOR_GRAY, 4, 40, SoundType.NETHERITE_BLOCK),

    EMERALD(4, 2, "Emerald", Items.EMERALD, false, true, MapColor.EMERALD, 4, 50, SoundType.METAL),
    GOLD(5, 2, "Gold", Items.GOLD_INGOT, false, true, MapColor.GOLD, 5, 60, SoundType.NETHERITE_BLOCK),

    CRYSTAL(6, 3, "Crystal", Items.GLASS, true, true, MapColor.GLOW_LICHEN, 5, 70, SoundType.GLASS),
    DIAMOND(7, 3, "Diamond", Items.DIAMOND, false, true, MapColor.DIAMOND, 6, 80, SoundType.METAL),

    OBSIDIAN(8, 4, "Obsidian", Items.OBSIDIAN, false, true, MapColor.COLOR_BLACK, 6, 90, SoundType.NETHERITE_BLOCK),
    NETHERITE(9, 4, "Netherite", Items.NETHERITE_INGOT, false, true, MapColor.COLOR_BLACK, 7, 100, SoundType.NETHERITE_BLOCK);

    public final int level;
    public final int baseLevel;
    public final String name;
    public final Ingredient ingredient;
    public final boolean valid;
    public final boolean seeThrough;
    public final MapColor mapColor;
    public final int multiplierLength;
    public final int capacity;
    public final SoundType soundType;

    TierMaterials(int level, int baseLevel, String name, Ingredient ingredient, boolean seeThrough, boolean valid, MapColor mapColor, int multiplierLength, int capacity, SoundType soundType) {
        this.level = level;
        this.baseLevel = baseLevel;
        this.name = name;
        this.ingredient = ingredient;
        this.seeThrough = seeThrough;
        this.valid = valid;
        this.mapColor = mapColor;
        this.multiplierLength = multiplierLength;
        this.capacity = capacity;
        this.soundType = soundType;
    }


    TierMaterials(int level, int baseLevel, String name, TagKey<Item> tag, boolean seeThrough, boolean valid, MapColor mapColor, int multiplierLength, int capacity, SoundType soundType) {
        this(level, baseLevel, name, Ingredient.of(tag), seeThrough, valid, mapColor, multiplierLength, capacity, soundType);
    }
    TierMaterials(int level, int baseLevel, String name, ItemLike item, boolean seeThrough, boolean valid, MapColor mapColor, int multiplierLength, int capacity, SoundType soundType) {
        this(level, baseLevel, name, Ingredient.of(item), seeThrough, valid, mapColor, multiplierLength, capacity, soundType);
    }
}
