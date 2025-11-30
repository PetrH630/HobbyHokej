package cz.phsoft.hokej.services.impl;

import cz.phsoft.hokej.data.entities.UserEntity;
import cz.phsoft.hokej.data.repositories.UserRepository;
import cz.phsoft.hokej.models.services.UserService;

import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    // Constructor injection (bez Lomboku)
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserEntity createUser(UserEntity user) {
        return userRepository.save(user);
    }

    @Override
    public Optional<UserEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
