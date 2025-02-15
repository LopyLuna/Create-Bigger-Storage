package uwu.lopyluna.create_bs;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.tterrag.registrate.fabric.EnvExecutor;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.resources.ResourceLocation;
import uwu.lopyluna.create_bs.content.TierMaterials;
import uwu.lopyluna.create_bs.registry.*;

@SuppressWarnings("unused")
public class CreateBS implements ModInitializer {
    public static final String NAME = "Create: Better Storages";
    public static final String MOD_ID = "create_bs";

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID);
    static {
        REGISTRATE.setTooltipModifierFactory(item -> new ItemDescription.Modifier(item, TooltipHelper.Palette.STANDARD_CREATE));
    }

	@Override
	public void onInitialize() {
		EnvExecutor.runWhenOn(EnvType.CLIENT, () -> BSSpriteShifts::register);

		BSBlocks.register();
		BSBlockEntities.register();
		BSMovementChecks.register();

		REGISTRATE.register();

		ItemGroupEvents.modifyEntriesEvent(AllCreativeModeTabs.BASE_CREATIVE_TAB.key()).register(entries -> {
			for (TierMaterials tier : TierMaterials.values()) {
				if (!tier.valid) continue;
				entries.addBefore(AllBlocks.ITEM_VAULT.asStack(), BSBlocks.VAULTS.get(tier).asStack());
			}
		});
	}

    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
