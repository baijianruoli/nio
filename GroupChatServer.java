package com.ut.lpf.netty.group;

import org.springframework.expression.spel.ast.Selection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

public class GroupChatServer {

    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private  static final int POST=6667;
    public GroupChatServer()
    {
        try{
            selector=Selector.open();
           serverSocketChannel= ServerSocketChannel.open();
           serverSocketChannel.socket().bind(new InetSocketAddress(POST));
           serverSocketChannel.configureBlocking(false);
           serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        }catch (IOException e)
        {
            e.printStackTrace();
        }

    }
    public void listen()
    {
        try {
            while(true)
            {
                int ans=selector.select();

                if(ans>0)
                {
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while(iterator.hasNext())
                    {
                        SelectionKey next = iterator.next();
                        if(next.isAcceptable())
                        {
                            SocketChannel socketChannel= serverSocketChannel.accept();
                            socketChannel.configureBlocking(false);
                            socketChannel.register(selector,SelectionKey.OP_READ);
                            System.out.println(socketChannel.getRemoteAddress()+"上线");

                        }
                        if(next.isReadable())
                        {
                                 readData(next);
                        }
                        iterator.remove();
                    }
                }
                else
                {
                    System.out.println("等待");
                }
            }
        }catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    public void readData(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel)key.channel();
        try{
            ByteBuffer allocate = ByteBuffer.allocate(1024);
            int read = channel.read(allocate);
            if(read>0)
            {
                String  msg=new String(allocate.array());
                System.out.println("from 客户端 "+msg);
                sendOther(msg,channel);
            }
        }catch (IOException e)
        {
            System.out.println(channel.getRemoteAddress()+"离线了。。。");
            key.cancel();
            channel.close();
        }
    }
    public void sendOther(String msg,SocketChannel self) throws IOException {
        System.out.println("服务器转发消息中");
        for(SelectionKey key:selector.keys())
        {
            Channel channel = key.channel();
            if(channel instanceof  SocketChannel && channel !=self)
            {

                SocketChannel socketChannel = (SocketChannel) channel;
                ByteBuffer wrap = ByteBuffer.wrap(msg.getBytes());
                socketChannel.write(wrap);
            }
        }
    }
    public static void main(String[] args) {
        GroupChatServer groupChatServer = new GroupChatServer();
        groupChatServer.listen();
    }
}
