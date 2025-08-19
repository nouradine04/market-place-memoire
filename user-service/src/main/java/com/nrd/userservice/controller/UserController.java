package com.nrd.userservice.controller;


import com.nrd.userservice.dto.UserLoginDto;
import com.nrd.userservice.dto.UserProfileUpdateDto;
import com.nrd.userservice.dto.UserRegistrationDto;
import com.nrd.userservice.dto.UserResponseDto;
import com.nrd.userservice.security.JwtUtil;
import com.nrd.userservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody UserRegistrationDto dto) {
        return ResponseEntity.ok(userService.register(dto));
    }

    @PutMapping("/{id}/profile")
    public ResponseEntity<UserResponseDto> updateProfile(@PathVariable Long id, @RequestBody UserProfileUpdateDto dto) {
        return ResponseEntity.ok(userService.updateProfile(id, dto));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody UserLoginDto dto) {
        return ResponseEntity.ok(userService.login(dto.getIdentifier(), dto.getPassword()));
    }

    @PutMapping("/profile")  // Sans {id}, assume from token
    public ResponseEntity<UserResponseDto> updateProfile(@RequestBody UserProfileUpdateDto dto, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Long userId = jwtUtil.getUserIdFromToken(token);
        return ResponseEntity.ok(userService.updateProfile(userId, dto));
    }

    /// /// admin

    @GetMapping("/admin/users")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/admin/block/{userId}")
    public ResponseEntity<String> blockUser(@PathVariable Long userId) {
        userService.blockUser(userId);
        return ResponseEntity.ok("User bloqué");
    }

    @PutMapping("/admin/promote/{userId}")
    public ResponseEntity<String> promoteToAdmin(@PathVariable Long userId) {
        userService.promoteToAdmin(userId);
        return ResponseEntity.ok("Promu admin");
    }

    @GetMapping("/admin/stats/users")
    public ResponseEntity<Long> getTotalUsers() {
        return ResponseEntity.ok(userService.getTotalUsers());
    }
}