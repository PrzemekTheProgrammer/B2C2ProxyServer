package pszerszenowicz.b2c2proxyserver.client;

import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import pszerszenowicz.b2c2proxyserver.server.Server;

import javax.net.ssl.SSLException;
import java.net.URISyntaxException;

public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {

    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;
    private Server server;
    private TargetConnection targetConnection;
    private boolean reconnectEnabled = true;

    public WebSocketClientHandler(WebSocketClientHandshaker handshaker, Server server, TargetConnection targetConnection) {
        this.handshaker = handshaker;
        this.server = server;
        this.targetConnection = targetConnection;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws InterruptedException, URISyntaxException, SSLException {
        if (reconnectEnabled) {
            reconnectEnabled = false;
            targetConnection.connect(server);
        } else {
            server.deleteUnusedTargetConnection(targetConnection);
            System.out.println("WebSocket Client disconnected!");
        }
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            try {
                handshaker.finishHandshake(ch, (FullHttpResponse) msg);
                System.out.println("WebSocket Client connected!");
                handshakeFuture.setSuccess();
            } catch (WebSocketHandshakeException e) {
                System.out.println("WebSocket Client failed to connect");
                handshakeFuture.setFailure(e);
            }
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException(
                    "Unexpected FullHttpResponse (getStatus=" + response.status() +
                            ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }

        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof TextWebSocketFrame textFrame) {
            String msgReceived = textFrame.text();
            System.out.println("WebSocket Client received message: " + msgReceived);
            server.sendMessage(msgReceived, targetConnection);
        } else if (frame instanceof PongWebSocketFrame) {
            System.out.println("WebSocket Client received pong");
        } else if (frame instanceof CloseWebSocketFrame) {
            System.out.println("WebSocket Client received closing");
            ch.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }

    public void disableReconnect() {
        this.reconnectEnabled = false;
    }
}
