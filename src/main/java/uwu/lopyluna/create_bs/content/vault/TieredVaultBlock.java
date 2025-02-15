package uwu.lopyluna.create_bs.content.vault;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.item.ItemHelper;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.ForgeSoundType;
import org.jetbrains.annotations.NotNull;
import uwu.lopyluna.create_bs.content.TierMaterials;
import uwu.lopyluna.create_bs.registry.BSBlockEntities;
import uwu.lopyluna.create_bs.registry.BSBlocks;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@SuppressWarnings("deprecation")
public class TieredVaultBlock extends Block implements IWrenchable, IBE<TieredVaultBlockEntity> {

    public static final Property<Direction.Axis> HORIZONTAL_AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    public static final BooleanProperty LARGE = BooleanProperty.create("large");
    TierMaterials tierMaterials;

    public TieredVaultBlock(Properties p_i48440_1_, TierMaterials tierMaterials) {
        super(p_i48440_1_);
        this.tierMaterials = tierMaterials;
        registerDefaultState(defaultBlockState().setValue(LARGE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(HORIZONTAL_AXIS, LARGE);
        super.createBlockStateDefinition(pBuilder);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        if (pContext.getPlayer() == null || !pContext.getPlayer()
                .isShiftKeyDown()) {
            BlockState placedOn = pContext.getLevel()
                    .getBlockState(pContext.getClickedPos()
                            .relative(pContext.getClickedFace()
                                    .getOpposite()));
            Direction.Axis preferredAxis = getVaultBlockAxis(placedOn, tierMaterials);
            if (preferredAxis != null)
                return this.defaultBlockState()
                        .setValue(HORIZONTAL_AXIS, preferredAxis);
        }
        return this.defaultBlockState()
                .setValue(HORIZONTAL_AXIS, pContext.getHorizontalDirection()
                        .getAxis());
    }

    @Override
    @ParametersAreNonnullByDefault
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        if (pOldState.getBlock() == pState.getBlock())
            return;
        if (pIsMoving)
            return;
        withBlockEntityDo(pLevel, pPos, TieredVaultBlockEntity::updateConnectivity);
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if (context.getClickedFace().getAxis().isVertical()) {
            BlockEntity be = context.getLevel().getBlockEntity(context.getClickedPos());
            if (be instanceof TieredVaultBlockEntity vault) {
                ConnectivityHandler.splitMulti(vault);
                vault.removeController(true);
            }
            state = state.setValue(LARGE, false);
        }
        return IWrenchable.super.onWrenched(state, context);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean pIsMoving) {
        if (state.hasBlockEntity() && (state.getBlock() != newState.getBlock() || !newState.hasBlockEntity())) {
            BlockEntity be = world.getBlockEntity(pos);
            if (!(be instanceof TieredVaultBlockEntity vaultBE))
                return;
            ItemHelper.dropContents(world, pos, vaultBE.inventory);
            world.removeBlockEntity(pos);
            ConnectivityHandler.splitMulti(vaultBE);
        }
    }

    public static boolean isVault(BlockState state, TierMaterials tier) {
        return BSBlocks.VAULTS.get(tier).has(state);
    }

    @Nullable
    public static Direction.Axis getVaultBlockAxis(BlockState state, TierMaterials tier) {
        if (!isVault(state, tier))
            return null;
        return state.getValue(HORIZONTAL_AXIS);
    }

    public static boolean isLarge(BlockState state, TierMaterials tier) {
        if (!isVault(state, tier))
            return false;
        return state.getValue(LARGE);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        Direction.Axis axis = state.getValue(HORIZONTAL_AXIS);
        return state.setValue(HORIZONTAL_AXIS, rot.rotate(Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE))
                .getAxis());
    }

    @Override
    @ParametersAreNonnullByDefault
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state;
    }

    // Vaults are less noisy when placed in batch
    public SoundType silencedSound() {
        SoundType type = tierMaterials.soundType;
        return new ForgeSoundType(0.1F, 1.5F, type::getBreakSound, type::getStepSound, type::getPlaceSound, type::getHitSound, type::getFallSound);
    }

    @Override
    public SoundType getSoundType(BlockState state, LevelReader world, BlockPos pos, Entity entity) {
        SoundType soundType = super.getSoundType(state, world, pos, entity);
        if (entity != null && entity.getPersistentData().contains("SilenceVaultSound")) return silencedSound();
        return soundType;
    }


    @Override
    public boolean hasAnalogOutputSignal(@NotNull BlockState p_149740_1_) {
        return true;
    }

    @Override
    @ParametersAreNonnullByDefault
    public int getAnalogOutputSignal(BlockState pState, Level pLevel, BlockPos pPos) {
        return getBlockEntityOptional(pLevel, pPos)
                .map(vte -> vte.getCapability(ForgeCapabilities.ITEM_HANDLER))
                .map(lo -> lo.map(ItemHelper::calcRedstoneFromInventory)
                        .orElse(0))
                .orElse(0);
    }

    @Override
    public BlockEntityType<? extends TieredVaultBlockEntity> getBlockEntityType() {
        return BSBlockEntities.VAULTS.get(tierMaterials).get();
    }

    @Override
    public Class<TieredVaultBlockEntity> getBlockEntityClass() {
        return TieredVaultBlockEntity.class;
    }


    public boolean isSeeThrough() {
        return tierMaterials.seeThrough;
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean skipRendering(BlockState pState, BlockState pAdjacentBlockState, Direction pSide) {
        return isSeeThrough() ? (pAdjacentBlockState.getBlock() instanceof TieredVaultBlock block && block.isSeeThrough()) || super.skipRendering(pState, pAdjacentBlockState, pSide) : super.skipRendering(pState, pAdjacentBlockState, pSide);
    }

    @Override
    @ParametersAreNonnullByDefault
    public VoxelShape getVisualShape(BlockState pState, BlockGetter pReader, BlockPos pPos, CollisionContext pContext) {
        return isSeeThrough() ? Shapes.empty() : super.getVisualShape(pState, pReader, pPos, pContext);
    }

    @Override
    @ParametersAreNonnullByDefault
    public float getShadeBrightness(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return isSeeThrough() ? 1.0F : super.getShadeBrightness(pState, pLevel, pPos);
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean propagatesSkylightDown(BlockState pState, BlockGetter pReader, BlockPos pPos) {
        return isSeeThrough() || super.propagatesSkylightDown(pState, pReader, pPos);
    }

    @Override
    public boolean shouldDisplayFluidOverlay(BlockState state, BlockAndTintGetter world, BlockPos pos, FluidState fluidState) {
        return isSeeThrough() || shouldDisplayFluidOverlay(state, world, pos, fluidState);
    }
}
