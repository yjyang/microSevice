package com.fd.microSevice.helper;

/**
 * 请求来源数据
 * 
 * @author 符冬
 *
 */
public class ReqInfo {
	private String remoteAddr;

	public String getRemoteAddr() {
		return remoteAddr;
	}

	public void setRemoteAddr(String remoteAddr) {
		this.remoteAddr = remoteAddr;
	}

	public ReqInfo(String remoteAddr) {
		super();
		this.remoteAddr = remoteAddr;
	}

	public ReqInfo() {
		super();
	}

}
