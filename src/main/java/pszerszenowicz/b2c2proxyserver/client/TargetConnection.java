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
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import pszerszenowicz.b2c2proxyserver.server.Server;

import javax.net.ssl.SSLException;
import java.net.URI;
import java.net.URISyntaxException;

public class TargetConnection {

    //    private final String targetHost = "wss://socket.uat.b2c2.net/quotes";
    private final String targetHost = "localhost";
    private final int targetPort = 8081;
    private Bootstrap bootstrap;
    private Channel channel;
    private final EventLoopGroup group = new NioEventLoopGroup();

    public void start(Server server) throws Exception {
        try {
            connect(server);
        } catch (SSLException | InterruptedException | URISyntaxException e) {
            stop();
        }
    }

    public void stop() {
        if (channel != null) {
            channel.pipeline().get(WebSocketClientHandler.class).disableReconnect();
            if (channel.isActive()) {
                channel.close();
            }
        }
        group.shutdownGracefully();
    }

    public void connect(Server server) throws InterruptedException, URISyntaxException, SSLException {
        configureBootstrap(server, group, TargetConnection.this);
        ChannelFuture future = bootstrap.connect(targetHost, targetPort).sync();
        channel = future.channel();
    }

    public void sendMessage(String message) {
        if (channel != null && channel.isActive()) {
            WebSocketFrame toSend = new TextWebSocketFrame(message);
            channel.writeAndFlush(toSend);
        }
    }

    private void configureBootstrap(Server server, EventLoopGroup group, TargetConnection targetConnection) throws SSLException, URISyntaxException {
        URI uri = new URI("ws://" + targetHost + ":" + targetPort + "/");
        final SslContext sslCtx = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        final WebSocketClientHandler handler =
                new WebSocketClientHandler(
                        WebSocketClientHandshakerFactory.newHandshaker(
                                uri, WebSocketVersion.V13, null, true, new DefaultHttpHeaders()),
                        server,
                        targetConnection
                );
        bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(
                                new LoggingHandler(LogLevel.INFO),
                                sslCtx.newHandler(socketChannel.alloc(), targetHost, targetPort),
                                new HttpClientCodec(),
                                new HttpObjectAggregator(9182),
                                handler);
                    }
                });
    }
}
