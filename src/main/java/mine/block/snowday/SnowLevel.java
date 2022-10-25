package mine.block.snowday;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.color.world.FoliageColors;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

public enum SnowLevel implements StringIdentifiable {
    none,
    low,
    medium,
    high,
    full;

    public static final EnumProperty<SnowLevel> SNOW_LEVEL = EnumProperty.of("snow_level", SnowLevel.class);

    private static int getLeafColor(Block block, BlockRenderView world, BlockPos pos) {
        if (block == Blocks.BIRCH_LEAVES) {
            return FoliageColors.getBirchColor();
        } else if (block == Blocks.SPRUCE_LEAVES) {
            return FoliageColors.getSpruceColor();
        } else {
            return world != null ? BiomeColors.getFoliageColor(world, pos) : FoliageColors.getDefaultColor();
        }
    }

    private static int white(int colour, double whiteness) {
        int red = (colour >> 16 & 0xff);
        int green = (colour >> 8 & 0xff);
        int blue = (colour & 0xff);
        red += (0xff - red) * whiteness;
        green += (0xff - green) * whiteness;
        blue += (0xff - blue) * whiteness;
        return red << 16 | green << 8 | blue;
    }

    public static void initialize() {
        ColorProviderRegistry.BLOCK.register(
                (state, world, pos, index) -> {
                    int colour = getLeafColor(state.getBlock(), world, pos);
                    try {
                        return white(colour, switch (state.get(SNOW_LEVEL)) {
                            case none -> 0;
                            case low -> 0.25;
                            case medium -> 0.5;
                            case high -> 0.75;
                            case full -> 1;
                        });
                    } catch(Exception e) {
                        return colour;
                    }
                },
                Blocks.ACACIA_LEAVES,
                Blocks.AZALEA_LEAVES,
                Blocks.BIRCH_LEAVES,
                Blocks.JUNGLE_LEAVES,
                Blocks.OAK_LEAVES,
                Blocks.DARK_OAK_LEAVES,
                Blocks.FLOWERING_AZALEA_LEAVES,
                Blocks.SPRUCE_LEAVES
        );
    }

    public SnowLevel increase() {
        if (this.ordinal() + 1 < SnowLevel.values().length) {
            return SnowLevel.values()[this.ordinal() + 1];
        } else {
            return this;
        }
    }

    public SnowLevel decrease() {
        if (this.ordinal() - 1 > 0) {
            return SnowLevel.values()[this.ordinal() - 1];
        } else {
            return this;
        }
    }

    @Override
    public String asString() {
        return this.name();
    }
}
