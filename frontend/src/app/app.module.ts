import { NgModule, isDevMode } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { GuardianComponent } from './components/guardian.component';
import { SignInComponent } from './components/sign-in.component';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { MaterialModule } from './material/material.module';
import { HttpClientModule } from '@angular/common/http';
import { ReactiveFormsModule } from '@angular/forms';
import { PatientListComponent } from './components/patient-list.component';
import { AddPatientComponent } from './components/add-patient.component';
import { PatientRegistrationService } from './service/patient.register.service';
import { MatLuxonDateModule } from '@angular/material-luxon-adapter';
import { PatientComponent } from './components/patient.component';
import { MedicalDetailsComponent } from './components/medical-details.component';
import { MedicationIntakeStore } from './service/medicationIntake.store';
import { MedicationService } from './service/medication.service';
import { TokenStorageService } from './service/token.storage.service';
import { AuthService } from './service/authenticate.service';
import { authInterceptorProviders } from './service/auth.interceptor';
import { ReminderFormComponent } from './components/reminder-form.component';
import { ReminderService } from './service/reminder.service';
import { ServiceWorkerModule } from '@angular/service-worker';
import { AngularFireMessagingModule } from '@angular/fire/compat/messaging';
import { AngularFireDatabaseModule } from '@angular/fire/compat/database';
import { AngularFireAuthModule } from '@angular/fire/compat/auth';
import { AngularFireModule } from '@angular/fire/compat';
import { environment } from '../environments/environment';
import { EventsComponent } from './components/events.component';
import { DailyFormComponent } from './components/daily-form.component';
import { FormsComponent } from './components/forms.component';
import { EventsNotificationStore } from './service/eventsNotification.store';
import { ReminderSummaryComponent } from './components/reminder-summary.component';
import { SearchLocationComponent } from './components/search-location.component';
import { LocationService } from './service/location.service';



@NgModule({
  declarations: [
    AppComponent,
    GuardianComponent,
    SignInComponent,
    PatientListComponent,
    AddPatientComponent,
    PatientComponent,
    MedicalDetailsComponent,
    ReminderFormComponent,
    EventsComponent,
    DailyFormComponent,
    FormsComponent,
    ReminderSummaryComponent,
    SearchLocationComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    MatLuxonDateModule,
    MaterialModule,
    HttpClientModule,
    ReactiveFormsModule,
    ServiceWorkerModule.register('ngsw-worker.js', {
      enabled: !isDevMode(),
      // Register the ServiceWorker as soon as the application is stable
      // or after 30 seconds (whichever comes first).
      registrationStrategy: 'registerWhenStable:30000'
    }),
    ServiceWorkerModule.register('firebase-messaging-sw.js', {
      enabled: !isDevMode(),
      // Register the ServiceWorker as soon as the application is stable
      // or after 30 seconds (whichever comes first).
      registrationStrategy: 'registerWhenStable:30000'
    }),
    AngularFireMessagingModule,
    AngularFireDatabaseModule,
    AngularFireAuthModule,
    AngularFireModule.initializeApp(environment.firebase)
  ],
  providers: [
    provideAnimationsAsync(),
    PatientRegistrationService,
    MedicationIntakeStore,
    MedicationService,
    TokenStorageService,
    AuthService,
    authInterceptorProviders,
    ReminderService,
    EventsNotificationStore,
    LocationService
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
