package com.selling.service;

import com.selling.dto.PagedResponse;
import com.selling.model.User;
import com.selling.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    public PagedResponse<User> getUsers(int page, int size, String query) {
        var pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<User> userPage;
        
        // Simple implementation for now, can expand search logic
        userPage = userRepo.findAll(pageable);
        
        return PagedResponse.<User>builder()
                .items(userPage.getContent())
                .totalItems(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .currentPage(userPage.getNumber() + 1)
                .build();
    }

    public User getUserById(Long id) {
        return userRepo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepo.save(user);
    }

    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);
        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        user.setRole(userDetails.getRole());
        user.setStatus(userDetails.getStatus());
        user.setMobile(userDetails.getMobile());
        return userRepo.save(user);
    }

    public void deleteUser(Long id) {
        userRepo.deleteById(id);
    }
}
