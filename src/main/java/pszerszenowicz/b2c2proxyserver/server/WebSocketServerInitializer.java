package pszerszenowicz.b2c2proxyserver.server;


import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import pszerszenowicz.b2c2proxyserver.client.TargetConnection;


public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {

    WebSocketFrameHandler handler;

    public WebSocketServerInitializer(TargetConnection targetConnection) {
        handler = new WebSocketFrameHandler(targetConnection);
    }

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new LoggingHandler(LogLevel.INFO));
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new WebSocketServerProtocolHandler("/", null, true));
        pipeline.addLast(handler);
    }
}
