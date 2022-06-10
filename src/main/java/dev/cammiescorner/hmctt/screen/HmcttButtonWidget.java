package dev.cammiescorner.hmctt.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.cammiescorner.hmctt.HmcttClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class HmcttButtonWidget extends ButtonWidget {
	private static final Identifier TEXTURE = HmcttClient.id("textures/gui/widgets.png");
	public final ButtonType type;

	public HmcttButtonWidget(int x, int y, ButtonType type, PressAction onPress) {
		super(x, y, 14, 14, new TranslatableText("text." + HmcttClient.MOD_ID + ".refresh_button"), onPress);
		this.type = type;
	}

	@Override
	public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		MinecraftClient client = MinecraftClient.getInstance();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TEXTURE);
		RenderSystem.setShaderColor(1F, 1F, 1F, alpha);
		int i = getYImage(isHovered());
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		drawTexture(matrices, x, y, 0, i * 16, width, height);
		drawTexture(matrices, x, y, 16, type.ordinal() * 16, width, height);
		renderBackground(matrices, client, mouseX, mouseY);

		if(isHovered())
			renderTooltip(matrices, mouseX, mouseY);
	}

	public enum ButtonType {
		REFRESH, ROTATE_LEFT, ROTATE_RIGHT, MIRROR_VERTICAL, MIRROR_HORIZONTAL
	}
}
