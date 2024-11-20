package pszerszenowicz.b2c2proxyserver.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import pszerszenowicz.b2c2proxyserver.client.TargetConnection;

public class ServerResponseHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    private TargetConnection targetConnection;

    public ServerResponseHandler(TargetConnection targetConnection) {
        this.targetConnection = targetConnection;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, WebSocketFrame webSocketFrame) throws Exception {
        String message = ((TextWebSocketFrame) webSocketFrame).text();
        targetConnection.sendMessage(message);
    }
}
