package com.nimbuscart.user_service.Service;
import com.nimbuscart.user_service.Model.User;
import com.nimbuscart.user_service.dto.LoginRequestDto;
import com.nimbuscart.user_service.dto.LoginResponseDto;
import com.nimbuscart.user_service.dto.UserRequestDto;
import com.nimbuscart.user_service.dto.UserResponseDto;
import com.nimbuscart.user_service.repository.UserRepository;
import io.jsonwebtoken.security.Password;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class UserService {
    private final UserRepository userRepository;
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;


    }
    public UserResponseDto createUser(UserRequestDto dto) {
        User user = new User(dto.getName(), dto.getEmail());
        User saved = userRepository.save(user);
        return toResponseDto(saved);
    }
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponseDto)
                .toList();
    }
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id).orElse(null);
        return user == null ? null : toResponseDto(user);
    }
    public UserResponseDto updateUser(Long id, UserRequestDto dto) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return null;
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        return toResponseDto(userRepository.save(user));
    }
    public boolean deleteUser(Long id) {
        if (!userRepository.existsById(id)) return false;
        userRepository.deleteById(id);
        return true;
    }
    private UserResponseDto toResponseDto(User user) {
        return new UserResponseDto(user.getId(), user.getName(), user.getEmail());
    }
    public String login(LoginRequestDto dto) {
        User user = userRepository.findByEmail(dto.getEmail()).orElse(null);
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return null;
        }
        return jwtUtil.generateToken(user.getEmail());
    }
    public String login(LoginRequestDto dto) {
        User user = userRepository.findByEmail(dto.getEmail()).orElse(null);
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return null;
        }
        return jwtUtil.generateToken(user.getEmail(), user.getRole());
    }
}