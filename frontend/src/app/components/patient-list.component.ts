import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { Observable, Subscription, lastValueFrom, of } from 'rxjs';
import { __values } from 'tslib';
import { PatientRegistrationService } from '../service/patient.register.service';
import { Router } from '@angular/router';
import { TokenStorageService } from '../service/token.storage.service';
import { AuthService } from '../service/authenticate.service';
import { ReminderService } from '../service/reminder.service';
import { EventsNotificationStore } from '../service/eventsNotification.store';
import { dailyDetails, eventDetails } from '../models';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatExpansionPanel } from '@angular/material/expansion';

@Component({
  selector: 'app-patient-list',
  templateUrl: './patient-list.component.html',
  styleUrl: './patient-list.component.css'
})
export class PatientListComponent implements OnInit, OnDestroy {


  // guardian!: Guardian
  guardianId!: string
  eventList: eventDetails[] = []
  dailyList: dailyDetails[] = []



  private reminderSvc = inject(ReminderService)
  private patientSvc = inject(PatientRegistrationService)
  private authSvc = inject(AuthService)
  private tokenStorage = inject(TokenStorageService)
  private notificationStorage = inject(EventsNotificationStore)
  private router = inject(Router)

  getEventsSub!: Subscription
  getDailySub!: Subscription
  guardianDetails$!: Observable<any>
  tableHeaders = ['patientId', 'patientFirstName', 'patientLastName', 'age',
    'patientGender', 'patientRelation', 'role'];


  token!: string
  reminderSub!: Subscription


  ngOnInit(): void {

    this.tokenStorage.setOriginalToken()

    console.info("Patient list Oninit")
    this.guardianId = this.tokenStorage.getGuardianIdFromStorage()
    if (!!this.guardianId) {
      lastValueFrom(this.patientSvc.getPatientRelationDetails(this.guardianId))
        .then(
          value => {
            console.info("Relaionship Details Recieved: ", value)
            if (value.length > 0) {
              this.guardianDetails$ = of(value)
            } else {
              this.guardianDetails$ = of(null)
            }
            return value
          }
        ).catch(err => {
          alert("Error getting patient Details")
        })
    }

    this.getEventsSub = this.reminderSvc.getAllEvents(this.guardianId).subscribe(
      events => {
        console.info("Value from Event Reminders: ", events)
        if (events != null) {
          this.eventList.push(...events)
          this.notificationStorage.clearEventsFromStore()
          events.forEach(event => {
            this.notificationStorage.addEvent(event)
          })
        }
      }
    )

    this.getDailySub = this.reminderSvc.getAllDaily(this.guardianId).subscribe(
      dailyList => {
        console.info("Value from Daily Reminders: ", dailyList)
        if (dailyList != null) {
          this.dailyList.push(...dailyList)
          this.notificationStorage.clearDailyFromStore()
          dailyList.forEach(daily => {
            this.notificationStorage.addDaily(daily)
          })
        }
      }
    )


    this.reminderSvc.requestNotification(this.guardianId)

    this.reminderSvc.receiveMessage()

  }

  ngOnDestroy(): void {
    this.getEventsSub.unsubscribe()
    this.getDailySub.unsubscribe()
    // this.notificationStorage.clearNotificationStore()

  }



  loadPatients() {
    if (!!this.guardianId)
      this.guardianDetails$ = this.patientSvc.getPatientRelationDetails(this.guardianId)
    console.info("Load Patients function")

  }



  selectPatient(data: any) {
    console.info("Data from clicking row: ", data)
    this.authSvc.selectPatient(data.guardianId, data.patientId)
      .then(
        resp => {
          this.tokenStorage.cacheToken(resp.token)
          this.tokenStorage.cacheAuthority(resp.authority)
          this.router.navigate(['/patients', data.patientId])
        }
      ).catch(
        err => {
          alert(err)
        }
      )
    // this.router.navigate(['/patients', data.guardianId, data.patientId])
  }

  

editGuardian(){
  this.router.navigate(['/guardian'])

}






}
