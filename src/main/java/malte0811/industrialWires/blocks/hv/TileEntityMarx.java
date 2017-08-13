/*
 * This file is part of Industrial Wires.
 * Copyright (C) 2016-2017 malte0811
 *
 * Industrial Wires is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Industrial Wires is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Industrial Wires.  If not, see <http://www.gnu.org/licenses/>.
 */

package malte0811.industrialWires.blocks.hv;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.TargetingInfo;
import blusunrize.immersiveengineering.api.energy.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.api.energy.wires.redstone.IRedstoneConnector;
import blusunrize.immersiveengineering.api.energy.wires.redstone.RedstoneWireNetwork;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.BlockTypes_MetalsIE;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IDirectionalTile;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWallmount;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import ic2.api.item.IC2Items;
import malte0811.industrialWires.*;
import malte0811.industrialWires.blocks.IBlockBoundsIW;
import malte0811.industrialWires.blocks.ISyncReceiver;
import malte0811.industrialWires.blocks.IWProperties;
import malte0811.industrialWires.blocks.TileEntityIWMultiblock;
import malte0811.industrialWires.client.render.TileRenderMarx;
import malte0811.industrialWires.hv.MarxOreHandler;
import malte0811.industrialWires.network.MessageTileSyncIW;
import malte0811.industrialWires.util.DualEnergyStorage;
import malte0811.industrialWires.util.MiscUtils;
import malte0811.industrialWires.wires.IC2Wiretype;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

import static malte0811.industrialWires.util.MiscUtils.getOffset;
import static malte0811.industrialWires.util.MiscUtils.offset;

