package mine.block.snowday.worldgen;

import mine.block.snowday.mixins.ThreadedAnvilChunkStorageInvoker;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.block.SnowyBlock;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.DefaultFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.PlacedFeature;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static mine.block.snowday.SnowDay.CONFIG;
import static mine.block.snowday.SnowDay.MODID;

public class SnowUnderTreeHelper {
    private static final Feature<DefaultFeatureConfig> SNOW_UNDER_TREES_FEATURE = new UndersnowFeature(DefaultFeatureConfig.CODEC);
    public static final ConfiguredFeature<?, ?> SNOW_UNDER_TREES_CONFIGURED = new ConfiguredFeature<>(SNOW_UNDER_TREES_FEATURE, new DefaultFeatureConfig());
    private static final List<Identifier> biomesToAddTo = new ArrayList<>();

    public static void initialize() {
        Registry.register(Registry.FEATURE, new Identifier(MODID, "snow_under_trees"), SNOW_UNDER_TREES_FEATURE);
        RegistryKey<ConfiguredFeature<?, ?>> snowUnderTrees = RegistryKey.of(Registry.CONFIGURED_FEATURE_KEY, new Identifier(MODID, "snow_under_trees"));
        Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, snowUnderTrees.getValue(), SNOW_UNDER_TREES_CONFIGURED);
        Registry.register(BuiltinRegistries.PLACED_FEATURE, new Identifier(MODID, "snow_under_trees"), new PlacedFeature(BuiltinRegistries.CONFIGURED_FEATURE.getEntry(snowUnderTrees).get(), new ArrayList<>()));
        RegistryKey<PlacedFeature> placedFeatureKey = RegistryKey.of(Registry.PLACED_FEATURE_KEY, new Identifier(MODID, "snow_under_trees"));

        ServerTickEvents.START_WORLD_TICK.register(SnowUnderTreeHelper::onWorldTick);
        BiomeModifications.addFeature(b -> shouldAddSnow(b.getBiome(), b.getBiomeKey()), GenerationStep.Feature.TOP_LAYER_MODIFICATION, placedFeatureKey);
    }

    private static boolean shouldAddSnow(Biome biome, RegistryKey<Biome> key)
    {
        Identifier id = key.getValue();
        return CONFIG.enableBiomeFeature && (biome.getPrecipitation() == Biome.Precipitation.SNOW || biomesToAddTo.contains(id)) && !CONFIG.filteredBiomes.contains(id.toString());
    }

    public static void addSnowUnderTrees(Biome biome)
    {
        Identifier id = BuiltinRegistries.BIOME.getId(biome);
        if (!biomesToAddTo.contains(id))
            biomesToAddTo.add(id);
    }

    public static void onWorldTick(ServerWorld world)
    {
        if (world.isRaining() && CONFIG.enableWhenSnowing)
        {
            ((ThreadedAnvilChunkStorageInvoker) world.getChunkManager().threadedAnvilChunkStorage).invokeEntryIterator().forEach(chunkHolder -> {
                Optional<WorldChunk> optional = chunkHolder.getEntityTickingFuture().getNow(ChunkHolder.UNLOADED_WORLD_CHUNK).left();

                if (optional.isPresent() && world.random.nextInt(16) == 0)
                {
                    WorldChunk chunk = optional.get();
                    ChunkPos chunkPos = chunk.getPos();
                    int chunkX = chunkPos.getStartX();
                    int chunkY = chunkPos.getStartZ();
                    BlockPos randomPos = world.getRandomPosInChunk(chunkX, 0, chunkY, 15);
                    RegistryEntry<Biome> biomeKey = world.getBiome(randomPos);
                    Biome biome = biomeKey.value();
                    boolean biomeDisabled = CONFIG.filteredBiomes.contains(world.getRegistryManager().get(Registry.BIOME_KEY).getKey(biome).toString());

                    if (!biomeDisabled && world.getBlockState(world.getTopPosition(Heightmap.Type.MOTION_BLOCKING, randomPos).down()).getBlock() instanceof LeavesBlock)
                    {
                        BlockPos pos = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, randomPos);
                        BlockState state = world.getBlockState(pos);

                        if (biome.canSetSnow(world, pos) && state.isAir())
                        {
                            BlockPos downPos = pos.down();
                            BlockState stateBelow = world.getBlockState(downPos);

                            if (stateBelow.isSideSolidFullSquare(world, downPos, Direction.UP))
                            {
                                world.setBlockState(pos, Blocks.SNOW.getDefaultState());

                                if (stateBelow.contains(SnowyBlock.SNOWY))
                                    world.setBlockState(downPos, stateBelow.with(SnowyBlock.SNOWY, true), 2);
                            }
                        }
                    }
                }
            });
        }
    }
}
