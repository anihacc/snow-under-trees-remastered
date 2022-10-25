package mine.block.snowday;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import mine.block.snowday.worldgen.SnowUnderTreeHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.block.LeavesBlock;
import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.StringIdentifiable;

public class SnowDay implements ModInitializer
{
	public static final String MODID = "snowday";
	public static SnowdayConfig CONFIG;

	@Override
	public void onInitialize()
	{
		AutoConfig.register(SnowdayConfig.class, JanksonConfigSerializer::new);
		SnowDay.CONFIG = AutoConfig.getConfigHolder(SnowdayConfig.class).getConfig();
		ServerLifecycleEvents.SERVER_STARTING.register(server -> AutoConfig.getConfigHolder(SnowdayConfig.class).load());
		SnowUnderTreeHelper.initialize();
		SnowLevel.initialize();
	}

}
