package uwu.lopyluna.create_bs.mixins;

import com.simibubi.create.content.contraptions.MountedStorage;

import com.simibubi.create.content.logistics.crate.BottomlessItemHandler;

import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandler;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import uwu.lopyluna.create_bs.content.vault.TieredVaultBlockEntity;

@Mixin(value = MountedStorage.class, remap = false)
public class MountedStorageMixin {

    @Shadow private BlockEntity blockEntity;
    @Shadow ItemStackHandler handler;
    @Shadow boolean valid;

	@Shadow boolean noFuel;

	@SuppressWarnings("all")
	@Inject(at = @At("HEAD"), method = "canUseAsStorage(Lnet/minecraft/world/level/block/entity/BlockEntity;)Z", cancellable = true)
    private static void BS$canUseAsStorage(BlockEntity be, CallbackInfoReturnable<Boolean> cir) {
        if (be != null && be instanceof TieredVaultBlockEntity)
			cir.setReturnValue(true);
    }

	@Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/world/level/block/entity/BlockEntity;)V")
	private void BS$MountedStorage(BlockEntity be, CallbackInfo ci) {
		noFuel = noFuel || be instanceof TieredVaultBlockEntity;
	}

    @Inject(at = @At("HEAD"), method = "removeStorageFromWorld()V", cancellable = true)
    private void BS$removeStorageFromWorld(CallbackInfo ci) {
        if (blockEntity != null && blockEntity instanceof TieredVaultBlockEntity be) {
            handler = be.getInventoryOfBlock();
            valid = true;
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "addStorageToWorld(Lnet/minecraft/world/level/block/entity/BlockEntity;)V", cancellable = true)
    private void BS$addStorageToWorld(BlockEntity blockEntity, CallbackInfo ci) {
        if (!(handler instanceof BottomlessItemHandler) && blockEntity instanceof TieredVaultBlockEntity be) {
            be.applyInventoryToBlock(handler);
            ci.cancel();
        }
    }
}
