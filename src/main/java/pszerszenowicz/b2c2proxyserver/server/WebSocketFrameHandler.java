package pszerszenowicz.b2c2proxyserver.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import pszerszenowicz.b2c2proxyserver.client.TargetConnection;

public class WebSocketFrameHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    TargetConnection targetConnection;

    public WebSocketFrameHandler(TargetConnection targetConnection) {
        this.targetConnection = targetConnection;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        // ping and pong frames already handled

        if (frame instanceof TextWebSocketFrame) {
            // Send the uppercase string back.
            String request = ((TextWebSocketFrame) frame).text();
            System.out.println("Server got request: \n" + request + "\n");
            targetConnection.sendMessage(request);
        } else {
            String message = "unsupported frame type: " + frame.getClass().getName();
            throw new UnsupportedOperationException(message);
        }

    }
}
