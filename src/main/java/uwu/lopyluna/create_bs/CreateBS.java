package uwu.lopyluna.create_bs;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllCreativeModeTabs;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import uwu.lopyluna.create_bs.content.vault.TieredVaultBlock;
import uwu.lopyluna.create_bs.infrastructure.data.BSDatagen;
import uwu.lopyluna.create_bs.registry.*;

import static net.minecraft.world.item.CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS;

@SuppressWarnings("unused")
@Mod(CreateBS.MOD_ID)
public class CreateBS {
    public static final String NAME = "Create: Better Storages";
    public static final String MOD_ID = "create_bs";

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID);
    static {
        REGISTRATE.setTooltipModifierFactory(item -> new ItemDescription.Modifier(item, TooltipHelper.Palette.STANDARD_CREATE));
    }

    public CreateBS() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        REGISTRATE.registerEventListeners(modEventBus);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> BSSpriteShifts::register);

        BSBlocks.register();
        BSBlockEntities.register();
        BSMovementChecks.register();

        modEventBus.addListener(this::addCreative);
        modEventBus.addListener(EventPriority.LOWEST, (GatherDataEvent event) -> BSDatagen.gatherData());
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(AllCreativeModeTabs.BASE_CREATIVE_TAB.getKey())) for (BlockEntry<TieredVaultBlock> entry : BSBlocks.VAULTS)
                event.getEntries().putBefore(AllBlocks.ITEM_VAULT.asStack(), entry.asStack(), PARENT_AND_SEARCH_TABS);
    }

    public static ResourceLocation asResource(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
