package com.fd.microSevice.helper;

import java.util.HashSet;
import java.util.Set;

/**
 * 客户端API数据
 * 
 * @author 符冬
 *
 */
public class ClientApi {
	/**
	 * 接口数组
	 */
	private Set<ApiInfo> apis = new HashSet<>();
	/**
	 * 连接信息
	 */
	private HttpApiInfo httpApiInfo;

	public Set<ApiInfo> getApis() {
		return apis;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((httpApiInfo == null) ? 0 : httpApiInfo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClientApi other = (ClientApi) obj;
		if (httpApiInfo == null) {
			if (other.httpApiInfo != null)
				return false;
		} else if (!httpApiInfo.equals(other.httpApiInfo))
			return false;
		return true;
	}

	public void setApis(Set<ApiInfo> apis) {
		this.apis = apis;
	}

	public HttpApiInfo getHttpApiInfo() {
		return httpApiInfo;
	}

	@Override
	public String toString() {
		return "ClientApi [apis=" + apis + ", httpApiInfo=" + httpApiInfo + "]";
	}

	public ClientApi() {
		super();
	}

	public ClientApi(Set<ApiInfo> apis, HttpApiInfo httpApiInfo) {
		super();
		this.apis = apis;
		this.httpApiInfo = httpApiInfo;
	}

	public void setHttpApiInfo(HttpApiInfo httpApiInfo) {
		this.httpApiInfo = httpApiInfo;
	}

}
