package NIO;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

/**
 * @Time : 2018/4/7 22:45
 * @Author : Dallo Gao
 * @File : ChatServer.java
 * @Software: IntelliJ IDEA
 **/
public class ChatServer implements Runnable {

    private boolean isRun;
    private Vector<String> unames;
    private Selector selector;
    private SelectionKey selectionKey;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    private ChatServer(int port) {
        isRun = true;
        unames = new Vector<String>();
        init(port);
    }

    private void init(int port) {

        try {
            selector = Selector.open();

            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

            serverSocketChannel.socket().bind(new InetSocketAddress(port));

            serverSocketChannel.configureBlocking(false);

            selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            printInfo("server start....");

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void run() {
        try {
            while (isRun) {
                int n = selector.select();
                if (n > 0) {
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {

                        SelectionKey key = iterator.next();

                        if(key.isValid()){
                            try {
                                if (key.isAcceptable()) {
                                    iterator.remove();

                                    ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

                                    SocketChannel socketChannel = serverSocketChannel.accept();

                                    if (socketChannel == null) {
                                        continue;
                                    }
                                    socketChannel.configureBlocking(false);
                                    socketChannel.register(selector, SelectionKey.OP_READ);

                                } else if (key.isReadable()) {
                                    readMsg(key);

                                }else if (key.isWritable()) {
                                    writeMsg(key);

                                }
                            }catch (Exception e){
                                System.out.println("key need to be cancled！");
                                key.cancel();
                            }
                        }
                    }

                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void readMsg(SelectionKey key) throws IOException {

        SocketChannel channel = (SocketChannel) key.channel();

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        StringBuffer sb = new StringBuffer();

        int count = 0;
        try {
            count = channel.read(byteBuffer);
        }catch (IOException e){
            key.cancel();
            channel.socket().close();
            channel.close();
            return;
        }

        if (count > 0){
            byteBuffer.flip();
            sb.append(new String(byteBuffer.array(),0,count));
        }
        String str = sb.toString();

        if (str.contains("open_")){
            String name = str.substring(5);
            printInfo(name + " online");
            unames.add(name);

            for (SelectionKey selkey : selector.selectedKeys()) {
                if (selkey != selectionKey) {
                    selkey.attach(unames);
                    selkey.interestOps(selkey.interestOps() | SelectionKey.OP_WRITE);
                }
            }
        }else if(str.contains("exit_")){
            String uname = str.substring(5);
            unames.add(uname);
            key.attach("close");
            key.interestOps(SelectionKey.OP_WRITE);
            for (SelectionKey selkey : selector.selectedKeys()) {
                if (selkey != selectionKey && selkey != key) {
                    selkey.attach(unames);
                    selkey.interestOps(selkey.interestOps() | SelectionKey.OP_WRITE);
                }
            }
            printInfo(uname + " offline");
        }else {
            String uname = str.substring(0, str.indexOf("^"));
            String msg = str.substring(str.indexOf("^")+1);
            printInfo("(" + uname + ")说:" + msg);
            String dateTime = simpleDateFormat.format(new Date());
            String smsg = uname + " " + dateTime + "\n " + msg + "\n ";
            for (SelectionKey selkey : selector.selectedKeys()) {
                if (selkey != selectionKey) {
                    selkey.attach(smsg);
                    selkey.interestOps(selkey.interestOps() | SelectionKey.OP_WRITE);
                }
            }
        }
    }

    private void writeMsg(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();

        Object obj = key.attachment();

        key.attach("");
        if (obj.toString().equals("close")){
            key.cancel();
            channel.socket().close();
            channel.close();
        }else{
            channel.write(ByteBuffer.wrap(obj.toString().getBytes()));
        }
        key.interestOps(SelectionKey.OP_READ);


    }

    private void printInfo(String str) {
        System.out.println("[" + simpleDateFormat.format(new Date()) + "] -> " + str);
    }

    public static void main(String[] args) {

        ChatServer server = new ChatServer(19999);

        new Thread(server).start();

    }

}
