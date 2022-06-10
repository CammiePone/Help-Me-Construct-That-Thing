package dev.cammiescorner.hmctt.mixin;

import dev.cammiescorner.hmctt.HmcttClient;
import dev.cammiescorner.hmctt.HmcttConfig;
import net.minecraft.client.render.*;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Matrix4f;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
	@Shadow @Final private BufferBuilderStorage bufferBuilders;
	@Shadow @Nullable private ClientWorld world;

	@Inject(method = "render", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=entities"))
	public void hmctt$renderShit(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo info) {
		if(HmcttClient.structureMap != null && !HmcttClient.structureMap.isEmpty()) {
			VertexConsumerProvider vertexConsumers = renderLayer -> bufferBuilders.getEntityVertexConsumers().getBuffer(renderLayer);
			HmcttClient.renderStructureShenigans(matrices, vertexConsumers, world, camera, tickDelta, HmcttConfig.animateBlocks);
		}
	}
}
