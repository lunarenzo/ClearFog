package de.rapha149.clearfog.network.impl;

import de.rapha149.clearfog.cache.ViewDistanceCache;
import de.rapha149.clearfog.network.NetworkInjector;
import de.rapha149.clearfog.service.FogService;
import de.rapha149.clearfog.version.VersionWrapper;
import io.netty.channel.*;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public final class NettyNetworkInjector implements NetworkInjector {

    private static final String HANDLER_NAME = "ClearFog";
    private final ViewDistanceCache cache;
    private final FogService fogService;
    private final VersionWrapper wrapper;

    public NettyNetworkInjector(ViewDistanceCache cache, FogService fogService, VersionWrapper wrapper) {
        this.cache = Objects.requireNonNull(cache, "cache cannot be null");
        this.fogService = Objects.requireNonNull(fogService, "fogService cannot be null");
        this.wrapper = Objects.requireNonNull(wrapper, "wrapper cannot be null");
    }

    @Override
    public void registerServerPipelines() throws Exception {
        ChannelHandler packetInit = new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel channel) {
                channel.eventLoop().submit(() -> {
                    ChannelPipeline pipeline = channel.pipeline();
                    if (!pipeline.names().contains(HANDLER_NAME)) {
                        pipeline.addAfter("packet_handler", HANDLER_NAME, new ChannelDuplexHandler() {
                            private UUID playerId;

                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                try {
                                    Class<?> clazz = msg.getClass();

                                    if (clazz == wrapper.getLoginSuccessPacketClass()) {
                                        this.playerId = wrapper.getUUIDFromLoginPacket(msg);
                                    }

                                    if (this.playerId != null && (clazz == wrapper.getLoginPlayPacketClass() ||
                                            clazz == wrapper.getUpdateViewDistanceClass())) {
                                        
                                        // Thread-safe fallback to immutable config snapshot if PlayerJoinEvent hasn't populated cache yet
                                        int targetDistance = cache.get(this.playerId);
                                        if (targetDistance <= 0) {
                                            targetDistance = fogService.calculateEffectiveViewDistance(this.playerId, null, -1);
                                        }

                                        if (targetDistance > 0) {
                                            msg = wrapper.replaceViewDistance(msg, targetDistance);
                                            cache.put(this.playerId, targetDistance);
                                        }
                                    }
                                } catch (Throwable ignored) {
                                }
                                super.write(ctx, msg, promise);
                            }
                        });
                    }
                });
            }
        };

        ChannelHandler init = new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel channel) {
                channel.pipeline().addLast(packetInit);
            }
        };

        ChannelHandler handler = new ChannelInboundHandlerAdapter() {
            @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) {
                ((Channel) msg).pipeline().addFirst(init);
                ctx.fireChannelRead(msg);
            }
        };

        for (ChannelPipeline pipeline : wrapper.getServerPipelines()) {
            if (pipeline.names().contains(HANDLER_NAME)) {
                pipeline.remove(HANDLER_NAME);
            }
            pipeline.addFirst(HANDLER_NAME, handler);
        }
    }

    @Override
    public void unregisterServerPipelines() throws Exception {
        for (ChannelPipeline pipeline : wrapper.getServerPipelines()) {
            if (pipeline.names().contains(HANDLER_NAME)) {
                pipeline.remove(HANDLER_NAME);
            }
        }
    }

    @Override
    public void uninjectPlayer(Player player) {
    }
}
