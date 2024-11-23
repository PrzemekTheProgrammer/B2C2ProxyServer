package pszerszenowicz.b2c2proxyserver.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import pszerszenowicz.b2c2proxyserver.client.TargetConnection;

import java.util.HashMap;
import java.util.Map;


public class Server {

    private final int port = 8080;
    private Map<TargetConnection,Channel> channels = new HashMap<>();

    public void start() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            TargetConnection targetConnection = new TargetConnection();
                            socketChannel.pipeline().addLast(
                                    new WebSocketServerInitializer(targetConnection)
                            );
                            targetConnection.start(Server.this);
                            channels.put(targetConnection,socketChannel);
                        }
                    });
            ChannelFuture f = b.bind(port).sync();
            f.channel().closeFuture().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    bossGroup.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }
            });
        } catch (InterruptedException e) {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void sendMessage(String message, TargetConnection targetConnection) {
        Channel channel = channels.get(targetConnection);
        if (channel != null && channel.isActive()) {
            TextWebSocketFrame toSend = new TextWebSocketFrame(message);
            channel.writeAndFlush(toSend);
        }
    }

    public void deleteUnusedTargetConnection(TargetConnection targetConnection) {
        channels.remove(targetConnection);
    }
}
