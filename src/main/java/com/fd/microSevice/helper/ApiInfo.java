package com.fd.microSevice.helper;

import org.springframework.web.bind.annotation.RequestMethod;

/**
 * REST API 描述
 * 
 * @author 符冬
 *
 */
public class ApiInfo {
	/**
	 * 接口名称/users
	 */
	private String name;

	/**
	 * HTTP请求方法，GET, HEAD, POST, PUT, PATCH, DELETE, OPTIONS, TRACE
	 */
	private RequestMethod method;

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return "ApiInfo [name=" + name + ", method=" + method + "]";
	}

	public ApiInfo(String name, RequestMethod method) {
		super();
		this.name = name;
		this.method = method;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		ApiInfo other = (ApiInfo) obj;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ApiInfo(String name) {
		super();
		this.name = name;
	}

	public RequestMethod getMethod() {
		return method;
	}

	public ApiInfo() {
		super();
	}

	public void setMethod(RequestMethod method) {
		this.method = method;
	}

}
