package pszerszenowicz.b2c2proxyserver.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import pszerszenowicz.b2c2proxyserver.server.Server;

public class TargetConnectionResponseHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private Server server;

    public TargetConnectionResponseHandler(Server server) {
        this.server = server;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, WebSocketFrame webSocketFrame) throws Exception {
        String message = ((TextWebSocketFrame) webSocketFrame).text();
        server.sendMessage(message);
    }
}
