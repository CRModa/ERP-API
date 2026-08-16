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
import reset.reset.Models.auth.Role;
import reset.reset.Services.auth.RoleService;
import reset.reset.dto.request.RoleRequest;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Role", description = "Role management endpoints")
@PreAuthorize("hasRole('ADMIN')")
public class RoleController extends BaseController {

    private final RoleService roleService;

    @PostMapping
    @Operation(summary = "Create a new role")
    public ResponseEntity<ApiResponse<Role>> create(@Valid @RequestBody RoleRequest request) {
        log.info("Creating new role: {}", request.getNome());
        Role role = request.toEntity();
        Role saved = roleService.save(role);
        return created(saved);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing role")
    public ResponseEntity<ApiResponse<Role>> update(@PathVariable Long id,
                                                    @Valid @RequestBody RoleRequest request) {
        log.info("Updating role with id: {}", id);
        Role role = request.toEntity();
        role.setId(id);
        Role updated = roleService.update(id, role);
        return success(updated, "Role updated successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get role by ID")
    public ResponseEntity<ApiResponse<Role>> findById(@PathVariable Long id) {
        Role role = roleService.findByIdOrThrow(id);
        return success(role);
    }

    @GetMapping
    @Operation(summary = "Get all roles")
    public ResponseEntity<ApiResponse<Page<Role>>> findAll(
            @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<Role> roles = roleService.findAll(pageable);
        return success(roles);
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active roles")
    public ResponseEntity<ApiResponse<List<Role>>> findActive() {
        List<Role> roles = roleService.findActiveRoles();
        return success(roles);
    }

    @PatchMapping("/{id}/permissions")
    @Operation(summary = "Add permissions to role")
    public ResponseEntity<ApiResponse<Role>> addPermissions(@PathVariable Long id,
                                                            @RequestBody Set<String> permissionNames) {
        Role role = roleService.addPermissions(id, permissionNames);
        return success(role, "Permissions added successfully");
    }

    @DeleteMapping("/{id}/permissions")
    @Operation(summary = "Remove permissions from role")
    public ResponseEntity<ApiResponse<Role>> removePermissions(@PathVariable Long id,
                                                               @RequestBody Set<String> permissionNames) {
        Role role = roleService.removePermissions(id, permissionNames);
        return success(role, "Permissions removed successfully");
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate role")
    public ResponseEntity<ApiResponse<Role>> activate(@PathVariable Long id) {
        Role role = roleService.ativarRole(id);
        return success(role, "Role activated successfully");
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate role")
    public ResponseEntity<ApiResponse<Role>> deactivate(@PathVariable Long id) {
        Role role = roleService.desativarRole(id);
        return success(role, "Role deactivated successfully");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete role")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        roleService.deleteById(id);
        return noContent();
    }
}
