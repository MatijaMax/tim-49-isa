package ftn.isa.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;

import ftn.isa.domain.*;
import ftn.isa.dto.CompanyCreateDTO;
import ftn.isa.service.EquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;
import ftn.isa.dto.CompanyResponseDTO;
import ftn.isa.dto.EquipmentDTO;
import ftn.isa.service.CompanyService;
import ftn.isa.dto.UserCreateDTO;
import ftn.isa.dto.UserResponseDTO;
import ftn.isa.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static ftn.isa.domain.EquipmentType.*;

@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*")
@RestController
@RequestMapping(value = "api/companies")
public class CompanyController {
	@Autowired
	private CompanyService companyService;
    @Autowired
    private EquipmentService equipmentService;
    @Autowired
    private UserService userService;
	@GetMapping(value = "/all")
    public ResponseEntity<List<CompanyResponseDTO>> getCompanies() {
        List<Company> companies = companyService.findAll();

        List<CompanyResponseDTO> companyResponseDTOs = new ArrayList<>();
        for (Company c : companies) {
        	companyResponseDTOs.add(new CompanyResponseDTO(c));
        }

        return new ResponseEntity<>(companyResponseDTOs, HttpStatus.OK);
    }

    @GetMapping(value = "/all/admins/{id}")
    public ResponseEntity<List<UserResponseDTO>> getCompanyAdmins(@PathVariable Integer id) {
        Company company = companyService.findOne(id);
        if(company==null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<UserResponseDTO> userResponseDTOs = company.getAdmins()
                .stream()
                .map(UserResponseDTO::new)
                .toList();

        return new ResponseEntity<>(userResponseDTOs, HttpStatus.OK);
    }

	@GetMapping(value = "/last/created/id")
    public ResponseEntity<Integer> getLastCreatedId() {
        Integer id = companyService.getLastCompanyId();
        return new ResponseEntity<>(id, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<CompanyResponseDTO> getCompanieById(@PathVariable Integer id) {
        Company company = companyService.getById(id);
        CompanyResponseDTO companyResponseDTO = new CompanyResponseDTO(company);

        return new ResponseEntity<>(companyResponseDTO, HttpStatus.OK);
    }

    //Trebam na frontu da probam da testiram ovu metodu i da promenim
    @PostMapping(consumes = "application/json")
    public ResponseEntity<CompanyResponseDTO> save(@RequestBody CompanyCreateDTO companyDTO){
        Company company = new Company();
        company.setCountry(companyDTO.getCountry());
        company.setCity(company.getCity());
        company.setName(companyDTO.getName());
        company.setDescription(companyDTO.getDescription());
        company.setAverageGrade(companyDTO.getAverageGrade());

        company = companyService.save(company);


        CompanyResponseDTO companyResponseDTO = new CompanyResponseDTO(company);
        return new ResponseEntity<>(companyResponseDTO, HttpStatus.OK);
    }


	@GetMapping(value = "/{companyId}/equipment")
	public ResponseEntity<List<EquipmentDTO>> getCompanyEquipment(@PathVariable Integer companyId) {

		Company company = companyService.findOneWithEquipment(companyId);

		Set<Equipment> equipment = company.getEquipment();
		List<EquipmentDTO> equipmentDTOs = new ArrayList<>();

		for (Equipment e : equipment) {
			equipmentDTOs.add(new EquipmentDTO(e));
		}
		return new ResponseEntity<>(equipmentDTOs, HttpStatus.OK);
	}

    @GetMapping(value = "/search")
    public ResponseEntity<List<CompanyResponseDTO>> searchNameCountryCity(@RequestParam(required = false) String name, @RequestParam(required = false) String country, @RequestParam(required = false) String city){
        if(name == null){ name = ""; }
        if(country == null){ country = ""; }
        if(city == null){ city = ""; }

        List<Company> companies = companyService.searchNameCountryCity(name, country, city);
        if(companies == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        List<CompanyResponseDTO> responseDTOS = new ArrayList<>();
        for(Company c: companies){
            responseDTOS.add(new CompanyResponseDTO(c));
        }
        return new ResponseEntity<>(responseDTOS, HttpStatus.OK);
    }

}
