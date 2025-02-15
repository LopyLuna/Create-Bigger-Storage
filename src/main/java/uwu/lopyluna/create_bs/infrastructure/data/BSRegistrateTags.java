package uwu.lopyluna.create_bs.infrastructure.data;

import com.simibubi.create.foundation.data.TagGen;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import uwu.lopyluna.create_bs.CreateBS;
import uwu.lopyluna.create_bs.registry.BSBlocks;

import java.util.Collections;
import java.util.Objects;

import static uwu.lopyluna.create_bs.content.TierMaterials.*;

@SuppressWarnings({"deprecation", "SameParameterValue"})
public class BSRegistrateTags {

	public static void addGenerators() {
		CreateBS.REGISTRATE.addDataGenerator(ProviderType.BLOCK_TAGS, BSRegistrateTags::genBlockTags);
		CreateBS.REGISTRATE.addDataGenerator(ProviderType.ITEM_TAGS, BSRegistrateTags::genItemTags);
	}
	private static void genItemTags(RegistrateTagsProvider<Item> provIn) {
		TagGen.CreateTagsProvider<Item> prov = new TagGen.CreateTagsProvider<>(provIn, Item::builtInRegistryHolder);

		prov.tag(createTagItem("vaults", true))
				.add(BSBlocks.VAULTS.get(WOOD).asItem())
				.add(BSBlocks.VAULTS.get(COPPER).asItem())
				.add(BSBlocks.VAULTS.get(IRON).asItem())
				.add(BSBlocks.VAULTS.get(EMERALD).asItem())
				.add(BSBlocks.VAULTS.get(GOLD).asItem())
				.add(BSBlocks.VAULTS.get(CRYSTAL).asItem())
				.add(BSBlocks.VAULTS.get(DIAMOND).asItem())
				.add(BSBlocks.VAULTS.get(OBSIDIAN).asItem())
				.add(BSBlocks.VAULTS.get(NETHERITE).asItem())
		;
	}

	private static void genBlockTags(RegistrateTagsProvider<Block> prov) {
	}


	private static TagKey<Item> createTagItem(String path, boolean optional) {
		ResourceLocation id = new ResourceLocation(CreateBS.MOD_ID, path);
		if (optional) return optionalTag(ForgeRegistries.ITEMS, id);
		else return ItemTags.create(id);
	}

	public static <T> TagKey<T> optionalTag(IForgeRegistry<T> registry, ResourceLocation id) {
		return Objects.requireNonNull(registry.tags()).createOptionalTagKey(id, Collections.emptySet());
	}
}
