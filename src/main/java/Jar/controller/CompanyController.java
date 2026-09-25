package Jar.controller;
import Jar.entity.Company;
import Jar.repository.CompanyRepository;
import Jar.service.AuthService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;
@RestController
@RequestMapping("/api/companies")
public class CompanyController {
 private final CompanyRepository companies;private final AuthService auth;
 public CompanyController(CompanyRepository companies,AuthService auth){this.companies=companies;this.auth=auth;}
 @GetMapping public List<Company> getCompanies(){return companies.findAll();}
 @PostMapping public Company createCompany(@RequestBody Company c){auth.requireRecruiter();if(c.getCompanyName()==null || c.getCompanyName().isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Company name is required");c.setCompanyId(null);return companies.save(c);}
}
