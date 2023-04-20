package fr.iamacat.mobsnorender;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import fr.iamacat.mobsnorender.proxy.CommonProxy;
import fr.iamacat.mobsnorender.utils.Reference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraftforge.common.config.Configuration;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION, acceptedMinecraftVersions = Reference.MC_VERSION)
public class Mobsnorender {

    private final List<String> blacklist = new ArrayList<String>();

    // Définir les valeurs par défaut pour la distance X, Y et Z
    private int distanceX = 48;
    private int distanceY = 32;
    private int distanceZ = 48;
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


        // Ajouter les noms d'entités à la liste noire
        for (String entityName : blacklistArray) {
            blacklist.add(entityName.toLowerCase());
        }

        // Récupérer les valeurs de distance X, Y et Z du fichier de configuration et les utiliser pour mettre à jour les valeurs par défaut
        distanceX = config.getInt("distanceX", "general", 48, 1, 1000, "The maximum X distance to render entities(X and Z must be equalized)");
        distanceY = config.getInt("distanceY", "general", 32, 1, 1000, "The maximum Y distance to render entities");
        distanceZ = config.getInt("distanceZ", "general", 48, 1, 1000, "The maximum Z distance to render entities(X and Z must be equalized)");

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
        if (event.entity != null && event.entity instanceof EntityLivingBase) {
            EntityLivingBase livingEntity = (EntityLivingBase) event.entity;
            // Check if the entity should be excluded
            if (blacklist.contains(livingEntity.getCommandSenderName().toLowerCase())) {
                return;
            }

            // Calculate the X, Y, and Z distance between the entity and the player
            double distanceX = Math.abs(livingEntity.posX - Minecraft.getMinecraft().thePlayer.posX);
            double distanceY = Math.abs(livingEntity.posY - Minecraft.getMinecraft().thePlayer.posY);
            double distanceZ = Math.abs(livingEntity.posZ - Minecraft.getMinecraft().thePlayer.posZ);

            // Check if the X, Y, and Z distances are greater than the configured values
            if (distanceX > this.distanceX || distanceY > this.distanceY || distanceZ > this.distanceZ) {// Disable rendering of the entity
                event.setCanceled(true);
            }
        }
    }
}