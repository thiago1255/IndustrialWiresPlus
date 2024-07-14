/*
|| UNDER 'GNU General Public License v3.0'
|| File made by thiago1255 based (copied a lot) of files of mods 'Industrial Wires', and 'Immersive Engineering'.
||
|| (check github for credits of this mods:)
|| IW: https://github.com/malte0811/IndustrialWires
|| IE: https://github.com/BluSunrize/ImmersiveEngineering
*/
package malte0811.industrialwires.client.render;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import blusunrize.immersiveengineering.api.IEProperties;

import malte0811.industrialwires.blocks.stuff.TileEntityRetifierValve;
import malte0811.industrialwires.client.ClientUtilsIW;
import malte0811.industrialwires.client.RawQuad;
import malte0811.industrialwires.util.Beziers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.block.state.IBlockState;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import java.util.*;

import static malte0811.industrialwires.util.MiscUtils.interpolate;

public class TileRenderRetifierValve extends TileEntitySpecialRenderer<TileEntityRetifierValve> {
	
	private final float[] cor = {1, 0.87F, 1};
	private int animation = 1;
	
	@Override
	public void render(TileEntityRetifierValve tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		super.render(tile, x, y, z, partialTicks, destroyStage, alpha); //renderizacao padrao.
		if (tile.isDummy()) {return;}
		//based and copied from TileRenderJacobsLadder
		if (tile.ignition)
		{
			GlStateManager.pushMatrix();
		    GlStateManager.translate(x , y - 1, z);//double 1 1 1 S M H|offset da altura para baixo do inicio do arco. //.5 1 .5
            GlStateManager.rotate(0, 0, 1, 0); //lado que o bloco está virado/ 0 / 1 / 0
            GlStateManager.translate(.15 / 2, 0, 0); //double .15 .20 .25 S M H / 2 / 0 / 0
            GlStateManager.disableTexture2D();
		    GlStateManager.disableLighting();
		    GlStateManager.shadeModel(GL11.GL_SMOOTH);
		    if (Shaders.areShadersEnabled()) {
			   GlStateManager.enableBlend();
			   GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		    }
		    setLightmapDisabled(true);
		    GlStateManager.color(0, 0, 1, 1);//RGBA 
		    Vec3d[] controls = new Vec3d[5]; 
			
			//Y = (0,71875)
			//X = 0.05 - 0.573
			if(tile.facing == EnumFacing.NORTH) {
				//norte
				final double offsetZ = 1.156; 
			    final double offsetX = 0.48;
				controls[0] = new Vec3d(0.05 - offsetX, 0.72, 1 - offsetZ); 
			    controls[1] = new Vec3d(0.18 - offsetX, 0.715, (1.4-(animation*0.05)) - offsetZ);
			    controls[2] = new Vec3d(0.30 - offsetX, 0.7, 1 - offsetZ);
			    controls[3] = new Vec3d(0.42 - offsetX, 0.715, (0.7+(animation*0.05)) - offsetZ); 
			    controls[4] = new Vec3d(0.573 - offsetX, 0.72, 1 - offsetZ);
			} else if (tile.facing == EnumFacing.SOUTH) {
				//sul
				final double offsetZ = .155; 
			    final double offsetX = .704;
				controls[0] = new Vec3d(0.05 + offsetX, 0.72, 1 + offsetZ); 
			    controls[1] = new Vec3d(0.18 + offsetX, 0.715, (1.4-(animation*0.05)) + offsetZ);
			    controls[2] = new Vec3d(0.30 + offsetX, 0.7, 1 + offsetZ);
			    controls[3] = new Vec3d(0.42 + offsetX, 0.715, (0.7+(animation*0.05)) + offsetZ); 
			    controls[4] = new Vec3d(0.573 + offsetX, 0.72, 1 + offsetZ);
			} else if (tile.facing == EnumFacing.EAST) {
				//leste
				final double offsetZ = 0.079; 
			    final double offsetX = 0.406;
				controls[0] = new Vec3d(1 + offsetZ, 0.72, 0.05 - offsetX);
			    controls[1] = new Vec3d((1.4-(animation*0.05)) + offsetZ, 0.715, 0.18 - offsetX); 
			    controls[2] = new Vec3d(1 + offsetZ, 0.7, 0.30 - offsetX); 
			    controls[3] = new Vec3d((0.7+(animation*0.05)) + offsetZ, 0.715, 0.42 - offsetX); 
			    controls[4] = new Vec3d(1 + offsetZ, 0.72, 0.573 - offsetX); 
			} else {
				//oeste
				final double offsetZ = 1.23; 
			    final double offsetX = 0.785;
				controls[0] = new Vec3d(1 - offsetZ, 0.72, 0.05 + offsetX);
			    controls[1] = new Vec3d((1.4-(animation*0.05)) - offsetZ, 0.715, 0.18 + offsetX); 
			    controls[2] = new Vec3d(1 - offsetZ, 0.7, 0.30 + offsetX); 
			    controls[3] = new Vec3d((0.7+(animation*0.05)) - offsetZ, 0.715, 0.42 + offsetX); 
			    controls[4] = new Vec3d(1 - offsetZ, 0.72, 0.573 + offsetX); 
			}
			
		    drawBezier(controls);
            setLightmapDisabled(false);
		    GlStateManager.enableTexture2D();
		    GlStateManager.enableLighting();
		    GlStateManager.shadeModel(GL11.GL_FLAT);
		    GlStateManager.disableBlend();
		    GlStateManager.popMatrix();
			animation++;
			if(animation > 12) { animation = 1; }
		}

		//based and copied from TileRenderDieselGenerator
		final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		BlockPos blockPos = tile.getPos();
		IBlockState state = getWorld().getBlockState(blockPos);
		state = state.getBlock().getActualState(state, getWorld(), blockPos);
		state = state.withProperty(IEProperties.DYNAMICRENDER, true);
		IBakedModel model = blockRenderer.getBlockModelShapes().getModelForState(state);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldRenderer = tessellator.getBuffer();

		ClientUtils.bindAtlas();
		GlStateManager.pushMatrix();
		double offsetZ = tile.offsetZ;
		double offsetX = tile.offsetX;
		if(tile.facing == EnumFacing.SOUTH) {
		    GlStateManager.translate(x+8/16f, y+2/16f, z);
		} else if (tile.facing == EnumFacing.NORTH) {
			GlStateManager.translate(x-8/16f, y+2/16f, z);
		} else if (tile.facing == EnumFacing.EAST) {
			GlStateManager.translate(x, y+2/16f, z-8/16f);
		} else {
		    GlStateManager.translate(x, y+2/16f, z+8/16f);
		}
		GlStateManager.translate(.5, 0, .5);

		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		if(Minecraft.isAmbientOcclusionEnabled()) {
			GlStateManager.shadeModel(7425);
		} else {
			GlStateManager.shadeModel(7424);
		}

		GlStateManager.rotate(tile.fanAngle+((tile.fanWorking?18f:0)*partialTicks), tile.facing.getXOffset(), 0, tile.facing.getZOffset());

		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		worldRenderer.setTranslation(-.5-blockPos.getX(), -blockPos.getY(), -.5-blockPos.getZ());
		worldRenderer.color(255, 255, 255, 255);
		blockRenderer.getBlockModelRenderer().renderModel(tile.getWorld(), model, state, blockPos, worldRenderer, true);
		worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();

		RenderHelper.enableStandardItemLighting();

		GlStateManager.popMatrix();
	}
	
