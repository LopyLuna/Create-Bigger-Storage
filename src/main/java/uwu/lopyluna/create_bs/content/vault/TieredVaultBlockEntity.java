package uwu.lopyluna.create_bs.content.vault;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.VersionedInventoryWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uwu.lopyluna.create_bs.content.TierMaterials;
import uwu.lopyluna.create_bs.registry.BSBlockEntities;

import java.util.List;
import java.util.Objects;

public class TieredVaultBlockEntity extends SmartBlockEntity implements IMultiBlockEntityContainer.Inventory {

    protected IItemHandler itemHandlerCache;

    protected ItemStackHandler inventory;
    protected BlockPos controller;
    protected BlockPos lastKnownPos;
    protected boolean updateConnectivity;
    protected int radius;
    protected int length;
    protected Direction.Axis axis;
    protected final TierMaterials tierMaterials;

    public TieredVaultBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, TierMaterials tierMaterials) {
        super(type, pos, state);
        this.tierMaterials = tierMaterials;

        inventory = new ItemStackHandler(tierMaterials.capacity) {
            @Override
            protected void onContentsChanged(int slot) {
                super.onContentsChanged(slot);
                updateComparators();
                setChanged();
                sendData();
            }
        };

        itemHandlerCache = null;
        radius = 1;
        length = 1;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    protected void updateConnectivity() {
        updateConnectivity = false;
        if (level == null || level.isClientSide()) return;
        if (!isController()) return;
        ConnectivityHandler.formMulti(this);
    }

    protected void updateComparators() {
        TieredVaultBlockEntity controllerBE = getControllerBE();
        if (controllerBE == null || level == null) return;

        level.blockEntityChanged(controllerBE.worldPosition);

        BlockPos pos = controllerBE.getBlockPos();
        for (int y = 0; y < controllerBE.radius; y++) {
            for (int z = 0; z < (controllerBE.axis == Direction.Axis.X ? controllerBE.radius : controllerBE.length); z++) {
                for (int x = 0; x < (controllerBE.axis == Direction.Axis.Z ? controllerBE.radius : controllerBE.length); x++) {
                    level.updateNeighbourForOutputSignal(pos.offset(x, y, z), getBlockState().getBlock());
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        if (lastKnownPos == null) {
            lastKnownPos = getBlockPos();
        } else if (!lastKnownPos.equals(worldPosition)) {
            onPositionChanged();
            return;
        }

        if (updateConnectivity) {
            updateConnectivity();
        }
    }

    @Override
    public BlockPos getLastKnownPos() {
        return lastKnownPos;
    }

    @Override
    public boolean isController() {
        return controller == null || worldPosition.equals(controller);
    }

    private void onPositionChanged() {
        removeController(true);
        lastKnownPos = worldPosition;
    }

    @Override
    @SuppressWarnings("unchecked")
    public TieredVaultBlockEntity getControllerBE() {
        if (isController() || level == null) return this;
        BlockEntity blockEntity = level.getBlockEntity(controller);
        if (blockEntity instanceof TieredVaultBlockEntity vault) return vault;
        return null;
    }

    public void removeController(boolean keepContents) {
        if (level == null || level.isClientSide()) return;

        updateConnectivity = true;
        controller = null;
        radius = 1;
        length = 1;

        BlockState state = getBlockState();
        if (TieredVaultBlock.isVault(state, tierMaterials)) {
            state = state.setValue(TieredVaultBlock.LARGE, false);
            level.setBlock(worldPosition, state, 22);
        }

        itemHandlerCache = null;
        setChanged();
        sendData();
    }

    @Override
    public void setController(BlockPos controller) {
        if (level == null || (level.isClientSide && !isVirtual())) return;
        if (controller.equals(this.controller)) return;

        this.controller = controller;
        itemHandlerCache = null;
        setChanged();
        sendData();
    }

    @Override
    public BlockPos getController() {
        return isController() ? worldPosition : controller;
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);

        BlockPos controllerBefore = controller;
        int prevSize = radius;
        int prevLength = length;

        updateConnectivity = compound.contains("Uninitialized");
        controller = null;
        lastKnownPos = null;

        if (compound.contains("LastKnownPos")) {
    lastKnownPos = NbtUtils.readBlockPos(compound, "LastKnownPos").orElse(null);
        }
        if (compound.contains("Controller")) {
    controller = NbtUtils.readBlockPos(compound, "Controller").orElse(null);
        }

        if (isController()) {
            radius = compound.getInt("Size");
            length = compound.getInt("Length");
        }

        if (compound.contains("Inventory")) {
            inventory.deserializeNBT(registries, compound.getCompound("Inventory"));
        }

        itemHandlerCache = null;

        if (!clientPacket) return;

        boolean changeOfController = !Objects.equals(controllerBefore, controller);
        if (level != null && (changeOfController || prevSize != radius || prevLength != length)) {
            level.setBlocksDirty(getBlockPos(), Blocks.AIR.defaultBlockState(), getBlockState());
        }
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        if (updateConnectivity) {
            compound.putBoolean("Uninitialized", true);
        }
        if (lastKnownPos != null) {
            compound.put("LastKnownPos", NbtUtils.writeBlockPos(lastKnownPos));
        }
        if (!isController() && controller != null) {
            compound.put("Controller", NbtUtils.writeBlockPos(controller));
        }
        if (isController()) {
            compound.putInt("Size", radius);
            compound.putInt("Length", length);
        }

        super.write(compound, registries, clientPacket);
        compound.put("Inventory", inventory.serializeNBT(registries));

        if (!clientPacket) {
            compound.putString("StorageType", "CombinedInv");
        }
    }

    public ItemStackHandler getInventoryOfBlock() {
        return inventory;
    }

    public void applyInventoryToBlock(ItemStackHandler handler) {
        for (int i = 0; i < inventory.getSlots(); i++) {
            inventory.setStackInSlot(i, i < handler.getSlots() ? handler.getStackInSlot(i) : ItemStack.EMPTY);
        }
        itemHandlerCache = null;
    }

    /**
     * NeoForge 1.21 block capabilities are provided through registration rather than overriding
     * getCapability on the block entity. Register this method as the provider target later.
     */
    public @Nullable IItemHandler getItemHandler(@Nullable Direction side) {
        initCapabilityCache();
        return itemHandlerCache;
    }

    private void initCapabilityCache() {
        if (itemHandlerCache != null) return;

        if (!isController()) {
            TieredVaultBlockEntity controllerBE = getControllerBE();
            if (controllerBE == null) return;
            controllerBE.initCapabilityCache();
            itemHandlerCache = controllerBE.itemHandlerCache;
            return;
        }

        boolean alongZ = TieredVaultBlock.getVaultBlockAxis(getBlockState(), tierMaterials) == Direction.Axis.Z;
        IItemHandlerModifiable[] invs = new IItemHandlerModifiable[length * radius * radius];

        for (int yOffset = 0; yOffset < length; yOffset++) {
            for (int xOffset = 0; xOffset < radius; xOffset++) {
                for (int zOffset = 0; zOffset < radius; zOffset++) {
                    BlockPos vaultPos = alongZ
                            ? worldPosition.offset(xOffset, zOffset, yOffset)
                            : worldPosition.offset(yOffset, xOffset, zOffset);

                    TieredVaultBlockEntity vaultAt = null;
                    if (level != null) {
                        vaultAt = ConnectivityHandler.partAt(
                                BSBlockEntities.VAULTS.get(tierMaterials).get(),
                                level,
                                vaultPos
                        );
                    }

                    invs[yOffset * radius * radius + xOffset * radius + zOffset] =
                            vaultAt != null ? vaultAt.inventory : new ItemStackHandler();
                }
            }
        }

        itemHandlerCache = new VersionedInventoryWrapper(new CombinedInvWrapper(invs));
    }

    public static int getMaxLength(int radius, TierMaterials tier) {
        return radius * tier.multiplierLength;
    }

    @Override
    public void preventConnectivityUpdate() {
        updateConnectivity = false;
    }

    @Override
    public void notifyMultiUpdated() {
        BlockState state = getBlockState();
        if (level != null && TieredVaultBlock.isVault(state, tierMaterials)) {
            level.setBlock(getBlockPos(), state.setValue(TieredVaultBlock.LARGE, radius > 2), 6);
        }
        itemHandlerCache = null;
        setChanged();
    }

    @Override
    public Direction.Axis getMainConnectionAxis() {
        return getMainAxisOf(this);
    }

    @Override
    public int getMaxLength(Direction.Axis longAxis, int width) {
        if (longAxis == Direction.Axis.Y) return getMaxWidth();
        return getMaxLength(width, tierMaterials);
    }

    @Override
    public int getMaxWidth() {
        return 3;
    }

    @Override
    public int getHeight() {
        return length;
    }

    @Override
    public int getWidth() {
        return radius;
    }

    @Override
    public void setHeight(int height) {
        this.length = height;
    }

    @Override
    public void setWidth(int width) {
        this.radius = width;
    }

    @Override
    public boolean hasInventory() {
        return true;
    }
}