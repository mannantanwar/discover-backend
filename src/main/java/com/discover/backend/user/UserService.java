package com.discover.backend.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final UserMapper userMapper;

    public UserDto getByPublicId(UUID publicId) {
        User user = userRepo.findByPublicId(publicId)
                .orElseThrow(() -> new RuntimeException("User not found: " + publicId));
        return userMapper.toDto(user);
    }
}
