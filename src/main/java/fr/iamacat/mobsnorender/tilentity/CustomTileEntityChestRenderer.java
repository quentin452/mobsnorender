package fr.iamacat.mobsnorender.tilentity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.tileentity.TileEntityChestRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

public class CustomTileEntityChestRenderer extends TileEntityChestRenderer {
// for now this code completly remove the chest render, add a chest skipper in function of the player and X Y Z by using ChestModel???

    /*@Override
public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float partialTicks) {
    EntityPlayerSP player = Minecraft.getMinecraft().thePlayer;
    if (player != null) {
        double distanceSq = Math.pow(tileEntity.xCoord - player.posX, 2) + Math.pow(tileEntity.yCoord - player.posY, 2) + Math.pow(tileEntity.zCoord - player.posZ, 2);
        if (distanceSq > 10000) { // Skip rendering if the player is more than 100 blocks away
            return;
        }
    }
    // Render the chest if the player is close enough
    super.renderTileEntityAt(tileEntity, x, y, z, partialTicks);
    }

     */
}
