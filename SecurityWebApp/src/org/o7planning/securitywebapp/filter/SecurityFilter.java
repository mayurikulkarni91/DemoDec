package org.o7planning.securitywebapp.filter;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.o7planning.securitywebapp.bean.UserAccount;
import org.o7planning.securitywebapp.request.UserRoleRequestWrapper;
import org.o7planning.securitywebapp.utils.AppUtils;
import org.o7planning.securitywebapp.utils.SecurityUtils;


@WebFilter("/*")
public class SecurityFilter implements Filter {

    
    public SecurityFilter() {
       
    }

	
	public void destroy() {
		
	}

	
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		 HttpServletRequest request1 = (HttpServletRequest) request;
	        HttpServletResponse response1 = (HttpServletResponse) response;
	 
	        String servletPath = request1.getServletPath();
	
	        UserAccount loginedUser = AppUtils.getLoginedUser(request1.getSession());
	 
	        if (servletPath.equals("/login")) {
	            chain.doFilter(request1, response1);
	            return;
	        }
	        HttpServletRequest wrapRequest = request1;
	 
	        if (loginedUser != null) {
	            // User Name
	            String userName = loginedUser.getUserName();
	 
	            // Roles
	            List<String> roles = loginedUser.getRoles();
	 
	            // Wrap old request by a new Request with userName and Roles information.
	            wrapRequest = new UserRoleRequestWrapper(userName, roles, request1);
	        }
	 
	        // Pages must be signed in.
	        if (SecurityUtils.isSecurityPage(request1)) {
	 
	            // If the user is not logged in,
	            // Redirect to the login page.
	            if (loginedUser == null) {
	 
	                String requestUri = request1.getRequestURI();
	 
	                // Store the current page to redirect to after successful login.
	                int redirectId = AppUtils.storeRedirectAfterLoginUrl(request1.getSession(), requestUri);
	 
	                response1.sendRedirect(wrapRequest.getContextPath() + "/login?redirectId=" + redirectId);
	                return;
	            }
	 
	            // Check if the user has a valid role?
	            boolean hasPermission = SecurityUtils.hasPermission(wrapRequest);
	            if (!hasPermission) {
	 
	                RequestDispatcher dispatcher = request1.getServletContext().getRequestDispatcher("/WEB-INF/views/accessDenied.jsp");
	 
	                dispatcher.forward(request1, response1);
	                return;
	            }
	        }
		chain.doFilter(request1, response1);
	}

	
	public void init(FilterConfig fConfig) throws ServletException {
		
	}

}