	//based and copied from TileRenderJacobsLadder
	private void drawBezier(Vec3d[] controls) {
		Shaders.useShader(Shaders.JACOBS_ARC);
		Vec3d radY = new Vec3d(0, .05, 0);
		Vec3d radZ = new Vec3d(0, 0, .05);
		float[][] colors = new float[11][];
		colors[0] = cor;
		if (Shaders.areShadersEnabled()) {
			colors[0][0] = 0;
		}
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder vertBuffer = tes.getBuffer();
		vertBuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		Vec3d last = Beziers.getPoint(0, controls);
		for (int i = 1; i <= 10; i++) {
		    double d = i / (double) 10;
			colors[i] = cor;
			Vec3d pos = Beziers.getPoint(d, controls);
			if (Shaders.areShadersEnabled()) {
				colors[i][0] = (float) d;
			}
		    drawQuad(last, pos, radY, colors[i - 1], colors[i], vertBuffer);
		    drawQuad(last, pos, radZ, colors[i - 1], colors[i], vertBuffer);
			last = pos;
		}
		tes.draw();
		Shaders.stopUsingShaders();
	}

	private void drawQuad(Vec3d v0, Vec3d v1, Vec3d rad, float[] color0, float[] color1, BufferBuilder vertexBuffer) {
		color(color1, 0, vertexBuffer.pos(v1.x - rad.x, v1.y - rad.y, v1.z - rad.z)).endVertex();
		color(color0, 0, vertexBuffer.pos(v0.x - rad.x, v0.y - rad.y, v0.z - rad.z)).endVertex();
		color(color0, 1, vertexBuffer.pos(v0.x + rad.x, v0.y + rad.y, v0.z + rad.z)).endVertex();
		color(color1, 1, vertexBuffer.pos(v1.x + rad.x, v1.y + rad.y, v1.z + rad.z)).endVertex();

		color(color1, 1, vertexBuffer.pos(v1.x + rad.x, v1.y + rad.y, v1.z + rad.z)).endVertex();
		color(color0, 1, vertexBuffer.pos(v0.x + rad.x, v0.y + rad.y, v0.z + rad.z)).endVertex();
		color(color0, 0, vertexBuffer.pos(v0.x - rad.x, v0.y - rad.y, v0.z - rad.z)).endVertex();
		color(color1, 0, vertexBuffer.pos(v1.x - rad.x, v1.y - rad.y, v1.z - rad.z)).endVertex();
	}

	private BufferBuilder color(float[] color, float alpha, BufferBuilder vb) {
		vb.color(color[0], color[1], color[2], alpha);
		return vb;
	}
}