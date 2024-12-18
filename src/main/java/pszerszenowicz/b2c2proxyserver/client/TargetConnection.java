package pszerszenowicz.b2c2proxyserver.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import pszerszenowicz.b2c2proxyserver.requests.AuthenticationRequest;
import pszerszenowicz.b2c2proxyserver.server.Server;

import java.net.URI;

public class TargetConnection {

//    private final String targetHost = "wss://socket.uat.b2c2.net/quotes";
    private final String targetHost = "localhost";
    private final int targetPort = 8081;

    private ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
    private Channel channel;

    public void start(Server server) throws InterruptedException{
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(
                                    new LoggingHandler(LogLevel.INFO),
                                    new HttpClientCodec(),
                                    new WebSocketClientProtocolHandler(
                                            WebSocketClientHandshakerFactory.newHandshaker(
                                                    new URI("ws://" + targetHost + ":" + targetPort + "/"), WebSocketVersion.V13, null, true, new DefaultHttpHeaders())
                                            ),
                                    new TargetConnectionResponseHandler(server));
                        }
                    });
            ChannelFuture future = bootstrap.connect(targetHost,targetPort).sync();
            channel = future.channel();
            String json = ow.writeValueAsString(new AuthenticationRequest());
            sendMessage(json);
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
        if(channel != null && channel.isActive()) {
            channel.writeAndFlush(message);
        }
    }
    public void closeChannel() {
        if (channel != null && channel.isActive()) {
            channel.close();
        }
    }

    public boolean isActive() {
        if(channel != null && channel.isActive()){
            return true;
        }
        return false;
    }

}
