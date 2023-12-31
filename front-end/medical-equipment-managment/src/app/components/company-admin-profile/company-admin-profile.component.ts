import { Component, OnDestroy, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Company } from 'src/app/model/company';
import { CompanyAdmin } from 'src/app/model/company-admin.model';
import { Equipment } from 'src/app/model/equipment';
import { AdminService } from 'src/app/services/admin.service';
import { CompanyService } from 'src/app/services/company.service';
import { EquipmentService } from 'src/app/services/equipment.service';
import { CompanyAdminEditComponent } from '../company-admin-edit/company-admin-edit.component';
import { OtherCompanyAdminsComponent } from '../other-company-admins/other-company-admins.component';
import { CompanyEditComponent } from '../company-edit/company-edit.component';
import { CompanyAdminPasswordChangeComponent } from '../company-admin-password-change/company-admin-password-change.component';

import { Subscription } from 'rxjs';
import { EquipmentEditComponent } from '../equipment-edit/equipment-edit.component';
import { EquipmentCreateComponent } from '../equipment-create/equipment-create.component';
import { EquipmentCreate } from 'src/app/model/equipment-create.model';
import { Router } from '@angular/router';

import { AuthService } from 'src/app/services/auth.service';
import { CurrentUser } from 'src/app/model/current-user';
import { CompanyMapComponent } from '../company-map/company-map.component';

@Component({
  selector: 'app-company-admin-profile',
  templateUrl: './company-admin-profile.component.html',
  styleUrls: ['./company-admin-profile.component.css'],
})
export class CompanyAdminProfileComponent implements OnInit, OnDestroy {
  items: Equipment[] = [];

  backupItems: Equipment[] = [];
  nameFilter: string = '';
  typeFilter: string = 'All';
  private subscriptions: Subscription[] = [];

  user: CurrentUser | undefined;

  company: Company = {
    id: 0,
    averageGrade: 0,
    city: '',
    country: '',
    description: '',
    name: '',
    startTime: { hours: 10, minutes: 10 },
    endTime: { hours: 10, minutes: 10 },
    lat: 0,
    lon: 0,
    street: '',
    houseNumber: 0,
  };
  admin: CompanyAdmin = {
    id: 0,
    firstName: '',
    lastName: '',
    password: '',
    email: '',
    city: '',
    country: '',
    phoneNumber: '',
    companyId: 0,
  };
  subscription: Subscription;

  newEquipment: EquipmentCreate = {
    amount: 0,
    companyId: 0,
    description: '',
    name: '',
    type: 'DiagnosticEquipment',
  };
  constructor(
    private router: Router,
    private companyService: CompanyService,
    private equipmentService: EquipmentService,
    private adminService: AdminService,
    private authService: AuthService,
    public dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.subscription = this.authService.currentUser.subscribe((user) => {
      this.user = user;
      console.log(this.user.id);
    });
    setTimeout(() => {
      this.loadAdmin();
    }, 100);
  }

  goToCalendar() {
    this.router.navigate(['/companyCalendar']);
  }

  editPersonalInfo() {
    const dialogRef = this.dialog.open(CompanyAdminEditComponent, {
      data: this.admin,
      width: '650px',
      height: '450px',
      panelClass: 'custom-dialog',
    });
    dialogRef.afterClosed().subscribe((item) => {
      this.adminService.getAdmin(this.user!.id).subscribe({
        next: (result: CompanyAdmin) => {
          this.admin = result;
        },
      });
    });
  }

  editPassword() {
    const dialogRef = this.dialog.open(CompanyAdminPasswordChangeComponent, {
      data: this.admin,
      width: '400px',
      height: '450px',
      panelClass: 'custom-dialog',
    });
    dialogRef.afterClosed().subscribe((item) => {
      this.adminService.getAdmin(this.user!.id).subscribe({
        next: (result: CompanyAdmin) => {
          this.admin = result;
        },
      });
    });
  }

  editCompanyInfo() {
    const dialogRef = this.dialog.open(CompanyEditComponent, {
      data: this.company,
      width: '680px',
      height: '250px',
      panelClass: 'custom-dialog',
    });
    dialogRef.afterClosed().subscribe((item) => {
      this.loadCompany();
    });
  }

  showMap() {
    const dialogRef = this.dialog.open(CompanyMapComponent, {
      data: this.company,
      width: '700px',
      height: '450px',
      panelClass: 'custom-dialog',
    });
    dialogRef.afterClosed().subscribe((item) => {
      this.loadCompany();
    });
  }

  otherAdmins() {
    const dialogRef = this.dialog.open(OtherCompanyAdminsComponent, {
      data: {
        adminId: this.admin.id,
        companyId: this.company.id,
      },
      width: '650px',
      height: '450px',
      panelClass: 'custom-dialog',
    });
  }

  loadAdmin() {
    this.adminService.getAdmin(this.user!.id).subscribe({
      next: (result: CompanyAdmin) => {
        this.admin = result;
        this.loadCompany();
      },
    });
  }

  loadCompany() {
    this.adminService.getCompanyForAdmin(this.admin.id).subscribe({
      next: (result: Company) => {
        this.company = result;
        //this.loadEquipment();
        this.loadItems();
      },
    });
  }

  loadEquipment() {
    this.equipmentService.getCompanyEquipment(this.company.id).subscribe({
      next: (result: Equipment[]) => {
        this.items = result;
      },
    });
  }

  loadItems() {
    const subscription = this.equipmentService
      .getCompanyEquipment(this.company.id)
      .subscribe(
        (data) => {
          this.items = data;
          this.backupItems = data;
        },
        (error) => {
          console.error('Error loading equipment:', error);
        }
      );
  }

  applyFilters() {
    this.items = this.backupItems;
    const filteredItems = this.items.filter((item) => {
      const nameMatch = item.name
        .toLowerCase()
        .includes(this.nameFilter.toLowerCase());
      const typeMatch =
        this.typeFilter === 'All' || item.type === this.typeFilter;
      return nameMatch && typeMatch;
    });
    // Update the items to display the filtered results
    this.items = filteredItems;
  }

  editEquipment(e: Equipment) {
    const dialogRef = this.dialog.open(EquipmentEditComponent, {
      data: e,
      width: '650px',
      height: '450px',
      panelClass: 'custom-dialog',
    });
    dialogRef.afterClosed().subscribe((item) => {
      this.loadCompany();
    });
  }

  addEquipment() {
    this.newEquipment.companyId = this.company.id;
    const dialogRef = this.dialog.open(EquipmentCreateComponent, {
      data: this.newEquipment,
      width: '650px',
      height: '450px',
      panelClass: 'custom-dialog',
    });
    dialogRef.afterClosed().subscribe((item) => {
      this.loadCompany();
      this.newEquipment.amount = 0;
      this.newEquipment.companyId = 0;
      this.newEquipment.description = '';
      this.newEquipment.name = '';
      this.newEquipment.type = 'DiagnosticEquipment';
    });
  }

  deleteEquipment(e: Equipment) {
    if (e.reservedAmount > 0) {
      alert('You can not delete this item, it has been reserved');
      return;
    }
    this.equipmentService.deleteEquipment(e.id).subscribe({
      next: (result: Equipment) => {
        this.loadItems();
      },
    });
  }

  ngOnDestroy() {
    this.subscription.unsubscribe();
    // Unsubscribe from all subscriptions to avoid memory leaks when the component is destroyed
    this.subscriptions.forEach((subscription) => {
      subscription.unsubscribe();
    });
  }

  openCalendar() {}
}
