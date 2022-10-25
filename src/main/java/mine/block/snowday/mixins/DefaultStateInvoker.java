package mine.block.snowday.mixins;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Block.class)
public interface DefaultStateInvoker {
    @Invoker("setDefaultState")
    void invokeSetDefaultState(BlockState state);
}
