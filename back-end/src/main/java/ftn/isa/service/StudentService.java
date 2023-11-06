package ftn.isa.service;

import java.util.List;

import ftn.isa.domain.Student;
import ftn.isa.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;



@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    public Student findOne(Integer id) {
        return studentRepository.findById(id).orElseGet(null);
    }

    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    public Page<Student> findAll(Pageable page) {
        return studentRepository.findAll(page);
    }

    public Student save(Student student) {
        return studentRepository.save(student);
    }

    public void remove(Integer id) {
        studentRepository.deleteById(id);
    }

    public Student findByIndex(String index) {
        return studentRepository.findOneByIndex(index);
    }

    public List<Student> findByLastName(String lastName) {
        return studentRepository.findAllByLastName(lastName);
    }

    public List<Student> findByFirstNameAndLastName(String firstName, String lastName) {
        return studentRepository.findByFirstNameAndLastNameAllIgnoringCase(firstName, lastName);
    }

    public List<Student> pronadjiPoPrezimenu(String prezime) {
        return studentRepository.pronadjiStudentePoPrezimenu(prezime);
    }

}
