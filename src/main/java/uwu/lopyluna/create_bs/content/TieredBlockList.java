package uwu.lopyluna.create_bs.content;

import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class TieredBlockList<T extends Block> implements Iterable<BlockEntry<T>> {

    private static final int AMOUNT = TierMaterials.values().length;

    private final BlockEntry<?>[] values = new BlockEntry<?>[AMOUNT];

    public TieredBlockList(Function<TierMaterials, BlockEntry<? extends T>> filler) {
        for (TierMaterials tier : TierMaterials.values()) values[tier.ordinal()] = filler.apply(tier);
    }

    @SuppressWarnings("unchecked")
    public BlockEntry<T> get(TierMaterials tier) {
        return (BlockEntry<T>) values[tier.ordinal()];
    }

    @Override
    public @NotNull Iterator<BlockEntry<T>> iterator() {
        return new Iterator<>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < values.length;
            }

            @SuppressWarnings("unchecked")
            @Override
            public BlockEntry<T> next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                return (BlockEntry<T>) values[index++];
            }
        };
    }

}