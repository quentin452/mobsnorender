package fr.iamacat.mobsnorender;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import fr.iamacat.mobsnorender.proxy.CommonProxy;
import fr.iamacat.mobsnorender.utils.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAmbientCreature;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;

import java.util.*;

import net.minecraftforge.common.config.Configuration;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION, acceptedMinecraftVersions = Reference.MC_VERSION)
public class Mobsnorender {

    private static final String VERSION = "1.2"; // Change this to the desired version
    private final List<String> entityblacklist = new ArrayList<String>();
   // private final List<String> tileEntityBlacklist = new ArrayList<>();

    // Define default values for X, Y, and Z distances
    private int distanceXAmbient = 32;
    private int distanceYAmbient = 24;
    private int distanceZAmbient = 32;

    private int distanceXAggressive = 64;
    private int distanceYAggressive = 24;
    private int distanceZAggressive = 64;

    private int distanceXPassive = 48;
    private int distanceYPassive = 24;
    private int distanceZPassive = 48;

    private boolean notcancelAmbientRendering = true;

    private boolean notcancelPassiveRendering = true;

    private boolean notcancelAgressiveRendering = true;

//    private int distanceXTileEntity = 64;
//    private int distanceYTileEntity = 48;
//    private int distanceZTileEntity = 64;

    @Mod.Instance(Reference.MOD_ID)
    public static Mobsnorender instance;


