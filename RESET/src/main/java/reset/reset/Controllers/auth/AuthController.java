package reset.reset.Controllers.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reset.reset.Models.auth.User;
import reset.reset.Security.JwtService;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = (User) authentication.getPrincipal();

        String token = jwtService.generateTokenWithUserInfo(
                user,
                user.getId(),
                user.getEmpresa().getId()
        );

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .nome(user.getNome())
                .email(user.getEmail())
                .roles(user.getRoles().stream()
                        .map(role -> role.getNome())
                        .collect(Collectors.toList()))
                .permissions(user.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .empresaId(user.getEmpresa().getId())
                .empresaNome(user.getEmpresa().getNome())
                .expiresIn(jwtService.extractExpiration(token))
                .build();
    }

    @PostMapping("/refresh")
    public LoginResponse refresh(@RequestBody RefreshTokenRequest request) {
        String newToken = jwtService.refreshToken(request.getToken());
        // Get user info from old token
        String username = jwtService.extractUsername(request.getToken());

        // You might want to load user from database here
        // to get updated permissions

        return LoginResponse.builder()
                .token(newToken)
                .expiresIn(jwtService.extractExpiration(newToken))
                .build();
    }
}