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
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        targetConnection.stop();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if (frame instanceof TextWebSocketFrame) {
            String request = ((TextWebSocketFrame) frame).text();
            System.out.println("Server got request: \n" + request + "\n");
            targetConnection.sendMessage(request);
        } else {
            String message = "unsupported frame type: " + frame.getClass().getName();
            throw new UnsupportedOperationException(message);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        if (cause instanceof java.net.SocketException && "Connection reset".equals(cause.getMessage())) {
            System.out.println("Connection was closed by client.");
        } else {
            cause.printStackTrace();
        }
        ctx.close();
    }
}
