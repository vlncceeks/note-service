package app.controller;

import app.dto.RegisterRequest;
import app.dto.UserResponse;
import app.entity.User;
import app.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  @PostMapping("/register")
  public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
  }

  /** Проверка логина: если Basic-заголовок верный, вернётся 200 и текущий пользователь. */
  @GetMapping("/me")
  public ResponseEntity<UserResponse> me(@AuthenticationPrincipal User user) {
    return ResponseEntity.ok().body(UserResponse.from(user));
  }
}
