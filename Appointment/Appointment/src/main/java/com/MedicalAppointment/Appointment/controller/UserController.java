package com.MedicalAppointment.Appointment.controller;


import com.MedicalAppointment.Appointment.Enum.TimeAppointmentEnum;
import com.MedicalAppointment.Appointment.dto.CreateDto;
import com.MedicalAppointment.Appointment.model.AppointmentModel;
import com.MedicalAppointment.Appointment.model.CloseAppointmentsModel;
import com.MedicalAppointment.Appointment.model.NumberTurnModel;
import com.MedicalAppointment.Appointment.model.UserModel;

import com.MedicalAppointment.Appointment.repository.AppointmentRepository;
import com.MedicalAppointment.Appointment.repository.IsClosedRepository;
import com.MedicalAppointment.Appointment.repository.NumberTurnRepository;
import com.MedicalAppointment.Appointment.repository.UserRepository;
import com.MedicalAppointment.Appointment.service.GenerateCodeAcessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final GenerateCodeAcessService generateCodeAcessService;
    private final NumberTurnRepository numberTurnRepository;
    private final IsClosedRepository isClosedRepository;


    @PostMapping("/appointment/create")
    public ResponseEntity<String> create(@RequestBody CreateDto createDto, @AuthenticationPrincipal UserModel userModel) {

        // Create payment system to register to avoid unnecessary medical appointments!

        Optional<CloseAppointmentsModel> closeAppointmentsModel = this.isClosedRepository.findById(Long.valueOf(1));
        if (closeAppointmentsModel.get().isClosed()){return ResponseEntity.status(HttpStatus.LOCKED).body("Appointments is Closed!");}

        if (userModel.getQuantity() == 1) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("There is already an appointment scheduled!");
        }

        Optional<AppointmentModel> existingAppointment = this.appointmentRepository.findByDateAndTimeAppointmentEnum(createDto.date(), createDto.time());
        if (existingAppointment.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Consultation already scheduled!");
        }


        AppointmentModel appointmentModel = new AppointmentModel();
        appointmentModel.setDate(createDto.date());

        NumberTurnModel numberTurnModel = this.numberTurnRepository.findById(1);

        TimeAppointmentEnum timeEnum;
        timeEnum = TimeAppointmentEnum.valueOf(createDto.time());

        appointmentModel.setTimeAppointmentEnum(timeEnum.toString());
        appointmentModel.setIdUser(userModel.getId());

        numberTurnModel.setNumberTurn(numberTurnModel.getNumberTurn()+1);
        this.numberTurnRepository.save(numberTurnModel);

        appointmentModel.setNumberTurn(numberTurnModel.getNumberTurn());
        appointmentModel.setAcessCode(generateCodeAcessService.generateCode());

        this.appointmentRepository.save(appointmentModel);

        userModel.setQuantity(userModel.getQuantity()+1);
        this.userRepository.save(userModel);

        return ResponseEntity.status(HttpStatus.CREATED).body("Appointment created!");
    }


    @PutMapping("/confirm/{code}")
    public ResponseEntity<String> confirm(@PathVariable(value = "code") String code, @AuthenticationPrincipal UserModel userModel){
        Optional<AppointmentModel> optionalAppointmentModel = this.appointmentRepository.findByAcessCode(code);
        if (optionalAppointmentModel.isPresent() && optionalAppointmentModel.get().getIdUser() == userModel.getId()){
            return ResponseEntity.ok("Confirmed");
        }
        return ResponseEntity.badRequest().build();
    }








}
