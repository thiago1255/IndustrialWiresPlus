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

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import com.google.common.collect.ImmutableMap;
import malte0811.industrialwires.IndustrialWires;
import malte0811.industrialwires.blocks.BlockIWBase;
import malte0811.industrialwires.blocks.IMetaEnum;
import malte0811.industrialwires.blocks.controlpanel.TileEntityPanel;
import malte0811.industrialwires.blocks.hv.BlockHVMultiblocks;
import malte0811.industrialwires.client.panelmodel.PanelModel;
import malte0811.industrialwires.controlpanel.PanelComponent;
import malte0811.industrialwires.items.ItemIC2Coil;
import malte0811.industrialwires.items.ItemKey;
import malte0811.industrialwires.items.ItemPanelComponent;
import malte0811.industrialwires.items.ItemCraftingStuff;
import malte0811.industrialwires.mech_mb.MechMBPart;
import malte0811.industrialwires.wires.MixedWireType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.GuiIngameForge;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.Locale;
import java.util.Map;

import static malte0811.industrialwires.client.render.TileRenderMechMB.BASE_MODELS;

@Mod.EventBusSubscriber(modid = IndustrialWires.MODID, value = Side.CLIENT)
@SideOnly(Side.CLIENT)
public class ClientEventHandler {
	public static boolean shouldScreenshot = false;
	@SubscribeEvent(priority = EventPriority.LOW)
	public static void renderOverlayPost(RenderGameOverlayEvent.Post e) {
		if (ClientUtils.mc().player != null && e.getType() == RenderGameOverlayEvent.ElementType.TEXT) {
			EntityPlayer player = ClientUtils.mc().player;

			for (EnumHand hand : EnumHand.values()) {
				if (!player.getHeldItem(hand).isEmpty()) {
					ItemStack equipped = player.getHeldItem(hand);
					if (OreDictionary.itemMatches(new ItemStack(IndustrialWires.coil, 1, OreDictionary.WILDCARD_VALUE), equipped, false)) {
						MixedWireType type = MixedWireType.ALL[equipped.getItemDamage()];
						int color = type.getColour(null);
						final int threshold = 0x40-1;
						for (int i = 0;i<3;i++) {
							if (((color>>(8*i))&255)<threshold) {
								color |= threshold<<(8*i);
							}
						}
						String s = I18n.format(IndustrialWires.MODID + ".desc.wireLength", ItemIC2Coil.getLength(equipped));
						ClientUtils.font().drawString(s, e.getResolution().getScaledWidth() / 2 - ClientUtils.font().getStringWidth(s) / 2,
								e.getResolution().getScaledHeight() - GuiIngameForge.left_height - 40, color, true);
						if (ItemNBTHelper.hasKey(equipped, "linkingPos")) {
							int[] link = ItemNBTHelper.getIntArray(equipped, "linkingPos");
							if (link != null && link.length > 3) {
								s = I18n.format(Lib.DESC_INFO + "attachedTo", link[1], link[2], link[3]);
								RayTraceResult focussedBlock = ClientUtils.mc().objectMouseOver;
								double distSquared;
								if (focussedBlock != null && focussedBlock.typeOfHit == RayTraceResult.Type.BLOCK) {
									distSquared = focussedBlock.getBlockPos().distanceSq(link[1], link[2], link[3]);
								} else {
									distSquared = player.getDistanceSq(link[1], link[2], link[3]);
								}
								int length = Math.min(ItemIC2Coil.getLength(equipped), type.getMaxLength());
								if (length * length < distSquared) {
									color = 0xdd3333;
								}
								ClientUtils.font().drawString(s, e.getResolution().getScaledWidth() / 2 - ClientUtils.font().getStringWidth(s) / 2,
										e.getResolution().getScaledHeight() - GuiIngameForge.left_height - 20, color, true);
							}
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void renderBoundingBoxes(DrawBlockHighlightEvent event) {
		if (!event.isCanceled() && event.getSubID() == 0 && event.getTarget().typeOfHit == RayTraceResult.Type.BLOCK) {
			TileEntity tile = event.getPlayer().world.getTileEntity(event.getTarget().getBlockPos());
			if (tile instanceof TileEntityPanel) {
				TileEntityPanel panel = (TileEntityPanel) tile;
				Pair<PanelComponent, RayTraceResult> pc = panel.getSelectedComponent(Minecraft.getMinecraft().player, event.getTarget().hitVec, true);
				if (pc != null) {
					pc.getLeft().renderBox();
					event.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent
	public static void bakeModel(ModelBakeEvent event) {
		event.getModelRegistry().putObject(new ModelResourceLocation(IndustrialWires.MODID + ":control_panel", "inventory,type=top"), new PanelModel());
		event.getModelRegistry().putObject(new ModelResourceLocation(IndustrialWires.MODID + ":control_panel", "inventory,type=unfinished"), new PanelModel());
	}
	@SubscribeEvent
	public static void registerModels(ModelRegistryEvent evt) {
		if (IndustrialWires.coil!=null) {
			for (int meta = 0; meta < ItemIC2Coil.subNames.length; meta++) {
				ResourceLocation loc = new ResourceLocation(IndustrialWires.MODID, "ic2_wire_coil/" + ItemIC2Coil.subNames[meta]);
				ModelBakery.registerItemVariants(IndustrialWires.coil, loc);
				ModelLoader.setCustomModelResourceLocation(IndustrialWires.coil, meta, new ModelResourceLocation(loc, "inventory"));
			}
		}
		for (int meta = 0; meta < ItemCraftingStuff.subNames.length; meta++) {
			ResourceLocation loc = new ResourceLocation(IndustrialWires.MODID, "stuff_crafting/" + ItemCraftingStuff.subNames[meta]);
			ModelBakery.registerItemVariants(IndustrialWires.craftingStuff, loc);
			ModelLoader.setCustomModelResourceLocation(IndustrialWires.craftingStuff, meta, new ModelResourceLocation(loc, "inventory"));
		}
		for (int meta = 0; meta < ItemPanelComponent.types.length; meta++) {
			ResourceLocation loc = new ResourceLocation(IndustrialWires.MODID, "panel_component/" + ItemPanelComponent.types[meta]);
			ModelBakery.registerItemVariants(IndustrialWires.panelComponent, loc);
			ModelLoader.setCustomModelResourceLocation(IndustrialWires.panelComponent, meta, new ModelResourceLocation(loc, "inventory"));
		}
		for (int meta = 0; meta < ItemKey.types.length; meta++) {
			ResourceLocation loc = new ResourceLocation(IndustrialWires.MODID, "key/" + ItemKey.types[meta]);
			ModelBakery.registerItemVariants(IndustrialWires.key, loc);
			ModelLoader.setCustomModelResourceLocation(IndustrialWires.key, meta, new ModelResourceLocation(loc, "inventory"));
		}
		for (BlockIWBase b : IndustrialWires.blocks) {
			Item blockItem = Item.getItemFromBlock(b);
			final ResourceLocation loc = b.getRegistryName();
			assert loc != null;
			ModelLoader.setCustomMeshDefinition(blockItem, stack -> new ModelResourceLocation(loc, "inventory"));
			Object[] v = ((IMetaEnum) b).getValues();
			for (int meta = 0; meta < v.length; meta++) {
				String location = loc.toString();
				String prop = "inventory,type=" + v[meta].toString().toLowerCase(Locale.US);
				try {
					ModelLoader.setCustomModelResourceLocation(blockItem, meta, new ModelResourceLocation(location, prop));
				} catch (NullPointerException npe) {
					throw new RuntimeException(b + " lacks an item!", npe);
				}
			}
		}
		ModelLoader.setCustomStateMapper(IndustrialWires.hvMultiblocks, new StateMapperBase()
		{
			@Nonnull
			@Override
			protected ModelResourceLocation getModelResourceLocation(@Nonnull IBlockState state)
			{
				Map<IProperty<?>, Comparable<?>> properties = state.getProperties();
				boolean mirror = (Boolean) properties.get(IEProperties.BOOLEANS[0]);
				return new ModelResourceLocation(
						new ResourceLocation(IndustrialWires.MODID,
						BlockHVMultiblocks.NAME+(mirror?"_mirrored":"")),
						getPropertyString(properties));
			}
		});
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void renderWorldLastLow(RenderWorldLastEvent ev) {
		if (shouldScreenshot) {
			Minecraft mc = Minecraft.getMinecraft();
			ITextComponent comp = ScreenShotHelper.saveScreenshot(mc.gameDir, mc.displayWidth, mc.displayHeight, mc.getFramebuffer());//TODO
			mc.player.sendMessage(comp);
			shouldScreenshot = false;
		}
	}

	@SubscribeEvent
	public static void onTextureStitch(TextureStitchEvent event) {
		for (MechMBPart type:MechMBPart.INSTANCES.values()) {
			ResourceLocation loc = type.getRotatingBaseModel();
			try {
				IModel model = ModelLoaderRegistry.getModel(loc);
				if (model instanceof OBJModel) {
					model = model.process(ImmutableMap.of("flip-v", "true"));
				}
				model.getTextures().forEach((rl)->event.getMap().registerSprite(rl));
				IBakedModel b = model.bake(model.getDefaultState(), DefaultVertexFormats.BLOCK, (rl)->Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(rl.toString()));
				BASE_MODELS.put(loc, b);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}
}
