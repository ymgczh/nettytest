package com.ymgc.nettytest.socket;

import com.ymgc.nettytest.config.NettyConfig;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;

import java.util.Date;

/**
 * 接收处理相应客户端请求的核心处理类
 *
 * @Description: java class description
 * @Author: 张昊
 * @CreateDate: 2019-9-17 18:02
 * @Version: 1.0
 * <p>Copyright: 内蒙古金财信息技术有限公司 (c) 2019</p>
 */
public class MyWebSocketHandler extends SimpleChannelInboundHandler<Object> {

    private WebSocketServerHandshaker handshaker;

    private static final String WEB_SOCKET_URL="ws://localhost:8888/websocket";
    /**
     * 服务端处理客户端请求的核心方法
     * @param
     * @return   
     * @date     2019-9-17 18:09
     * @version  1.0
     */
    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {

        //处理客户端向服务端发起http握手请求的业务
        if (o instanceof FullHttpRequest) {
            handHttpRequest(channelHandlerContext, (FullHttpRequest) o);
        } else if (o instanceof WebSocketFrame) {
            //处理websocket连接业务
            handWebSocketFrame(channelHandlerContext, (WebSocketFrame) o);
        }
    }

    /**
     * 处理客户端与服务端的websocket业务
     * @param ctx
     * @param frame
     */
    private void handWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        //判断是否是关闭
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
        }
        //判读是否是ping
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PingWebSocketFrame(frame.content().retain()));
        }
        //判读是否是二进制 如果是抛出异常
        if (!(frame instanceof TextWebSocketFrame)){
            System.out.println("不支持二进制");
            throw new RuntimeException(this.getClass().getName() + "不支持消息");
        }
        //返回应答
        //获取客户端向服务端发送的消息
        String request =  ((TextWebSocketFrame) frame).text();
        System.out.println("收到客户端消息=====》" + request);
        TextWebSocketFrame tws = new TextWebSocketFrame
                (new Date().toString() +
                        ctx.channel().id() +
                        "===========>>" + request);

        //群发 服务端向每个链接的客户端群发消息
        NettyConfig.group.writeAndFlush(tws);

    }

    /**
     * 处理客户端向服务端发起http握手请求的业务
     * @param ctx
     * @param req
     */
    private void handHttpRequest(ChannelHandlerContext ctx , FullHttpRequest req) {
        if (!req.getDecoderResult().isSuccess() || !("websocket".equals(req.headers().get("Upgrade")))) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }
        WebSocketServerHandshakerFactory wsFactory =
                new WebSocketServerHandshakerFactory(WEB_SOCKET_URL,
                        null, false);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(),req);
        }
    }

    /**
     * 服务端向客户端相应消息
     * @param ctx
     * @param req
     * @param res
     */
    private void sendHttpResponse(ChannelHandlerContext ctx , FullHttpRequest req,
                                  DefaultFullHttpResponse res) {
        if (res.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8) ;
            res.content().writeBytes(buf);
            buf.release();
        }
        //向客户端发送数据
        ChannelFuture future = ctx.channel().writeAndFlush(res);
        if (res.getStatus().code() != 200) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * 工程异常时候调用
     * @param
     * @return
     * @date     2019-9-17 18:09
     * @version  1.0
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * 客户端与服务端创建连接时候调用
     * @param
     * @return   
     * @date     2019-9-17 18:08
     * @version  1.0
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        NettyConfig.group.add(ctx.channel());
        System.out.println("客户端与服务端连接开启");
    }

    /**
     * 断开连接时候
     * @param
     * @return
     * @date     2019-9-17 18:08
     * @version  1.0
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NettyConfig.group.remove(ctx.channel());
        System.out.println("客户端与服务端连接关闭");
    }

    /**
     * 接收客户端发送的数据结束之后调用
     * @param
     * @return
     * @date     2019-9-17 18:09
     * @version  1.0
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
