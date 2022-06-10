package dev.cammiescorner.hmctt.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.cammiescorner.hmctt.HmcttClient;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3f;

import java.util.*;

public class HmcttMenuScreen extends Screen {
	public static final Identifier TEXTURE = HmcttClient.id("textures/gui/menu.png");
	protected List<ButtonWidget> structures = new ArrayList<>();
	protected ButtonWidget deselectButton, placeModeButton, refreshButton, rotateLeftButton, rotateRightButton, mirrorVerticalButton, mirrorHorizontalButton;
	protected TextFieldWidget searchBar, textX, textY, textZ;
	protected int x, y, posX, posY, posZ, scrollProgress;

	public HmcttMenuScreen() {
		super(new TranslatableText("screen." + HmcttClient.MOD_ID + ".menu"));
	}

	@Override
	public void init() {
		super.init();
		x = (width - 256) / 2;
		y = (height - 158) / 2;
		HmcttClient.reloadStructureFiles();
		refreshButton = addDrawableChild(new HmcttButtonWidget(x + 89, y + 7, HmcttButtonWidget.ButtonType.REFRESH, this::doTransformButtonShit));
		rotateLeftButton = addDrawableChild(new HmcttButtonWidget(x + 112, y + 10, HmcttButtonWidget.ButtonType.ROTATE_LEFT, this::doTransformButtonShit));
		rotateRightButton = addDrawableChild(new HmcttButtonWidget(x + 232, y + 10, HmcttButtonWidget.ButtonType.ROTATE_RIGHT, this::doTransformButtonShit));
		mirrorVerticalButton = addDrawableChild(new HmcttButtonWidget(x + 164, y + 10, HmcttButtonWidget.ButtonType.MIRROR_VERTICAL, this::doTransformButtonShit));
		mirrorHorizontalButton = addDrawableChild(new HmcttButtonWidget(x + 180, y + 10, HmcttButtonWidget.ButtonType.MIRROR_HORIZONTAL, this::doTransformButtonShit));
		placeModeButton = addDrawableChild(new ButtonWidget(x + 7, y + 131, 96, 20, new LiteralText("Place Structure"), this::doPlaceModeButtonShit));

		if(client != null) {
			searchBar = addDrawableChild(new TextFieldWidget(client.textRenderer, x + 7, y + 7, 80, 14, new LiteralText("")));
			textX = addDrawableChild(new TextFieldWidget(client.textRenderer, x + 113, y + 133, 40, 16, new LiteralText("")));
			textY = addDrawableChild(new TextFieldWidget(client.textRenderer, x + 159, y + 133, 40, 16, new LiteralText("")));
			textZ = addDrawableChild(new TextFieldWidget(client.textRenderer, x + 205, y + 133, 40, 16, new LiteralText("")));

			textX.setText(String.valueOf(HmcttClient.offset.getX()));
			textY.setText(String.valueOf(HmcttClient.offset.getY()));
			textZ.setText(String.valueOf(HmcttClient.offset.getZ()));
		}

		deselectButton = addDrawableChild(new ButtonWidget(x + 8, y + 24, 78, 20, new LiteralText("-- None --"), this::doListButtonShit));
		drawListButtons();
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		int scroll = (int) Math.ceil(scrollProgress * (85 / Math.max(1F, structures.size() - 4)));
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, TEXTURE);
		DrawableHelper.drawTexture(matrices, x, y, 0, 0, 0, 256, 158, 256, 256);
		DrawableHelper.drawTexture(matrices, x + 90, y + 24 + scroll, 0, 0, 160, 12, 15, 256, 256);
		setBlockPos();
		renderStructurePreview(matrices, delta);

		placeModeButton.active = HmcttClient.listIndex >= 0;
		rotateLeftButton.active = HmcttClient.listIndex >= 0;
		rotateRightButton.active = HmcttClient.listIndex >= 0;
		mirrorVerticalButton.active = HmcttClient.listIndex >= 0;
		mirrorHorizontalButton.active = HmcttClient.listIndex >= 0;
		deselectButton.active = HmcttClient.listIndex >= 0;
		deselectButton.visible = scrollProgress <= 0;

		for(ButtonWidget widget : structures) {
			int index = structures.indexOf(widget);
			widget.active = HmcttClient.listIndex != index;
			widget.visible = index - 4 < scrollProgress && index >= scrollProgress - 1;
			widget.y = y + 44 + 20 * (index - scrollProgress);
		}

		super.render(matrices, mouseX, mouseY, delta);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		if(textX.isFocused() && (mouseX >= textX.x && mouseX <= textX.x + textX.getWidth()) && (mouseY >= textX.y && mouseY <= textX.y + textX.getHeight())) {
			if(amount > 0)
				posX++;
			if(amount < 0)
				posX--;

			textX.setText(String.valueOf(posX));
		}
		if(textY.isFocused() && (mouseX >= textY.x && mouseX <= textY.x + textY.getWidth()) && (mouseY >= textY.y && mouseY <= textY.y + textY.getHeight())) {
			if(amount > 0)
				posY++;
			if(amount < 0)
				posY--;

			textY.setText(String.valueOf(posY));
		}
		if(textZ.isFocused() && (mouseX >= textZ.x && mouseX <= textZ.x + textZ.getWidth()) && (mouseY >= textZ.y && mouseY <= textZ.y + textZ.getHeight())) {
			if(amount > 0)
				posZ++;
			if(amount < 0)
				posZ--;

			textZ.setText(String.valueOf(posZ));
		}

