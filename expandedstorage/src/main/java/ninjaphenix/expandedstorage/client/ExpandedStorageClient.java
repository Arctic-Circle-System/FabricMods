package ninjaphenix.expandedstorage.client;

import blue.endless.jankson.JsonPrimitive;
import io.netty.buffer.Unpooled;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.BlockItem;
import ninjaphenix.chainmail.api.config.JanksonConfigParser;
import ninjaphenix.expandedstorage.client.config.ContainerConfig;
import ninjaphenix.expandedstorage.client.screen.PagedScreen;
import ninjaphenix.expandedstorage.client.screen.ScrollableScreen;
import ninjaphenix.expandedstorage.client.screen.SelectContainerScreen;
import ninjaphenix.expandedstorage.client.screen.SingleScreen;
import ninjaphenix.expandedstorage.common.Const;
import ninjaphenix.expandedstorage.common.ExpandedStorage;
import ninjaphenix.expandedstorage.common.ModContent;
import ninjaphenix.expandedstorage.common.Registries;
import ninjaphenix.expandedstorage.common.block.CursedChestBlock;
import ninjaphenix.expandedstorage.common.block.entity.CursedChestBlockEntity;
import ninjaphenix.expandedstorage.common.misc.CursedChestType;
import org.apache.logging.log4j.MarkerManager;

import static ninjaphenix.expandedstorage.common.ModContent.*;

public final class ExpandedStorageClient implements ClientModInitializer
{
    @SuppressWarnings({"unused", "RedundantSuppression"})
    public static final ExpandedStorageClient INSTANCE = new ExpandedStorageClient();
    public static final ContainerConfig CONFIG = getConfigParser().load(ContainerConfig.class, ContainerConfig::new, getConfigPath(), new MarkerManager.Log4jMarker(Const.MOD_ID));
    private static final CursedChestBlockEntity CURSED_CHEST_RENDER_ENTITY = new CursedChestBlockEntity(null);

    static
    {
        if (CONFIG.preferred_container_type.getNamespace().equals("ninjaphenix-container-lib"))
        {
            setPreference(new ResourceLocation(Const.MOD_ID, CONFIG.preferred_container_type.getPath()));
        }
    }

    private static JanksonConfigParser getConfigParser()
    {
        return new JanksonConfigParser.Builder().deSerializer(
                JsonPrimitive.class, ResourceLocation.class, (it, marshaller) -> new ResourceLocation(it.asString()),
                ((identifier, marshaller) -> marshaller.serialize(identifier.toString()))).build();
    }

    private static Path getConfigPath() { return FabricLoader.getInstance().getConfigDir().resolve("ninjaphenix-container-library.json"); }

    public static void sendPreferencesToServer()
    {
        ClientPlayNetworking.send(Const.SCREEN_SELECT, new FriendlyByteBuf(Unpooled.buffer())
                .writeResourceLocation(CONFIG.preferred_container_type));
    }

    public static void sendCallbackRemoveToServer()
    {
        ClientPlayNetworking.send(Const.SCREEN_SELECT, new FriendlyByteBuf(Unpooled.buffer())
                .writeResourceLocation(Const.resloc("auto")));
    }

    public static void setPreference(final ResourceLocation handlerType)
    {
        CONFIG.preferred_container_type = handlerType;
        getConfigParser().save(CONFIG, getConfigPath(), new MarkerManager.Log4jMarker(Const.MOD_ID));
    }

    @Override
    public void onInitializeClient()
    {
        //noinspection CodeBlock2Expr
        ClientPlayConnectionEvents.INIT.register((handler, client) ->
                                                 {
                                                     ClientPlayNetworking.registerReceiver(Const.SCREEN_SELECT, ((minecraft, listener, buffer, sender) ->
                                                     {
                                                         final int count = buffer.readInt();
                                                         final HashMap<ResourceLocation, Tuple<ResourceLocation, Component>> allowed = new HashMap<>();
                                                         for (int i = 0; i < count; i++)
                                                         {
                                                             final ResourceLocation containerFactoryId = buffer.readResourceLocation();
                                                             if (ExpandedStorage.INSTANCE.isContainerTypeDeclared(containerFactoryId))
                                                             {
                                                                 allowed.put(containerFactoryId, ExpandedStorage.INSTANCE.getScreenSettings(containerFactoryId));
                                                             }
                                                         }
                                                         minecraft.setScreen(new SelectContainerScreen(allowed));
                                                     }));
                                                 });
        ClientSpriteRegistryCallback.event(Sheets.CHEST_SHEET).register(
                (atlas, registry) -> Registries.CHEST.stream().forEach(data -> Arrays.stream(CursedChestType.values())
                        .map(data::getChestTexture).forEach(registry::register)));
        BlockEntityRendererRegistry.INSTANCE.register(ModContent.CHEST, CursedChestBlockEntityRenderer::new);
        ModContent.CHEST.validBlocks.forEach(block -> BuiltinItemRendererRegistry.INSTANCE.register(
                block.asItem(), (itemStack, type, stack, vertexConsumers, light, overlay) ->
                {
                    CursedChestBlock renderBlock = (CursedChestBlock) ((BlockItem) itemStack.getItem()).getBlock();
                    CURSED_CHEST_RENDER_ENTITY.setBlock(renderBlock.TIER_ID);
                    BlockEntityRenderDispatcher.instance.renderItem(CURSED_CHEST_RENDER_ENTITY, stack, vertexConsumers, light, overlay);
                }));
        ScreenRegistry.register(SCROLLABLE_HANDLER_TYPE, ScrollableScreen::new);
        ScreenRegistry.register(PAGED_HANDLER_TYPE, PagedScreen::new);
        ScreenRegistry.register(SINGLE_HANDLER_TYPE, SingleScreen::new);
    }
}