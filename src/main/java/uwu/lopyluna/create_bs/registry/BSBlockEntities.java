package uwu.lopyluna.create_bs.registry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import uwu.lopyluna.create_bs.content.TieredBEBlockList;
import uwu.lopyluna.create_bs.content.vault.SeeThroughVaultRenderer;
import uwu.lopyluna.create_bs.content.vault.TieredVaultBlockEntity;

import static uwu.lopyluna.create_bs.CreateBS.REGISTRATE;

public class BSBlockEntities {
    public static final TieredBEBlockList<TieredVaultBlockEntity> VAULTS = new TieredBEBlockList<>(tier -> {
        if (!tier.valid) return null;
        String tierID = tier.name.toLowerCase();
        var be = REGISTRATE.blockEntity(tierID + "_item_vault", (BlockEntityType<TieredVaultBlockEntity> t, BlockPos p, BlockState s) -> new TieredVaultBlockEntity(t, p, s, tier))
                .validBlocks(BSBlocks.VAULTS.get(tier));
        if (tier.seeThrough) be.renderer(() -> SeeThroughVaultRenderer::new);
        return be.register();
    });

    public static void register() {}
}
