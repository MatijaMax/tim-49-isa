package ftn.isa.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import ftn.isa.domain.Reservation;
import ftn.isa.dto.*;
import ftn.isa.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import ftn.isa.domain.Appointment;
import ftn.isa.domain.Company;
import ftn.isa.service.AppointmentService;
import ftn.isa.service.CompanyAdminService;


@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping(value = "api/appointments")
public class AppointmentController {
	@Autowired
    private AppointmentService appointmentService;
	@Autowired
    private CompanyAdminService cAdminService;
    @Autowired
    private ReservationService reservationService;
	
	@GetMapping(value = "/all")
    public ResponseEntity<List<AppointmentResponseDTO>> getAppointments() {
        List<Appointment> appointments = appointmentService.findAll();

        List<AppointmentResponseDTO> aResponseDTOs = new ArrayList<>();
        for (Appointment c : appointments) {
        	aResponseDTOs.add(new AppointmentResponseDTO(c));
        }

        return new ResponseEntity<>(aResponseDTOs, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('COMPANY_ADMIN')")
	@PostMapping(consumes = "application/json")
    public ResponseEntity<AppointmentResponseDTO> save(@RequestBody AppointmentCreateDTO aDTO){
        Appointment a = new Appointment();
        a.setDateTime(aDTO.getDateTime());
        a.setDuration(aDTO.getDuration());
        a.setAdmin(cAdminService.findOne(aDTO.getAdminsId()));
        
        a = appointmentService.save(a);


        AppointmentResponseDTO aResponseDTO = new AppointmentResponseDTO(a);
        return new ResponseEntity<>(aResponseDTO, HttpStatus.OK);
    }
	
	@GetMapping(value = "/{id}")
    public ResponseEntity<AppointmentResponseDTO> getById(@PathVariable Integer id) {
		Appointment a = appointmentService.getById(id);
        AppointmentResponseDTO aResponseDTO = new AppointmentResponseDTO(a);

        return new ResponseEntity<>(aResponseDTO, HttpStatus.OK);
    }

    @GetMapping(value = "/company/{id}")
    public ResponseEntity<List<AppointmentResponseDTO>> getAppointmentsByCompanyId(@PathVariable Integer id) {
        List<Appointment> appointments = appointmentService.findByCompanyId(id);
        List<AppointmentResponseDTO> aResponseDTOs = new ArrayList<>();
        for (Appointment c : appointments) {
            if(c.getDateTime().isBefore(LocalDateTime.now())){
                continue;
            }
            else if(c.getReservation() != null){
                continue;
            }
            aResponseDTOs.add(new AppointmentResponseDTO(c));
        }
        return new ResponseEntity<>(aResponseDTOs, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('REGISTERED_USER')")
    @GetMapping(value = "/extraordinaryAppointments")
    public ResponseEntity<List<ExtraordinaryAppointmentDTO>> getExtraordinaryAppointments(@RequestParam("date") String date, @RequestParam("companyId") int companyId){
        var yearMonthDay = date.split("-");
        var list = appointmentService.getExtraordinaryAppointments(LocalDate.of(Integer.parseInt(yearMonthDay[0]), Integer.parseInt(yearMonthDay[1]), Integer.parseInt(yearMonthDay[2])), companyId);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('REGISTERED_USER')")
    @PostMapping(value = "/selectExtraordinaryAppointment")
    public ResponseEntity<AppointmentResponseDTO> selectExtraordinaryAppointment(@RequestBody ExtraordinaryAppointmentCreateDTO extraordinaryAppointmentCreateDTO){
        ExtraordinaryAppointmentDTO eoAppointment = extraordinaryAppointmentCreateDTO.getExtraordinaryAppointmentDTO();
        ReservationCreateDTO reservationDTO = extraordinaryAppointmentCreateDTO.getReservationCreateDTO();
        int companyId = extraordinaryAppointmentCreateDTO.getCompanyId();
        Appointment appointment = appointmentService.CreateExtraordinaryAppointment(eoAppointment, companyId);
        if(appointment == null){
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }
        reservationDTO.setAppointmentId(appointment.getId());
        Reservation reservation = reservationService.makeReservation(reservationDTO);
        int numberOfEquipments = reservationDTO.getEquipmentIds().size();
        for(int i=0; i<numberOfEquipments; i++){
            reservationService.makeReservationItem(reservation.getId(), reservationDTO.getEquipmentIds().get(i), reservationDTO.getAmounts().get(i));
        }
        appointment.setReservation(reservation);
        appointment = appointmentService.save(appointment);
        AppointmentResponseDTO responseDTO = new AppointmentResponseDTO(
                appointment.getId(),
                appointment.getAdmin().getFirstName() + " " + appointment.getAdmin().getLastName(),
                appointment.getDateTime(),
                appointment.getDuration()
        );
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }
}
