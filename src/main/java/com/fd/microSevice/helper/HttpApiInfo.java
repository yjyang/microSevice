package com.fd.microSevice.helper;

/**
 * API主机信息
 * 
 * @author 符冬
 *
 */
public class HttpApiInfo {
	/**
	 * 使用的协议默认HTTP
	 */
	private String scheme = "http";
	/**
	 * 服务的域名或者IP
	 */
	private String host;
	/**
	 * 服务端口
	 */
	private Integer port = 8080;
	/**
	 * 服务访问上下文根路径
	 */
	private String contextPath;

	/**
	 * 主机是否在线
	 */
	private Boolean isOnline = true;

	public String getScheme() {
		return scheme;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((contextPath == null) ? 0 : contextPath.hashCode());
		result = prime * result + ((host == null) ? 0 : host.hashCode());
		result = prime * result + ((port == null) ? 0 : port.hashCode());
		result = prime * result + ((scheme == null) ? 0 : scheme.hashCode());
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
		HttpApiInfo other = (HttpApiInfo) obj;
		if (contextPath == null) {
			if (other.contextPath != null)
				return false;
		} else if (!contextPath.equals(other.contextPath))
			return false;
		if (host == null) {
			if (other.host != null)
				return false;
		} else if (!host.equals(other.host))
			return false;
		if (port == null) {
			if (other.port != null)
				return false;
		} else if (!port.equals(other.port))
			return false;
		if (scheme == null) {
			if (other.scheme != null)
				return false;
		} else if (!scheme.equals(other.scheme))
			return false;
		return true;
	}

	public HttpApiInfo(String host, Integer port) {
		super();
		this.host = host;
		this.port = port;
	}

	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	public Boolean getIsOnline() {
		return isOnline;
	}

	public void setIsOnline(Boolean isOnline) {
		this.isOnline = isOnline;
	}

	public HttpApiInfo(String scheme, String host, Integer port, String contextPath) {
		super();
		this.scheme = scheme;
		this.host = host;
		this.port = port;
		this.contextPath = contextPath;
	}

	public HttpApiInfo(String contextPath) {
		super();
		this.contextPath = contextPath;
	}

	public HttpApiInfo(String host, Integer port, String contextPath) {
		super();
		this.host = host;
		this.port = port;
		this.contextPath = contextPath;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public HttpApiInfo() {
		super();
	}

	public String getContextPath() {
		return contextPath;
	}

	@Override
	public String toString() {
		return "HttpApiInfo [scheme=" + scheme + ", host=" + host + ", port=" + port + ", contextPath=" + contextPath
				+ "]";
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public String getBaseUrl() {
		boolean b = (scheme.equalsIgnoreCase("http") && port == 80)
				|| (scheme.equalsIgnoreCase("https") && port == 443);
		return String.format("%s://%s%s%s", scheme, host, b ? "" : ":", b ? "" : port, contextPath);
	}
}
