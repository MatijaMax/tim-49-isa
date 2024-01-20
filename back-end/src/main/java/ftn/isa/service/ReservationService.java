package ftn.isa.service;

import ftn.isa.domain.*;
import ftn.isa.dto.ExtraordinaryAppointmentDTO;
import ftn.isa.dto.ReservationCancelDTO;
import ftn.isa.dto.ReservationCreateDTO;
import ftn.isa.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ReservationService {
    @Autowired
    RegisteredUserRepository registeredUserRepository;
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    AppointmentRepository appointmentRepository;
    @Autowired
    EquipmentRepository equipmentRepository;
    @Autowired
    ReservationItemRepository reservationItemRepository;

    public Reservation makeReservation(ReservationCreateDTO reservationDTO){
        Reservation reservation = new Reservation();
        reservation.setStatus(ReservationStatus.NotFinalized);
        reservation.setRegisteredUser(registeredUserRepository.getReferenceById(reservationDTO.getUserId()));
        reservation.setAppointment(appointmentRepository.getReferenceById(reservationDTO.getAppointmentId()));
        return reservationRepository.save(reservation);
    }
    public ReservationItem makeReservationItem(int reservationId, int equipmentId, int amount){
        ReservationItem reservationItem = new ReservationItem();
        reservationItem.setAmount(amount);
        reservationItem.setReservation(reservationRepository.getReferenceById(reservationId));
        Equipment equipment = equipmentRepository.getReferenceById(equipmentId);
        equipment.setFreeAmount(equipment.getFreeAmount() - amount);
        equipment.setReservedAmount(equipment.getReservedAmount() + amount);
        equipmentRepository.save(equipment);
        reservationItem.setEquipment(equipment);
        return reservationItemRepository.save(reservationItem);
    }
    public Reservation cancelReservation(ReservationCancelDTO cancelDTO){
        //nemam validaciju da li je korisnik zaista rezervisao taj termin
        Reservation reservation = reservationRepository.findByAppointmentIdAndRegisteredUserId(cancelDTO.getAppointmentId(), cancelDTO.getUserId()).get(0);
        reservation.setStatus(ReservationStatus.Cancelled);
        reservationRepository.save(reservation);
        Appointment appointment = appointmentRepository.getReferenceById(cancelDTO.getAppointmentId());
        RegisteredUser user = registeredUserRepository.getReferenceById(cancelDTO.getUserId());
        if(calculateHoursDifference(LocalDateTime.now(), LocalDateTime.of(appointment.getDateTime().getYear(), appointment.getDateTime().getMonth(), appointment.getDateTime().getDayOfMonth(), appointment.getDateTime().getHour(), 0)) < 24){
            user.setPenalPoints(user.getPenalPoints()+2);
        }
        else{
            user.setPenalPoints(user.getPenalPoints()+1);
        }
        registeredUserRepository.save(user);
        return reservation;
    }
    private long calculateHoursDifference(LocalDateTime currentDateTime, LocalDateTime appointmentDateTime) {
        Duration duration = Duration.between(appointmentDateTime, currentDateTime);
        long hoursDifference = duration.toHours();
        return hoursDifference;
    }
    public List<Reservation> getReservationsByAppointmentId(int id){
        return reservationRepository.findByAppointmentId(id);
    }
    public boolean isAvailable(int appointmentId, int userId){
        List<Reservation> reservations = reservationRepository.findByAppointmentId(appointmentId);
        if(reservations.isEmpty())
            return true;
        for(Reservation r: reservations){
            if(r.getStatus() != ReservationStatus.Cancelled)
                return false;
        }
        return reservationRepository.findByAppointmentIdAndRegisteredUserId(appointmentId, userId).isEmpty();
    }
}
