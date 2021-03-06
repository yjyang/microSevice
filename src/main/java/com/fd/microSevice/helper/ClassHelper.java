package com.fd.microSevice.helper;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fd.microSevice.anno.RestApi;
import com.fd.microSevice.em.Pl;

/**
 * 反射类型操作
 * 
 * @author 符冬
 *
 */
public final class ClassHelper {
	public static ClassLoader getDefaultClassLoader() {

		ClassLoader cl = null;

		try {
			cl = MethodHandles.lookup().lookupClass().getClassLoader();

		} catch (Throwable ex) {
		}

		if (cl == null) {

			cl = Thread.currentThread().getContextClassLoader();

			if (cl == null) {

				try {

					cl = ClassLoader.getSystemClassLoader();

				} catch (Throwable ex) {
				}

			}

		}

		return cl;

	}

	public final static void getjarpms(Set<ApiInfo> ais, Path file, String rootpath, boolean enableaotuscan) {
		try {
			FileSystem fs = FileSystems.newFileSystem(file, null);
			Files.walkFileTree(fs.getPath(rootpath), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					String name = file.toString();
					if (name.startsWith(rootpath)) {
						name = name.replaceFirst(rootpath, "");
					}
					if (name.endsWith(".class") && !name.contains("$")) {
						String cln = name.replace('/', '.').substring(0, name.lastIndexOf("."));
						getPms(ais, cln, enableaotuscan);

					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
					return FileVisitResult.CONTINUE;
				}

			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static void getPms(Set<ApiInfo> ais, String cln, boolean enableaotuscan) {
		try {
			Class<?> contr = Class.forName(cln);
			if (contr.isAnnotationPresent(Controller.class) || contr.isAnnotationPresent(RestController.class)) {
				String[] classpaths = getpaths(contr);
				RestApi ra = null;
				if (contr.isAnnotationPresent(RestApi.class)) {
					ra = contr.getAnnotation(RestApi.class);
				}
				Method[] ms = contr.getMethods();
				for (Method m : ms) {
					RestApi mra = null;
					if (m.isAnnotationPresent(RestApi.class)) {
						mra = m.getAnnotation(RestApi.class);
					}
					if (m.isAnnotationPresent(RequestMapping.class) || m.isAnnotationPresent(PostMapping.class)
							|| m.isAnnotationPresent(GetMapping.class)) {
						if (mra == null && ra == null) {
							if (enableaotuscan) {
								collect(ais, classpaths, m);
							}
						} else {
							if (mra == null) {
								if (!ra.value().equals(Pl.PRIVATE)) {
									collect(ais, classpaths, m);
								}
							} else {
								if (!mra.value().equals(Pl.PRIVATE)) {
									collect(ais, classpaths, m);
								}
							}
						}
					}
				}
			}

		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private static String[] getpaths(Class<?> contr) {
		String[] classpaths = null;
		if (contr.isAnnotationPresent(RequestMapping.class)) {
			RequestMapping crm = contr.getAnnotation(RequestMapping.class);
			String[] value = crm.value();
			String[] path = crm.path();
			classpaths = getmppaths(classpaths, value, path);
		} else {
			if (contr.isAnnotationPresent(GetMapping.class)) {
				GetMapping crm = contr.getAnnotation(GetMapping.class);
				String[] value = crm.value();
				String[] path = crm.path();
				classpaths = getmppaths(classpaths, value, path);

			} else if (contr.isAnnotationPresent(PostMapping.class)) {
				PostMapping crm = contr.getAnnotation(PostMapping.class);
				String[] value = crm.value();
				String[] path = crm.path();
				classpaths = getmppaths(classpaths, value, path);

			}
		}
		return classpaths;
	}

	private static String[] getmppaths(String[] classpaths, String[] value, String[] path) {
		if (value.length > 0 || path.length > 0) {
			classpaths = value.length > 0 ? value : path;
		}
		return classpaths;
	}

	private static void collect(Set<ApiInfo> ais, String[] classpaths, Method m) {
		if (m.isAnnotationPresent(RequestMapping.class)) {
			RequestMapping pm = m.getAnnotation(RequestMapping.class);
			RequestMethod[] method = pm.method();
			String[] value = pm.value();
			collectApiInfo(ais, classpaths, method, value);
		} else if (m.isAnnotationPresent(GetMapping.class)) {
			GetMapping pm = m.getAnnotation(GetMapping.class);
			RequestMethod[] method = new RequestMethod[] { RequestMethod.GET };
			String[] value = pm.value();
			collectApiInfo(ais, classpaths, method, value);
		} else if (m.isAnnotationPresent(PostMapping.class)) {
			PostMapping pm = m.getAnnotation(PostMapping.class);
			RequestMethod[] method = new RequestMethod[] { RequestMethod.POST };
			String[] value = pm.value();
			collectApiInfo(ais, classpaths, method, value);

		}
	}

	private static void collectApiInfo(Set<ApiInfo> ais, String[] classpaths, RequestMethod[] method, String[] value) {
		if (value.length > 0) {
			for (String mp : value) {
				if (classpaths != null) {
					for (String clspath : classpaths)
						format(ais, clspath, method, mp);
				} else {
					format(ais, "", method, mp);

				}
			}
		} else {
			if (classpaths != null) {
				for (String name : classpaths) {
					add(ais, method, name);
				}
			} else {
				add(ais, method, "");
			}
		}
	}

	private static void format(Set<ApiInfo> ais, String clspath, RequestMethod[] method, String mp) {
		String name = String.format("%s/%s", clspath, mp);
		if (name.startsWith("/")) {
			name = name.replaceFirst("/", "");
		}
		if (name.contains("//")) {
			name = name.replaceFirst("//", "/");
		}
		add(ais, method, name);
	}

	private static void add(Set<ApiInfo> ais, RequestMethod[] method, String name) {
		name = getApiName(name);
		if (method.length > 0) {
			for (RequestMethod mthod : method) {
				ais.add(new ApiInfo(name, mthod));
			}
		} else {
			log.info("add sec" + name);
			ais.add(new ApiInfo(name, RequestMethod.POST));
			ais.add(new ApiInfo(name, RequestMethod.GET));
		}
	}

	public static String getApiName(String name) {
		if (!name.startsWith("/")) {
			name = "/" + name;
		}
		return name;
	}

	static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
}
