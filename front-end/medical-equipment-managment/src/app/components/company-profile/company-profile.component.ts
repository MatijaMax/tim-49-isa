import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ActivatedRoute } from '@angular/router';
import { Company } from 'src/app/model/company';
import { Equipment } from 'src/app/model/equipment';
import { CompanyService } from 'src/app/services/company.service';
import { EquipmentService } from 'src/app/services/equipment.service';
import { EquipmentAppointmentComponent } from '../equipment-appointment/equipment-appointment.component';

@Component({
  selector: 'app-company-profile',
  templateUrl: './company-profile.component.html',
  styleUrls: ['./company-profile.component.css'],
})
export class CompanyProfileComponent implements OnInit {
  companyId: number;
  name: string;
  description: string;
  country: string;
  city: string;
  averageGrade: number;
  items: Equipment[] = [];
  constructor(
    private companyService: CompanyService,
    private route: ActivatedRoute,
    private equipmentService: EquipmentService,
    public dialog: MatDialog,
  ) {}

  ngOnInit(): void {
    this.companyService.getCompany(this.route.snapshot.params['id']).subscribe({
      next: (result: Company) => {
        this.name = result.name;
        this.description = result.description;
        this.country = result.country;
        this.city = result.city;
        this.averageGrade = result.averageGrade;
        this.companyId = result.id;
        this.loadItems();
      },
    });
  }

  loadItems() {
    this.equipmentService.getCompanyEquipment(this.companyId).subscribe({
      next: (result: Equipment[]) => {
        this.items = result;
      },
    });
  }

  buyEquipment(equipment: Equipment) {
    const dialog = this.dialog.open(EquipmentAppointmentComponent, {
      data: {
        equipment: equipment
      },
      width: '800px',
      height: '600px'
    })
    //TODO: Odradi poziv ovde da se otvori prozorce za zakazivanje
  }
}
