package com.MedicalAppointment.Appointment.controller;

import com.MedicalAppointment.Appointment.dto.LoginDto;
import com.MedicalAppointment.Appointment.dto.RegisterDto;
import com.MedicalAppointment.Appointment.dto.ResponseDto;
import com.MedicalAppointment.Appointment.infra.security.TokenService;
import com.MedicalAppointment.Appointment.model.UserModel;
import com.MedicalAppointment.Appointment.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TokenService tokenService;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody LoginDto loginDto){
        UserModel userModel = this.userRepository.findByEmail(loginDto.email()).orElseThrow(() -> new RuntimeException("User Not Found"));
        if (passwordEncoder.matches(loginDto.password(),userModel.getPassword())){
            String token = this.tokenService.generateToken(userModel);
            return ResponseEntity.ok(new ResponseDto(userModel.getName(),userModel.getEmail(),token));
        }
        return ResponseEntity.badRequest().build();
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody RegisterDto registerDto){
        Optional<UserModel> userModelOptional = this.userRepository.findByEmail(registerDto.email());
        if (userModelOptional.isPresent()){
            UserModel newUser = new UserModel();
            newUser.setName(registerDto.name());
            newUser.setLastName(registerDto.lastName());
            newUser.setCpf(registerDto.cpf());
            newUser.setPassword(registerDto.password());
            newUser.setEmail(registerDto.email());
            newUser.setYearOld(registerDto.yearOld());
            newUser.setNumberWhatsapp(registerDto.number());
            this.userRepository.save(newUser);
            String token = this.tokenService.generateToken(newUser);
            return ResponseEntity.ok(new ResponseDto(newUser.getName(), newUser.getCpf(), newUser.getEmail()), newUser.getNumberWhatsapp(),newUser.getYearOld());
        }

    }
}