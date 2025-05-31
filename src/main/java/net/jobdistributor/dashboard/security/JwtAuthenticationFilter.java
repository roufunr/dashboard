
package net.jobdistributor.dashboard.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.jobdistributor.dashboard.service.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtService jwtService;

    // REMOVED: LogoutService dependency - no longer needed

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String authHeader = request.getHeader("Authorization");
            String token = jwtService.extractTokenFromHeader(authHeader);

            // If no token or already authenticated, continue
            if (token == null || SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            // UPDATED: Single validation method that handles everything:
            // - JWT signature and expiration
            // - Token blacklist checking (individual logout)
            // - Token generation checking (logout-all)
            if (jwtService.validateToken(token)) {
                Long userId = jwtService.getUserIdFromToken(token);
                String email = jwtService.getEmailFromToken(token);

                if (userId != null && email != null) {
                    // Create authentication object
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userId,  // Principal (user ID)
                                    null,    // Credentials (not needed for JWT)
                                    new ArrayList<>()  // Authorities (can add roles here later)
                            );

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    logger.debug("JWT authentication successful for user: {}", userId);
                } else {
                    logger.debug("JWT token missing user data");
                }
            } else {
                // UPDATED: More specific logging about why token failed
                logger.debug("JWT token validation failed - token may be expired, blacklisted, or invalidated by logout-all");
            }
        } catch (Exception e) {
            logger.error("JWT authentication failed", e);
            // Don't set authentication - request will be rejected by security config
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // ADDED: Skip filter for public endpoints to improve performance
        String path = request.getRequestURI();

        // Skip JWT filter for public authentication endpoints
        return path.equals("/api/auth/login") ||
                path.equals("/api/auth/register") ||
                path.equals("/api/auth/forgot-password") ||
                path.equals("/api/auth/reset-password") ||
                path.startsWith("/api/public/") ||
                path.startsWith("/public/") ||
                path.equals("/health") ||
                path.equals("/actuator/health");
    }
}