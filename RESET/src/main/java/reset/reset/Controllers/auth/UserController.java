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
import reset.reset.dto.auth.UserDTO;
import reset.reset.dto.auth.UserResumoDTO;
import reset.reset.dto.filter.UserFilter;
import reset.reset.dto.request.UpdateUserRequest;

import java.util.List;

@RestController
@RequestMapping("/utilizadores")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User", description = "User management endpoints")
@PreAuthorize("hasRole('ADMIN')")
public class UserController extends BaseController {

    private final UserService utilizadorService;

    @GetMapping
    @Operation(summary = "Get all users with pagination and filtering - returns summary DTOs")
    public ResponseEntity<ApiResponse<Page<UserResumoDTO>>> findAll(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            UserFilter filter) {
        Page<UserResumoDTO> users = utilizadorService.filterSummarized(filter, pageable);
        return success(users);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all users as list - returns summary DTOs")
    public ResponseEntity<ApiResponse<List<UserResumoDTO>>> findAllList() {
        List<UserResumoDTO> users = utilizadorService.findAllSummarized();
        return success(users);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID - returns full DTO")
    public ResponseEntity<ApiResponse<UserDTO>> findById(@PathVariable Long id) {
        User user = utilizadorService.findByIdOrThrow(id);
        return success(UserDTO.fromEntity(user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user - returns full DTO")
    public ResponseEntity<ApiResponse<UserDTO>> update(@PathVariable Long id,
                                                       @Valid @RequestBody UpdateUserRequest request) {
        User user = request.toEntity();
        user.setId(id);
        User updated = utilizadorService.update(id, user);
        return success(UserDTO.fromEntity(updated), "User updated successfully");
    }

    @PatchMapping("/{id}/change-password")
    @Operation(summary = "Change user password - returns full DTO")
    public ResponseEntity<ApiResponse<UserDTO>> changePassword(@PathVariable Long id,
                                                               @Valid @RequestBody ChangePasswordRequest request) {
        User user = utilizadorService.changePassword(id, request);
        return success(UserDTO.fromEntity(user), "Password changed successfully");
    }

    @PatchMapping("/{id}/reset-password")
    @Operation(summary = "Reset user password (Admin only) - returns full DTO")
    public ResponseEntity<ApiResponse<UserDTO>> resetPassword(@PathVariable Long id,
                                                              @RequestParam String newPassword) {
        User user = utilizadorService.resetPassword(id, newPassword);
        return success(UserDTO.fromEntity(user), "Password reset successfully");
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate user - returns full DTO")
    public ResponseEntity<ApiResponse<UserDTO>> activate(@PathVariable Long id) {
        User user = utilizadorService.ativarUser(id);
        return success(UserDTO.fromEntity(user), "User activated successfully");
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate user - returns full DTO")
    public ResponseEntity<ApiResponse<UserDTO>> deactivate(@PathVariable Long id) {
        User user = utilizadorService.desativarUser(id);
        return success(UserDTO.fromEntity(user), "User deactivated successfully");
    }

    @PatchMapping("/{id}/roles")
    @Operation(summary = "Update user roles - returns full DTO")
    public ResponseEntity<ApiResponse<UserDTO>> updateRoles(@PathVariable Long id,
                                                            @RequestBody UpdateUserRequest request) {
        User user = utilizadorService.updateRoles(id, request.getRoleNames());
        return success(UserDTO.fromEntity(user), "Roles updated successfully");
    }

    @GetMapping("/empresa/{empresaId}")
    @Operation(summary = "Get users by company - returns summary DTOs")
    public ResponseEntity<ApiResponse<Page<UserResumoDTO>>> findByEmpresa(
            @PathVariable Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UserResumoDTO> users = utilizadorService.findActiveByEmpresaIdSummarized(empresaId, pageable);
        return success(users);
    }

    @GetMapping("/role/{roleName}")
    @Operation(summary = "Get users by role - returns summary DTOs")
    public ResponseEntity<ApiResponse<List<UserResumoDTO>>> findByRole(@PathVariable String roleName) {
        List<UserResumoDTO> users = utilizadorService.findByRoleSummarized(roleName);
        return success(users);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        utilizadorService.deleteById(id);
        return noContent();
    }
}