public class TileEntityMarx extends TileEntityIWMultiblock implements ITickable, ISyncReceiver, IBlockBoundsIW, IImmersiveConnectable, IIC2Connector,
		IRedstoneConnector{

	private static final String TYPE = "type";
	private static final String STAGES = "stages";
	private static final String HAS_CONN = "hasConn";
	private static final String CAP_VOLTAGES = "capVoltages";
	private double rcTimeConst;
	private double timeFactor;
	private double timeFactorBottom;
	private final static double CAPACITANCE = 0.00_000_5;
	private final static double MAX_VOLTAGE = 250_000;
	private boolean allowSlowDischarge = true;

	public IWProperties.MarxType type = IWProperties.MarxType.NO_MODEL;
	private int stageCount = 0;
	public FiringState state = FiringState.CHARGING;
	@SideOnly(Side.CLIENT)
	public TileRenderMarx.Discharge dischargeData;
	// Voltage=100*storedEU
	private DualEnergyStorage storage = new DualEnergyStorage(50_000, 32_000);
	private boolean hasConnection;
	private double[] capVoltages;
	//RS channel 1/white
	private int voltageControl = 0;
	private boolean loaded = false;
	private double leftover;

	public TileEntityMarx(EnumFacing facing, IWProperties.MarxType type, boolean mirrored) {
		this.facing = facing;
		this.type = type;
		this.mirrored = mirrored;
	}
	public TileEntityMarx() {}

	@Override
	public void writeNBT(NBTTagCompound out, boolean updatePacket) {
		super.writeNBT(out, updatePacket);
		out.setInteger(TYPE, type.ordinal());
		out.setInteger(STAGES, stageCount);
		out.setBoolean(HAS_CONN, hasConnection);
		storage.writeToNbt(out, ENERGY_TAG);
		NBTTagList voltages = new NBTTagList();
		if (capVoltages != null) {
			for (int i = 0; i < stageCount; i++) {
				voltages.appendTag(new NBTTagDouble(capVoltages[i]));
			}
		}
		out.setTag(CAP_VOLTAGES, voltages);
	}

	@Override
	public void readNBT(NBTTagCompound in, boolean updatePacket) {
		super.readNBT(in, updatePacket);
		type = IWProperties.MarxType.values()[in.getInteger(TYPE)];
		setStageCount(in.getInteger(STAGES));
		NBTTagList voltages = in.getTagList(CAP_VOLTAGES, 6);//DOUBLE
		capVoltages = new double[stageCount];
		for (int i = 0;i<stageCount;i++) {
			capVoltages[i] = voltages.getDoubleAt(i);
		}
		storage.readFromNBT(in.getCompoundTag(ENERGY_TAG));
		hasConnection = in.getBoolean(HAS_CONN);
		collisionAabb = null;
		renderAabb = null;
	}

	@Nonnull
	@Override
	protected BlockPos getOrigin() {
		return getPos().subtract(offset).offset(facing.getOpposite(), 3);
	}


	@SuppressWarnings("unchecked")
	@Override
	public IBlockState getOriginalBlock() {
		int forward = getForward();
		int right = getRight();
		int up = offset.getY();
		if (forward==0) {
			return IEContent.blockMetalDevice0.getDefaultState().withProperty(IEContent.blockMetalDevice0.property, BlockTypes_MetalDevice0.CAPACITOR_HV);
		} else if (forward==-1) {
			return IEContent.blockConnectors.getDefaultState().withProperty(IEContent.blockConnectors.property, BlockTypes_Connector.RELAY_HV)
					.withProperty(IEProperties.FACING_ALL, facing);
		} else if (forward==4&&up==0&&right==1) {
			return IEContent.blockStorage.getDefaultState().withProperty(IEContent.blockStorage.property, BlockTypes_MetalsIE.STEEL);
		} else if (forward>0) {
			if ((right==0&&up==0)||(right==1&&up==stageCount-1)) {
				return IEContent.blockMetalDecoration1.getDefaultState().withProperty(IEContent.blockMetalDecoration1.property, BlockTypes_MetalDecoration1.STEEL_FENCE);
			} else {
				return IEContent.blockMetalDecoration2.getDefaultState().withProperty(IEContent.blockMetalDecoration2.property, BlockTypes_MetalDecoration2.STEEL_WALLMOUNT)
						.withProperty(IEProperties.INT_4, 1-right).withProperty(IEProperties.FACING_ALL, facing.getOpposite());
			}
		} else if (forward==-2) {
			return IEContent.blockMetalDecoration0.getDefaultState().withProperty(IEContent.blockMetalDecoration0.property, BlockTypes_MetalDecoration0.HEAVY_ENGINEERING);
		} else if (right==0) {
			return IEContent.blockConnectors.getDefaultState().withProperty(IEContent.blockConnectors.property, BlockTypes_Connector.CONNECTOR_REDSTONE)
					.withProperty(IEProperties.FACING_ALL, facing);
		} else {
			return IEContent.blockConnectors.getDefaultState().withProperty(IEContent.blockConnectors.property, BlockTypes_Connector.CONNECTOR_HV)
					.withProperty(IEProperties.FACING_ALL, facing);
		}
	}

	@Override
	public BiConsumer<World, BlockPos> getOriginalBlockPlacer() {
		IBlockState original = getOriginalBlock();
		if (original!=null) {
			return (w, p) -> {
				w.setBlockState(p, original);
				TileEntity te = w.getTileEntity(p);
				if (te instanceof IDirectionalTile&&original.getProperties().containsKey(IEProperties.FACING_ALL)) {
					((IDirectionalTile) te).setFacing(original.getValue(IEProperties.FACING_ALL));
					te.markDirty();
				}
				if (te instanceof TileEntityWallmount) {
					((TileEntityWallmount) te).orientation = original.getValue(IEProperties.INT_4);
				}
			};
		}
		return (a, b)->IndustrialWires.logger.warn(a+", "+b+" wasn't found");//NOP
	}

	@Override
	public void update() {
		if (state==FiringState.FIRE) {
			state = FiringState.CHARGING;
		} else if (state==FiringState.NEXT_TICK) {
			state = FiringState.FIRE;
			if (world.isRemote) {
				IndustrialWires.proxy.playMarxBang(this, getMiddle(), (float) getNormedEnergy(dischargeData.energy));
			} else {
				fire();
			}
		}
		if (!world.isRemote&&type== IWProperties.MarxType.BOTTOM) {
			if (capVoltages==null||capVoltages.length!=stageCount) {
				capVoltages = new double[stageCount];
			}
			double oldTopVoltage = capVoltages[stageCount-1];
			double oldBottomVoltage = capVoltages[0];
			for (int i = stageCount-1;i>0;i--) {
				double oldVoltage = capVoltages[i];
				double u0 = capVoltages[i-1];
				capVoltages[i] = u0-(u0-oldVoltage)*timeFactor;
				capVoltages[i-1] -= capVoltages[i]-oldVoltage;
			}
			//charge bottom cap from storage
			double setVoltage = MAX_VOLTAGE * voltageControl / 15D;
			double u0 = Math.min(setVoltage, 100 * storage.getEnergyStoredEU());
			if (u0<0) {
				u0 = 0;
			}
			if (u0 < capVoltages[0] && setVoltage > capVoltages[0]) {
				u0 = capVoltages[0];
			}
			if (allowSlowDischarge || u0 > capVoltages[0]) {
				if (u0<0) {
					IndustrialWires.logger.info("VOLTAGE: "+u0+", "+voltageControl);
				}
				double tmp = u0 - (u0 - oldBottomVoltage) * timeFactorBottom;
				double energyUsed = .5*(tmp * tmp - oldBottomVoltage * oldBottomVoltage)*CAPACITANCE;
				if (energyUsed > 0 && storage.extractEU(energyUsed, false)==energyUsed) {// energyUsed can be negative when discharging the caps
					storage.extractEU(energyUsed, true);
					capVoltages[0] = tmp;
				}
				if (Math.round(15*oldBottomVoltage/MAX_VOLTAGE)!=Math.round(15*capVoltages[0]/MAX_VOLTAGE)) {
					net.updateValues();
				} else if (Math.round(15*oldTopVoltage/MAX_VOLTAGE)!=Math.round(15*capVoltages[stageCount-1]/MAX_VOLTAGE)) {
					net.updateValues();
				}
				if (capVoltages[0] > MAX_VOLTAGE * 14 / 15) {
					state = FiringState.NEXT_TICK;
				}
			}
		}
		leftover = storage.getMaxInputIF();
	}

	private void fire() {
		if (!world.isRemote) {
			//calculate energy
			double energyStored = 0;
			for (int i = 0;i<stageCount;i++) {
				energyStored += .5*capVoltages[i]*capVoltages[i]*CAPACITANCE;
				capVoltages[i] = 0;
			}
			net.updateValues();
			NBTTagCompound data = new NBTTagCompound();
			data.setDouble("energy", energyStored);
			IndustrialWires.packetHandler.sendToDimension(new MessageTileSyncIW(this, data), world.provider.getDimension());
			handleEntities(energyStored);
			handleOreProcessing(energyStored);//After entities to prevent killing the newly dropped items
		}
	}

	public void handleOreProcessing(double energyStored) {
		BlockPos bottom = getBottomElectrode();
		List<BlockPos> toBreak = new ArrayList<>(2*stageCount-2);
		int ores = 0;
		for (int i = 1;i<stageCount-1;i++) {
			BlockPos here = bottom.up(i);
			if (!world.isAirBlock(here)) {
				toBreak.add(here);
				ores++;
			}
			double radius = Utils.RAND.nextDouble()*Math.abs(.5-i/(double)stageCount)*Math.sqrt(stageCount)*.5;
			double angle = Utils.RAND.nextDouble()*Math.PI*2;
			Vec3d offset = new Vec3d(Math.cos(angle)*radius, 0, Math.sin(angle)*radius);
			BlockPos outside = here.add(new BlockPos(offset));
			if (!outside.equals(here)&&canBreak(outside)) {
				toBreak.add(outside);
			}
		}
		if (ores>0) {
			double energyPerOre = energyStored / ores;
			for (BlockPos here:toBreak) {
				IBlockState state = world.getBlockState(here);
				if (state.getBlockHardness(world, here) < 0) {
					continue;
				}
				if (!world.isAirBlock(here)) {
					ItemStack input = state.getBlock().getPickBlock(state, null, world, here, null);
					if (!input.isEmpty()) {
						ItemStack[] out = MarxOreHandler.getYield(input, energyPerOre);
						for (ItemStack stack : out) {
							EntityItem item = new EntityItem(world, here.getX() + .5, here.getY() + .5, here.getZ() + .5, stack);
							final double maxMotion = .3;
							item.motionX = 2*maxMotion*(Utils.RAND.nextDouble()-.5);
							item.motionY = 2*maxMotion*(Utils.RAND.nextDouble()-.5);
							item.motionZ = 2*maxMotion*(Utils.RAND.nextDouble()-.5);
							world.spawnEntity(item);
						}
					}
					world.setBlockToAir(here);
				}
			}
		}
	}

	public void handleEntities(double energyStored) {
		Vec3d v0 = getMiddle();
		AxisAlignedBB aabb = new AxisAlignedBB(v0, v0);
		aabb = aabb.expand(0, stageCount/2-1,0);
		final double sqrtStages = Math.sqrt(stageCount);
		aabb = aabb.grow(5*sqrtStages);
		List<Entity> fools = world.getEntitiesWithinAABB(Entity.class, aabb);
		double energyNormed = getNormedEnergy(energyStored);
		double damageDistSqu = energyNormed * sqrtStages;
		double tinnitusDistSqu = 5 * energyNormed * sqrtStages;
		damageDistSqu *= damageDistSqu;
		tinnitusDistSqu *= tinnitusDistSqu;
		if (IWConfig.HVStuff.marxSoundDamage == 2) {
			damageDistSqu = tinnitusDistSqu;
			tinnitusDistSqu = -1;
		}
		for (Entity entity : fools) {
			double y;
			if (entity.posY<pos.getY()+1) {
				y = pos.getY()+1;
			} else if (entity.posY>pos.getY()+stageCount-2) {
				y = pos.getY()+stageCount-2;
			} else {
				y = entity.posY;
			}
			double distSqu = entity.getDistanceSq(v0.x, y, v0.z);
			if (distSqu<=damageDistSqu) {
				float dmg = (float) (10*stageCount*(1-distSqu/damageDistSqu));
				entity.attackEntityFrom(IWDamageSources.dmg_marx, dmg);
			}
			if (distSqu<=tinnitusDistSqu && entity instanceof EntityPlayer) {
				ItemStack helmet = ((EntityPlayer) entity).inventory.armorInventory.get(3);
				boolean earMuff = helmet.getItem()==IEContent.itemEarmuffs;
				if (!earMuff&&helmet.hasTagCompound()) {
					earMuff = helmet.getTagCompound().hasKey("IE:Earmuffs");
				}
				if (!earMuff) {
					double multipl = Math.min(5, Math.sqrt(stageCount));
					int duration = (int) (20*20*(1+multipl*(1-distSqu/tinnitusDistSqu)));
					if (IWConfig.HVStuff.marxSoundDamage == 0) {
						((EntityPlayer) entity).addPotionEffect(new PotionEffect(IWPotions.tinnitus, duration));
					} else {
						((EntityPlayer) entity).addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("nausea"), duration));
					}
				}
			}
		}
	}

	//checks whether the given pos can't be broken because it is part of the generator
	public boolean canBreak(BlockPos pos) {
		BlockPos dischargePos = offset(pos, facing, mirrored, 1, 3, 0);
		Vec3i offset = getOffset(dischargePos, facing, mirrored, pos);
		return Math.abs(offset.getX())>Math.abs(offset.getY());
	}

	@Override
	public Vec3i getSize() {
		return new Vec3i(stageCount, 8, 2);
	}

	@Override
	public void onSync(NBTTagCompound nbt) {
		state = FiringState.NEXT_TICK;
		if (dischargeData==null) {
			dischargeData = new TileRenderMarx.Discharge(stageCount);
		}
		dischargeData.energy = nbt.getFloat("energy");
		dischargeData.diameter = (float) getNormedEnergy(dischargeData.energy);
		dischargeData.genMarxPoint(0, dischargeData.vertices.length-1);
	}

	public double getNormedEnergy(double total) {
		return total*2/(stageCount*MAX_VOLTAGE*MAX_VOLTAGE*CAPACITANCE);
	}

	AxisAlignedBB renderAabb = null;
	@Nonnull
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if (renderAabb ==null) {
			if (type== IWProperties.MarxType.BOTTOM) {
				renderAabb = new AxisAlignedBB(pos,
						offset(pos, facing, mirrored, 2, 4, stageCount));
			} else {
				renderAabb = new AxisAlignedBB(pos, pos);
			}
		}
		return renderAabb;
	}
	AxisAlignedBB collisionAabb = null;
	@Override
	public AxisAlignedBB getBoundingBox() {
		if (collisionAabb ==null) {
			int forward = getForward();
			int right = getRight();
			int up = offset.getY();
			AxisAlignedBB ret = Block.FULL_BLOCK_AABB;
			switch (forward) {
			case -3://IO
				if (right == 1) {
					ret = new AxisAlignedBB(5 / 16D, 5 / 16D, .25, 11 / 16D, 11 / 16D, 1);
				} else {
					ret = new AxisAlignedBB(5 / 16D, 5 / 16D, 7 / 16D, 11 / 16D, 11 / 16D, 1);
				}
				break;
			case -1://charging resistors
				if (up == 0) {
					ret = new AxisAlignedBB(.375, 0, 0, .625, 1, 1);
				} else if (up == stageCount - 1) {
					ret = new AxisAlignedBB(.375, 0, 9 / 16D, .625, 5 / 16D, 1);
				} else {
					ret = new AxisAlignedBB(.375, 0, 9 / 16D, .625, 1, 1);
				}
				break;
			case 1://spark gaps
				if (right == 0) {
					if (up!=0) {
						ret = new AxisAlignedBB(0, 0, 0, 9 / 16D, up == stageCount - 1 ? .5 : 1, 7 / 16D);
					} else {
						ret = new AxisAlignedBB(7/16D, 0, 0, 9/16D, 5/16D, 1);
					}
				} else {
					if (stageCount - 1 == up) {
						ret = new AxisAlignedBB(7 / 16D, 3 / 16D, 0, 9 / 16D, 5 / 16D, 1);
					} else {
						ret = new AxisAlignedBB(7 / 16D, 0, 0, 1, 1, 7 / 16D);
					}
				}
				break;
			case -2://Controller
				break;
			case 0://Caps
				if (up == stageCount - 1) {
					ret = new AxisAlignedBB(0, 0, 0, 1, .5, 1);
				}
				break;
			default:
				if (right == 0) {
					if (forward<4) {
						ret = new AxisAlignedBB(7/16D, 0, 0, 9/16D, 5/16D, 1);
					} else {
						ret = new AxisAlignedBB(0, 0, 0, 9/16D, 5/16D, 9/16D);
					}
				} else {
					if (up==0) {
						ret = Block.FULL_BLOCK_AABB;
					} else if (forward < 4) {
						ret = new AxisAlignedBB(7 / 16D, 3 / 16D, 0, 9 / 16D, 5 / 16D, 1);
					} else {
						ret = new AxisAlignedBB(6 / 16D, 1 / 16D, 0, 10 / 16D, 5 / 16D, 10 / 16D);
					}
				}
			}
			collisionAabb = MiscUtils.apply(getBaseTransform(), ret);
		}
		return collisionAabb;
	}

	private Matrix4 getBaseTransform() {
		Matrix4 transform = new Matrix4();
		transform.translate(.5, 0, .5);
		transform.rotate(facing.getHorizontalAngle() * Math.PI / 180, 0, 1, 0);
		if (mirrored) {
			transform.scale(-1, 1, 1);
		}
		transform.translate(-.5, 0, -.5);
		return transform;
	}
	//WIRE STUFF
	@Override
	public boolean canConnect() {
		return getForward()==-3;
	}

	@Override
	public boolean isEnergyOutput() {
		return getForward()==-3&&getRight()==1;
	}

	@Override
	public int outputEnergy(int amount, boolean simulate, int energyType) {
		TileEntityMarx master = master(this);
		if (master!=null && amount>0) {
			double ret = master.storage.insertIF(amount, leftover, !simulate);
			leftover -= ret;
			return (int) ret;
		} else {
			return 0;
		}
	}

	@Override
	public double insertEnergy(double eu, boolean simulate) {
		TileEntityMarx master = master(this);
		if (master!=null) {
			double ret = master.storage.insertEU(eu, leftover, !simulate);
			leftover -= ret;
			return eu-ret;
		} else {
			return 0;
		}
	}

	@Override
	public BlockPos getConnectionMaster(@Nullable WireType cableType, TargetingInfo target) {
		return pos;
	}

	@Override
	public boolean canConnectCable(WireType cableType, TargetingInfo target) {
		if (hasConnection) {
			return false;
		}
		if (getRight()==0) {
			return cableType==WireType.REDSTONE;
		} else {
			return cableType==WireType.STEEL||cableType== IC2Wiretype.IC2_TYPES[3];
		}
	}

	@Override
	public void connectCable(WireType cableType, TargetingInfo target, IImmersiveConnectable other) {
		hasConnection = true;
	}

	@Override
	public WireType getCableLimiter(TargetingInfo target) {
		return getRight()==0?WireType.REDSTONE:IC2Wiretype.IC2_TYPES[3];
	}

	@Override
	public boolean allowEnergyToPass(ImmersiveNetHandler.Connection con) {
		return false;
	}

	@Override
	public void onEnergyPassthrough(int amount) {

	}

	@Override
	public void removeCable(ImmersiveNetHandler.Connection connection) {
		hasConnection = false;
	}

	@Override
	public Vec3d getRaytraceOffset(IImmersiveConnectable link) {
		Matrix4 transf = getBaseTransform();
		if (getRight()==0) {
			return transf.apply(new Vec3d(.5, .5, 7/16D));
		} else {
			return transf.apply(new Vec3d(.5, .5, 4/16D));
		}
	}

	@Override
	public Vec3d getConnectionOffset(ImmersiveNetHandler.Connection con) {
		return getRaytraceOffset(null);
	}

	private RedstoneWireNetwork net = new RedstoneWireNetwork();
	@Override
	public void setNetwork(RedstoneWireNetwork net) {
		masterOr(this, this).net = net;
	}

	@Override
	public RedstoneWireNetwork getNetwork() {
		TileEntityMarx master = masterOr(this, this);
		if (!loaded) {
			master.net.add(this);
			loaded = true;
		}
		return master.net;
	}

	@Override
	public void onChange() {
		TileEntityMarx master = masterOr(this, this);
		master.voltageControl = master.net.channelValues[0];
		if (master.net.channelValues[3]!=0) {//light blue is firing trigger
			master.tryTriggeredDischarge();
		}
		//yellow determines whether a lower charge- than cap0-voltage will discharge the generator
		master.allowSlowDischarge = master.net.channelValues[4] == 0;
	}
	public void tryTriggeredDischarge() {
		if (capVoltages[0]>=8/15D* MAX_VOLTAGE) {
			state = FiringState.NEXT_TICK;
		} else {
			for (int i = 0;i<stageCount;i++) {
				capVoltages[i] = 0;
			}
			net.updateValues();
		}
	}

	@Override
	public World getConnectorWorld() {
		return world;
	}

	@Override
	public void updateInput(byte[] signals) {
		TileEntityMarx master = masterOr(this, this);
		if (master.capVoltages!=null&&master.capVoltages.length==stageCount) {
			//1/orange is voltage measurement from the top cap
			//2/magenta is for the bottom one
			byte signal1 = (byte)(Math.round(15*master.capVoltages[stageCount-1]/ MAX_VOLTAGE));
			byte signal2 = (byte)(Math.round(15*master.capVoltages[0]/ MAX_VOLTAGE));
			signals[1] = (byte) Math.max(signals[1], signal1);
			signals[2] = (byte) Math.max(signals[2], signal2);
		}
	}

	public void setStageCount(int stageCount) {
		this.stageCount = stageCount;
		rcTimeConst = 5D/stageCount;
		timeFactor = Math.exp(-1/(20*rcTimeConst));
		timeFactorBottom = Math.exp(-1 / (20 * rcTimeConst * 2 / 3));
		collisionAabb = null;
		renderAabb = null;
	}

	public int getStageCount() {
		return stageCount;
	}

	public Vec3d getMiddle() {
		double middleY = pos.getY()+(stageCount)/2D;
		Vec3i electrodXZ = getBottomElectrode();
		return new Vec3d(electrodXZ.getX()+.5, middleY, electrodXZ.getZ()+.5);
	}

	public BlockPos getBottomElectrode() {
		return offset(pos, facing, mirrored, 1, 4, 0);
	}

	public enum FiringState {
		CHARGING,
		NEXT_TICK,
		FIRE;
	}
}
