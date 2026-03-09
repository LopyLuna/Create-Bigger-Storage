package uwu.lopyluna.create_bs.registry;

import com.simibubi.create.AllMountedStorageTypes;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import uwu.lopyluna.create_bs.CreateBS;
import uwu.lopyluna.create_bs.content.TieredBlockList;
import uwu.lopyluna.create_bs.content.vault.TieredVaultBlock;
import uwu.lopyluna.create_bs.content.vault.TieredVaultCTBehaviour;
import uwu.lopyluna.create_bs.content.vault.TieredVaultItem;
import uwu.lopyluna.create_bs.content.vault.TieredVaultMountedStorageType;

import static com.simibubi.create.api.contraption.storage.item.MountedItemStorageType.mountedItemStorage;
import static com.simibubi.create.foundation.data.CreateRegistrate.connectedTextures;
import static com.simibubi.create.foundation.data.TagGen.axeOrPickaxe;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;
import static uwu.lopyluna.create_bs.CreateBS.REGISTRATE;

@SuppressWarnings("removal")
public class BSBlocks {

    public static final RegistryEntry<MountedItemStorageType<?>, TieredVaultMountedStorageType> TIERED_VAULT =
            REGISTRATE.mountedItemStorage("tiered_vault", TieredVaultMountedStorageType::new).register();

    public static final TieredBlockList<TieredVaultBlock> VAULTS = new TieredBlockList<>(tier -> {
        if (!tier.valid) {
            return null;
        }

        String tierID = tier.name.toLowerCase();

        var block = REGISTRATE.block(tierID + "_item_vault", properties -> new TieredVaultBlock(properties, tier))
                .transform(mountedItemStorage(TIERED_VAULT));

        block.initialProperties(SharedProperties::softMetal)
                .properties(properties -> properties
                        .mapColor(tier.mapColor)
                        .sound(tier.soundType)
                        .explosionResistance(1200));

        if (tier.seeThrough) {
            block.properties(properties -> properties
                            .instrument(NoteBlockInstrument.HAT)
                            .noOcclusion()
                            .isValidSpawn(BSBlocks::never)
                            .isRedstoneConductor(BSBlocks::never)
                            .isSuffocating(BSBlocks::never)
                            .isViewBlocking(BSBlocks::never))
                    .addLayer(() -> RenderType::cutout);
        }

        if (tier.soundType.equals(SoundType.WOOD)) {
            block.transform(axeOrPickaxe());
        } else {
            block.transform(pickaxeOnly());
        }

        block.blockstate((context, provider) -> {
                    ResourceLocation bottom = CreateBS.asResource("block/" + tierID + "_vault/vault_bottom_small");
                    ResourceLocation front = CreateBS.asResource("block/" + tierID + "_vault/vault_front_small");
                    ResourceLocation side = CreateBS.asResource("block/" + tierID + "_vault/vault_side_small");
                    ResourceLocation top = CreateBS.asResource("block/" + tierID + "_vault/vault_top_small");
                    ResourceLocation particle = CreateBS.asResource("block/" + tierID + "_vault/vault_top_small");

                    ResourceLocation refModel = CreateBS.asResource("block/item_vault");

                    provider.models()
                            .withExistingParent("block/" + context.getName(), refModel)
                            .texture("0", bottom)
                            .texture("1", front)
                            .texture("2", side)
                            .texture("3", top)
                            .texture("particle", particle);

                    provider.getVariantBuilder(context.get())
                            .forAllStates(state -> ConfiguredModel.builder()
                                    .modelFile(AssetLookup.standardModel(context, provider))
                                    .rotationY(state.getValue(TieredVaultBlock.HORIZONTAL_AXIS) == Direction.Axis.X ? 90 : 0)
                                    .build());
                })
                .onRegister(connectedTextures(() -> new TieredVaultCTBehaviour(tier)))
                .item((blockEntry, properties) -> new TieredVaultItem(blockEntry, properties, tier))
                .build();

        return block.register();
    });

    private static boolean never(BlockState state, BlockGetter level, BlockPos pos) {
        return false;
    }

    private static Boolean never(BlockState state, BlockGetter level, BlockPos pos, EntityType<?> entityType) {
        return false;
    }

    public static void register() {
    }
}