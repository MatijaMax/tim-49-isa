import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { Company } from 'src/app/model/company';
import { CompanyService } from 'src/app/services/company.service';

@Component({
  selector: 'app-companies',
  templateUrl: './companies.component.html',
  styleUrls: ['./companies.component.css'],
})
export class CompaniesComponent implements OnInit {
  companies: Company[];
  nameSearch: string;
  countrySearch: string;
  citySearch: string;

  constructor(private companyService: CompanyService, private router: Router) {}

  ngOnInit(): void {
    this.LoadCompanies();
  }

  ViewProfile(id: number) {
    this.router.navigate(['/companies/' + id]);
    console.log(id);
  }

  LoadCompanies() {
    this.companyService.getCompanies().subscribe({
      next: (result: Company[]) => {
        this.companies = result;
      },
    });
  }

  SearchCompanies() {
    this.companyService.searchCompanies(this.nameSearch, this.countrySearch, this.citySearch).subscribe({
      next: (result: Company[]) => {
        this.companies = result;
      }
    })
  }

  ResetSearch() {
    this.LoadCompanies();
    this.nameSearch = '';
    this.countrySearch = '';
    this.citySearch = '';
  }
}
