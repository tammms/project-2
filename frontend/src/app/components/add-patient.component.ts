import { AfterViewInit, Component, Input, OnInit, Output, inject, input } from '@angular/core';
import { FormBuilder, FormGroup, ValidatorFn, AbstractControl, ValidationErrors, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { EMPTY_PATIENT_DETAILS, Guardian, Patient, PatientDetails } from '../models';
import { PatientRegistrationService } from '../service/patient.register.service';
import { Observable, Subject } from 'rxjs';
import { v4 as uuid } from 'uuid'
import { TokenStorageService } from '../service/token.storage.service';



@Component({
  selector: 'app-add-patient',
  templateUrl: './add-patient.component.html',
  styleUrl: './add-patient.component.css'
})
export class AddPatientComponent implements OnInit {

  private fb = inject(FormBuilder)
  newPatientForm!: FormGroup
  existingPatientForm!: FormGroup


  private patientSvc = inject(PatientRegistrationService)
  private tokenStorage = inject(TokenStorageService)
  private router = inject(Router)

  guardianId!: string

  // guardianDetails$!: Observable<Guardian>

  // @Input({ required: true })
  // guardian!: Guardian

  panelOpenState = true
  step = 0;
  setStep(index: number) { this.step = index; }
  nextStep() { this.step++; }
  prevStep() { this.step--; }

  @Input()
  isDisabled = false

  @Input()
  isEdit = false

  @Input()
  patientDetails: PatientDetails = EMPTY_PATIENT_DETAILS


  ngOnInit(): void {
    if(this.isEdit){
      this.newPatientForm = this.UpdatePatientForm(this.patientDetails)
    }else{
      this.newPatientForm = this.createNewPatientForm(EMPTY_PATIENT_DETAILS)
    }
    this.existingPatientForm = this.createExitingPatientForm()
    this.guardianId = this.tokenStorage.getGuardianIdFromStorage()

    

  }

  birthDateValidation: ValidatorFn = (ctrl: AbstractControl): ValidationErrors | null => {
    if (new Date(ctrl.value) > new Date()) {
      return { birthDateValidation: true } as ValidationErrors
    }
    return (null)
  }

  createNewPatientForm(patient: PatientDetails): FormGroup {

    return this.fb.group({
      firstName: this.fb.control<string>(patient.patientFirstName, [Validators.required, Validators.minLength(3)]),
      lastName: this.fb.control<string>(patient.patientLastName, [Validators.required, Validators.minLength(3)]),
      gender: this.fb.control<string>(patient.patientGender, [Validators.required]),
      birthDate: this.fb.control<string>('', [Validators.required, this.birthDateValidation]),
      phoneNo: this.fb.control<string>(patient.patientPhoneNo, [Validators.pattern("(8|9)[0-9]{7}")]),
      relationship: this.fb.control<string>(patient.patientRelation, [Validators.required])


    })
  }

 UpdatePatientForm(patient: PatientDetails): FormGroup {

    return this.fb.group({
      firstName: this.fb.control({value: patient.patientFirstName, disabled:true}, [Validators.required, Validators.minLength(3)]),
      lastName: this.fb.control({value: patient.patientLastName, disabled:true}, [Validators.required, Validators.minLength(3)]),
      gender: this.fb.control({value: "", disabled:false}, [Validators.required]),
      birthDate: this.fb.control({value: "", disabled:true}),
      phoneNo: this.fb.control({value: patient.patientPhoneNo, disabled:false}, [Validators.pattern("(8|9)[0-9]{7}")]),
      relationship: this.fb.control({value: "", disabled:false}, [Validators.required])


    })
  }

  createExitingPatientForm(): FormGroup {
    return this.fb.group({
      patientId: this.fb.control<string>('', [Validators.required, Validators.minLength(8),
      Validators.maxLength(8)]),
      relationship: this.fb.control<string>('', [Validators.required])

    })
  }

  @Output()
  patientAddedEvent = new Subject<void>()



  addNewPatient() {
    const newPatientInput = this.newPatientForm.value
    const birthDate: string = new Date(newPatientInput["birthDate"]).getTime().toString()
    const relationship: string = newPatientInput["relationship"]
    const phone: string = newPatientInput["phoneNo"]
    const id: string = uuid().substring(0, 8)


    console.log("New Patient Input: ", newPatientInput)
    console.log("Patient birthDate: ", birthDate)


    const patient: Patient = {
      patientId: id,
      firstName: newPatientInput["firstName"],
      lastName: newPatientInput["lastName"],
      gender: newPatientInput["gender"],
      birthDate: birthDate,
      age: 0,
      phoneNo: phone,
      guardians: [],
      medications: [],
      medicalNotes: []

    }

  

    this.patientSvc.patientDetailsExists(newPatientInput, birthDate)
      .then(resp => {
        console.info("Patient Details Exist: ", resp)
        if (resp) {
          const confirmation = confirm("Patient details already exist. Do you want to continue creating Patient acount?")
          if (confirmation) {

            this.patientSvc.addNewPatient(patient, relationship, this.guardianId)
              .then(resp => {
                alert(resp.message)
                console.info("response: ", resp)
                this.patientAddedEvent.next()
                this.newPatientForm = this.createNewPatientForm(EMPTY_PATIENT_DETAILS)
                this.router.navigate(['/patients'])
              }).catch(err => {
                alert(`FAILED TO CREATE PATIENT: ${err.error.message}`)
              })

          } else {
            this.existingPatientForm.reset()
          }
        } else {
          this.patientSvc.addNewPatient(patient, relationship, this.guardianId)
            .then(resp => {
              alert(resp.message)
              console.info("response: ", resp)
              this.patientAddedEvent.next()
              this.newPatientForm = this.createNewPatientForm(EMPTY_PATIENT_DETAILS)
              this.router.navigate(['/patients'])
            }).catch(err => {
              alert(`FAILED TO CREATE PATIENT: ${err.error.message}`)
            })
        }
      })
     


  }


  addExistingPatient() {

    const existingPatientInput = this.existingPatientForm.value

    console.log("Existing Patient Input: ", existingPatientInput['patientId'])

    // Existing Patient Input:  {patientId: 'zxcv0998', relationship: 'parent'}

    this.patientSvc.patientRelationsExists(this.guardianId,existingPatientInput['patientId'])
      .then(resp => {
        console.info("Patient Relations Details Exist: ", resp)
        if (resp) {

          alert("FAILED TO ADD PATIENT: Patient already exists in User Dashboard")

        } else{
          this.patientSvc.addExistingPatient(existingPatientInput['patientId'],
          existingPatientInput['relationship'],
          this.guardianId)
          .then(
            resp => {
              alert(resp.message)
              console.info("response: ", resp)
              this.existingPatientForm = this.createExitingPatientForm()
              this.patientAddedEvent.next()
              this.router.navigate(['/patients'])
            })
          .catch(
            err => {
              alert(`FAILED TO ADD PATIENT: ${err.error.message}`)
            }
          )
        }
      })
      .catch(err=>{
        alert("FAILED TO ADD PATIENT: Patient ID does not Exist \nPlease create a new patient profile")
        
      })

  }

  editPatient(){

    const newPatientInput = this.newPatientForm.value
    const birthDate: string = new Date(newPatientInput["birthDate"]).getTime().toString()
    const relationship: string = newPatientInput["relationship"]
    const id: string = uuid().substring(0, 8)
    const phone: string = newPatientInput["phoneNo"]



    console.log("New Patient Input: ", newPatientInput)

    const patient: Patient = {
      patientId: this.patientDetails.patientId,
      firstName: this.patientDetails.patientFirstName,
      lastName: this.patientDetails.patientLastName,
      gender: newPatientInput["gender"],
      birthDate: this.patientDetails.patientBirthDate,
      age: 0,
      phoneNo: phone,
      guardians: [],
      medications: [],
      medicalNotes: []

    }

    this.patientSvc.updatePatientDetails(patient, relationship, this.guardianId)
    .then(resp => {
      alert(resp.message)
      console.info("response: ", resp)
      this.newPatientForm = this.createNewPatientForm(EMPTY_PATIENT_DETAILS)
      this.router.navigate(['/patients'])
    }).catch(err => {
      alert(`FAILED TO UPDATE PATIENT: ${err.error.message}`)
    })


  }

  discardChanges(){
    this.patientSvc.isEditingEvent.next(false)
  }
  


}
