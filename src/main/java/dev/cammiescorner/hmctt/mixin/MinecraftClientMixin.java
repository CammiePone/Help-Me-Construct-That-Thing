package dev.cammiescorner.hmctt.mixin;

import dev.cammiescorner.hmctt.HmcttClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@Shadow @Final public GameRenderer gameRenderer;
	@Shadow @Nullable public HitResult crosshairTarget;
	@Shadow @Nullable public ClientPlayerEntity player;

	@Inject(method = "doItemUse", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/item/ItemStack;isEmpty()Z",
			ordinal = 1
	), locals = LocalCapture.CAPTURE_FAILSOFT, cancellable = true)
	public void arcanus$castSpell(CallbackInfo info, Hand[] var1, int var2, int var3, Hand hand, ItemStack itemStack) {
		if(player != null && HmcttClient.placeMode && crosshairTarget instanceof BlockHitResult hitResult) {
			HmcttClient.offset.set(hitResult.getBlockPos().offset(hitResult.getSide()));
			player.swingHand(Hand.MAIN_HAND);
			gameRenderer.firstPersonRenderer.resetEquipProgress(hand);
			HmcttClient.placeMode = false;
			info.cancel();
		}
	}
}
