package com.fd.microSevice.web;

import java.io.IOException;
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
import java.util.Collections;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fd.microSevice.helper.ApiInfo;
import com.fd.microSevice.helper.ClassHelper;
import com.fd.microSevice.helper.ClientApi;
import com.fd.microSevice.helper.CoordinateUtil;
import com.fd.microSevice.helper.HttpApiInfo;

/**
 * 初始化
 * 
 * @author 符冬
 *
 */
@WebFilter
public class InitFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) {

		HttpApiInfo hai = new HttpApiInfo(filterConfig.getServletContext().getContextPath());
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
					RdsWsClient.sendHbData(CoordinateUtil.JSON_MP.writeValueAsString(new ClientApi(apis, hai)));
				} catch (JsonProcessingException e) {
					e.printStackTrace();
				}

			});
		}

	}

	private Set<ApiInfo> getapis(boolean enableAutoScan) {
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

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
	}

}
