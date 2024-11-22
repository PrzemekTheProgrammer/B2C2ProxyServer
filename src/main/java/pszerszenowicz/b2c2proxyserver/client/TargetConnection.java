package pszerszenowicz.b2c2proxyserver.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import pszerszenowicz.b2c2proxyserver.server.Server;

import java.net.URI;

public class TargetConnection {

    //    private final String targetHost = "wss://socket.uat.b2c2.net/quotes";
    private final String targetHost = "localhost";
    private final int targetPort = 8081;

    private Channel channel;

    public void start(Server server) throws Exception {
        URI uri = new URI("ws://" + targetHost + ":" + targetPort + "/");
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            final WebSocketClientHandler handler =
                    new WebSocketClientHandler(
                            WebSocketClientHandshakerFactory.newHandshaker(
                                    uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders()),
                            server
                    );
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(
                                    new LoggingHandler(LogLevel.INFO),
                                    new HttpClientCodec(),
                                    new HttpObjectAggregator(9182),
                                    handler);
                        }
                    });
            ChannelFuture future = bootstrap.connect(targetHost, targetPort).sync();
            channel = future.channel();
            future.channel().closeFuture().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    group.shutdownGracefully();
                }
            });
        } catch (Exception e) {
            group.shutdownGracefully();
        }
    }

    public void sendMessage(String message) {
        if (channel != null && channel.isActive()) {
            WebSocketFrame toSend = new TextWebSocketFrame(message);
            channel.writeAndFlush(toSend);
        }
    }

    public void sendMessage(WebSocketFrame message) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(message);
        }
    }

    public void closeChannel() {
        if (channel != null && channel.isActive()) {
            channel.close();
        }
    }

}
