package cz.phsoft.hokej.models.services;

import cz.phsoft.hokej.models.dto.AppUserDTO;
import cz.phsoft.hokej.models.dto.RegisterUserDTO;

public interface AppUserService {
    /**
     * Registrace nového uživatele
     *
     * @param registerUserDTO data pro registraci
     * @throws IllegalArgumentException pokud email existuje nebo hesla se neshodují
     */
    void register(RegisterUserDTO registerUserDTO);

    AppUserDTO getCurrentUser(String email);
}
