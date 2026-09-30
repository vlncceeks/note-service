package app.config;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * 401/403 возникают в фильтрах Spring Security до контроллеров, поэтому @RestControllerAdvice их не видит.
 * Отвечаем тем же форматом problem+json и без заголовка WWW-Authenticate (чтобы браузер не показывал popup).
 */
@Component
public class SecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
  private static final Logger log = LoggerFactory.getLogger(SecurityErrorHandler.class);

  private final ObjectMapper objectMapper;

  public SecurityErrorHandler(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
      throws IOException {
    log.warn("Unauthorized: {} {}", request.getMethod(), request.getRequestURI());
    write(response, HttpStatus.UNAUTHORIZED, "Authentication required");
  }

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException ex)
      throws IOException {
    log.warn("Forbidden: {} {}", request.getMethod(), request.getRequestURI());
    write(response, HttpStatus.FORBIDDEN, "Access denied");
  }

  private void write(HttpServletResponse response, HttpStatus status, String detail) throws IOException {
    response.setStatus(status.value());
    response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
    objectMapper.writeValue(response.getOutputStream(), ProblemDetail.forStatusAndDetail(status, detail));
  }
}
