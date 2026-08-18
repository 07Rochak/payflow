package com.rochak.payflow.controller;

import com.rochak.payflow.dto.auth.AuthResponseDTO;
import com.rochak.payflow.dto.auth.LoginRequestDTO;
import com.rochak.payflow.dto.request.LogoutRequestDto;
import com.rochak.payflow.dto.request.RefreshTokenRequestDto;
import com.rochak.payflow.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication, refresh-token rotation and logout operations.")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user", description = "Authenticates a PayFlow user and returns a JWT access token and stateful refresh token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentication successful", content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid credentials or request", content = @Content)
    })
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid LoginRequestDTO request, HttpServletRequest httpServletRequest)
    {
        return ResponseEntity.ok(authService.login(request, httpServletRequest));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Validates the refresh token and its Redis-backed session, then issues a new access/refresh token pair through token rotation.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens refreshed successfully", content = @Content(schema = @Schema(implementation = AuthResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid, expired or reused refresh token", content = @Content)
    })
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid RefreshTokenRequestDto token, HttpServletRequest request){
        return ResponseEntity.ok(authService.refreshToken(token, request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Invalidates the session associated with the supplied refresh token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logged out successfully", content = @Content(examples = @ExampleObject(value = "Logged out successfully"))),
            @ApiResponse(responseCode = "400", description = "Invalid refresh token or session", content = @Content)
    })
    public ResponseEntity<String> logout(@RequestBody @Valid LogoutRequestDto request){
        authService.logout(request);

        return ResponseEntity.ok("Logged out successfully");
    }
}
