package uwu.lopyluna.create_bs.content;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class TieredBEBlockList<T extends BlockEntity> implements Iterable<BlockEntityEntry<T>> {

    private static final int AMOUNT = TierMaterials.values().length;

    private final BlockEntityEntry<?>[] values = new BlockEntityEntry<?>[AMOUNT];

    public TieredBEBlockList(Function<TierMaterials, BlockEntityEntry<? extends T>> filler) {
        for (TierMaterials tier : TierMaterials.values()) {
            values[tier.ordinal()] = filler.apply(tier);
        }
    }

    @SuppressWarnings("unchecked")
    public BlockEntityEntry<T> get(TierMaterials tier) {
        return (BlockEntityEntry<T>) values[tier.ordinal()];
    }

    @Override
    public @NotNull Iterator<BlockEntityEntry<T>> iterator() {
        return new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < values.length;
            }

            @SuppressWarnings("unchecked")
            @Override
            public BlockEntityEntry<T> next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                return (BlockEntityEntry<T>) values[index++];
            }
        };
    }

}