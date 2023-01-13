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
package malte0811.industrialwires.client;

import blusunrize.immersiveengineering.api.ManualHelper;
import blusunrize.immersiveengineering.api.ManualPageMultiblock;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.lib.manual.IManualPage;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.ManualPages;
import blusunrize.lib.manual.ManualPages.PositionedItemStack;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import malte0811.industrialwires.*;
import malte0811.industrialwires.blocks.controlpanel.BlockTypes_Panel;
import malte0811.industrialwires.blocks.controlpanel.TileEntityPanelCreator;
import malte0811.industrialwires.blocks.controlpanel.TileEntityRSPanel;
import malte0811.industrialwires.blocks.converter.TileEntityMechMB;
import malte0811.industrialwires.blocks.hv.TileEntityJacobsLadder;
import malte0811.industrialwires.blocks.hv.TileEntityMarx;
import malte0811.industrialwires.blocks.stuff.TileEntityRetifierValve;
import malte0811.industrialwires.client.gui.GuiPanelComponent;
import malte0811.industrialwires.client.gui.GuiPanelCreator;
import malte0811.industrialwires.client.gui.GuiRSPanelConn;
import malte0811.industrialwires.client.gui.GuiRenameKey;
import malte0811.industrialwires.client.manual.TextSplitter;
import malte0811.industrialwires.client.multiblock_io_model.MBIOModelLoader;
import malte0811.industrialwires.client.panelmodel.PanelModelLoader;
import malte0811.industrialwires.client.render.*;
import malte0811.industrialwires.compat.Compat;
import malte0811.industrialwires.controlpanel.PanelComponent;
import malte0811.industrialwires.crafting.IC2TRHelper;
import malte0811.industrialwires.entities.EntityBrokenPart;
import malte0811.industrialwires.hv.MarxOreHandler;
import malte0811.industrialwires.hv.MultiblockMarx;
import malte0811.industrialwires.items.ItemIC2Coil;
import malte0811.industrialwires.items.ItemPanelComponent;
import malte0811.industrialwires.mech_mb.*;
import malte0811.industrialwires.util.CommandIWClient;
import malte0811.industrialwires.util.ConversionUtil;
import malte0811.industrialwires.util.MiscUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

