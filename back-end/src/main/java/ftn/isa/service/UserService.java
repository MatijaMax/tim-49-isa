package ftn.isa.service;


import ftn.isa.domain.Company;
import ftn.isa.domain.User;
import ftn.isa.dto.UserUpdateDTO;
import ftn.isa.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    @Autowired
    UserRepository userRepository;
    public List<User> findAll() {
        return userRepository.findAll();
    }
    public User save(User user) {
        return userRepository.save(user);
    }

    public User findOne(int id){ return userRepository.findById(id).orElseGet(null); }
}
