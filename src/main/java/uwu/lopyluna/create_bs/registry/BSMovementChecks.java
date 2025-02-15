package uwu.lopyluna.create_bs.registry;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.contraptions.BlockMovementChecks;
import uwu.lopyluna.create_bs.content.vault.TieredVaultBlock;

public class BSMovementChecks {
    public static void register() {
        BlockMovementChecks.registerAttachedCheck((state, world, pos, direction) -> {
            if (state.getBlock() instanceof TieredVaultBlock && ConnectivityHandler.isConnected(world, pos, pos.relative(direction))) return BlockMovementChecks.CheckResult.SUCCESS;
            return BlockMovementChecks.CheckResult.PASS;
        });
    }
}
