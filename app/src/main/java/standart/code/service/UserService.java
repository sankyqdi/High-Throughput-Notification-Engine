package standart.code.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import standart.code.dto.ChangePasswordRequest;
import standart.code.dto.RegisterUserRequest;
import standart.code.dto.UserResponseDTO;
import standart.code.error.UserAlreadyExistsException;
import standart.code.mapper.UserMapper;
import standart.code.model.User;
import standart.code.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService implements UserServiceInterface {

    private final UserRepository userRepository;

    private final UserMapper mapper;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponseDTO register(RegisterUserRequest request) {

        if (userRepository.existsByUsername(request.email())) {

            throw new UserAlreadyExistsException("HTTP 409 Conflict");

        }

        String encryptPassword = passwordEncoder.encode(request.password());

        User user = mapper.map(request, encryptPassword);

        User saveUser = userRepository.save(user);

        return mapper.map(saveUser);

    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с email " + username + " не найден"));

    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getProfile(Long id) {

        return mapper.map(userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с id " + id + " не найден")));

    }

    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с email " + username + " не найден"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {

            throw new BadCredentialsException("Неверный текущий пароль");

        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);


    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDTO getCurrentProfile(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с email " + username + " не найден"));

        return mapper.map(user);

    }
}
