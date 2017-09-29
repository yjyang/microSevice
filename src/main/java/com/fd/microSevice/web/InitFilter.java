package com.fd.microSevice.web;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

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
		CoordinateUtil.connCoor(hai);

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
