package app.service;

import app.dto.RegisterRequest;
import app.dto.UserResponse;
import app.entity.Role;
import app.entity.User;
import app.exception.ConflictException;
import app.repository.UserRepository;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  private static final Logger log = LoggerFactory.getLogger(AuthService.class);

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final MeterRegistry meterRegistry;

  public AuthService(
    UserRepository userRepository,
    PasswordEncoder passwordEncoder,
    MeterRegistry meterRegistry
) {
  this.userRepository = userRepository;
  this.passwordEncoder = passwordEncoder;
  this.meterRegistry = meterRegistry;
}

  @Transactional
  public UserResponse register(RegisterRequest request) {
    if (userRepository.existsByUsername(request.username())) {
      throw new ConflictException("User already exists");
    }
    User user = new User();
    user.setUsername(request.username());
    user.setPassword(passwordEncoder.encode(request.password()));
    user.setRole(Role.USER);
    userRepository.save(user);

    meterRegistry.counter("users.registered").increment();

    log.info("User registered: id={}", user.getId());
    return UserResponse.from(user);
  }
}
