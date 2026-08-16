package reset.reset.Controllers.auth;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reset.reset.Controllers.base.ApiResponse;
import reset.reset.Controllers.base.BaseController;
import reset.reset.Models.auth.User;
import reset.reset.Services.auth.UserService;
import reset.reset.dto.auth.ChangePasswordRequest;
import reset.reset.dto.filter.UserFilter;
import reset.reset.dto.request.UpdateUserRequest;

@RestController
@RequestMapping("/utilizadores")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User", description = "User management endpoints")
@PreAuthorize("hasRole('ADMIN')")
public class UserController extends BaseController {

    private final UserService utilizadorService;

    @GetMapping
    @Operation(summary = "Get all users with pagination and filtering")
    public ResponseEntity<ApiResponse<Page<User>>> findAll(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            UserFilter filter) {
        Page<User> users = utilizadorService.filter(filter);
        return success(users);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<ApiResponse<User>> findById(@PathVariable Long id) {
        User user = utilizadorService.findByIdOrThrow(id);
        return success(user);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user")
    public ResponseEntity<ApiResponse<User>> update(@PathVariable Long id,
                                                          @Valid @RequestBody UpdateUserRequest request) {
        User user = request.toEntity();
        user.setId(id);
        User updated = utilizadorService.update(id, user);
        return success(updated, "User updated successfully");
    }

    @PatchMapping("/{id}/change-password")
    @Operation(summary = "Change user password")
    public ResponseEntity<ApiResponse<User>> changePassword(@PathVariable Long id,
                                                                  @Valid @RequestBody ChangePasswordRequest request) {
        User user = utilizadorService.changePassword(id, request);
        return success(user, "Password changed successfully");
    }

    @PatchMapping("/{id}/reset-password")
    @Operation(summary = "Reset user password (Admin only)")
    public ResponseEntity<ApiResponse<User>> resetPassword(@PathVariable Long id,
                                                                 @RequestParam String newPassword) {
        User user = utilizadorService.resetPassword(id, newPassword);
        return success(user, "Password reset successfully");
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate user")
    public ResponseEntity<ApiResponse<User>> activate(@PathVariable Long id) {
        User user = utilizadorService.ativarUser(id);
        return success(user, "User activated successfully");
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate user")
    public ResponseEntity<ApiResponse<User>> deactivate(@PathVariable Long id) {
        User user = utilizadorService.desativarUser(id);
        return success(user, "User deactivated successfully");
    }

    @PatchMapping("/{id}/roles")
    @Operation(summary = "Update user roles")
    public ResponseEntity<ApiResponse<User>> updateRoles(@PathVariable Long id,
                                                               @RequestBody UpdateUserRequest request) {
        User user = utilizadorService.updateRoles(id, request.getRoleNames());
        return success(user, "Roles updated successfully");
    }

    @GetMapping("/empresa/{empresaId}")
    @Operation(summary = "Get users by company")
    public ResponseEntity<ApiResponse<Page<User>>> findByEmpresa(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<User> users = utilizadorService.findActiveByEmpresaId(empresaId, pageable);
        return success(users);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        utilizadorService.deleteById(id);
        return noContent();
    }
}
