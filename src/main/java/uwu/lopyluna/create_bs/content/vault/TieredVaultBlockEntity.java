package uwu.lopyluna.create_bs.content.vault;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.inventory.VersionedInventoryWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.CombinedInvWrapper;
import org.jetbrains.annotations.NotNull;
import uwu.lopyluna.create_bs.content.TierMaterials;
import uwu.lopyluna.create_bs.registry.BSBlockEntities;

import java.util.*;

public class TieredVaultBlockEntity extends SmartBlockEntity implements IMultiBlockEntityContainer.Inventory {

    protected LazyOptional<IItemHandler> itemCapability;

    protected ItemStackHandler inventory;
    protected BlockPos controller;
    protected BlockPos lastKnownPos;
    protected boolean updateConnectivity;
    protected int radius;
    protected int length;
    protected Direction.Axis axis;
    TierMaterials tierMaterials;

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

        itemCapability = LazyOptional.empty();
        radius = 1;
        length = 1;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {}

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
        for (int y = 0; y < controllerBE.radius; y++) for (int z = 0; z < (controllerBE.axis == Direction.Axis.X ? controllerBE.radius : controllerBE.length); z++) for (int x = 0; x < (controllerBE.axis == Direction.Axis.Z ? controllerBE.radius : controllerBE.length); x++)
            level.updateNeighbourForOutputSignal(pos.offset(x, y, z), getBlockState().getBlock());
    }

    @Override
    public void tick() {
        super.tick();

        if (lastKnownPos == null) lastKnownPos = getBlockPos();
        else if (!lastKnownPos.equals(worldPosition)) {
            onPositionChanged();
            return;
        }

        if (updateConnectivity) updateConnectivity();
    }

    @Override
    public BlockPos getLastKnownPos() {
        return lastKnownPos;
    }

    @Override
    public boolean isController() {
        return controller == null ||
                worldPosition.getX() == controller.getX() &&
                worldPosition.getY() == controller.getY() &&
                worldPosition.getZ() == controller.getZ();
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
        if (blockEntity instanceof TieredVaultBlockEntity) return (TieredVaultBlockEntity) blockEntity;
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

        itemCapability.invalidate();
        setChanged();
        sendData();
    }

    @Override
    public void setController(BlockPos controller) {
        if (level == null || (level.isClientSide && !isVirtual())) return;
        if (controller.equals(this.controller)) return;
        this.controller = controller;
        itemCapability.invalidate();
        setChanged();
        sendData();
    }

    @Override
    public BlockPos getController() {
        return isController() ? worldPosition : controller;
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);

        BlockPos controllerBefore = controller;
        int prevSize = radius;
        int prevLength = length;

        updateConnectivity = compound.contains("Uninitialized");
        controller = null;
        lastKnownPos = null;

        if (compound.contains("LastKnownPos")) lastKnownPos = NbtUtils.readBlockPos(compound.getCompound("LastKnownPos"));
        if (compound.contains("Controller")) controller = NbtUtils.readBlockPos(compound.getCompound("Controller"));

        if (isController()) {
            radius = compound.getInt("Size");
            length = compound.getInt("Length");
        }

        if (compound.contains("Inventory")) inventory.deserializeNBT(compound.getCompound("Inventory"));

        if (!clientPacket)
            return;

        boolean changeOfController = !Objects.equals(controllerBefore, controller);
        if (level != null && (changeOfController || prevSize != radius || prevLength != length))
            level.setBlocksDirty(getBlockPos(), Blocks.AIR.defaultBlockState(), getBlockState());
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        if (updateConnectivity) compound.putBoolean("Uninitialized", true);
        if (lastKnownPos != null) compound.put("LastKnownPos", NbtUtils.writeBlockPos(lastKnownPos));
        if (!isController()) compound.put("Controller", NbtUtils.writeBlockPos(controller));
        if (isController()) {
            compound.putInt("Size", radius);
            compound.putInt("Length", length);
        }
        super.write(compound, clientPacket);
        compound.put("Inventory", inventory.serializeNBT());
        if (!clientPacket) compound.putString("StorageType", "CombinedInv");
    }

    public ItemStackHandler getInventoryOfBlock() {
        return inventory;
    }

    public void applyInventoryToBlock(ItemStackHandler handler) {
        for (int i = 0; i < inventory.getSlots(); i++) inventory.setStackInSlot(i, i < handler.getSlots() ? handler.getStackInSlot(i) : ItemStack.EMPTY);
    }

    @Override
    public <T> @NotNull LazyOptional<T> getCapability(@NotNull Capability<T> cap, Direction side) {
        if (isItemHandlerCap(cap)) {
            initCapability();
            return itemCapability.cast();
        }
        return super.getCapability(cap, side);
    }

    private void initCapability() {
        if (itemCapability.isPresent())
            return;
        if (!isController()) {
            TieredVaultBlockEntity controllerBE = getControllerBE();
            if (controllerBE == null)
                return;
            controllerBE.initCapability();
            itemCapability = controllerBE.itemCapability;
            return;
        }

        boolean alongZ = TieredVaultBlock.getVaultBlockAxis(getBlockState(), tierMaterials) == Direction.Axis.Z;
        IItemHandlerModifiable[] invs = new IItemHandlerModifiable[length * radius * radius];
        for (int yOffset = 0; yOffset < length; yOffset++) for (int xOffset = 0; xOffset < radius; xOffset++) for (int zOffset = 0; zOffset < radius; zOffset++) {
            BlockPos vaultPos = alongZ ? worldPosition.offset(xOffset, zOffset, yOffset)
                    : worldPosition.offset(yOffset, xOffset, zOffset);
            TieredVaultBlockEntity vaultAt = null;
            if (level != null) vaultAt = ConnectivityHandler.partAt(BSBlockEntities.VAULTS.get(tierMaterials).get(), level, vaultPos);
            invs[yOffset * radius * radius + xOffset * radius + zOffset] = vaultAt != null ? vaultAt.inventory : new ItemStackHandler();
        }
        IItemHandler itemHandler = new VersionedInventoryWrapper(new CombinedInvWrapper(invs));
        itemCapability = LazyOptional.of(() -> itemHandler);
    }

    public static int getMaxLength(int radius, TierMaterials tier) {
        return radius * tier.multiplierLength;
    }

    @Override
    public void preventConnectivityUpdate() { updateConnectivity = false; }

    @Override
    public void notifyMultiUpdated() {
        BlockState state = this.getBlockState();
        if (level != null && TieredVaultBlock.isVault(state, tierMaterials)) level.setBlock(getBlockPos(), state.setValue(TieredVaultBlock.LARGE, radius > 2), 6);
        itemCapability.invalidate();
        setChanged();
    }

    @Override
    public Direction.Axis getMainConnectionAxis() { return getMainAxisOf(this); }

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
    public int getHeight() { return length; }

    @Override
    public int getWidth() { return radius; }

    @Override
    public void setHeight(int height) { this.length = height; }

    @Override
    public void setWidth(int width) { this.radius = width; }

    @Override
    public boolean hasInventory() { return true; }
}
