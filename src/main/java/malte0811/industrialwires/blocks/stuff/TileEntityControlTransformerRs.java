public class TileEntityControlTransformerRs extends TileEntityImmersiveConnectable implements ITickable, IIEInternalFluxHandler, IBlockBoundsDirectional, IDirectionalTile  
{
// VARIABLES/CONS.: --------------------------------------
    private static final String SOUTH = "south";
    private static final String NORTH = "north";
    private static final String EAST = "east";
    private static final String WEST = "west";
    EnumFacing facing = EnumFacing.NORTH;
    public BlockPos endOfLeftConnection = null;
    public int maxvalue = 2048;
    private int redstonevalue = 0;
    private boolean wireenergy = false;
    public FluxStorage energyStorage = new FluxStorage(getMaxStorage());
    boolean firstTick = true;

// NBT DATA: --------------------------------------
    @Override
    public void readCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
        super.readCustomNBT(nbt, descPacket);
        facing = EnumFacing.byHorizontalIndex(nbt.getByte("facing"));
        wireenergy = nbt.getBoolean("wireenergy");
        energyStorage.readFromNBT(nbt);
    }
       
    @Override
	  public void writeCustomNBT(@Nonnull NBTTagCompound nbt, boolean descPacket) {
		    super.writeCustomNBT(nbt, descPacket);
		    nbt.setByte("facing",  (byte) facing.getHorizontalIndex());
        nbt.setBoolean("wireenergy", wireenergy);
		    energyStorage.writeToNBT(nbt);
    }

// ITICKABLE: --------------------------------------
    @Override
 	  public void update() {
		    if (!world.isRemote) {
            redstonevalue = world.getRedstonePowerFromNeighbors(pos);    
            maxvalue = ((redstonevalue + 1)*2048);     
			  }
        else if(firstTick) {
		        Set<Connection> conns = ImmersiveNetHandler.INSTANCE.getConnections(world, pos);
		        if(conns!=null) { for(Connection conn : conns) { if(pos.compareTo(conn.end) < 0&&world.isBlockLoaded(conn.end)) { this.markContainingBlockForUpdate(null); } } }
		        firstTick = false;
		    }               
    }
    
//WIRE STUFF: --------------------------------------
    @Override
    protected boolean canTakeLV() { return false; }
        
    @Override
	  protected boolean canTakeMV() { return false; }

    @Override
	  protected boolean canTakeHV() { return true; }
 
    @Override
	  protected boolean isRelay() { return false; }

    @Override
	  public boolean allowEnergyToPass(Connection con) { return true; }

    @Override
	  public boolean isEnergyOutput() { return true; }

    @Override
	  public boolean canConnectCable(WireType cableType, TargetingInfo target, Vec3i offset) {
	      if(wireenergy) { return false; }
		    if(!cableType.isEnergyWire()) { return false; }
		    if(!HV_CATEGORY.equals(cableType.getCategory())) { return false; }
		    return limitType==null||WireApi.canMix(cableType, limitType);
	  }

    @Override
	  public WireType getCableLimiter(TargetingInfo target) { return limitType; }

    @Override
	  public void connectCable(WireType cableType, TargetingInfo target, IImmersiveConnectable other) {
		    if(this.limitType==null) { this.limitType = cableType; }
		    wireenergy = true;
	  }

    @Override 
	  public void removeCable(Connection connection) {
        wireenergy = false;
		    limitType = null;
	  }
  
    @Override
	  public Vec3d getConnectionOffset(Connection con)
	  {
        return new Vec3d(0.5, 1.7, 0.5);
	  }
    
//ENERGY STRG: --------------------------------------       
    public int getMaxStorage() { return 32768; }

	  public int getMaxInput() { return maxvalue; }

	  public int getMaxOutput() { return maxvalue; }

    @Override
	  public int outputEnergy(int amount, boolean simulate, int energyType) {
        if(amount > 0&&energyStorage.getEnergyStored() < getMaxStorage()){
            int quantityenergy = Math.min(getMaxStorage()-energyStorage.getEnergyStored(), Math.min(amount, maxvalue));
		        if(!simulate){
		            energyStorage.modifyEnergyStored(+quantityenergy);
		        }
		        return quantityenergy;
	      }
	      return 0;
    }

    @Override
	  public boolean canConnectEnergy(EnumFacing from) {
        return false; 
    }

    @Override
    public FluxStorage getFluxStorage() { return energyStorage; }

    IEForgeEnergyWrapper energyWrapper;    

    @Override
	  public IEForgeEnergyWrapper getCapabilityWrapper(EnumFacing facing) { return null; } 
        
    @Override
	  public SideConfig getEnergySideConfig(EnumFacing facing) { return SideConfig.NONE; }   

// GENERAL PROPERTYES: --------------------------------------       
    AxisAlignedBB aabb = null;
    @Override
    public AxisAlignedBB getBoundingBoxNoRot() { return new AxisAlignedBB(0, 0, 0, 1, 1, 1); }

    @Override
    public AxisAlignedBB getBoundingBox() {
        if (aabb==null) { aabb = IBlockBoundsDirectional.super.getBoundingBox(); }
	      return aabb;
    }

    @Nonnull
    @Override
    public EnumFacing getFacing() { return facing; }

    @Override
    public void setFacing(@Nonnull EnumFacing facing) { this.facing = facing; }

    @Override
    public int getFacingLimitation() { return 2; }

    @Override
    public boolean mirrorFacingOnPlacement(@Nonnull EntityLivingBase placer) { return false; }

    @Override
    public boolean canHammerRotate(@Nonnull EnumFacing side, float hitX, float hitY, float hitZ, @Nonnull EntityLivingBase entity) { return false; }

    @Override
    public boolean canRotate(@Nonnull EnumFacing axis) { return false; }
    
// FINISH OF THIS CLASS ------------------------------------------------------------------------
}
