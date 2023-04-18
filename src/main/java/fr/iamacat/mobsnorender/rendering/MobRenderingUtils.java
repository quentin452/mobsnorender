package fr.iamacat.mobsnorender.rendering;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

import java.util.List;

public class MobRenderingUtils {
    public static void disableRenderingForDistantMobs() {
        Minecraft mc = Minecraft.getMinecraft();
        EntityRenderer entityRenderer = mc.entityRenderer;
        RenderGlobal renderGlobal = mc.renderGlobal;

        // Obtenir la liste des entités vivantes chargées dans le monde
        List<Entity> entities = mc.theWorld.loadedEntityList;

        // Désactiver le rendu pour les entités vivantes qui sont à une distance supérieure à (un certains nombre) de blocs en termes de couche Y
        for (Entity entity : entities) {
            if (entity instanceof EntityLivingBase) {
                EntityLivingBase livingEntity = (EntityLivingBase) entity;
                double distanceY = mc.thePlayer.posY - livingEntity.posY;

                if (distanceY > 8 || distanceY < -8) {

                    livingEntity.setInvisible(true);
                } else {
                    livingEntity.setInvisible(false);
                }
            }
        }
    }
}
