package uwu.lopyluna.create_bs.content.vault;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.ConnectedTextureBehaviour;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import uwu.lopyluna.create_bs.content.TierMaterials;
import uwu.lopyluna.create_bs.registry.BSSpriteShifts;

public class TieredVaultCTBehaviour extends ConnectedTextureBehaviour.Base {

    TierMaterials tierMaterials;

    public TieredVaultCTBehaviour(TierMaterials tierMaterials) {
        this.tierMaterials = tierMaterials;
    }

    @Override
    public CTSpriteShiftEntry getShift(BlockState state, Direction direction, @Nullable TextureAtlasSprite sprite) {
        Direction.Axis vaultBlockAxis = TieredVaultBlock.getVaultBlockAxis(state, tierMaterials);
        boolean small = !TieredVaultBlock.isLarge(state, tierMaterials);
        if (vaultBlockAxis == null)
            return null;

        if (direction.getAxis() == vaultBlockAxis)
            return BSSpriteShifts.VAULT_FRONT.get(tierMaterials).get(small);
        if (direction == Direction.UP)
            return BSSpriteShifts.VAULT_TOP.get(tierMaterials).get(small);
        if (direction == Direction.DOWN)
            return BSSpriteShifts.VAULT_BOTTOM.get(tierMaterials).get(small);

        return BSSpriteShifts.VAULT_SIDE.get(tierMaterials).get(small);
    }

    @Override
    protected Direction getUpDirection(BlockAndTintGetter reader, BlockPos pos, BlockState state, Direction face) {
        Direction.Axis vaultBlockAxis = TieredVaultBlock.getVaultBlockAxis(state, tierMaterials);
        boolean alongX = vaultBlockAxis == Direction.Axis.X;
        if (face.getAxis().isVertical() && alongX)
            return super.getUpDirection(reader, pos, state, face).getClockWise();
        if (face.getAxis() == vaultBlockAxis || face.getAxis().isVertical())
            return super.getUpDirection(reader, pos, state, face);
        assert vaultBlockAxis != null;
        return Direction.fromAxisAndDirection(vaultBlockAxis, alongX ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE);
    }

    @Override
    protected Direction getRightDirection(BlockAndTintGetter reader, BlockPos pos, BlockState state, Direction face) {
        Direction.Axis vaultBlockAxis = TieredVaultBlock.getVaultBlockAxis(state, tierMaterials);
        if (face.getAxis()
                .isVertical() && vaultBlockAxis == Direction.Axis.X)
            return super.getRightDirection(reader, pos, state, face).getClockWise();
        if (face.getAxis() == vaultBlockAxis || face.getAxis()
                .isVertical())
            return super.getRightDirection(reader, pos, state, face);
        return Direction.fromAxisAndDirection(Direction.Axis.Y, face.getAxisDirection());
    }

    public boolean buildContextForOccludedDirections() {
        return super.buildContextForOccludedDirections();
    }

    @Override
    public boolean connectsTo(BlockState state, BlockState other, BlockAndTintGetter reader, BlockPos pos, BlockPos otherPos, Direction face) {
        return state == other && ConnectivityHandler.isConnected(reader, pos, otherPos);
    }

}
