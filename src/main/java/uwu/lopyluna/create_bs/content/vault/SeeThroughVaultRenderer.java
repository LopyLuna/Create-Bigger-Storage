package uwu.lopyluna.create_bs.content.vault;

import com.jozufozu.flywheel.util.transform.TransformStack;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.VecHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class SeeThroughVaultRenderer extends SmartBlockEntityRenderer<TieredVaultBlockEntity> {
    public SeeThroughVaultRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(TieredVaultBlockEntity blockEntity, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(blockEntity, partialTicks, ms, buffer, light, overlay);

        BlockPos pos = blockEntity.getBlockPos();
        int maxItems = (int) (blockEntity.getInventoryOfBlock().getSlots() * 0.5);
        for (int slot = 0; slot < maxItems; slot++) {
            ItemStack stack = blockEntity.getInventoryOfBlock().getStackInSlot(slot);
            if (!stack.isEmpty()) {
                renderItem(pos, ms, buffer, light, overlay, stack, slot);
            }
        }
    }

    protected void renderItem(BlockPos pos, PoseStack ms, MultiBufferSource buffer, int light, int overlay, ItemStack stack, int slot) {
        ms.pushPose();
        Minecraft mc = Minecraft.getInstance();
        RandomSource r = RandomSource.create((long) pos.hashCode() * (slot + 1L));

        Vec3 vec = VecHelper.offsetRandomly(Vec3.ZERO, r, 0.35f);
        float multiplier = r.nextIntBetweenInclusive(150, 500) / 100f;
        float time = AnimationTickHolder.getRenderTime() / 20;
        float angle = (((time * 10f) * multiplier) % 360f) * (r.nextBoolean() ? 1f : -1f);
        float timeOffset = (slot * 57.2958f) % 360f;
        float bob = (float) Math.sin((time + timeOffset) * Math.PI) * 0.05f;

        ms.translate(vec.x + 0.5, vec.y + 0.35 + bob, vec.z + 0.5);
        TransformStack.cast(ms).rotateY(angle);
        TransformStack.cast(ms).scale(0.75f);
        mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND, light, overlay, ms, buffer, mc.level, 0);
        ms.popPose();
    }
}
