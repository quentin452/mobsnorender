package fr.iamacat.mobsnorender;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import fr.iamacat.mobsnorender.proxy.CommonProxy;
import fr.iamacat.mobsnorender.tilentity.CustomTileEntityChestRenderer;
import fr.iamacat.mobsnorender.utils.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import java.util.Map;
import java.util.HashMap;

import java.util.*;

import net.minecraftforge.common.config.Configuration;
import org.lwjgl.opengl.GL11;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION, acceptedMinecraftVersions = Reference.MC_VERSION)
public class Mobsnorender {

    private static final String VERSION = "0.5"; // Change this to the desired version
    private final List<String> entityblacklist = new ArrayList<String>();
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
        String[] blacklistArray = config.getStringList("00_entityblacklist", "general", new String[]{}, "List of entity names to exclude from rendering canceller(example : <Cow,Skeleton>");

        // Add tile entity names to blacklist
        String[] tileEntityBlacklistArray = config.getStringList("04_tileEntityBlacklist_BROKEN", "general", new String[]{}, "List of tile entity names to exclude from rendering canceller(example : <minecraft:chest,minecraft:something>");
        for (String tileEntityName : tileEntityBlacklistArray) {
            tileEntityBlacklist.add(tileEntityName.toLowerCase());
        }

        // Add entity names to blacklist
        for (String entityName : blacklistArray) {
            entityblacklist.add(entityName.toLowerCase());
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
        // Register the CustomTileEntityChestRenderer class with Minecraft
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityChest.class, new CustomTileEntityChestRenderer());
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
                if (entityblacklist.contains(livingEntity.getCommandSenderName().toLowerCase())) {
                    // Entity is in blacklist, do not cancel rendering
                } else {
                    // Entity is not in blacklist, check distance and cancel rendering if necessary
                    double distanceX = Math.abs(livingEntity.posX - Minecraft.getMinecraft().thePlayer.posX);
                    double distanceY = Math.abs(livingEntity.posY - Minecraft.getMinecraft().thePlayer.posY);
                    double distanceZ = Math.abs(livingEntity.posZ - Minecraft.getMinecraft().thePlayer.posZ);
                    if (distanceX > this.distanceXEntity || distanceY > this.distanceYEntity || distanceZ > this.distanceZEntity) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }
    private Map<TileEntity, TileEntitySpecialRenderer> renderersSpecial = new HashMap<>();

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        // Iterate over all Tile Entities in the world
        for (Object obj : Minecraft.getMinecraft().theWorld.loadedTileEntityList) {
            if (obj instanceof TileEntity) {
                TileEntity tileEntity = (TileEntity) obj;
                // Check if the Tile Entity is in the blacklist
                if (tileEntityBlacklist.contains(tileEntity.getClass())) {
                    // Tile Entity is in blacklist, skip renderer
                    continue;
                }
                // Check if the Tile Entity has a special renderer
                    if (TileEntityRendererDispatcher.instance.hasSpecialRenderer(tileEntity)) {
                        // Check distance from player to Tile Entity
                        double distanceX = tileEntity.xCoord - Minecraft.getMinecraft().thePlayer.posX;
                        double distanceY = tileEntity.yCoord - Minecraft.getMinecraft().thePlayer.posY;
                        double distanceZ = tileEntity.zCoord - Minecraft.getMinecraft().thePlayer.posZ;
                        if (distanceX > this.distanceXTileEntity || distanceY > this.distanceYTileEntity || distanceZ > this.distanceZTileEntity) {
                            // Distance is too great, remove Tile Entity special renderer if it exists
                            if (renderersSpecial.containsKey(tileEntity)) {
                                TileEntitySpecialRenderer renderer = renderersSpecial.get(tileEntity);
                                if (renderer != null) {
                                    renderer.renderTileEntityAt(tileEntity, tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, event.partialTicks);
                                }
                            }
                        }
                    }
                    // Always render Tile Entity with special renderer
                    if (!renderersSpecial.containsKey(tileEntity)) {
                        TileEntitySpecialRenderer renderer = TileEntityRendererDispatcher.instance.getSpecialRenderer(tileEntity);
                        renderersSpecial.put(tileEntity, renderer);
                        renderer.func_147496_a(tileEntity.getWorldObj());
                    }
                    renderersSpecial.get(tileEntity).renderTileEntityAt(tileEntity, tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, event.partialTicks);
                }
            }
        }
    }