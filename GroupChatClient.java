package com.ut.lpf.netty.group;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

public class GroupChatClient {
      private final  String HOST="localhost";
      private final int PORT=6667;
      private Selector selector;
      private SocketChannel socketChannel;
      private String username;
      public GroupChatClient() throws IOException {
         selector= Selector.open();
          socketChannel=SocketChannel.open(new InetSocketAddress("127.0.0.1",PORT));
         socketChannel.configureBlocking(false);
         socketChannel.register(selector, SelectionKey.OP_READ);
         username=socketChannel.getLocalAddress().toString().substring(1);
          System.out.println(username+" is ok");

      }
      public void sendInfo(String info)
      {
          info=username+ " 说"+ info;
          try{
              socketChannel.write(ByteBuffer.wrap(info.getBytes()));
          }catch (IOException e)
          {
              e.printStackTrace();
          }
      }
      public void readInfo()
      {
          try{
              System.out.println("wait");
              int read=selector.select();

              if(read>0)
              {
                  Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                  while(iterator.hasNext())
                  {

                      SelectionKey key=iterator.next();
                      if(key.isReadable())
                      {

                         SocketChannel channel =(SocketChannel) key.channel();
                          ByteBuffer allocate = ByteBuffer.allocate(1024);
                          channel.read(allocate);
                          String msg=new String(allocate.array());
                          System.out.println(msg);
                      }
                      iterator.remove();
                  }
              }
              else
              {
                  System.out.println("没有可用通道");
              }
          }catch (IOException e)
          {
              e.printStackTrace();
          }
      }

    public static void main(String[] args) throws IOException {
        GroupChatClient groupChatClient = new GroupChatClient();
        new Thread(()->{
            while(true)
            {

                groupChatClient.readInfo();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        Scanner input=new Scanner(System.in);
        while(input.hasNextLine())
        {
            String s=input.nextLine();
            groupChatClient.sendInfo(s);
        }

    }
}

