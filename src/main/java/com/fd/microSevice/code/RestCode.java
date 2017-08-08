package com.fd.microSevice.code;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import javax.websocket.DecodeException;
import javax.websocket.Decoder.Text;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fd.microSevice.helper.ClientApi;
import com.fd.microSevice.helper.CoordinateUtil;

/**
 * 对数据进行编码和解码
 * 
 * @author 符冬
 *
 */
public class RestCode implements Text<ClientApi>, javax.websocket.Encoder.Text<ClientApi> {
	static Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Override
	public void init(EndpointConfig config) {
	}

	@Override
	public void destroy() {
	}

	@Override
	public String encode(ClientApi object) throws EncodeException {
		try {
			return CoordinateUtil.JSON_MP.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			log.error(String.format("encode 报错:%s", object), e);
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public ClientApi decode(String s) throws DecodeException {
		try {
			return CoordinateUtil.JSON_MP.readValue(s, new TypeReference<ClientApi>() {
			});
		} catch (IOException e) {
			log.error(String.format("decode 报错:%s", s), e);
			throw new IllegalArgumentException(e);
		}
	}

	@Override
	public boolean willDecode(String s) {
		if (s != null && s.length() > 0 && s.startsWith("{") && s.contains("}")) {
			return true;
		}
		log.error(String.format("无法识别%s", s));
		return false;
	}

}
