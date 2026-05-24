/* package com.redpatitas.redPatitas.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.crypto.SecretKey;

import org.springframework.util.AntPathMatcher;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.slf4j.MDC;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redpatitas.redPatitas.config.SecurityProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import com.redpatitas.redPatitas.config.TraceIdFilter;
import com.redpatitas.redPatitas.dto.response.ApiErrorResponse;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final AntPathMatcher MATCHER = new AntPathMatcher();

	private final JwtProperties jwtProperties;
	private final ObjectMapper objectMapper;
	private final SecurityProperties securityProperties;

	/** No validar JWT en login ni documentación (evita 401 si se envía un Bearer erróneo en /login). 
	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getServletPath();
		return MATCHER.match("/api/v1/auth/login", path)
				|| MATCHER.match("/api/v1/auth/refresh", path)
				|| MATCHER.match("/api/v1/auth/register", path)
				|| MATCHER.match("/v3/api-docs/**", path)
				|| MATCHER.match("/swagger-ui/**", path)
				|| MATCHER.match("/swagger-ui.html", path);
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		// Para pruebas locales: usar únicamente X-User-Id y X-User-Roles como autenticación.
		// La validación de JWT está comentada temporalmente.
		if (securityProperties != null && securityProperties.isAllowHeaderAuth()) {
			String userId = request.getHeader("X-User-Id");
			String rolesHeader = request.getHeader("X-User-Roles");
			if (userId != null && !userId.isBlank()) {
				List<GrantedAuthority> authorities = new ArrayList<>();
				if (rolesHeader != null && !rolesHeader.isBlank()) {
					for (String r : rolesHeader.split(",")) {
						if (r != null && !r.isBlank()) {
							String role = r.trim().startsWith("ROLE_") ? r.trim() : "ROLE_" + r.trim();
							authorities.add(new SimpleGrantedAuthority(role));
						}
					}
				} else {
					// Para pruebas locales: si no se envía X-User-Roles, asumir ADMIN para poder probar desde Swagger.
					authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
				}
				Authentication auth = new UsernamePasswordAuthenticationToken(new JwtPrincipal(userId, null), null, authorities);
				SecurityContextHolder.getContext().setAuthentication(auth);
			}
			filterChain.doFilter(request, response);
			return;
		}

		/*
		// JWT validation temporarily disabled for local testing.
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null || !header.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}
		String token = header.substring(7);
		try {
			SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
			Claims claims = Jwts.parserBuilder()
					.setSigningKey(key)
					.build()
					.parseClaimsJws(token)
					.getBody();

			String userId = claims.getSubject();
			if (userId == null || userId.isBlank()) {
				userId = claims.get("userid", String.class);
			}
			String email = claims.get("email", String.class);
			List<String> roles = extractRoles(claims.get("roles"));
			Collection<GrantedAuthority> authorities = new ArrayList<>();
			for (String r : roles) {
				if (r != null && !r.isBlank()) {
					authorities.add(new SimpleGrantedAuthority(r.startsWith("ROLE_") ? r : "ROLE_" + r));
				}
			}
			Authentication auth = new UsernamePasswordAuthenticationToken(
					new JwtPrincipal(userId, email),
					null,
					authorities);
			SecurityContextHolder.getContext().setAuthentication(auth);
		}
		catch (JwtException | IllegalArgumentException ex) {
			SecurityContextHolder.clearContext();
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			String trace = firstNonBlank(MDC.get(TraceIdFilter.TRACE_ID_MDC), request.getHeader(TraceIdFilter.TRACE_ID_HEADER));
			var body = new ApiErrorResponse(
					"INVALID_OR_EXPIRED_TOKEN",
					"Token Bearer inválido o expirado",
					null,
					trace);
			objectMapper.writeValue(response.getOutputStream(), body);
			return;
		}
		
		filterChain.doFilter(request, response);
	}

	private static List<String> extractRoles(Object raw) {
		if (raw == null) {
			return List.of();
		}
		if (raw instanceof Collection<?> c) {
			List<String> out = new ArrayList<>();
			for (Object o : c) {
				if (o != null) {
					out.add(o.toString());
				}
			}
			return out;
		}
		return List.of(raw.toString());
	}

	private static String firstNonBlank(String a, String b) {
		if (a != null && !a.isBlank()) {
			return a;
		}
		if (b != null && !b.isBlank()) {
			return b;
		}
		return null;
	}
}

 */