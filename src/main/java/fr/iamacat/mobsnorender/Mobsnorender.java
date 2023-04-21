package fr.iamacat.mobsnorender;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import fr.iamacat.mobsnorender.proxy.CommonProxy;
import fr.iamacat.mobsnorender.utils.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;
import java.util.logging.LogManager;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.lang.reflect.Field;
import java.util.*;

import net.minecraftforge.common.config.Configuration;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION, acceptedMinecraftVersions = Reference.MC_VERSION)
public class Mobsnorender {
    private static final String VERSION = "0.3"; // Change this to the desired version
    private final List<String> blacklist = new ArrayList<String>();

    private final List<String> tileEntityBlacklist = new ArrayList<String>();

    // Define default values for X, Y, and Z distances
    private int distanceXEntity = 48;
    private int distanceYEntity = 32;
    private int distanceZEntity = 48;
    private int distanceXTileEntity = 64;
    private int distanceYTileEntity = 48;
    private int distanceZTileEntity = 64;

    @Mod.Instance(Reference.MOD_ID)
    public static Mobsnorender instance;


    @SidedProxy(clientSide = Reference.CLIENT_PROXY, serverSide = Reference.SERVER_PROXY)
    public static CommonProxy proxy;

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
        String[] blacklistArray = config.getStringList("00_blacklist", "general", new String[]{}, "List of entity names to exclude from rendering canceller");

        // Add tile entity names to blacklist
        String[] tileEntityBlacklistArray = config.getStringList("04_tileEntityBlacklist_BROKEN", "general", new String[]{}, "List of tile entity names to exclude from rendering canceller");
        for (String tileEntityName : tileEntityBlacklistArray) {
            tileEntityBlacklist.add(tileEntityName.toLowerCase());
        }

        // Add entity names to blacklist
        for (String entityName : blacklistArray) {
            blacklist.add(entityName.toLowerCase());
        }


        // Retrieve the X, Y, and Z distance values from the configuration file and use them to update the default values.
        distanceXEntity = config.getInt("01_distanceXEntity", "general", 48, 1, 1000, "The maximum X distance to render entities(X and Z must be equalized)");
        distanceYEntity = config.getInt("02_distanceYEntity", "general", 32, 1, 1000, "The maximum Y distance to render entities");
        distanceZEntity = config.getInt("03_distanceZEntity", "general", 48, 1, 1000, "The maximum Z distance to render entities(X and Z must be equalized)");

        // Retrieve the X, Y, and Z distance values from the configuration file and use them to update default values.

        distanceXTileEntity = config.getInt("05_distanceXTileEntity_BROKEN", "general", 64, 1, 1000, "The maximum X distance to render tile entities(X and Z must be equalized)");
        distanceYTileEntity = config.getInt("06_distanceYTileEntity_BROKEN", "general", 48, 1, 1000, "The maximum Y distance to render tile entities");
        distanceZTileEntity = config.getInt("07_distanceZTileEntity_BROKEN", "general", 64, 1, 1000, "The maximum Z distance to render tile entities(X and Z must be equalized)");

        // Save the updated configuration file
        if (config.hasChanged()) {
            config.save();
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }


    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    }

    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Pre event) {
            // Check if the entity is not null and is a living entity
            if (event.entity != null && (event.entity instanceof EntityLivingBase)) {
                if (event.entity instanceof EntityLivingBase) {
                    EntityLivingBase livingEntity = (EntityLivingBase) event.entity;
                    if (blacklist.contains(livingEntity.getCommandSenderName().toLowerCase())) {
                        event.setCanceled(true);
                        return;
                    }
                    // Calculate the X, Y, and Z distance between the entity and the player
                    double distanceX = Math.abs(livingEntity.posX - Minecraft.getMinecraft().thePlayer.posX);
                    double distanceY = Math.abs(livingEntity.posY - Minecraft.getMinecraft().thePlayer.posY);
                    double distanceZ = Math.abs(livingEntity.posZ - Minecraft.getMinecraft().thePlayer.posZ);
                    if (distanceX > this.distanceXEntity || distanceY > this.distanceYEntity || distanceZ > this.distanceZEntity) {
                        event.setCanceled(true);
                    }
                }
            }
        }

    TileEntityRendererDispatcher tileEntityRenderer = TileEntityRendererDispatcher.instance;

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        World world = mc.theWorld;
        double x = mc.thePlayer.posX;
        double y = mc.thePlayer.posY;
        double z = mc.thePlayer.posZ;

        AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(
                x - distanceXTileEntity,
                y - distanceYTileEntity,
                z - distanceZTileEntity,
                x + distanceXTileEntity + 1,
                y + distanceYTileEntity + 1,
                z + distanceZTileEntity + 1
        );

        // Get all tile entities within the bounding box
        List<TileEntity> tileEntities = world.getEntitiesWithinAABB(TileEntity.class, bb);

        Map<Class<? extends TileEntity>, TileEntitySpecialRenderer> specialRenderers = new HashMap<>(tileEntityRenderer.mapSpecialRenderers);
        for (TileEntity tileEntity : tileEntities) {
            if (tileEntity != null) {
                if (tileEntityBlacklist.contains(tileEntity.getClass().getSimpleName().toLowerCase())) {
                    specialRenderers.remove(tileEntity.getClass());
                } else {
                    TileEntitySpecialRenderer renderer = tileEntityRenderer.getSpecialRenderer(tileEntity);
                    if (renderer != null) {
                        Vec3 playerPos = Vec3.createVectorHelper(x, y, z);
                        Vec3 tilePos = Vec3.createVectorHelper(tileEntity.xCoord + 0.5, tileEntity.yCoord + 0.5, tileEntity.zCoord + 0.5);
                        double distance = playerPos.distanceTo(tilePos);
                        if (distance <= this.distanceXTileEntity && Math.abs(y - tileEntity.yCoord) <= this.distanceYTileEntity && distance <= this.distanceZTileEntity) {
                            renderer.renderTileEntityAt(tileEntity, tileEntity.xCoord - x, tileEntity.yCoord - y, tileEntity.zCoord - z, event.partialTicks);
                        } else {
                            specialRenderers.remove(tileEntity.getClass());
                        }
                    }
                }
            }
        }
        tileEntityRenderer.mapSpecialRenderers = specialRenderers;
    }
}