import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { GuardianComponent } from './components/guardian.component';
import { SignInComponent } from './components/sign-in.component';
import { PatientListComponent } from './components/patient-list.component';
import { PatientComponent } from './components/patient.component';
import { MedicalDetailsComponent } from './components/medical-details.component';
import { ReminderFormComponent } from './components/reminder-form.component';
import { EventsComponent } from './components/events.component';
import { DailyFormComponent } from './components/daily-form.component';
import { FormsComponent } from './components/forms.component';
import { AddPatientComponent } from './components/add-patient.component';
import { SearchLocationComponent } from './components/search-location.component';

const routes: Routes = [

  {path:"", component:SignInComponent, title:"Care Aid"},
  {path:"guardian", component:GuardianComponent, title:"Guardian Details"},
  {path:"patients", component:PatientListComponent, title:"Patient Dashboard"},
  {path:"patientDetails", component:AddPatientComponent},
  {path:"patients/:patientId", component:PatientComponent, title:"Patient Details"},
  {path:"medicalInput", component:MedicalDetailsComponent, title:"Medication Details"},
  {path:"schedule", component:ReminderFormComponent},
  {path:"events", component:EventsComponent},
  {path:"daily", component:DailyFormComponent},
  {path:"forms", component:FormsComponent, title:"Create Reminder"},
  {path:"location", component:SearchLocationComponent, title:"Search Amenities"},
  {path:"**", redirectTo:"/", pathMatch:"full"}
  
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule { }
