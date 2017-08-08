package com.fd.microSevice.helper;

import javax.websocket.Session;

/**
 * 客户端API连接
 * 
 * @author 符冬
 *
 */
public class ClientInfo {
	private ClientApi clientApi;
	private Session session;

	public ClientApi getClientApi() {
		return clientApi;
	}

	public void setClientApi(ClientApi clientApi) {
		this.clientApi = clientApi;
	}

	public Session getSession() {
		return session;
	}

	public ClientInfo(ClientApi clientApi, Session session) {
		super();
		this.clientApi = clientApi;
		this.session = session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public ClientInfo(ClientApi clientApi) {
		super();
		this.clientApi = clientApi;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clientApi == null) ? 0 : clientApi.hashCode());
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
		ClientInfo other = (ClientInfo) obj;
		if (clientApi == null) {
			if (other.clientApi != null)
				return false;
		} else if (!clientApi.equals(other.clientApi))
			return false;
		return true;
	}

	public ClientInfo() {
		super();
	}

}
