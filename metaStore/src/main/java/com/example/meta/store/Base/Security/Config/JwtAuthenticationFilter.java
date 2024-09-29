package com.example.meta.store.Base.Security.Config;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.meta.store.werehouse.Controllers.CompanyController;
import com.example.meta.store.werehouse.Enums.AccountType;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final UserDetailsService userDetailsService;
	
	private final JwtService jwtService;
	
	
	public String userName;
	
	public AccountType accountType;

	private final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
	@Override
	protected void doFilterInternal(
			 HttpServletRequest request,
			 HttpServletResponse response, 
			 FilterChain filterChain)
			throws ServletException, IOException {
		final String authHeader = request.getHeader("Authorization");
	    final String accountTypeHeader = request.getHeader("Account-Type");
	    if(accountTypeHeader!=null) {	    	
	    this.accountType = AccountType.valueOf(accountTypeHeader);
	    }
		final String jwt;
		if(authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}
		jwt = authHeader.substring(7);
		this.userName = jwtService.extractUserName(jwt);
		if(this.userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			UserDetails userDetails = userDetailsService.loadUserByUsername(this.userName);
			if(jwtService.isTokenValid(jwt, userDetails)) {
				UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
					 userDetails, null,
					 userDetails.getAuthorities());
				authToken.setDetails(
						new WebAuthenticationDetailsSource().buildDetails(request));
				SecurityContextHolder.getContext().setAuthentication(authToken);
			}
		}
		
		 filterChain.doFilter(request, response);
	}
	


	    @Override
	    public void destroy() {}

}
