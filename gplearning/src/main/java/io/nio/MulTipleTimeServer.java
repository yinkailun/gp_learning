package io.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author yinkailun
 * @description:
 * @date 2019-06-28 5:05 PM
 */
public class MulTipleTimeServer implements Runnable{

	private Selector selector;

	private ServerSocketChannel serverSocketChannel;

	private volatile boolean stop;
	public MulTipleTimeServer(int port) {
		try {
			selector = Selector.open();
			serverSocketChannel = ServerSocketChannel.open();
			serverSocketChannel.configureBlocking(false);
			serverSocketChannel.socket().bind(new InetSocketAddress(port),1024);
			serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("start on port :" + port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void stop(){
		stop = true;
	}

	@Override
	public void run() {

		while (!stop){
			try{
				selector.select(1000);
				Set<SelectionKey> selectionKeys = selector.selectedKeys();
				Iterator<SelectionKey> iterator = selectionKeys.iterator();
				SelectionKey selectionKey = null;
				if(iterator.hasNext()){
					selectionKey = iterator.next();

				}
			}catch (Exception e){
				e.printStackTrace();
			}
		}
	}
}
