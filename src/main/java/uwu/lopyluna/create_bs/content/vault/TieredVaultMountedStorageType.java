package uwu.lopyluna.create_bs.content.vault;

import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TieredVaultMountedStorageType extends MountedItemStorageType<TieredVaultMountedStorage> {
	public TieredVaultMountedStorageType() {
		super(TieredVaultMountedStorage.CODEC);
	}

	@Override
	@Nullable
	public TieredVaultMountedStorage mount(Level level, BlockState state, BlockPos pos, @Nullable BlockEntity be) {
		return be instanceof TieredVaultBlockEntity vault ? TieredVaultMountedStorage.fromVault(vault) : null;
	}
}