		if(mouseX >= x + 7 && mouseX <= x + 102 && mouseY >= y + 23 && mouseY <= y + 124) {
			if(scrollProgress > 0 && amount > 0)
				scrollProgress--;
			if(scrollProgress < structures.size() - 4 && amount < 0)
				scrollProgress++;
		}

		return super.mouseScrolled(mouseX, mouseY, amount);
	}

	// TODO renders in the world instead of the menu, not sure how to fix
	private void renderStructurePreview(MatrixStack matrices, float delta) {
		if(client != null && client.world != null) {
			BlockRenderManager blockRenderer = client.getBlockRenderManager();
			VertexConsumerProvider vertexConsumers = renderLayer -> client.worldRenderer.bufferBuilders.getEntityVertexConsumers().getBuffer(renderLayer);
			Random random = new Random();
			float time = HmcttClient.clientTick + delta;

			matrices.push();
			matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(time));
			matrices.translate(-HmcttClient.structureWidth * 0.5, 0, -HmcttClient.structureLength * 0.5);

			for(Map.Entry<BlockPos, BlockState> entry : HmcttClient.structureMap.entrySet()) {
				BlockPos pos = entry.getKey();
				BlockState state = entry.getValue();
				VertexConsumer vertices = vertexConsumers.getBuffer(RenderLayer.getTranslucent());

				matrices.push();
				matrices.translate(pos.getX(), pos.getY(), pos.getZ());
				blockRenderer.renderBlock(state, pos, client.world, matrices, vertices, true, random);
				matrices.pop();
			}

			matrices.pop();
		}
	}

	private void drawListButtons() {
		if(!structures.isEmpty()) {
			for(ButtonWidget widget : structures)
				remove(widget);

			structures.clear();
		}

		for(int i = 0; i < HmcttClient.structureFiles.size(); i++) {
			String fileName = HmcttClient.structureFiles.get(i).getFileName().toString();
			ButtonWidget button = new ButtonWidget(x + 8, y + (i * 20) + 44, 78, 20, new LiteralText(fileName), this::doListButtonShit);

			structures.add(button);
			addDrawableChild(button);
		}
	}

	private void setBlockPos() {
		try {
			posX = Integer.parseInt(textX.getText());
		}
		catch(NumberFormatException e) {
			posX = 0;
		}

		try {
			posY = Integer.parseInt(textY.getText());
		}
		catch(NumberFormatException e) {
			posY = 0;
		}

		try {
			posZ = Integer.parseInt(textZ.getText());
		}
		catch(NumberFormatException e) {
			posZ = 0;
		}

		HmcttClient.offset.set(posX, posY, posZ);
	}

	private void doListButtonShit(ButtonWidget button) {
		if(structures.contains(button)) {
			HmcttClient.listIndex = structures.indexOf(button);
			HmcttClient.constructStructureMap(HmcttClient.listIndex);
			button.active = false;
			return;
		}

		HmcttClient.listIndex = -1;
		HmcttClient.structureMap.clear();
	}

	private void doPlaceModeButtonShit(ButtonWidget button) {
		HmcttClient.placeMode = !HmcttClient.placeMode;
		HmcttClient.constructStructureMap(HmcttClient.listIndex);
		close();
	}

	private void doTransformButtonShit(ButtonWidget b) {
		if(b instanceof HmcttButtonWidget button) {
			switch(button.type) {
				case REFRESH -> {
					HmcttClient.reloadStructureFiles();
					drawListButtons();
				}
				case ROTATE_LEFT -> {
					HmcttClient.rotation = HmcttClient.rotation.rotate(BlockRotation.COUNTERCLOCKWISE_90);
					HmcttClient.constructStructureMap(HmcttClient.listIndex);
				}
				case ROTATE_RIGHT -> {
					HmcttClient.rotation = HmcttClient.rotation.rotate(BlockRotation.CLOCKWISE_90);
					HmcttClient.constructStructureMap(HmcttClient.listIndex);
				}
				case MIRROR_VERTICAL -> {
					if(HmcttClient.mirror == BlockMirror.FRONT_BACK)
						HmcttClient.mirror = BlockMirror.NONE;
					else
						HmcttClient.mirror = BlockMirror.FRONT_BACK;

					HmcttClient.constructStructureMap(HmcttClient.listIndex);
				}
				case MIRROR_HORIZONTAL -> {
					if(HmcttClient.mirror == BlockMirror.LEFT_RIGHT)
						HmcttClient.mirror = BlockMirror.NONE;
					else
						HmcttClient.mirror = BlockMirror.LEFT_RIGHT;

					HmcttClient.constructStructureMap(HmcttClient.listIndex);
				}
			}
		}
	}
}
