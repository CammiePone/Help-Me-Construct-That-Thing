package dev.cammiescorner.hmctt;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.cammiescorner.hmctt.models.TranslucentBakedModel;
import dev.cammiescorner.hmctt.screen.HmcttMenuScreen;
import eu.midnightdust.lib.config.MidnightConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.List;

public class HmcttClient implements ClientModInitializer {
	public static final String MOD_ID = "hmctt";
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	public static KeyBinding menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key." + MOD_ID + ".menu",
			GLFW.GLFW_KEY_M,
			"category." + MOD_ID + ".keyBinds"
	));
	public static List<Path> structureFiles = new ArrayList<>();
	public static HashMap<BlockPos, BlockState> structureMap = new HashMap<>();
	public static BlockPos.Mutable offset = BlockPos.ORIGIN.mutableCopy();
	public static BlockRotation rotation = BlockRotation.NONE;
	public static BlockMirror mirror = BlockMirror.NONE;
	public static int clientTick = 0;
	public static int listIndex = -1;
	public static int structureWidth, structureLength;
	public static boolean placeMode = false;

	@Override
	public void onInitializeClient() {
		MidnightConfig.init(MOD_ID, HmcttConfig.class);

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			clientTick++;

			if(menuKey.wasPressed())
				MinecraftClient.getInstance().setScreen(new HmcttMenuScreen());
			if(placeMode && client.crosshairTarget instanceof BlockHitResult hitResult)
				offset.set(hitResult.getBlockPos().offset(hitResult.getSide()));
		});
	}

	public static Identifier id(String name) {
		return new Identifier(MOD_ID, name);
	}

	public static void renderStructureShenigans(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ClientWorld world, Camera camera, float tickDelta, boolean animated) {
		BlockRenderManager blockRenderer = MinecraftClient.getInstance().getBlockRenderManager();
		Random random = new Random();
		double time = clientTick + tickDelta;
		float scale = animated ? (float) (0.8125F + (Math.sin(time * 0.075) * 0.0625F)) : 1;
		int rgb;

		try {
			rgb = Integer.decode(HmcttConfig.wrongBlockColour);

			if(rgb > 0xffffff || rgb < 0)
				throw new NumberFormatException("ARGB value unsupported!");
		}
		catch(NumberFormatException e) {
			rgb = 0xff0000;
			LOGGER.error(HmcttConfig.wrongBlockColour + " is not a valid colour!");
			HmcttConfig.wrongBlockColour = "#ff0000";
		}

		float[] colour = new float[]{(rgb >> 16 & 255) / 255F, (rgb >> 8 & 255) / 255F, (rgb & 255) / 255F};

		random.setSeed(42L);
		matrices.push();
		matrices.translate(-camera.getPos().getX(), -camera.getPos().getY(), -camera.getPos().getZ());
		matrices.translate(offset.getX(), offset.getY(), offset.getZ());

		for(Map.Entry<BlockPos, BlockState> entry : structureMap.entrySet()) {
			BlockPos pos = entry.getKey();
			BlockPos adjustedPos = pos.add(offset);
			BlockState state = entry.getValue();
			BlockState realState = world.getBlockState(adjustedPos);
			VertexConsumer vertices;
			BakedModel model;

			if(!realState.equals(state)) {
				matrices.push();

				if(!realState.isAir()) {
					matrices.translate(pos.getX(), pos.getY(), pos.getZ());
					matrices.translate(-0.0005, -0.0005, -0.0005);
					matrices.scale(1.001F, 1.001F, 1.001F);

					vertices = vertexConsumers.getBuffer(RenderLayer.getLightning());
					RenderSystem.setShaderColor(colour[0], colour[1], colour[2], 1F);
					model = blockRenderer.getModel(Blocks.STONE.getDefaultState());
				}
				else {
					matrices.translate(pos.getX(), pos.getY(), pos.getZ());

					matrices.translate(0.5, 0.5, 0.5);

					if(animated)
						matrices.scale(scale, scale, scale);
					else
						matrices.scale(HmcttConfig.blockScale, HmcttConfig.blockScale, HmcttConfig.blockScale);

					matrices.translate(-0.5, -0.5, -0.5);

					vertices = vertexConsumers.getBuffer(RenderLayer.getTranslucent());
					model = TranslucentBakedModel.wrap(blockRenderer.getModel(state), () -> HmcttConfig.blockTransparency);
				}

				blockRenderer.getModelRenderer().render(world, model, state, offset, matrices, vertices, !animated, random, state.getRenderingSeed(offset), OverlayTexture.DEFAULT_UV);
				matrices.pop();
			}
		}

		matrices.pop();
	}

	public static void reloadStructureFiles() {
		structureFiles.clear();

		Path configFolderPath = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + "/structures");

		try {
			Files.createDirectories(configFolderPath);
			Files.walkFileTree(configFolderPath, new SimpleFileVisitor<>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					if(file.getFileName().toString().endsWith(".nbt"))
						structureFiles.add(file);

					return FileVisitResult.CONTINUE;
				}
			});
		}
		catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void constructStructureMap(int index) {
		if(structureFiles.isEmpty() || index >= structureFiles.size() || index < 0) {
			LOGGER.warn("Index " + index + " is out of bounds for list of size " + structureFiles.size());
			return;
		}

		structureMap.clear();

		BlockPos pos = BlockPos.ORIGIN;
		StructurePlacementData placementData = new StructurePlacementData();
		placementData.setRotation(rotation);
		placementData.setMirror(mirror);
		Structure structure = new Structure();

		try {
			NbtCompound nbt = NbtIo.readCompressed(structureFiles.get(index).toFile());
			structure.readNbt(nbt);
		}
		catch(IOException e) {
			LOGGER.error("Unable to read structure file [" + structureFiles.get(index) + "]", e);
			return;
		}

		List<Structure.StructureBlockInfo> randInfoList = placementData.getRandomBlockInfos(structure.blockInfoLists, pos).getAll();
		List<Structure.StructureBlockInfo> infoList = Structure.process(MinecraftClient.getInstance().world, pos, pos, placementData, randInfoList);

		for(Structure.StructureBlockInfo info : infoList)
			if(!info.state.isAir())
				structureMap.put(info.pos, info.state);

		structureWidth = structure.getSize().getX();
		structureLength = structure.getSize().getZ();
	}
}
