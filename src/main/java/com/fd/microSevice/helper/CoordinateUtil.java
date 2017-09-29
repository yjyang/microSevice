package com.fd.microSevice.helper;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fd.microSevice.web.RdsWsClient;

/**
 * 工具类
 * 
 * @author 符冬
 *
 */
public final class CoordinateUtil {
	/**
	 * 客戶端服务集合
	 */
	public static final Set<ClientInfo> CLIENTS = Collections.synchronizedSet(new HashSet<>());
	public static final Set<ClientApi> CAS = Collections.synchronizedSet(new HashSet<>());
	/**
	 * JSON 序列化
	 */
	public static ObjectMapper JSON_MP = new ObjectMapper();
	static {
		JSON_MP.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		JSON_MP.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		JSON_MP.setSerializationInclusion(Include.NON_NULL);
	}
	static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	public static HttpApiInfo getHttpApiInfo(Session session) {
		Optional<HttpApiInfo> opt = CLIENTS.stream().filter(o -> o.getSession().getId().equals(session.getId()))
				.map(o -> o.getClientApi().getHttpApiInfo()).findAny();
		return opt.orElse(null);
	}

	public static Set<ClientApi> getConcurrentCas(String name, RequestMethod m) {
		Set<ClientApi> collect = CoordinateUtil.CAS.parallelStream()
				.filter(o -> o.getApis().contains(CoordinateUtil.getApiInfo(name, m))).collect(Collectors.toSet());
		return collect;
	}

	public static Map<String, HttpApiInfo> getApiUrl(String name, RequestMethod rm) {
		HttpApiInfo url = getHttpApiInfo(getApiInfo(name, rm));
		String apiurl = url.getBaseUrl();
		if (!apiurl.endsWith("/")) {
			apiurl = apiurl + "/";
		}
		if (name.startsWith("/")) {
			name = name.replaceFirst("/", "");
		}
		Map<String, HttpApiInfo> rt = new HashMap<>(1);
		rt.put(apiurl + name, url);
		return rt;
	}

	public static ApiInfo getApiInfo(String name, RequestMethod rm) {
		return new ApiInfo(ClassHelper.getApiName(name.split("[?;]")[0]), rm);
	}

	public static void connCoor(HttpApiInfo hai) {
		if (ClassHelper.getDefaultClassLoader().getResource("acurl.properties") != null
				|| ClassHelper.getDefaultClassLoader().getResource("application.properties") != null) {

			ResourceBundle acurl = null;
			if (ClassHelper.getDefaultClassLoader().getResource("acurl.properties") != null) {
				acurl = ResourceBundle.getBundle("acurl");
			} else {
				acurl = ResourceBundle.getBundle("application");
			}
			boolean enableautoscan = true;
			String enableapiaotuscankey = "enable.api.auto.scan";
			if (acurl.containsKey(enableapiaotuscankey)
					&& acurl.getString(enableapiaotuscankey).trim().equalsIgnoreCase("false")) {
				enableautoscan = false;
			}
			Set<ApiInfo> apis = getapis(enableautoscan);
			if (apis.size() > 0) {
				if (acurl.containsKey("ws.mid.url")) {
					if (acurl.containsKey("server.ssl.enabled")
							&& acurl.getString("server.ssl.enabled").equalsIgnoreCase("true")) {
						hai.setScheme("https");
					}
					if (acurl.containsKey("server.port")) {
						String port = acurl.getString("server.port");
						if (port.trim().length() > 0) {
							hai.setPort(Integer.valueOf(port.trim()));
						}
					}
					if (acurl.containsKey("server.host")) {
						String host = acurl.getString("server.host");
						if (host.trim().length() > 1) {
							hai.setHost(host);
						}
					}

				}
			}
			RdsWsClient.connectHbWs(acurl.getString("ws.mid.url").trim(), () -> {
				try {
					if (hai.getContextPath() != null) {
						RdsWsClient.sendHbData(CoordinateUtil.JSON_MP.writeValueAsString(new ClientApi(apis, hai)));
					}
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}

			});
		}
	}

	private static Set<ApiInfo> getapis(boolean enableAutoScan) {
		Set<ApiInfo> apis = Collections.synchronizedSet(new HashSet<>());
		try {
			ClassLoader cl = ClassHelper.getDefaultClassLoader();
			URL[] urls = ((URLClassLoader) cl).getURLs();
			// 判断是否直接通过 java -jar 运行 spring boot
			boolean b = urls[0].toURI().toString().endsWith("!/")
					&& urls[0].toURI().toString().startsWith("jar:file:/");
			if (b) {
				JarURLConnection connection = (JarURLConnection) urls[0].openConnection();
				URI uri = connection.getJarFileURL().toURI();
				// spring boot 项目只扫描类路径下面的CLASS
				ClassHelper.getjarpms(apis, Paths.get(uri), "/BOOT-INF/classes/", enableAutoScan);
			} else {

				for (URL url : urls) {
					Path start = Paths.get(url.toURI());
					if (start.toFile().isDirectory()) {
						Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
							@Override
							public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
								String name = file.getFileName().toString();
								if (name.endsWith(".class") && !name.contains("$")) {
									scanclass(apis, start, file, enableAutoScan);
								}
								return FileVisitResult.CONTINUE;
							}

							@Override
							public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
								return FileVisitResult.CONTINUE;
							}

						});
					}

				}

			}
		} catch (Exception e) {
			e.printStackTrace();

		}
		return apis;
	}

	private static void scanclass(Set<ApiInfo> ais, Path start, Path file, boolean enableAutoScan) {
		String cpn = file.toUri().toString().split(start.toUri().toString())[1];
		String cln = cpn.substring(0, cpn.lastIndexOf(".")).replace('/', '.');
		ClassHelper.getPms(ais, cln, enableAutoScan);
	}

	public static HttpApiInfo getHttpApiInfo(ApiInfo ai) {
		if (CAS.size() > 0) {
			Set<HttpApiInfo> collects = CAS.parallelStream().filter(o -> o.getApis().contains(ai))
					.map(o -> o.getHttpApiInfo()).collect(Collectors.toSet());
			if (collects.size() > 0) {
				return collects.toArray(new HttpApiInfo[0])[new SecureRandom().nextInt(collects.size())];
			} else {
				throw new IllegalStateException(String.format("%s服务不可用", ai));
			}
		} else {
			throw new IllegalStateException(String.format("%s服务不可用", ai));
		}
	}
}
