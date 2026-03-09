package uwu.lopyluna.create_bs;

import com.simibubi.create.foundation.data.CreateRegistrate;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import uwu.lopyluna.create_bs.registry.BSBlockEntities;
import uwu.lopyluna.create_bs.registry.BSBlocks;
import uwu.lopyluna.create_bs.registry.BSSpriteShifts;

@Mod(CreateBS.MOD_ID)
public class CreateBS {
    public static final String MOD_ID = "create_bs";
    public static final String NAME = "Create Better Storages";

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MOD_ID);

    public CreateBS(IEventBus modBus, ModContainer container, Dist dist) {
        REGISTRATE.registerEventListeners(modBus);

        BSBlocks.register();
        BSBlockEntities.register();

        modBus.addListener(BSBlockEntities::registerCapabilities);

        if (dist == Dist.CLIENT) {
            BSSpriteShifts.register();
        }
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}