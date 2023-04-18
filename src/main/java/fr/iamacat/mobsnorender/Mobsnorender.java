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

import java.util.List;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.MOD_VERSION, acceptedMinecraftVersions = Reference.MC_VERSION, dependencies = "before:mcinstanceloader")
public class Mobsnorender {

    @Mod.Instance(Reference.MOD_ID)
    public static Mobsnorender instance;

    @SidedProxy(clientSide = Reference.CLIENT_PROXY, serverSide = Reference.SERVER_PROXY)
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
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
        // Vérifiez si l'entité est une entité vivante
        if (event.entity instanceof EntityLivingBase) {
            EntityLivingBase livingEntity = (EntityLivingBase) event.entity;

            // Calculez la distance XZ entre l'entité et le joueur
            double distanceX = Math.abs(livingEntity.posX - Minecraft.getMinecraft().thePlayer.posX);
            double distanceY = Math.abs(livingEntity.posY - Minecraft.getMinecraft().thePlayer.posY);
            double distanceZ = Math.abs(livingEntity.posZ - Minecraft.getMinecraft().thePlayer.posZ);

            // Vérifiez si la distance XZ est supérieure à une certaine valeur
            if (distanceX > 50 || distanceY > 32 || distanceZ > 50) {
                // Désactivez le rendu de l'entité
                event.setCanceled(true);

            }
        }
    }
}

