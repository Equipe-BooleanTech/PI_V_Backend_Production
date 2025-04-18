package com.fatec.backend.service.user;


import com.fatec.backend.DTO.user.CreateUserDTO;
import com.fatec.backend.DTO.auth.JwtTokenDTO;
import com.fatec.backend.DTO.auth.LoginUserDTO;
import com.fatec.backend.DTO.user.UpdateUserDTO;
import com.fatec.backend.DTO.user.UserDTO;
import com.fatec.backend.DTO.vehicle.VehicleDTO;
import com.fatec.backend.exception.InvalidCredentialsException;
import com.fatec.backend.exception.UserNotFoundException;
import com.fatec.backend.mapper.user.UserMapper;
import com.fatec.backend.mapper.vehicle.VehicleMapper;
import com.fatec.backend.model.UserDetailsImpl;
import com.fatec.backend.model.Vehicle;
import com.fatec.backend.repository.UserRepository;
import com.fatec.backend.configuration.SecurityConfig;
import com.fatec.backend.model.User;
import com.fatec.backend.service.auth.JwtTokenService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;


@AllArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final SecurityConfig securityConfig;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;

    public UUID buscarIdPorEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
        return user.getId();
    }

    public String buscarNomePorEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);
        return user.getName() + " " + user.getLastname();
    }



        public UUID salvarUsuario(CreateUserDTO createUserDTO) {
            if (userRepository.findByEmail(createUserDTO.email()).isPresent()) {
                throw new RuntimeException("E-mail já cadastrado!");
            }
            User user = User.builder()
                    .email(createUserDTO.email())
                    .username(createUserDTO.username())
                    .password(passwordEncoder.encode(createUserDTO.password()))
                    .name(createUserDTO.name())
                    .lastname(createUserDTO.lastname())
                    .Phone(createUserDTO.phone())
                    .birthdate(createUserDTO.birthdate())
                    .build();

            return userRepository.save(user).getId();
        }

        public void atualizarUsuario(UUID id, UpdateUserDTO updateUserDTO) {
            User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuário não encontrado!"));
            user.setEmail(updateUserDTO.email);
            user.setUsername(updateUserDTO.username);
            userRepository.save(user);
        }

        public void deletarUsuario(UUID id) {
            if (!userRepository.existsById(id)) {
                throw new RuntimeException("Usuário não encontrado!");
            }
            userRepository.deleteById(id);
        }

    public JwtTokenDTO authenticarUsuario(LoginUserDTO loginUserDTO){
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                loginUserDTO.email(),
                loginUserDTO.password());
        try {
            Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);
            UserDetailsImpl modelUserDetails = (UserDetailsImpl) authentication.getPrincipal();
            String token = jwtTokenService.generateToken(modelUserDetails);
            return new JwtTokenDTO(token);

        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("FALHA NA AUTENTICAÇÃO: CREDENCIAIS INVÁLIDAS");
        } catch (Exception e) {
            throw new RuntimeException("ERRO INESPERADO NA AUTENTICAÇÃO: " + e.getMessage());
        }
    }

    public Page<UserDTO> listUsers(PageRequest pageRequest) {
        return userRepository.findAll(pageRequest)
                .map(UserMapper.INSTANCE::toUserDTO);
    }

    public User findById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Vehicle not found"));
    }
    }
