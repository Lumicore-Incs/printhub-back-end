package com.selling.security.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.selling.dto.get.UserDtoForGet;
import com.selling.model.Otp;
import com.selling.model.User;
import com.selling.repository.OtpRepo;
import com.selling.security.UserService;
import com.selling.util.MailService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.selling.dto.UserDto;
import com.selling.repository.UserRepo;
import com.selling.util.MapperService;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final OtpRepo otpRepo;
    private final MailService mailService;
    private final MapperService mapperService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserServiceImpl(UserRepo userRepo, MapperService mapperService, OtpRepo otpRepo, MailService mailService) {
        this.userRepo = userRepo;
        this.otpRepo = otpRepo;
        this.mapperService = mapperService;
        this.mailService = mailService;
    }

    @Override
    public UserDto getUserById(String id) {
        User byId = userRepo.findUserById(Long.valueOf(id));
        return mapperService.map(byId, UserDto.class);
    }

    @Override
    public UserDto userLogin(UserDto dto) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        List<User> userNames = userRepo.findAllByName(dto.getName());
        for (User name : userNames) {
            boolean isPasswordMatches = passwordEncoder.matches(dto.getPassword(), name.getPassword());
            if (isPasswordMatches) {
                return mapperService.map(name, UserDto.class);
            }
        }
        return null;
    }

    @Override
    public List<UserDto> findUserByName(String userName) {
        List<User> byName = userRepo.findByName(userName);
        List<UserDto> userList = new ArrayList<>();

        for (User name : byName) {
            userList.add(mapperService.map(name, UserDto.class));
        }
        return userList;
    }

    @Override
    public UserDtoForGet registerUser(UserDto userDto) {
        User user = mapperService.map(userDto, User.class);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRegistration_date(String.valueOf(LocalDateTime.now()));
        User save = userRepo.save(user);
        return mapperService.map(save, UserDtoForGet.class);
    }

    @Override
    public UserDtoForGet createUser(UserDto userDto) {
        // Basic validation
        if (userDto.getName() == null || userDto.getName().isBlank() || userDto.getPassword() == null
                || userDto.getPassword().isBlank() || userDto.getName() == null || userDto.getName().isBlank()) {
            throw new IllegalArgumentException("Missing required fields: name, email, or password");
        }

        // Check for existing email
        Optional<User> existing = userRepo.findByEmail(userDto.getName());
        if (existing.isPresent()) {
            throw new RuntimeException("User Name already exists");
        }

        User user = mapperService.map(userDto, User.class);
        user.setId(null);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User save = userRepo.save(user);
        return mapperService.map(save, UserDtoForGet.class);
    }

    @Override
    public UserDtoForGet updateUser(UserDto userDto, Long userId) {
        User byId = userRepo.findUserById(userId);
        if (byId == null) {
            return null;
        } else {
            userDto.setId(byId.getId());
            userDto.setRegistration_date(byId.getRegistration_date());
            if (userDto.getPassword() == null) {
                userDto.setPassword(byId.getPassword());
            } else {
                userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
            }
            User user = mapperService.map(userDto, User.class);
            User save = userRepo.save(user);
            return mapperService.map(save, UserDtoForGet.class);
        }
    }

    @Override
    public List<UserDtoForGet> getAllUser() {
        List<User> all = userRepo.findAll();
        List<UserDtoForGet> allUsers = new ArrayList<>();
        for (User user : all) {
            UserDtoForGet userDto = mapperService.map(user, UserDtoForGet.class);
            allUsers.add(userDto);
        }
        return allUsers;
    }

    // otp part ===================================

    // Generate random 4-digit OTP
    private String generateOtp() {
        Random random = new Random();
        int otp = 1000 + random.nextInt(9000);
        return String.valueOf(otp);
    }

    // Send OTP to email
    public boolean sendOtpToEmail(String email) {
        try {
            Optional<User> user = userRepo.findByEmail(email);
            if (user.isPresent()) {
                Optional<Otp> byEmail = otpRepo.findByEmail(email);
                byEmail.ifPresent(otp -> otpRepo.deleteById(otp.getId()));

                // Generate new OTP
                String otp = generateOtp();
                LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(5);

                // Save OTP to database
                Otp otpEntity = new Otp(email, otp, expiryTime);
                otpRepo.save(otpEntity);

                // Send OTP via email
                return mailService.sendOtpEmail(email, otp);
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    // Validate OTP
    public boolean validateOtp(String email, String otp) {
        Otp otpEntity = otpRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("OTP not found for this email"));

        if (otpEntity.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired");
        }

        return otpEntity.getOtpCode().equals(otp);
    }


    @Override
    public boolean deleteUser(Integer id) {
        User user = userRepo.findById(Long.valueOf(id))
                .orElseThrow(() -> new RuntimeException("User not found"));
        // user.setStatus("DISABLED");
        userRepo.delete(user);
        return true;
    }

    @Override
    public List<UserDtoForGet> getAllUserWithOutAdmin() {
        List<User> all = userRepo.findAll();
        List<UserDtoForGet> allUsers = new ArrayList<>();
        for (User user : all) {
            if (!user.getRole().equals("ADMIN")) {
                UserDtoForGet userDto = mapperService.map(user, UserDtoForGet.class);
                allUsers.add(userDto);
            }
        }
        return allUsers;
    }

    @Override
    public void changePassword(String email, String newPassword) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        User save = userRepo.save(user);
        mapperService.map(save, UserDtoForGet.class);
    }
}
