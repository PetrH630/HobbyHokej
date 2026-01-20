package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.models.dto.AppUserDTO;
import cz.phsoft.hokej.models.dto.RegisterUserDTO;

import java.util.List;

public interface AppUserService {
    /**
     * Registrace nového uživatele
     *
     * @param registerUserDTO data pro registraci
     * @throws IllegalArgumentException pokud email existuje nebo hesla se neshodují
     */
    void register(RegisterUserDTO registerUserDTO);

    AppUserDTO getCurrentUser(String email);

    List<AppUserDTO> getAllUsers();

    void changePassword(String email, String oldPassword, String newPassword, String newPasswordConfirm);

    void resetPassword(Long userId);

    void updateUser(String email, AppUserDTO dto);

    boolean activateUser(String token);
}
