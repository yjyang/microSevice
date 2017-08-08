package com.fd.microSevice.helper;

import java.lang.invoke.MethodHandles;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

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
		return CoordinateUtil.CAS.parallelStream().filter(o -> o.getApis().contains(CoordinateUtil.getApiInfo(name, m)))
				.collect(Collectors.toSet());
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

	public static HttpApiInfo getHttpApiInfo(ApiInfo ai) {
		log.info("CAS.size():" + CAS.size());
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
