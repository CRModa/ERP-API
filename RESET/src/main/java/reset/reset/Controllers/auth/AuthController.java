package reset.reset.Controllers.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import reset.reset.Controllers.base.ApiResponse;
import reset.reset.Exceptions.UserNotEnabledException;
import reset.reset.Models.auth.User;
import reset.reset.Services.auth.AuthService;
import reset.reset.Services.auth.UserService;
import reset.reset.dto.auth.AuthResponse;
import reset.reset.dto.auth.LoginRequest;
import reset.reset.dto.auth.RegisterRequest;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication endpoints")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "Login user")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        log.info("Login attempt for user: {}", request.getUsername());

        try {
            AuthResponse authResponse = authService.authenticate(request);

            // Criar cookie HTTP-only com o token
            ResponseCookie authTokenCookie = ResponseCookie.from("AUTH-TOKEN", authResponse.getToken())
                    .httpOnly(true)
                    .secure(true)
                    .maxAge(4 * 3600) // 4 horas
                    .path("/")
                    .sameSite("None")
                    .build();

            // Criar cookie para indicar que o usuário está logado
            ResponseCookie loggedInCookie = ResponseCookie.from("IS_LOGGED_IN", "true")
                    .httpOnly(false)
                    .secure(true)
                    .maxAge(4 * 3600) // 4 horas
                    .path("/")
                    .sameSite("None")
                    .build();

            // Adicionar cookies ao cabeçalho da resposta
            response.addHeader(HttpHeaders.SET_COOKIE, authTokenCookie.toString());
            response.addHeader(HttpHeaders.SET_COOKIE, loggedInCookie.toString());

            // Remover o token da resposta para não enviar no body
            authResponse.setToken(null);

            return ResponseEntity.ok(ApiResponse.success(authResponse, "Login successful"));

        } catch (BadCredentialsException e) {
            log.error("Bad credentials for user: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Credenciais inválidas"));

        } catch (UserNotEnabledException e) {
            log.error("User not enabled: {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Usuário desativado. Contacte o administrador"));

        } catch (AuthenticationException e) {
            log.error("Authentication error for user: {}", request.getUsername(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(e.getMessage()));

        } catch (Exception e) {
            log.error("Unexpected error during login: {}", request.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Erro interno ao processar login"));
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user")
    public ResponseEntity<ApiResponse<User>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register attempt for user: {}", request.getUsername());
        try {
            User user = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(user, "User registered successfully"));
        } catch (Exception e) {
            log.error("Registration failed for user: {}", request.getUsername(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Erro ao registrar usuário: " + e.getMessage()));
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        log.info("Logout attempt");

        // Limpar o cookie AUTH-TOKEN
        ResponseCookie authTokenCookie = ResponseCookie.from("AUTH-TOKEN", "")
                .httpOnly(true)
                .secure(true)
                .maxAge(0) // Expira imediatamente
                .path("/")
                .sameSite("None")
                .build();

        // Limpar o cookie IS_LOGGED_IN
        ResponseCookie loggedInCookie = ResponseCookie.from("IS_LOGGED_IN", "false")
                .httpOnly(false)
                .secure(true)
                .maxAge(0) // Expira imediatamente
                .path("/")
                .sameSite("None")
                .build();

        // Adicionar cookies ao cabeçalho da resposta
        response.addHeader(HttpHeaders.SET_COOKIE, authTokenCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, loggedInCookie.toString());

        return ResponseEntity.ok(ApiResponse.success(null, "Logout realizado com sucesso"));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @CookieValue(value = "AUTH-TOKEN", required = false) String token,
            HttpServletResponse response) {

        log.info("Refresh token attempt");

        try {
            if (token == null || token.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Token não fornecido"));
            }

            AuthResponse authResponse = authService.refreshToken(token);

            // Criar novo cookie com o token atualizado
            ResponseCookie authTokenCookie = ResponseCookie.from("AUTH-TOKEN", authResponse.getToken())
                    .httpOnly(true)
                    .secure(true)
                    .maxAge(4 * 3600)
                    .path("/")
                    .sameSite("None")
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, authTokenCookie.toString());

            // Remover token da resposta
            authResponse.setToken(null);

            return ResponseEntity.ok(ApiResponse.success(authResponse, "Token refreshed successfully"));

        } catch (Exception e) {
            log.error("Token refresh failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Erro ao atualizar token: " + e.getMessage()));
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user information")
    public ResponseEntity<ApiResponse<User>> getCurrentUser() {
        try {
            User user = authService.getCurrentUser();
            return ResponseEntity.ok(ApiResponse.success(user));
        } catch (Exception e) {
            log.error("Error getting current user", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Usuário não autenticado"));
        }
    }

    @GetMapping("/check-permission/{permission}")
    @Operation(summary = "Check if current user has a specific permission")
    public ResponseEntity<ApiResponse<Boolean>> hasPermission(@PathVariable String permission) {
        try {
            boolean hasPermission = authService.hasPermission(permission);
            return ResponseEntity.ok(ApiResponse.success(hasPermission));
        } catch (Exception e) {
            log.error("Error checking permission: {}", permission, e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Erro ao verificar permissão"));
        }
    }
}