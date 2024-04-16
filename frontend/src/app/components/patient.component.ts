import { AfterViewInit, Component, OnDestroy, OnInit, ViewChild, inject } from '@angular/core';
import { PatientRegistrationService } from '../service/patient.register.service';
import { ActivatedRoute, Router } from '@angular/router';
import { Observable, Subscription, lastValueFrom } from 'rxjs';
import { TokenStorageService } from '../service/token.storage.service';
import { MedicationService } from '../service/medication.service';
import { MedicationRecord, Medication, eventDetails, dailyDetails, MedicationDetails, PatientDetails } from '../models';
import { MedicationIntakeStore } from '../service/medicationIntake.store';
import { ReminderService } from '../service/reminder.service';
import { EventsNotificationStore } from '../service/eventsNotification.store';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-patient',
  templateUrl: './patient.component.html',
  styleUrl: './patient.component.css'
})
export class PatientComponent implements OnInit, OnDestroy {

  private patientSvc = inject(PatientRegistrationService)
  private medSvc = inject(MedicationService)
  private reminderSvc = inject(ReminderService)
  private activatedRoute = inject(ActivatedRoute)
  private tokenStorage = inject(TokenStorageService)
  private medStorage = inject(MedicationIntakeStore)
  private router = inject(Router)
  private notifcationStorage = inject(EventsNotificationStore)

  private snackbar = inject(MatSnackBar)


  patientDetails$!: Observable<any>
  patientDetails! :PatientDetails
  medicineDetails!: MedicationRecord
  canDelete: boolean = false

  guardianId: string = this.tokenStorage.getGuardianIdFromStorage()
  patientId: string = this.activatedRoute.snapshot.params['patientId']
  patientName!: string

  eventList: eventDetails[] = []
  dailyList: dailyDetails[] = []
  getEventsSub!: Subscription
  getDailySub!: Subscription
  displayCategory: any
  frequencyListFilter: string[] = []

  getMedRecordSub!: Subscription
  sub!: Subscription

  medFrequencies: string[] = []
  hasWeekly!: boolean
  frequencyStatus!: string

  isEdit:boolean = false



  // @ViewChild(EventsComponent)
  // events!: EventsComponent
  // reminderFormSub!: Subscription




  ngOnInit(): void {
    console.info("patient component on init")

    this.patientDetails$ = this.patientSvc.getPatientDetails(this.guardianId, this.patientId)
    this.sub = this.patientDetails$.subscribe(
      value => {
        // console.info("Patient details from backend", value)
        this.patientDetails = value
        this.tokenStorage.savePatientNameToStorage(value.patientFirstName)
        this.patientName = value.patientFirstName
      }
    )
    this.tokenStorage.savePatientIdToStorage(this.patientId)

    


    this.getMedRecordSub = this.medSvc.getMedicationRecord(this.patientId).subscribe(
      (value: MedicationRecord) => {
        if (value != null) {
          this.canDelete = true
          console.info("Get medication reord ok", value)
          this.medicineDetails = value

        }

      })


      this.medSvc.getMedicatioinFrequencies(this.patientId)
      .then(value => {
        if(value != "{}"){
        console.info("Frequencies value from backend: ", value)
        this.medFrequencies = value['frequency']
        this.hasWeekly = value['weekly']
        this.frequencyStatus = value['frequencyStatus']
        if(this.medFrequencies.length>0){
        this.displayCategory = this.frequencyCategory.filter(
          freq => this.medFrequencies.includes(freq.value)
        )}
        }
        // console.info("Frequencies: ", this.medFrequencies)
        //  if no frequecies (no med) = list length will return undefined
      })
      .catch(
        err => { console.info(`FAILED TO GET FREQUENCIES: ${err}`) })

        

        this.patientSvc.isEditingEvent.subscribe(
          editing=>{ this.isEdit = editing}
        )

    this.getEventsSub = this.notifcationStorage.getEventbyID(this.patientId).subscribe(
      value => {
        this.eventList = value
      }
    )

    this.getDailySub = this.notifcationStorage.getDailybyID(this.patientId).subscribe(
      value => { this.dailyList = value }
    )

  }



  ngOnDestroy(): void {

    this.medStorage.clearMedicationStore()
    this.getMedRecordSub.unsubscribe()
    this.sub.unsubscribe()
    this.getEventsSub.unsubscribe()
    this.getDailySub.unsubscribe()
  }

  hasEditingAuthority() {
    const role = this.tokenStorage.getAuthority()
    if (role == "[ROLE_USERADMIN]") { return false }
    else { return true }
  }

  canDeleteAuthority() {
    const role = this.tokenStorage.getAuthority()
    if (role == "[ROLE_USERADMIN]" && this.canDelete) { return false }
    else { return true }
  }

  deleteRecord() {
    if (confirm("Confirm to delete the entire medical record?\nDeleted record cannot be retrieved")) {
      lastValueFrom(this.medSvc.deleteMedicationRecord(this.patientId))
        .then(value => {
          this.router.navigate(['/patients', this.patientId])
          console.info("message: ", value)
        })
        .catch(err => (alert(`DELETE FAILED: ${err.error.message}`)))
    }
  }



  frequencyCategory = [
    { value: 'all', displayValue: 'Show all' },
    { value: 'beforeBreakfast', displayValue: 'Before Breakfast' },
    { value: 'afterBreakfast', displayValue: 'After Breakfast' },
    { value: 'beforeLunch', displayValue: 'Before Lunch' },
    { value: 'afterLunch', displayValue: 'After Lunch' },
    { value: 'beforeDinner', displayValue: 'Before Dinner' },
    { value: 'afterDinner', displayValue: 'After Dinner' }
  ];



  deletePatient() {

    const role = this.tokenStorage.getAuthority()
    const user: string = "[ROLE_USER]"
    const admin: string = "[ROLE_USERADMIN]"

    console.info("This is ok", role == admin)
    console.info("guardian id", this.guardianId)
    console.info("patient id", this.patientId)


    if (role == user) {
      if (confirm("Are you sure to delete patient from your dashboard?\n All reminders will be deleted")) {
        this.patientSvc.deleteRelation(this.guardianId, this.patientId)
          .then(
            message => { this.snackbar.open(message.message, undefined, { duration: 3000 }) 
            this.router.navigate(['/patients'])

          }
            
          ).catch(err => {
            alert(`FAILED TO DELETE PATIENT: ${err.error.message}`)
            this.router.navigate(['/patients'])
          })
      }
    }
      if (role == admin) {
    console.info("This is ok")

        if (confirm("Are you sure to delete patient record?\n This will also delete patient record for other users with access to it")) {
          this.patientSvc.deleteRelationAdmin(this.guardianId, this.patientId)
            .then(
              message => { this.snackbar.open(message.message, undefined, { duration: 3000 }) 
              this.router.navigate(['/patients'])
            }
            ).catch(err => {
              alert(`FAILED TO DELETE PATIENT: ${err.error.message}`)
              this.router.navigate(['/patients'])
            })
        }
      }
    
  }

  
  editPatient(){
    this.isEdit = true
  }












}
