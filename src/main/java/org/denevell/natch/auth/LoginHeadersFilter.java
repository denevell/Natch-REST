package org.denevell.natch.auth;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.denevell.natch.serv.login.LoginModel;

public class LoginHeadersFilter implements Filter {
	
	private static final String AUTHENTICATION_KEY = "AuthKey";
	public static final String KEY_SERVLET_REQUEST_LOGGEDIN_USERNAME= "authed_username";
	public static final String KEY_SERVLET_REQUEST_LOGGEDIN_AUTHKEY = "authkey";
	private LoginModel mLoginModel;
	
	public LoginHeadersFilter() {
		mLoginModel = new LoginModel();
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
		// Get the request info for the header
		HttpServletRequest request = (HttpServletRequest) req;
		// Get the auth data from the header
		String authKey = request.getHeader(AUTHENTICATION_KEY);
		// Check it
		String username = mLoginModel.loggedInAs(authKey);
		if(username==null || username.trim().length()==0) {
			HttpServletResponse r = (HttpServletResponse) resp;
			r.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Doesn't man you need to HTTP auth, but this is a good response fit.
			return;
		} else { // Since we're logged in, set the auth object
			request.setAttribute(KEY_SERVLET_REQUEST_LOGGEDIN_USERNAME, username);
			request.setAttribute(KEY_SERVLET_REQUEST_LOGGEDIN_AUTHKEY, authKey);
			chain.doFilter(request, resp);		
		}
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

}