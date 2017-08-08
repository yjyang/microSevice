package com.fd.microSevice.web;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fd.microSevice.code.RestCode;
import com.fd.microSevice.helper.ClientApi;
import com.fd.microSevice.helper.CoordinateUtil;

/**
 * 连接客户端
 * 
 * @author 符冬
 *
 */
@ClientEndpoint(decoders = { RestCode.class }, encoders = { RestCode.class })
public class RdsWsClient {

	static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	private static Session HB_SESSION;
	private static String rurl;
	private static Runnable inittask;

	@OnMessage
	public void onMessage(ClientApi api) {
		log.info(api.toString());
		log.info(String.format("start服务数量%s", CoordinateUtil.CAS.size()));
		if (api.getHttpApiInfo().getIsOnline()) {
			CoordinateUtil.CAS.add(api);
			log.info(String.format("%s服务上线", api.getHttpApiInfo().getBaseUrl()));
		} else {
			log.info(String.format("%s服务下线", api.getHttpApiInfo().getBaseUrl()));
			CoordinateUtil.CAS.remove(api);
		}
		log.info(String.format("end服务数量%s", CoordinateUtil.CAS.size()));
	}

	@OnOpen
	public void open(Session session) {
		if (HB_SESSION == null || !HB_SESSION.isOpen())
			HB_SESSION = session;
		if (inittask != null) {
			inittask.run();
		}
		log.info("连接中间服务器成功...");

	}

	@OnClose
	public void close() {
		log.error("中间服务器关闭..");
		reconnet();
	}

	public static void connectHbWs(String url, Runnable r) {
		if (HB_SESSION == null || !HB_SESSION.isOpen()) {
			try {
				if (rurl == null)
					rurl = url;
				if (inittask == null)
					inittask = r;
				WebSocketContainer wsc = ContainerProvider.getWebSocketContainer();
				wsc.connectToServer(RdsWsClient.class, URI.create(url));
			} catch (Throwable e) {
				log.error("连接中央服务器失败");
				log.error("中间服务器URL:" + url);
			}
		}
	}

	private static void reconnet() {
		Thread thread = new Thread(() -> {
			while (HB_SESSION == null || !HB_SESSION.isOpen()) {
				try {
					TimeUnit.SECONDS.sleep(120);
					// 重连
					connectHbWs(rurl, inittask);
				} catch (Exception e) {
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
	}

	@OnError
	public void error(Throwable a) {
		log.error("中间服务器报错", a);
		reconnet();
	}

	public static void sendHbData(String s) {
		if (s != null) {
			try {
				if (HB_SESSION != null && HB_SESSION.isOpen()) {
					HB_SESSION.getBasicRemote().sendText(s);
				} else {
					log.info("中央服务器没有连上..................");
				}
			} catch (Throwable e) {
				e.printStackTrace();
				log.error("发送数据报错", e);
			}
		}
	}

	public static void sendObject(ClientApi ca) {
		if (HB_SESSION != null && HB_SESSION.isOpen()) {
			try {
				HB_SESSION.getBasicRemote().sendObject(ca);
			} catch (IOException | EncodeException e) {
				e.printStackTrace();
				log.error("发送对象数据报错", e);
			}
		}
	}
}
