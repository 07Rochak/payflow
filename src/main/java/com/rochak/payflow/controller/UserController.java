package com.rochak.payflow.controller;

import com.rochak.payflow.dto.ChangePasswordDto;
import com.rochak.payflow.dto.request.CreateUserRequestDTO;
import com.rochak.payflow.dto.request.UserRequestDTO;
import com.rochak.payflow.dto.response.UserResponseDTO;
import com.rochak.payflow.security.SecurityUtils;
import com.rochak.payflow.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User registration and self-service profile management.")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(summary = "Register user", description = "Creates a new USER account and initializes its wallet. The public registration endpoint always creates the USER role; the client cannot choose a role.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created successfully", content = @Content(schema = @Schema(implementation = UserResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or email already exists", content = @Content)
    })
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody CreateUserRequestDTO userRequestDTO){
        UserResponseDTO userResponseDTO = userService.createUser(userRequestDTO);
        return new ResponseEntity<>(userResponseDTO, HttpStatus.CREATED);
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id){
//        UserResponseDTO userResponseDTO = userService.getUserById(id);
//        return new ResponseEntity<>(userResponseDTO, HttpStatus.OK);
//    }
//
//    @GetMapping
//    public ResponseEntity<List<UserResponseDTO>> getAllUsers(){
//        return new ResponseEntity<>(userService.getAllUsers(), HttpStatus.OK);
//    }
//
    @PutMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update own profile", description = "Updates the authenticated user's name and email. The user identity is resolved from the JWT security context rather than a client-supplied user ID. If the email changes, all sessions are invalidated and the user must log in again.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated successfully", content = @Content(examples = @ExampleObject(value = "{\"message\":\"Profile updated successfully.\",\"requiresLogin\":false,\"user\":{\"id\":1,\"name\":\"Updated User\",\"email\":\"user@example.com\"}}"))),
            @ApiResponse(responseCode = "400", description = "Invalid request or email already exists", content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
            @ApiResponse(responseCode = "404", description = "Authenticated user not found", content = @Content)
    })
    public ResponseEntity<Map<String, Object>> updateUser(@Valid @RequestBody UserRequestDTO userRequestDTO){
        String currentEmail = SecurityUtils.getCurrentUserEmail();

        UserResponseDTO updatedUser =
                userService.updateUser(currentEmail, userRequestDTO);

        boolean emailChanged = !currentEmail.equals(userRequestDTO.getEmail());

        if (emailChanged) {
            return ResponseEntity.ok(
                    Map.of(
                            "message", "Profile updated successfully. Email was changed, so please log in again.",
                            "requiresLogin", true,
                            "user", updatedUser
                    )
            );
        }

        return ResponseEntity.ok(
                Map.of(
                        "message", "Profile updated successfully.",
                        "requiresLogin", false,
                        "user", updatedUser
                )
        );
    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<String> deleteUser(@PathVariable Long id){
//        userService.deleteUser(id);
//        return new ResponseEntity<>("User deleted successfully",HttpStatus.OK);
//    }

    @PutMapping("/change-password")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Change password", description = "Changes the authenticated user's password after verifying the current password.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed successfully", content = @Content(examples = @ExampleObject(value = "Password changed successfully"))),
            @ApiResponse(responseCode = "400", description = "Current password is incorrect or request is invalid", content = @Content),
            @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
    })
    public ResponseEntity<String> changePassword(@RequestBody @Valid ChangePasswordDto request) {
        String email = SecurityUtils.getCurrentUserEmail();
        userService.changePassword(email, request);
        return new ResponseEntity<>("Password changed successfully", HttpStatus.OK);
    }
}
