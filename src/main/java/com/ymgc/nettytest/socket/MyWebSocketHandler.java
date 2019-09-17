package com.ymgc.nettytest.socket;

import com.ymgc.nettytest.config.NettyConfig;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

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
    
    /**
     * 服务端处理客户端请求的核心方法
     * @param
     * @return   
     * @date     2019-9-17 18:09
     * @version  1.0
     */
    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {

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
