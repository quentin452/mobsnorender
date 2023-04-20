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

import java.util.logging.LogManager;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.lang.reflect.Field;
import java.util.*;

import net.minecraftforge.common.config.Configuration;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION, acceptedMinecraftVersions = Reference.MC_VERSION)
public class Mobsnorender {
    private static final Logger LOGGER = Logger.getLogger(Mobsnorender.class.getName());

    private final List<String> blacklist = new ArrayList<String>();

    private final List<String> tileEntityBlacklist = new ArrayList<String>();

    // Définir les valeurs par défaut pour la distance X, Y et Z
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
        // Charger les valeurs de configuration à partir du fichier de configuration
        Configuration config = new Configuration(event.getSuggestedConfigurationFile());
        config.load();
        // Récupérer les noms d'entités à exclure de la configuration
        String[] blacklistArray = config.getStringList("blacklist", "general", new String[]{}, "List of entity names to exclude from rendering canceller");

        // Ajouter les noms des tile entités à la liste noire
        String[] tileEntityBlacklistArray = config.getStringList("tileEntityBlacklist", "general", new String[]{}, "List of tile entity names to exclude from rendering canceller");
        for (String tileEntityName : tileEntityBlacklistArray) {
            tileEntityBlacklist.add(tileEntityName.toLowerCase());
        }

        // Ajouter les noms d'entités à la liste noire
        for (String entityName : blacklistArray) {
            blacklist.add(entityName.toLowerCase());
        }
        // Récupérer les valeurs de distance X, Y et Z du fichier de configuration et les utiliser pour mettre à jour les valeurs par défaut
        distanceXEntity = config.getInt("distanceXEntity", "general", 48, 1, 1000, "The maximum X distance to render entities(X and Z must be equalized)");
        distanceYEntity = config.getInt("distanceYEntity", "general", 32, 1, 1000, "The maximum Y distance to render entities");
        distanceZEntity = config.getInt("distanceZEntity", "general", 48, 1, 1000, "The maximum Z distance to render entities(X and Z must be equalized)");

        // Récupérer les valeurs de distance X, Y et Z du fichier de configuration et les utiliser pour mettre à jour les valeurs par défaut
        distanceXTileEntity = config.getInt("distanceXTileEntity", "general", 64, 1, 1000, "The maximum X distance to render tile entities(X and Z must be equalized)");
        distanceYTileEntity = config.getInt("distanceYTileEntity", "general", 48, 1, 1000, "The maximum Y distance to render tile entities");
        distanceZTileEntity = config.getInt("distanceZTileEntity", "general", 64, 1, 1000, "The maximum Z distance to render tile entities(X and Z must be equalized)");

        // Enregistrer les valeurs de configuration
        config.save();
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

        for (TileEntity tileEntity : tileEntities) {
            // Check if the tile entity is associated with a special renderer
            TileEntitySpecialRenderer renderer = TileEntityRendererDispatcher.instance.getSpecialRenderer(tileEntity);

            if (renderer != null) {
                // Check if the tile entity should be rendered
                Vec3 playerPos = Vec3.createVectorHelper(x, y, z);
                Vec3 tilePos = Vec3.createVectorHelper(tileEntity.xCoord + 0.5, tileEntity.yCoord + 0.5, tileEntity.zCoord + 0.5); // add 0.5 to center the tile entity
                double distance = playerPos.distanceTo(tilePos);
                if (distance <= this.distanceXTileEntity && Math.abs(y - tileEntity.yCoord) <= this.distanceYTileEntity && distance <= this.distanceZTileEntity) {
                    // Render the tile entity
                    renderer.renderTileEntityAt(tileEntity, tileEntity.xCoord - x, tileEntity.yCoord - y, tileEntity.zCoord - z, event.partialTicks);
                } else {
                    // Disable the rendering of the tile entity
                    TileEntityRendererDispatcher.instance.mapSpecialRenderers.remove(tileEntity.getClass());

                }
            }
        }
    }
    }