    @SidedProxy(clientSide = Reference.CLIENT_PROXY, serverSide = Reference.SERVER_PROXY)
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Load configuration values from configuration file
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());

        // Get the loaded config version from the configuration file
        String loadedModVersion = config.getLoadedConfigVersion();

        // Check if the loaded config version matches the current mod version
        if (loadedModVersion == null || !loadedModVersion.equals(VERSION)) {
            // Delete the old config
            event.getSuggestedConfigurationFile().delete();

            // Get the config version from the configuration file or use the default value if it doesn't exist
            String configVersion = config.getString("config_version", Configuration.CATEGORY_GENERAL, VERSION, "The version of the configuration file. Change this to reset the configuration file.");

            // Create a new config with the specified config version
            config = new Configuration(event.getSuggestedConfigurationFile(), configVersion);
        }

        // Load the configuration file
        config.load();

        // Get entity names to exclude from configuration
        String[] blacklistArray = config.getStringList("01_universalentityblacklist", "general", new String[]{}, "List of entity names to exclude from rendering canceller(example : <Cow,Skeleton> IMPORTANT add as a list , no as a line because the config will be reseted");

        // Add tile entity names to blacklist
  //      String[] tileEntityBlacklistArray = config.getStringList("04_tileEntityBlacklist_BROKEN", "general", new String[]{}, "List of tile entity names to exclude from rendering canceller(example : <minecraft:chest,minecraft:something>");
   //     for (String tileEntityName : tileEntityBlacklistArray) {
   //         tileEntityBlacklist.add(tileEntityName.toLowerCase());
    //    }

        // Add entity names to blacklist
        for (String entityName : blacklistArray) {
            entityblacklist.add(entityName.toLowerCase());
        }

        notcancelAgressiveRendering = config.getBoolean("02_distanceXAggressiveEntity", "general", false, "Disable the Aggressive entity canceller?");
        // Retrieve the X, Y, and Z distance values from the configuration file and use them to update the default values.
        distanceXAggressive = config.getInt("03_distanceXAggressiveEntity", "general", 64, 1, 1000, "The maximum X distance to render aggressive entities(X and Z must be equalized)");
        distanceYAggressive = config.getInt("04_distanceYAggressiveEntity", "general", 24, 1, 1000, "The maximum Y distance to render aggressive entities");
        distanceZAggressive = config.getInt("05_distanceZAggressiveEntity", "general", 64, 1, 1000, "The maximum Z distance to render aggressive entities(X and Z must be equalized)");
        notcancelPassiveRendering = config.getBoolean("06_distanceXPassiveEntity", "general", false, "Disable the Passive entity canceller?");
        // Retrieve the X, Y, and Z distance values from the configuration file and use them to update the default values.
        distanceXPassive = config.getInt("07_distanceXPassiveEntity", "general", 48, 1, 1000, "The maximum X distance to render passive entities(X and Z must be equalized)");
        distanceYPassive = config.getInt("08_distanceYPassiveEntity", "general", 24, 1, 1000, "The maximum Y distance to render passive entities");
        distanceZPassive = config.getInt("09_distanceZPassiveEntity", "general", 48, 1, 1000, "The maximum Z distance to render passive entities(X and Z must be equalized)");
        notcancelAmbientRendering = config.getBoolean("10_distanceXAmbientEntity", "general", false, "Disable the Ambient entity canceller?");
        // Retrieve the X, Y, and Z distance values from the configuration file and use them to update the default values.
        distanceXAmbient = config.getInt("11_distanceXAmbientEntity", "general", 32, 1, 1000, "The maximum X distance to render ambient entities(X and Z must be equalized)");
        distanceYAmbient = config.getInt("12_distanceYAmbientEntity", "general", 24, 1, 1000, "The maximum Y distance to render ambient entities");
        distanceZAmbient = config.getInt("13_distanceZAmbientEntity", "general", 32, 1, 1000, "The maximum Z distance to render ambient entities(X and Z must be equalized)");

        // Retrieve the X, Y, and Z distance values from the configuration file and use them to update default values.

      //  distanceXTileEntity = config.getInt("05_distanceXTileEntity_BROKEN", "general", 64, 1, 1000, "The maximum X distance to render tile entities(X and Z must be equalized)");
      //  distanceYTileEntity = config.getInt("06_distanceYTileEntity_BROKEN", "general", 48, 1, 1000, "The maximum Y distance to render tile entities");
     //   distanceZTileEntity = config.getInt("07_distanceZTileEntity_BROKEN", "general", 64, 1, 1000, "The maximum Z distance to render tile entities(X and Z must be equalized)");

        // Save the updated configuration file
        if (config.hasChanged()) {
            config.save();
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    }
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Pre event) {
        // Check if the entity is not null and is a living entity
        if (event.entity != null && (event.entity instanceof EntityLivingBase)) {
            EntityLivingBase livingEntity = (EntityLivingBase) event.entity;
            String name = livingEntity.getCommandSenderName().toLowerCase();
            if (entityblacklist.contains(name)) {
                // Entity is in blacklist, do not cancel rendering
            } else {
                // Entity is not in blacklist, check distance and cancel rendering if necessary
                double distanceX = Math.abs(livingEntity.posX - Minecraft.getMinecraft().thePlayer.posX);
                double distanceY = Math.abs(livingEntity.posY - Minecraft.getMinecraft().thePlayer.posY);
                double distanceZ = Math.abs(livingEntity.posZ - Minecraft.getMinecraft().thePlayer.posZ);
                boolean cancelAmbientRendering = false;
                boolean cancelAggressiveRendering = false;
                boolean cancelPassiveRendering = false;


                // Check the entity type and distance
                if (livingEntity instanceof EntityAmbientCreature) {
                    if (distanceX > this.distanceXAmbient || distanceY > this.distanceYAmbient || distanceZ > this.distanceZAmbient) {
                        cancelAmbientRendering = !notcancelAmbientRendering;
                    }
                } else if (livingEntity instanceof EntityMob) {
                    if (distanceX > this.distanceXAggressive || distanceY > this.distanceYAggressive || distanceZ > this.distanceZAggressive) {
                        cancelAggressiveRendering = !notcancelAgressiveRendering;
                    }
                } else if (livingEntity instanceof EntityAnimal) {
                    if (distanceX > this.distanceXPassive || distanceY > this.distanceYPassive || distanceZ > this.distanceZPassive) {
                        cancelPassiveRendering = !notcancelPassiveRendering;
                    }
                }
                if (cancelAmbientRendering && livingEntity instanceof EntityAmbientCreature) {
                    event.setCanceled(true);
                    }
                if (cancelAggressiveRendering && livingEntity instanceof EntityMob) {
                    event.setCanceled(true);
                    }
                if (cancelPassiveRendering && livingEntity instanceof EntityAnimal) {
                    event.setCanceled(true);
                }
            }
        }
    }
    /* i don't know why but doesn't work
    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        if (player == null) {
            return;
        }
        double playerX = player.posX;
        double playerY = player.posY;
        double playerZ = player.posZ;
        World world = player.worldObj;
        if (world == null) {
            return;
        }

        for (Object obj : world.loadedTileEntityList) {
            if (obj == null || !(obj instanceof TileEntity)) {
                continue;
            }
            TileEntity tileEntity = (TileEntity) obj;

            if (tileEntityBlacklist.contains(tileEntity.getClass())) {
                // Tile Entity is in blacklist, do not cancel rendering
            } else {
                // Tile Entity is not in blacklist, check distance and cancel rendering if necessary
                double distanceX = Math.sqrt(Math.pow(tileEntity.xCoord - Minecraft.getMinecraft().thePlayer.posX, 2));
                double distanceY = Math.sqrt(Math.pow(tileEntity.yCoord - Minecraft.getMinecraft().thePlayer.posY, 2));
                double distanceZ = Math.sqrt(Math.pow(tileEntity.zCoord - Minecraft.getMinecraft().thePlayer.posZ, 2));
                if (distanceX > this.distanceXEntity || distanceY > this.distanceYEntity || distanceZ > this.distanceZEntity) {
                    continue;
                }
                TileEntitySpecialRenderer renderer = TileEntityRendererDispatcher.instance.getSpecialRenderer(tileEntity);
                if (renderer == null) {
                    continue;
                }
                renderer.func_147496_a(tileEntity.getWorldObj());
                renderer.renderTileEntityAt(tileEntity, (float) playerX, (float) playerY, (float) playerZ, event.partialTicks);
            }
        }
    }

     */
}
