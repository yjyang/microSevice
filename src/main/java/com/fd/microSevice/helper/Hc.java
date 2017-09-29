package com.fd.microSevice.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fd.microSevice.web.RdsWsClient;

/**
 * HTTP REST 远程访问
 * 
 * @author 符冬
 *
 */
public final class Hc {

	public static String get(String name) {
		for (int i = 0; i < CoordinateUtil.getConcurrentCas(name, RequestMethod.GET).size(); i++) {
			Map<String, HttpApiInfo> apiUrl = CoordinateUtil.getApiUrl(name, RequestMethod.GET);
			String next = apiUrl.keySet().iterator().next();
			try {
				return sendget(next);
			} catch (Exception e) {
				if (isTimeout(e)) {
					try {
						return sendget(next);
					} catch (Exception e1) {
						if (isTimeout(e1)) {
							remove(apiUrl);
						}
					}
				}
			}
		}
		throw new IllegalStateException(String.format("%s当前服务的主机都已经下线。。。请至少启动一台服务器。。", name));
	}

	private static String sendget(String next) {
		return getHttpContentByUrl(next);
	}

	public static String post(String name, String jsonReqData) {
		for (int i = 0; i < CoordinateUtil.getConcurrentCas(name, RequestMethod.POST).size(); i++) {
			Map<String, HttpApiInfo> apiUrl = CoordinateUtil.getApiUrl(name, RequestMethod.POST);
			String next = apiUrl.keySet().iterator().next();
			try {
				return sendpost(jsonReqData, next);
			} catch (Exception e) {
				if (isTimeout(e)) {
					try {
						return sendpost(jsonReqData, next);
					} catch (Exception e1) {
						if (isTimeout(e1)) {
							remove(apiUrl);
						}
					}
				}
			}
		}
		throw new IllegalStateException(String.format("%s当前服务的主机都已经下线。。。请至少启动一台服务器。。", name));
	}

	private static String sendpost(String jsonReqData, String next) {
		return getHttpContentByBtPram(next, jsonReqData);
	}

	public static String post(String name, String... params) {
		for (int i = 0; i < CoordinateUtil.getConcurrentCas(name, RequestMethod.POST).size(); i++) {
			Map<String, HttpApiInfo> apiUrl = CoordinateUtil.getApiUrl(name, RequestMethod.POST);
			String next = apiUrl.keySet().iterator().next();
			try {
				return sendbtpost(next, params);
			} catch (Exception e) {
				if (isTimeout(e)) {
					try {
						return sendbtpost(next, params);
					} catch (Exception e1) {
						if (isTimeout(e1)) {
							remove(apiUrl);
						}
					}
				}
			}
		}
		throw new IllegalStateException(String.format("%s当前服务的主机都已经下线。。。请至少启动一台服务器。。", name));
	}

	private static String sendbtpost(String next, String... params) {
		return getHttpContentByParam(next, getListNamevaluepair(params));
	}

	private static boolean isTimeout(Throwable e) {
		StringWriter out = new StringWriter();
		e.printStackTrace(new PrintWriter(out));
		String error = out.toString();
		if (error.contains("SocketTimeoutException") || error.contains("ConnectException")) {
			return true;
		} else {
			System.err.println(error);
		}
		return false;
	}

	/**
	 * 访问不同服务器的数据
	 * 
	 * @return
	 */
	public static String getHttpContentByParam(String url, List<NameValuePair> formparams) {

		HttpPost httppost = new HttpPost(url);
		try (CloseableHttpClient closeableHttpClient = getCustomCloseableHttpClient()) {
			if (formparams != null && formparams.size() > 0) {
				httppost.setEntity(new UrlEncodedFormEntity(formparams, "UTF-8"));
			}
			try (CloseableHttpResponse response = closeableHttpClient.execute(httppost)) {
				HttpEntity entity = response.getEntity();
				return getResponse(entity);
			}

		} catch (IOException e) {
			throw new IllegalStateException(e);
		} finally {
			httppost.releaseConnection();
		}

	}

	private static String getResponse(HttpEntity entity) throws IOException, UnsupportedEncodingException {
		if (entity != null) {
			try (BufferedReader rd = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"))) {
				String str = rd.readLine();
				StringBuilder buf = new StringBuilder();
				while (str != null) {
					buf.append(str);
					str = rd.readLine();
				}
				return buf.toString();
			}
		} else {
			return "";
		}
	}

	/**
	 * 获取URL数据
	 * 
	 * @param url
	 * @return
	 */
	public static String getHttpContentByUrl(String url) {
		try (CloseableHttpClient closeableHttpClient = getCustomCloseableHttpClient()) {
			HttpGet hg = new HttpGet(url);
			try (CloseableHttpResponse res = closeableHttpClient.execute(hg)) {
				return getResponse(res.getEntity());
			} finally {
				hg.releaseConnection();
			}

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	/**
	 * 通过BODY传递字节数组请求参数
	 * 
	 * @param url
	 * @param param
	 * @return
	 */
	public static String getHttpContentByBtPram(String url, String param) {
		try (CloseableHttpClient closeableHttpClient = getCustomCloseableHttpClient()) {
			HttpPost httppost = new HttpPost(url);
			if (param != null) {
				httppost.setEntity(new ByteArrayEntity(param.getBytes("UTF-8")));
			}
			try (CloseableHttpResponse response = closeableHttpClient.execute(httppost)) {
				return getResponse(response.getEntity());
			} finally {
				httppost.releaseConnection();
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private static CloseableHttpClient getCustomCloseableHttpClient() {
		CloseableHttpClient getCustomCloseableHttpClient = HttpClients.custom()
				.setDefaultRequestConfig(getCustomRequestConfig(10000)).build();
		return getCustomCloseableHttpClient;
	}

	/**
	 * 封装HTTPCLIENT请求参数集合
	 * 
	 * @param str
	 * @return
	 */
	public static List<NameValuePair> getListNamevaluepair(String... str) {
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		if (str != null && str.length % 2 == 0) {
			for (int i = 0; i < str.length; i++) {
				formparams.add(new BasicNameValuePair(str[i], str[++i]));
			}
		}
		return formparams;
	}

	private static RequestConfig getCustomRequestConfig(int connectTimeout) {
		return RequestConfig.custom().setConnectTimeout(connectTimeout).build();
	}

	private static void remove(Map<String, HttpApiInfo> apiUrl) {
		Iterator<ClientApi> ite = CoordinateUtil.CAS.iterator();
		while (ite.hasNext()) {
			HttpApiInfo httpApiInfo = ite.next().getHttpApiInfo();
			if (httpApiInfo.equals(apiUrl.values().iterator().next())) {
				// 下线
				ite.remove();
				// 告訴心跳服务器当前服务器不可用
				httpApiInfo.setIsOnline(false);
				RdsWsClient.sendObject(new ClientApi(null, httpApiInfo));
			}
		}
	}
}
