/*
 * This file is part of Industrial Wires.
 * Copyright (C) 2016-2018 malte0811
 * Industrial Wires is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Industrial Wires is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Industrial Wires.  If not, see <http://www.gnu.org/licenses/>.
 */

package malte0811.industrialWires.client.multiblock_io_model;

import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.IWProperties;
import malte0811.industrialWires.client.ClientUtilsIW;
import malte0811.industrialWires.client.RawQuad;
import malte0811.industrialWires.util.MBSideConfig;
import malte0811.industrialWires.util.MBSideConfig.BlockFace;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SideOnly(Side.CLIENT)
public class BakedMBIOModel implements IBakedModel {
	private static final MBSideConfig NULL_CONFIG = new MBSideConfig(ImmutableList.of(new BlockFace(new BlockPos(0, 2, 0), EnumFacing.DOWN)));
	private static final Matrix4 ID = new Matrix4();

	static final ResourceLocation IO_LOC = new ResourceLocation(IndustrialWires.MODID, "blocks/io");
	static TextureAtlasSprite IO_TEX = null;

	private final IBakedModel base;
	private final int rotationInt;
	private final Rotation rotation;

	private final Cache<MBSideConfig, List<BakedQuad>> cache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES)
			.maximumSize(100).build();

	BakedMBIOModel(IBakedModel base, int rotationOffset) {
		this.base = base;
		this.rotationInt = rotationOffset;
		this.rotation = Rotation.values()[rotationOffset];
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
		MBSideConfig config = NULL_CONFIG;
		if (state instanceof IExtendedBlockState) {
			MBSideConfig tmpConfig = ((IExtendedBlockState) state).getValue(IWProperties.MB_SIDES);
			if (tmpConfig!=null) {
				config = tmpConfig;
			}
		}
		List<BakedQuad> ret = cache.getIfPresent(config);
		if (ret==null) {
			if (IO_TEX==null) {
				IO_TEX = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(IO_LOC.toString());
			}
			ret = new ArrayList<>(base.getQuads(state, side, rand));
			for (Map.Entry<BlockFace, SideConfig> f:config.sides.entrySet()) {
				if (f.getKey().face==null) {
					continue;
				}
				BlockPos transformedPos = f.getKey().offset.rotate(rotation);
				EnumFacing transformedFace = f.getKey().face;
				if (transformedFace.getAxis()!=EnumFacing.Axis.Y) {
					for (int i = 0; i < rotationInt; i++) {
						transformedFace = transformedFace.rotateY();
					}
				}
				Vector3f[] verts = getVerticesFromFace(transformedPos, transformedFace);
				RawQuad q = new RawQuad(verts[0], verts[1], verts[2], verts[3], transformedFace,
						IO_TEX, new float[]{1, 1, 1, 1}, getNormal(transformedFace),
						getUVsForConfig(f.getValue()));
				ret.add(ClientUtilsIW.bakeQuad(q, ID, ID));
			}
			ret = ImmutableList.copyOf(ret);
			cache.put(config.copy(), ret);
		}
		return ret;
	}

	private static final Vector3f[] NORMALS = new Vector3f[6];
	private static final Vector3f[][] VERTICES = new Vector3f[6][4];
	static {
		float[] vec = new float[3];
		for (int i = 0; i < EnumFacing.VALUES.length; i++) {
			EnumFacing f = EnumFacing.VALUES[i];
			NORMALS[i] = new Vector3f(f.getFrontOffsetX(), f.getFrontOffsetY(), f.getFrontOffsetZ());
			int axis = f.getAxis().ordinal();
			vec[axis] = f.getAxisDirection()==EnumFacing.AxisDirection.POSITIVE?1.001F:-.001F;
			float x1 = f.getAxisDirection()==EnumFacing.AxisDirection.POSITIVE?.625F:.375F;
			for (int j = 0;j<4;j++) {
				vec[(axis+1)%3] = 0<j&&j<3?x1:1-x1;
				vec[(axis+2)%3] = j<2?.375F:.625F;
				VERTICES[i][j] = vecFromArray(vec);
			}
		}
	}

	private static Vector3f vecFromArray(float[] in) {
		return new Vector3f(in[0], in[1], in[2]);
	}

	private Vector3f[] getVerticesFromFace(BlockPos p, EnumFacing f) {
		Vector3f[] orig = VERTICES[f.ordinal()];
		Vector3f[] ret = new Vector3f[4];
		Vector3f offset = new Vector3f(p.getX(), p.getY(), p.getZ());
		for (int i = 0; i < 4; i++) {
			ret[i] = Vector3f.add(orig[i], offset, null);
		}
		return ret;
	}

	private float[] getUVsForConfig(SideConfig sc) {
		return new float[]{sc.ordinal()*4, 0, (sc.ordinal()+1)*4, 4};
	}

	private Vector3f getNormal(EnumFacing face) {
		return NORMALS[face.ordinal()];
	}

	@Override
	public boolean isAmbientOcclusion() {
		return base.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return base.isGui3d();
	}

	@Override
	public boolean isBuiltInRenderer() {
		return base.isBuiltInRenderer();
	}

	@Nonnull
	@Override
	public TextureAtlasSprite getParticleTexture() {
		return base.getParticleTexture();
	}

	@Nonnull
	@Override
	public ItemOverrideList getOverrides() {
		return base.getOverrides();
	}

	public void clearCache() {
		cache.invalidateAll();
	}
}
