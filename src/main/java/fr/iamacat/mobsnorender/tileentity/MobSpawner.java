package fr.iamacat.mobsnorender.tileentity;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntityMobSpawnerRenderer;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

public class MobSpawner extends TileEntityMobSpawner {

    private final RenderManager renderManager = RenderManager.instance;

    public void func_145828_a(int p_145828_1_, int p_145828_2_, int p_145828_3_) {
        // do nothing to prevent vanilla rendering
    }

    @Override
    public void updateEntity() {
        // do nothing to prevent vanilla behavior
    }

    public void renderWithoutSpawnerAnimation(TileEntityMobSpawner mobSpawner, double x, double y, double z, float partialTicks) {
        Entity entity = mobSpawner.func_145881_a().func_98281_h();
        if (entity != null) {
            GL11.glPushMatrix();
            GL11.glTranslated(x + 0.5D, y, z + 0.5D);
            // Render the spawned entity without the spawner animation
            renderManager.renderEntitySimple(entity, partialTicks);
            GL11.glPopMatrix();
        }
    }

    public static class MobSpawnerRenderer extends TileEntityMobSpawnerRenderer {
        private final MobSpawner spawner = new MobSpawner();

        @Override
        public void renderTileEntityAt(TileEntityMobSpawner mobSpawner, double x, double y, double z, float partialTicks) {
            spawner.renderWithoutSpawnerAnimation(mobSpawner, x, y, z, partialTicks);
        }
    }
}
