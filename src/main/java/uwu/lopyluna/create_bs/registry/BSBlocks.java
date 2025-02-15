package uwu.lopyluna.create_bs.registry;

import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SharedProperties;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import uwu.lopyluna.create_bs.CreateBS;
import uwu.lopyluna.create_bs.content.TieredBlockList;
import uwu.lopyluna.create_bs.content.vault.TieredVaultBlock;
import uwu.lopyluna.create_bs.content.vault.TieredVaultCTBehaviour;
import uwu.lopyluna.create_bs.content.vault.TieredVaultItem;

import static com.simibubi.create.foundation.data.CreateRegistrate.connectedTextures;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;
import static uwu.lopyluna.create_bs.CreateBS.REGISTRATE;

@SuppressWarnings("removal")
public class BSBlocks {

    public static final TieredBlockList<TieredVaultBlock> VAULTS = new TieredBlockList<>(tier -> {
        if (!tier.valid) return null;
        String tierID = tier.name.toLowerCase();
        var block = REGISTRATE.block(tierID + "_item_vault", p -> new TieredVaultBlock(p, tier));
        block.initialProperties(SharedProperties::softMetal).properties(p -> p.mapColor(tier.mapColor).sound(tier.soundType).explosionResistance(1200));
        if (tier.seeThrough) block.properties(p -> p
                        .instrument(NoteBlockInstrument.HAT)
                        .noOcclusion()
                        .isValidSpawn(BSBlocks::never)
                        .isRedstoneConductor(BSBlocks::never)
                        .isSuffocating(BSBlocks::never)
                        .isViewBlocking(BSBlocks::never))
                .addLayer(() -> RenderType::cutout);
        block.transform(pickaxeOnly())
                .blockstate((c, p) -> {
                    ResourceLocation bottom = CreateBS.asResource("block/"+tierID+"_vault/vault_bottom_small");
                    ResourceLocation front = CreateBS.asResource("block/"+tierID+"_vault/vault_front_small");
                    ResourceLocation side = CreateBS.asResource("block/"+tierID+"_vault/vault_side_small");
                    ResourceLocation top = CreateBS.asResource("block/"+tierID+"_vault/vault_top_small");
                    ResourceLocation particle = CreateBS.asResource("block/"+tierID+"_vault/vault_top_small");

                    ResourceLocation refModel = CreateBS.asResource("block/item_vault");

                    p.models().withExistingParent("block/" + c.getName(), refModel)
                            .texture("0", bottom)
                            .texture("1", front)
                            .texture("2", side)
                            .texture("3", top)
                            .texture("particle", particle);
                    p.getVariantBuilder(c.get())
                            .forAllStates(s -> ConfiguredModel.builder()
                                    .modelFile(AssetLookup.standardModel(c, p))
                                    .rotationY(s.getValue(TieredVaultBlock.HORIZONTAL_AXIS) == Direction.Axis.X ? 90 : 0)
                                    .build());
                })
                .onRegister(connectedTextures(() -> new TieredVaultCTBehaviour(tier)))
                .item((b, p) -> new TieredVaultItem(b, p, tier))
                .build();
        return block.register();
    });

    private static boolean never(BlockState s, BlockGetter l, BlockPos p) {return false;}
    private static Boolean never(BlockState s, BlockGetter l, BlockPos p, EntityType<?> t) {return false;}

    public static void register() {}
}
