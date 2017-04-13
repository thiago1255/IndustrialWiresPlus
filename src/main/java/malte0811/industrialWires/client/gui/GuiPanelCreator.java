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

package malte0811.industrialWires.client.gui;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.common.util.IELogger;
import malte0811.industrialWires.IndustrialWires;
import malte0811.industrialWires.blocks.controlpanel.BlockTypes_Panel;
import malte0811.industrialWires.controlpanel.PanelComponent;
import malte0811.industrialWires.blocks.controlpanel.TileEntityPanelCreator;
import malte0811.industrialWires.containers.ComponentFakeSlot;
import malte0811.industrialWires.containers.ContainerPanelCreator;
import malte0811.industrialWires.controlpanel.MessageType;
import malte0811.industrialWires.network.MessageGUIInteract;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;

import java.io.IOException;

public class GuiPanelCreator extends GuiContainer {
	private int heightWithoutInv = 130;
	public int panelSize = 128;
	private ContainerPanelCreator container;

	public GuiPanelCreator(InventoryPlayer ip, TileEntityPanelCreator te) {
		super(new ContainerPanelCreator(ip, te));
		container = (ContainerPanelCreator) inventorySlots;
		ySize = 207;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		//TODO proper background
		int x0 = getX0();
		int y0 = getY0();
		int xRel = mouseX - x0;
		int yRel = mouseY - y0;
		GlStateManager.color(1, 1, 1, 1);
		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite("minecraft:blocks/iron_block");
		drawTexturedRect(x0, x0 + panelSize, y0, y0 + panelSize, sprite.getMinU(), sprite.getMaxU(), sprite.getMinV(), sprite.getMaxV());
		PanelComponent curr = getFloatingPC();
		if (curr!=null && 0 <= xRel && xRel <= panelSize && 0 <= yRel && yRel <= panelSize) {
			drawPanelComponent(curr, xRel, yRel);
		}
		for (PanelComponent pc : container.tile.components) {
			drawPanelComponent(pc, -1, -1);
		}
	}
	private void drawPanel() {

	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	private void drawPanelComponent(PanelComponent pc, int x, int y) {
		if (x >= 0 && y >= 0) {
			pc.setX(x / (float) panelSize);
			pc.setY(y / (float) panelSize);
		}
		if (!pc.isValidPos()) {
			AxisAlignedBB aabb = pc.getBlockRelativeAABB();
			int left = (int) (getX0()+aabb.minX*panelSize)-1;
			int top = (int) (getY0()+aabb.minZ*panelSize)-1;
			int right = (int) (getX0()+aabb.maxX*panelSize)+1;
			int bottom = (int) (getY0()+aabb.maxZ*panelSize)+1;
			Gui.drawRect(left, top, right, bottom, 0xffff0000);
		}
		pc.renderInGUI(this);
		GlStateManager.color(1, 1, 1);
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.clear();
		buttonList.add(new GuiButton(0, guiLeft, guiTop+54, 16, 16, "C"));
		buttonList.add(new GuiButton(1, guiLeft, guiTop+72, 16, 16, "D"));
	}

	private void drawTexturedRect(float xMin, float xMax, float yMin, float yMax, float uMin, float uMax, float vMin, float vMax) {
		Tessellator tes = Tessellator.getInstance();
		VertexBuffer buf = tes.getBuffer();
		buf.begin(7, DefaultVertexFormats.POSITION_TEX);
		buf.pos(xMin, yMax, zLevel).tex(uMin, vMax).endVertex();
		buf.pos(xMax, yMax, zLevel).tex(uMax, vMax).endVertex();
		buf.pos(xMax, yMin, zLevel).tex(uMax, vMin).endVertex();
		buf.pos(xMin, yMin, zLevel).tex(uMin, vMin).endVertex();
		tes.draw();
	}

	@Override
	protected void drawSlot(Slot slot) {
		if (slot instanceof ComponentFakeSlot && ((ComponentFakeSlot) slot).isSelected()) {
			drawRect(slot.xDisplayPosition, slot.yDisplayPosition, slot.xDisplayPosition + 16, slot.yDisplayPosition + 16, 0x8000ff00);
		}
		super.drawSlot(slot);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		int x0 = getX0();
		int y0 = getY0();
		int xRel = mouseX - x0;
		int yRel = mouseY - y0;
		PanelComponent curr = getFloatingPC();
		if (curr != null && 0 <= xRel && xRel <= panelSize && 0 <= yRel && yRel <= panelSize) {
			if (curr.isValidPos()) {
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setTag("component", new NBTTagCompound());
				curr.writeToNBT(nbt.getCompoundTag("component"), true);
				nbt.setInteger("type", MessageType.ADD.ordinal());
				IndustrialWires.packetHandler.sendToServer(new MessageGUIInteract(container.tile, nbt));
				container.tile.components.add(curr.copyOf());
				ItemStack currStack = mc.thePlayer.inventory.getItemStack();
				if (currStack != null) {
					currStack.stackSize--;
					if (currStack.stackSize <= 0) {
						mc.thePlayer.inventory.setItemStack(null);

					}
				}
			}
		}
	}

	public int getX0() {
		return (width - panelSize) / 2;
	}

	public int getY0() {
		return heightWithoutInv - panelSize;
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		NBTTagCompound nbt = new NBTTagCompound();
		switch (button.id) {
		case 0://create panel
			nbt.setInteger("type", MessageType.CREATE_PANEL.ordinal());
			break;
		case 1:// Delete all
			nbt.setInteger("type", MessageType.REMOVE_ALL.ordinal());
			break;
		}
		if (!nbt.hasNoTags()) {
			IndustrialWires.packetHandler.sendToServer(new MessageGUIInteract(container.tile, nbt));
		}
	}
	private ItemStack lastFloating;
	private PanelComponent lastFloatingPC;
	private PanelComponent getFloatingPC() {
		ItemStack floating = mc.thePlayer.inventory.getItemStack();
		if (floating==null||floating.getItem()!=IndustrialWires.panelComponent) {
			return null;
		}
		if (ItemStack.areItemStacksEqual(floating, lastFloating)) {
			return lastFloatingPC;
		}
		lastFloating = floating.copy();
		lastFloatingPC = IndustrialWires.panelComponent.componentFromStack(floating);
		return lastFloatingPC;
	}
}