import static malte0811.industrialwires.IndustrialWires.*;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
	@Override
	public void preInit() {
		super.preInit();
		OBJLoader.INSTANCE.addDomain(IndustrialWires.MODID);
		ModelLoaderRegistry.registerLoader(new PanelModelLoader());
		ModelLoaderRegistry.registerLoader(new MBIOModelLoader());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityJacobsLadder.class, new TileRenderJacobsLadder());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityRetifierValve.class, new TileRenderRetifierValve());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMarx.class, new TileRenderMarx());
		TileRenderMechMB tesr = new TileRenderMechMB();
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMechMB.class, tesr);
		((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(tesr);
		RenderingRegistry.registerEntityRenderingHandler(EntityBrokenPart.class, EntityRenderBrokenPart::new);

		Shaders.initShaders(true);
	}

	@Override
	public void postInit() {
		super.postInit();
		ManualInstance m = ManualHelper.getManual();
		boolean uni = m.fontRenderer.getUnicodeFlag();
		m.fontRenderer.setUnicodeFlag(true);
		m.entryRenderPre();
		TextSplitter splitter;
		{
			PositionedItemStack[][] wireRecipes = new PositionedItemStack[3][10];
			int xBase = 15;
			Ingredient copperCable = IC2TRHelper.getStack("cable", "type:copper,insulation:0");
			Object2IntMap<ItemStack> copperCables = new Object2IntLinkedOpenHashMap<>();
			for (ItemStack itemStack : copperCable.getMatchingStacks()) {
				copperCables.put(itemStack, 1);
			}
			copperCables.put(new ItemStack(IEObjects.itemWireCoil, 1, 0), 8);
			List<ItemStack> copperCableList = new ArrayList<>(copperCables.keySet());
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					wireRecipes[0][3 * i + j] = new PositionedItemStack(copperCableList, 18 * i + xBase, 18 * j);
				}
			}
			ItemStack copperCoil = new ItemStack(IndustrialWires.coil, 1, 1);
			ItemIC2Coil.setLength(copperCoil, 9);
			wireRecipes[0][9] = new PositionedItemStack(copperCoil, 18 * 4 + xBase, 18);
			Random r = new Random();
			for (int i = 1; i < 3; i++) {
				int lengthSum = 0;
				for (int j1 = 0; j1 < 3; j1++) {
					for (int j2 = 0; j2 < 3; j2++) {
						if (r.nextDouble() > 1 / (1. + copperCables.size())) {
							// cable
							ItemStack chosen = copperCableList.get(r.nextInt(copperCables.size()));
							lengthSum += copperCables.getInt(chosen);
							wireRecipes[i][3 * j1 + j2] = new PositionedItemStack(chosen, 18 * j1 + xBase, 18 * j2);
						} else {
							// wire coil
							int length = r.nextInt(99) + 1;
							copperCoil = new ItemStack(IndustrialWires.coil, 1, 1);
							ItemIC2Coil.setLength(copperCoil, length);
							wireRecipes[i][3 * j1 + j2] = new PositionedItemStack(copperCoil, 18 * j1 + xBase, 18 * j2);
							lengthSum += length;
						}
					}
				}
				copperCoil = new ItemStack(IndustrialWires.coil);
				ItemIC2Coil.setLength(copperCoil, lengthSum);
				wireRecipes[i][9] = new PositionedItemStack(copperCoil, 18 * 4 + xBase, 18);
			}

			splitter = new TextSplitter(m);
			splitter.addSpecialPage(0, 0, 10,
					s->new ManualPages.CraftingMulti(m, s, (Object[]) wireRecipes));
			String text = I18n.format("ie.manual.entry.industrialwires.wires");
			splitter.split(text);
			List<IManualPage> entry = splitter.toManualEntry();
			m.addEntry("industrialwires.wires", IndustrialWires.MODID, entry.toArray(new IManualPage[0]));
		}
		if (hasIC2 && IndustrialWires.mechConv != null) {
			m.addEntry("industrialwires.mechConv", "industrialwires",
					new ManualPages.Crafting(m, "industrialwires.mechConv0", new ItemStack(IndustrialWires.mechConv, 1, 1)),
					new ManualPages.Crafting(m, "industrialwires.mechConv1", new ItemStack(IndustrialWires.mechConv, 1, 2)),
					new ManualPages.Crafting(m, "industrialwires.mechConv2", new ItemStack(IndustrialWires.mechConv, 1, 0))
			);
		}
		addUnblockableSounds(TINNITUS, TURN_FAST, TURN_SLOW);

		ClientUtils.mc().getItemColors().registerItemColorHandler((stack, pass) -> {
			if (pass == 1) {
				PanelComponent pc = ItemPanelComponent.componentFromStack(stack);
				if (pc != null) {
					return 0xff000000 | pc.getColor();
				}
			}
			return ~0;
		}, IndustrialWires.panelComponent);

		Config.manual_doubleA.put("iwJacobsUsage", IWConfig.HVStuff.jacobsUsageWatt);
		Config.manual_double.put("iwFluxPerJoule", ConversionUtil.ifPerJoule());
		Config.manual_int.put("iwKeysOnRing", IWConfig.maxKeysOnRing);
		m.addEntry("industrialwires.jacobs", IndustrialWires.MODID,
				new ManualPages.CraftingMulti(m, "industrialwires.jacobs0", new ItemStack(IndustrialWires.jacobsLadder, 1, 0), new ItemStack(IndustrialWires.jacobsLadder, 1, 1), new ItemStack(IndustrialWires.jacobsLadder, 1, 2)),
				new ManualPages.Text(m, "industrialwires.jacobs1"));


		String text = I18n.format("ie.manual.entry.industrialwires.intro");
		splitter = new TextSplitter(m);
		splitter.addSpecialPage(0, 0, 9, s -> new ManualPages.Crafting(m, s,
				new ItemStack(IndustrialWires.panel, 1, BlockTypes_Panel.DUMMY.ordinal())));
		splitter.addSpecialPage(1, 0, 9, s -> new ManualPages.Crafting(m, s,
				new ItemStack(IndustrialWires.panel, 1, BlockTypes_Panel.UNFINISHED.ordinal())));
		splitter.split(text);
		m.addEntry("industrialwires.intro", "control_panels",
				splitter.toManualEntry().toArray(new IManualPage[0])
		);
		m.addEntry("industrialwires.panel_creator", "control_panels",
				new ManualPages.Crafting(m, "industrialwires.panel_creator0", new ItemStack(IndustrialWires.panel, 1, BlockTypes_Panel.CREATOR.ordinal())),
				new ManualPages.Text(m, "industrialwires.panel_creator1"),
				new ManualPages.Text(m, "industrialwires.panel_creator2")
		);
		text = I18n.format("ie.manual.entry.industrialwires.redstone");
		splitter = new TextSplitter(m);
		splitter.addSpecialPage(-1, 0, Compat.enableOtherRS ? 9 : 12, s -> new ManualPages.CraftingMulti(m, s,
				new ResourceLocation(IndustrialWires.MODID, "control_panel_rs_other"),
				new ResourceLocation(IndustrialWires.MODID, "control_panel_rs_wire")));
		splitter.split(text);
		m.addEntry("industrialwires.redstone", "control_panels",
				splitter.toManualEntry().toArray(new IManualPage[0])
		);
		m.addEntry("industrialwires.components", "control_panels",
				new ManualPages.Text(m, "industrialwires.components.general"),
				new ManualPages.Crafting(m, "industrialwires.button", new ItemStack(IndustrialWires.panelComponent, 1, 0)),
				new ManualPages.Crafting(m, "industrialwires.label", new ItemStack(IndustrialWires.panelComponent, 1, 1)),
				new ManualPages.Crafting(m, "industrialwires.indicator_light", new ItemStack(IndustrialWires.panelComponent, 1, 2)),
				new ManualPages.Crafting(m, "industrialwires.slider", new ItemStack(IndustrialWires.panelComponent, 1, 3)),
				new ManualPages.CraftingMulti(m, "industrialwires.toggle_switch", new ItemStack(IndustrialWires.panelComponent, 1, 5), new ItemStack(IndustrialWires.panelComponent, 1, 6)),
				new ManualPages.Text(m, "industrialwires.toggle_switch1"),
				new ManualPages.Crafting(m, "industrialwires.variac", new ItemStack(IndustrialWires.panelComponent, 1, 4)),
				new ManualPages.CraftingMulti(m, "industrialwires.lock", new ItemStack(IndustrialWires.panelComponent, 1, 7), new ItemStack(IndustrialWires.key)),
				new ManualPages.Crafting(m, "industrialwires.lock1", new ItemStack(IndustrialWires.key, 1, 2)),
				new ManualPages.Crafting(m, "industrialwires.panel_meter", new ItemStack(IndustrialWires.panelComponent, 1, 8)),
				new ManualPages.Crafting(m, "industrialwires.7seg", new ItemStack(IndustrialWires.panelComponent, 1, 9)),
				new ManualPages.Crafting(m, "industrialwires.rgb_led", new ItemStack(IndustrialWires.panelComponent, 1, 10))
		);
		List<MarxOreHandler.OreInfo> ores = MarxOreHandler.getRecipes();
		text = I18n.format("ie.manual.entry.industrialwires.marx");
		for (int i = 0; i < ores.size(); i++) {
			MarxOreHandler.OreInfo curr = ores.get(i);
			if (!curr.exampleInput.isEmpty()) {
				text += I18n.format(IndustrialWires.MODID + ".desc.input") + ": §l" + curr.exampleInput.get(0).getDisplayName() + "§r<br>";
				text += I18n.format(IndustrialWires.MODID + ".desc.output") + ": " + Utils.formatDouble(curr.maxYield, "0.#") + "x" + curr.output.get().getDisplayName() + "<br>";
				if (curr.outputSmall != null && !curr.outputSmall.get().isEmpty()) {
					text += I18n.format(IndustrialWires.MODID + ".desc.alt") + ": " + curr.smallMax + "x" + curr.outputSmall.get().getDisplayName() + "<br>";
				}
				text += I18n.format(IndustrialWires.MODID + ".desc.ideal_e") + ": " + Utils.formatDouble(curr.avgEnergy * MarxOreHandler.defaultEnergy / 1000, "0.#") + " kJ<br><br>";
			}
		}
		splitter = new TextSplitter(m);
		splitter.addSpecialPage(0, 0, 6,
				(s) -> new ManualPageMultiblock(m, s,
						MultiblockMarx.INSTANCE));
		splitter.split(text);
		List<IManualPage> marxEntry = splitter.toManualEntry();
		m.addEntry("industrialwires.marx", IndustrialWires.MODID, marxEntry.toArray(new IManualPage[0]));

		text = I18n.format("ie.manual.entry.industrialwires.mech_mb");
		splitter = new TextSplitter(m);
		splitter.addSpecialPage(0, 0, 8, (s) -> new ManualPageMultiblock(m, s,
				MiscUtils.getMBFromName(MechMBPart.EXAMPLE_MECHMB_LOC.toString())));
		splitter.split(text);
		List<IManualPage> mechMBEntry = splitter.toManualEntry();
		m.addEntry("industrialwires.mech_mb", IndustrialWires.MODID, mechMBEntry.toArray(new IManualPage[0]));

		String[][] flywheelTable;
		{
			List<String[]> flywheelTableList = new ArrayList<>(1 + Material.values().length);
			flywheelTableList.add(new String[]{"industrialwires.desc.material", "industrialwires.desc.inertia", "industrialwires.desc.max_speed"});
			for (Material mat : Material.values()) {
				MechPartFlywheel f = new MechPartFlywheel(mat);
				flywheelTableList.add(new String[]{mat.oreName(), Utils.formatDouble(f.getInertia(), "0.#"),
						Utils.formatDouble(f.getMaxSpeed(), "0.#")});
			}
			flywheelTable = flywheelTableList.toArray(new String[0][]);
		}
		text = I18n.format("ie.manual.entry.industrialwires.mech_mb_parts");
		splitter = new TextSplitter(m);
		splitter.addSpecialPage(0, 0, 10, (s) -> new ManualPageMultiblock(m, s,
				MechMBPart.getManualMBForPart(MechPartFlywheel.class)));
		splitter.addSpecialPage(1, 0, 1, s -> new ManualPages.Table(m, "", flywheelTable, true));
		splitter.addSpecialPage(2, 0, 10, (s) -> new ManualPageMultiblock(m, s,
				MechMBPart.getManualMBForPart(MechPartSingleCoil.class)));
		splitter.addSpecialPage(3, 0, 10, (s) -> new ManualPageMultiblock(m, s,
				MechMBPart.getManualMBForPart(MechPartFourElectrodes.class)));
		if (IWConfig.MechConversion.allowMBEU()) {
			text += I18n.format("ie.manual.entry.industrialwires.mech_mb_parts.commutator");
			splitter.addSpecialPage(4, 0, 10, (s) -> new ManualPageMultiblock(m, s,
					MechMBPart.getManualMBForPart(MechPartCommutator4Phase.class)));
		}
		splitter.split(text);
		List<IManualPage> partsEntry = splitter.toManualEntry();
		m.addEntry("industrialwires.mech_mb_parts", IndustrialWires.MODID, partsEntry.toArray(new IManualPage[0]));
		m.entryRenderPost();
		m.fontRenderer.setUnicodeFlag(uni);

		ClientCommandHandler.instance.registerCommand(new CommandIWClient());
	}

	private static ISound playingTinnitus = null;

	private void addUnblockableSounds(SoundEvent... sounds) {
		int oldLength = Config.IEConfig.Tools.earDefenders_SoundBlacklist.length;
		Config.IEConfig.Tools.earDefenders_SoundBlacklist =
				Arrays.copyOf(Config.IEConfig.Tools.earDefenders_SoundBlacklist, oldLength + sounds.length);
		for (int i = 0;i<sounds.length;i++) {
			Config.IEConfig.Tools.earDefenders_SoundBlacklist[oldLength+i] = sounds[i].getSoundName().toString();
		}
	}
	@Override
	public void startTinnitus() {
		final Minecraft mc = Minecraft.getMinecraft();
		if (playingTinnitus==null||!mc.getSoundHandler().isSoundPlaying(playingTinnitus)) {
			playingTinnitus = getTinnitus();
			mc.getSoundHandler().playSound(playingTinnitus);
		}
	}

	private ISound getTinnitus() {
		final Minecraft mc = Minecraft.getMinecraft();
		return  new MovingSound(TINNITUS, SoundCategory.PLAYERS) {
			@Override
			public void update() {
				if (mc.player.getActivePotionEffect(IWPotions.tinnitus)==null) {
					donePlaying = true;
					playingTinnitus = null;
				}
			}

			@Override
			public float getVolume() {
				return .5F;
			}

			@Override
			public float getXPosF() {
				return (float) mc.player.posX;
			}

			@Override
			public float getYPosF() {
				return (float) mc.player.posY;
			}

			@Override
			public float getZPosF() {
				return (float) mc.player.posZ;
			}

			@Override
			public boolean canRepeat() {
				return true;
			}
		};
	}

	@Override
	public World getClientWorld() {
		return Minecraft.getMinecraft().world;
	}

	private Map<BlockPos, List<ISound>> playingSounds = new HashMap<>();

	@Override
	public void playJacobsLadderSound(TileEntityJacobsLadder te, int phase, Vec3d soundPos) {
		stopAllSoundsExcept(te.getPos(), ImmutableSet.of());
		SoundEvent event;
		switch (phase) {
		case 0:
			event = LADDER_START;
			break;
		case 1:
			event = LADDER_MIDDLE;
			break;
		case 2:
			event = LADDER_END;
			break;
		default:
			return;
		}
		PositionedSoundRecord sound = new PositionedSoundRecord(event.getSoundName(), SoundCategory.BLOCKS, te.size.soundVolume, 1,
				false, 0, ISound.AttenuationType.LINEAR, (float) soundPos.x, (float) soundPos.y, (float) soundPos.z);
		ClientUtils.mc().getSoundHandler().playSound(sound);
		addSound(te.getPos(), sound);
	}

	@Override
	public void updateMechMBTurningSound(TileEntityMechMB te, MechEnergy energy) {
		SoundHandler sndHandler = ClientUtils.mc().getSoundHandler();
		List<ISound> soundsAtPos;
		if (playingSounds.containsKey(te.getPos())) {
			soundsAtPos = playingSounds.get(te.getPos());
			soundsAtPos.removeIf(s -> !sndHandler.isSoundPlaying(s));
			if (soundsAtPos.isEmpty()) {
				playingSounds.remove(te.getPos());
			}
		} else {
			soundsAtPos = ImmutableList.of();
		}
		boolean hasSlow = false, hasFast = false;
		for (ISound s:soundsAtPos) {
			if (s.getSoundLocation().equals(TURN_FAST.getSoundName())) {
				hasFast = true;
			} else if (s.getSoundLocation().equals(TURN_SLOW.getSoundName())) {
				hasSlow = true;
			}
		}
		if (!hasSlow && energy.getVolumeSlow() > 0) {
			ISound snd = new IWTickableSound(TURN_SLOW, SoundCategory.BLOCKS, energy::getVolumeSlow, energy::getPitch,
					te.getPos().getX(), te.getPos().getY(), te.getPos().getZ());
			sndHandler.playSound(snd);
			addSound(te.getPos(), snd);
		}
		if (!hasFast && energy.getVolumeFast() > 0) {
			ISound snd = new IWTickableSound(TURN_FAST, SoundCategory.BLOCKS, energy::getVolumeFast, energy::getPitch,
					te.getPos().getX(), te.getPos().getY(), te.getPos().getZ());
			sndHandler.playSound(snd);
			addSound(te.getPos(), snd);
		}
	}

	@Override
	public void playMarxBang(TileEntityMarx te, Vec3d pos, float energy) {
		SoundEvent soundLoc = MARX_BANG;
		if (energy<0) {
			energy = -energy;
			soundLoc = MARX_POP;
		}
		PositionedSoundRecord sound = new PositionedSoundRecord(soundLoc.getSoundName(), SoundCategory.BLOCKS, 5*energy, 1,
				false, 0, ISound.AttenuationType.LINEAR, (float) pos.x, (float) pos.y, (float) pos.z);
		ClientUtils.mc().getSoundHandler().playSound(sound);
		addSound(te.getPos(), sound);
	}

	private void addSound(BlockPos pos, ISound sound) {
		List<ISound> allForPos = playingSounds.get(pos);
		if (allForPos==null) {
			allForPos = new ArrayList<>();
		}
		allForPos.add(sound);
		if (allForPos.size()==1) {
			playingSounds.put(pos, allForPos);
		}
	}

	@Override
	public void stopAllSoundsExcept(BlockPos pos, Set<?> excluded) {
		if (playingSounds.containsKey(pos)) {
			SoundHandler manager = Minecraft.getMinecraft().getSoundHandler();
			List<ISound> sounds = playingSounds.get(pos);
			List<ISound> toRemove = new ArrayList<>(sounds.size()-excluded.size());
			for (ISound sound:sounds) {
				if (!excluded.contains(sound)) {
					manager.stopSound(sound);
					toRemove.add(sound);
				}
			}
			sounds.removeAll(toRemove);
			if (sounds.isEmpty()) {
				playingSounds.remove(pos);
			}
		}
	}

	@Override
	public boolean isSingleplayer() {
		return Minecraft.getMinecraft().isSingleplayer();
	}

	@Override
	public boolean isValidTextureSource(ItemStack stack) {
		if (!super.isValidTextureSource(stack)) {
			return false;
		}
		IBakedModel texModel = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack,
				null, null);
		TextureAtlasSprite sprite = texModel.getParticleTexture();
		//noinspection ConstantConditions
		if (sprite == null || sprite.hasAnimationMetadata()) {
			return false;
		}
		int[][] data = sprite.getFrameTextureData(0);
		for (int x = 0; x < data.length; x++) {
			for (int y = 0; y < data[x].length; y++) {
				if ((data[x][y] >>> 24) != 255) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public Gui getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == 0) {
			TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
			if (te instanceof TileEntityRSPanel) {
				return new GuiRSPanelConn((TileEntityRSPanel) te);
			}
			if (te instanceof TileEntityPanelCreator) {
				return new GuiPanelCreator(player.inventory, (TileEntityPanelCreator) te);
			}
		} else if (ID == 1) {
			EnumHand h = z == 1 ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
			ItemStack held = player.getHeldItem(h);
			if (!held.isEmpty()) {
				if (held.getItem() == IndustrialWires.panelComponent) {
					return new GuiPanelComponent(h, ItemPanelComponent.componentFromStack(held));
				} else if (held.getItem() == IndustrialWires.key) {
					return new GuiRenameKey(h);
				}
			}
		}
		return null;
	}
}
