package mine.block.snowday.mixins;

import mine.block.snowday.SnowDay;
import mine.block.snowday.SnowLevel;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LeavesBlock.class)
public class LeavesBlockMixin extends Block {
    public LeavesBlockMixin(Settings settings) {
        super(settings);
    }

    @Unique
    public boolean invalid() {
        return !this.getClass().equals(LeavesBlock.class);
    }

    @Inject(method = "appendProperties", at = @At("TAIL"))
    private void appendPropertiesInject(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        if(invalid()) return;
        builder.add(SnowLevel.SNOW_LEVEL);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initInject(AbstractBlock.Settings settings, CallbackInfo ci) {
        if(invalid()) return;
        ((DefaultStateInvoker) this).invokeSetDefaultState(((LeavesBlock)(Object) this).getDefaultState().with(SnowLevel.SNOW_LEVEL, SnowLevel.none));
    }

    @Inject(method = "hasRandomTicks", at = @At("RETURN"), cancellable = true)
    private void hasRandomTicksInject(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        if(invalid()) return;
        cir.setReturnValue(true);
    }

    @Inject(method = "randomTick", at = @At("HEAD"))
    private void randomTickInject(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        if(invalid()) return;
        SnowLevel currentLevel = state.get(SnowLevel.SNOW_LEVEL);

        if(!SnowDay.CONFIG.whiteLeavesWhenSnowing) {
            world.setBlockState(pos, state.with(SnowLevel.SNOW_LEVEL, SnowLevel.none));
        }

        if (world.isRaining() && world.getBiome(pos).value().getPrecipitation() == Biome.Precipitation.SNOW) {
            if (world.getLightLevel(LightType.SKY, pos) > 12) {
                world.setBlockState(pos, state.with(SnowLevel.SNOW_LEVEL, currentLevel.increase()));
            }
        } else {
            world.setBlockState(pos, state.with(SnowLevel.SNOW_LEVEL, currentLevel.decrease()));
        }
    }
}
