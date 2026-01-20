package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.data.entities.AppUserEntity;
import cz.phsoft.hokej.data.enums.Role;
import cz.phsoft.hokej.data.repositories.AppUserRepository;
import cz.phsoft.hokej.models.dto.AppUserDTO;
import cz.phsoft.hokej.models.dto.RegisterUserDTO;
import cz.phsoft.hokej.models.dto.mappers.AppUserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AppUserMapper appUserMapper;

    public AppUserServiceImpl(AppUserRepository userRepository,
                              BCryptPasswordEncoder passwordEncoder, AppUserMapper appUserMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.appUserMapper = appUserMapper;
    }

    @Override
    public void register(RegisterUserDTO dto) {
        if (!dto.getPassword().equals(dto.getPasswordConfirm())) {
            throw new IllegalArgumentException("Hesla se neshodují");
        }

        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Uživatel s tímto emailem již existuje");
        }

        AppUserEntity user = new AppUserEntity();
        user.setName(dto.getName());
        user.setSurname(dto.getSurname());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.ROLE_PLAYER);

        userRepository.save(user);
    }

    @Override
    public void updateUser(String email, AppUserDTO dto) {

        AppUserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Uživatel nenalezen"));

        // Nastavení nového hesla
        user.setName(dto.getName());
        user.setSurname(dto.getSurname());
        user.setEmail(dto.getEmail());
        userRepository.save(user);
    }

    @Override
    public AppUserDTO getCurrentUser(String email) {
        AppUserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ← využití mapperu
        return appUserMapper.toDTO(user);
    }

    @Override
    public List<AppUserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(appUserMapper::toDTO)
                .toList();
    }

    @Override
    public void changePassword(String email, String oldPassword, String newPassword, String newPasswordConfirm) {
        if (!newPassword.equals(newPasswordConfirm)) {
            throw new IllegalArgumentException("Nová hesla se neshodují");
        }

        AppUserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Uživatel nenalezen"));

        // Ověření starého hesla
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Staré heslo je nesprávné");
        }

        // Nastavení nového hesla
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // reset hesla
    @Override
    public void resetPassword(Long userId) {
        AppUserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Uživatel nenalezen"));

        // Nastavení nového hesla na "Player123"
        user.setPassword(passwordEncoder.encode("Player123"));
        userRepository.save(user);
    }


}