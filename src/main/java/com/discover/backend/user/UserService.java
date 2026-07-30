package com.discover.backend.user;

import com.discover.backend.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepo;
    private final UserMapper userMapper;

    public UserDto getByPublicId(UUID publicId) {
        User user = getEntityByPublicId(publicId);
        return userMapper.toDto(user);
    }

    public User getEntityByPublicId(UUID publicId) {
        return userRepo.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + publicId));
    }

    public User upsertFromOAuth(String email, String providerSub, String displayName, String avatarUrl, String authProvider) {
        Optional<User> existingUser = userRepo.findByProviderSub(providerSub);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        Optional<User> oldUser = userRepo.findByEmail(email);
        if (oldUser.isPresent()) {
            // this person signed up another way before — link this provider account to that existing user
            User linkedUser = oldUser.get();
            linkedUser.setProviderSub(providerSub);
            linkedUser.setAuthProvider(authProvider);
            return userRepo.save(linkedUser);
        }

        User newUser = User.builder()
                .publicId(UUID.randomUUID())
                .email(email)
                .emailVerified(true)
                .displayName(displayName)
                .avatarUrl(avatarUrl)
                .authProvider(authProvider)
                .providerSub(providerSub)
                .build();
        return userRepo.save(newUser);
    }
}
