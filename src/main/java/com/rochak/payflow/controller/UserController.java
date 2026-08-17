package com.rochak.payflow.controller;

import com.rochak.payflow.dto.ChangePasswordDto;
import com.rochak.payflow.dto.request.CreateUserRequestDTO;
import com.rochak.payflow.dto.request.UserRequestDTO;
import com.rochak.payflow.dto.response.UserResponseDTO;
import com.rochak.payflow.security.SecurityUtils;
import com.rochak.payflow.service.UserService;
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
public class UserController {

    private final UserService userService;

    @PostMapping
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
    public ResponseEntity<String> changePassword(@RequestBody @Valid ChangePasswordDto request) {
        String email = SecurityUtils.getCurrentUserEmail();
        userService.changePassword(email, request);
        return new ResponseEntity<>("Password changed successfully", HttpStatus.OK);
    }
}
