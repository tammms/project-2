import { Component, OnDestroy, OnInit, inject, input } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { Observable, Subject, Subscription, lastValueFrom, map, startWith } from 'rxjs';
import { Conditions, EMPTY_HEALTHCONDITIONS, EMPTY_MEDICATION, Medication, MedicationRecord } from '../models';
import { MedicationService } from '../service/medication.service';
import { MedicationIntakeStore } from '../service/medicationIntake.store';
import { TokenStorageService } from '../service/token.storage.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-medical-details',
  templateUrl: './medical-details.component.html',
  styleUrl: './medical-details.component.css'
})
export class MedicalDetailsComponent implements OnInit, OnDestroy {

  private medSvc = inject(MedicationService)
  private medStore = inject(MedicationIntakeStore)
  private tokenStorage = inject(TokenStorageService)
  private router = inject(Router)

  medicalDetails$!: Observable<any>
  medRecord!: MedicationRecord
  conditionsChecked: { [key: string]: boolean } = {};
  frequenciesChecked: { [key: string]: boolean } = {};


  private fb = inject(FormBuilder)
  medicalDetailsForm !: FormGroup
  healthConditionsForm !: FormGroup
  medicationDosageForm!: FormGroup

  medNameControl!: FormControl
  filteredMeds$!: Observable<string[]>
  sub!: Subscription
  addedMeds$!: Observable<Medication[]>
  medications: string[] = []
  tableHeaders = ['name', 'medicationType', 'dosage', 'edit',
    'delete'];

  addNew: boolean = false;
  update: boolean = false;
  addNewMedicationSubject = new Subject<boolean>();
  updateMedicationSubject = new Subject<boolean>();

  addNewSub!: Subscription
  updateSub!: Subscription
  getmedSub!: Subscription
  getmedRecordSub!: Subscription
  medExistSub!: Subscription



  patientId: string = this.tokenStorage.getPatientIdFromStorage()
  guardianId: string = this.tokenStorage.getGuardianIdFromStorage()

  ngOnInit(): void {

    this.addNewSub = this.addNewMedicationSubject.subscribe(
      (value: boolean) => {
        this.addNew = value
      }
    )

    this.updateSub = this.updateMedicationSubject.subscribe(
      (value: boolean) => {
        this.update = value
      }
    )

    this.loadMedicationToStore()

    this.healthConditionsForm = this.createHealthConditionsForm(EMPTY_HEALTHCONDITIONS)
    this.medicationDosageForm = this.createMedicineDosageForm(EMPTY_MEDICATION)
    this.medNameControl = this.medicationDosageForm.get('name') as FormControl

    // this.loadMedicationToStore()


    this.getmedSub = this.medSvc.getMedicines()
      .subscribe((value: string[]) => { this.medications = value })

    this.filteredMeds$ = this.medNameControl.valueChanges.pipe(
      startWith(''),
      map(value => this.filterMedicine(value || ''))
    )

    this.sub = this.filteredMeds$.subscribe()
    this.addedMeds$ = this.medStore.getMedications

  }

  ngOnDestroy(): void {
    this.sub.unsubscribe()
    this.addNewSub.unsubscribe()
    this.updateSub.unsubscribe()
    this.getmedSub.unsubscribe()
    this.getmedRecordSub.unsubscribe()

    if (this.medExistSub) {
      this.medExistSub.unsubscribe()

    }
  }

  loadMedicationToStore() {

    this.medicalDetails$ = this.medSvc.getMedicationRecord(this.patientId)

    this.getmedRecordSub = this.medSvc.getMedicationRecord(this.patientId).subscribe(
      (value: MedicationRecord) => {
        if (value != null) {
          this.medRecord = value
          console.info("Get medication reord ok", value)
          console.info("Healthconditions list", this.medRecord.medicationList)

          this.medRecord.medicationList.forEach(
            (value) => {
              // console.info("Value: ", value)
              const med: Medication = value
              this.medStore.addMedication(med)
            })

          const healthCond: Conditions = {
            conditions: [],
            otherConditions: "",
            notes: this.medRecord.notes
          }


          console.info("Notes under medical conditions: ", this.medRecord.notes)

          let hasOtherConditions: boolean = false

          this.medRecord.healthConditionList.forEach(
            (condition) => {
              console.info("Condition: ", condition)
              const index: number = this.healthCondition.findIndex(value => value == condition)
              if (index < 0) {
                hasOtherConditions = true
                this.conditionsChecked["others"] = true
                healthCond.otherConditions = condition
                healthCond.conditions.push(condition)

              } else {
                this.conditionsChecked[condition] = true
                healthCond.conditions.push(condition)
              }

            })

          this.healthConditionsForm = this.createHealthConditionsForm(healthCond)
          this.healthConditionsForm.get("notes")?.setValue(this.medRecord.notes)


          if (hasOtherConditions) {
            this.healthConditionsForm.get("otherConditions")?.enable()
          }
          else { this.healthConditionsForm.get("otherConditions")?.disable() }


        }
      }
    )

  }


  ////////////////// MEDICATION DOSAGE INPUT //////////////////

  addNewMed() {
    this.addNew = true
    this.addNewMedicationSubject.next(this.addNew)
  }

  createMedicineDosageForm(med: Medication): FormGroup {
    //  https://github.com/chukmunnlee/vttp2023_batch4/blob/main/day32-workshop/src/app/components/form.component.ts
    return this.fb.group({

      name: this.fb.control<string>(med.name, [Validators.required]),
      medicationType: this.fb.control<string>(med.medicationType, [Validators.required]),
      dosage: this.fb.control<string>(med.dosage, [Validators.required]),
      frequency: this.fb.array(med.frequency, [Validators.required]),
      frequencyUnits: this.fb.control<string>(med.frequencyUnits, [Validators.required]),
      notes: this.fb.control<string>(med.notes)
    })
  }


  filterMedicine(value: string): string[] {
    const filterValue = value.toLowerCase()
    return this.medications.filter(option => option.toLowerCase().startsWith(filterValue))
  }

  frequencyCategory = ["Before Breakfast", "After Breakfast",
    "Before Lunch", "After Lunch",
    "Before Dinner", "After Dinner"]

  addFrequency(event: any) {
    // console.info("Value from button: ", event.source.value)
    // console.info("isSelected: ", event.selected)
    // this.healthConditionsForm.get("frequency")

    const intakeFrequncy: FormArray = this.medicationDosageForm.get("frequency") as FormArray
    const isChecked = event.checked
    const value = event.source.value

    if (isChecked) {
      intakeFrequncy.push(new FormControl(value))
    } else {
      const i = intakeFrequncy.value.findIndex((x: string) => x == value)
      // console.info("index: ", i)
      if (i >= 0) {
        intakeFrequncy.removeAt(i)
      }
    }

    console.info("Selected Values: ", intakeFrequncy.value)

  }


  dosageUnits: string = ""
  selectMedType(input: any) {
    console.info("Value of input:", input)
    if (input == "ointment") {
      this.medicationDosageForm.get('dosage')?.disable()
      this.dosageUnits = ""
    } else {
      this.medicationDosageForm.get('dosage')?.enable()

      if (input == "tablet") {
        this.dosageUnits = "No. of tablets"
      } else {
        this.dosageUnits = "Milimeters Intake"
      }

    }
  }


  addMedicationDosage(): void {
    console.info("Added new medication")

    const formInput = this.medicationDosageForm.value
    console.info("Dosage Form input: ", formInput)

    const dosage = formInput['dosage']
    const medicationType = formInput['medicationType']
    let dosageFormatted: string = ""

    if (medicationType == "tablet") {
      dosageFormatted = `${dosage} tablets`
    }
    else if (medicationType == "liquid" || medicationType == "injection") {
      dosageFormatted = `${dosage}ml`
    }
    else {
      dosageFormatted = "-"
    }

    const inputMed: Medication = {

      name: formInput['name'],
      medicationType: formInput['medicationType'],
      dosage: dosageFormatted,
      frequency: formInput['frequency'],
      frequencyUnits: formInput['frequencyUnits'],
      notes: formInput['notes']
    }

    let medExist: boolean = false

    this.medExistSub = this.medStore.medicationExist(inputMed.name).subscribe(
      value => {
        console.info("number of medications in store: ", value)
        medExist = value
      }
    )

    if (medExist) {
      if (confirm("Medication already Exist, new input will be updated")) {
        this.medStore.updateMedication(inputMed)
      } else {
        this.medicationDosageForm = this.createMedicineDosageForm(EMPTY_MEDICATION)
      }

    } else {
      this.medStore.addMedication(inputMed)

    }
    this.addedMeds$ = this.medStore.getMedications

    this.addNew = false
    this.addNewMedicationSubject.next(this.addNew)

    this.medicationDosageForm = this.createMedicineDosageForm(EMPTY_MEDICATION)



    this.medNameControl = this.medicationDosageForm.get('name') as FormControl
    this.filteredMeds$ = this.medNameControl.valueChanges.pipe(
      startWith(''),
      map(value => this.filterMedicine(value || ''))
    )

    this.sub = this.filteredMeds$.subscribe()

    this.createMedicineDosageForm(EMPTY_MEDICATION).reset()
    this.router.navigate(['/medicalInput'])

  }


  editMedicationDosage(med: Medication): void {
    console.info("Edit medication input: ", med)

    const savedMed = this.medRecord.medicationList.find(
      medRec => medRec.name == med.name
    )

    if (savedMed) {
      savedMed.frequency.forEach(
        (freq) => { this.frequenciesChecked[freq] = true })

      // savedMed.name = med.name
      const dosageStr = savedMed.dosage.split(" ").at(0)
      if (dosageStr) {
        if (dosageStr == "-") { return }
        else { med.dosage = dosageStr }
      }
    }

    this.addNew = true
    this.addNewMedicationSubject.next(this.addNew)

    this.update = true
    this.updateMedicationSubject.next(this.update)
    this.addedMeds$ = this.medStore.getMedications
    this.medicationDosageForm = this.createMedicineDosageForm(med)
    // this.medicationDosageForm.get('name')?.disable()


  }

  editCheckBox(): boolean {
    return true
  }

  discardChanges() {

    if (this.medicationDosageForm.dirty || this.editCheckBox()) {
      if (confirm("Changes made are not saved.\nAre you sure to cancel changes made?")) {
        this.addNew = false
        this.update = false
        this.router.navigate(['/medicalInput'])
      } else {
        return
      }
    } else {
      this.router.navigate(['/medicalInput'])
    }
  }


  deleteMedicationDosage(med: Medication): void {
    console.info("delete medication input: ", med)
    this.medStore.deleteMedication(med)
    this.addedMeds$ = this.medStore.getMedications


  }


  ////////////////// HEALTH CONDIDTIONS INPUT //////////////////

  healthCondition = ["asthma", "cancer", "cardiac disease", "diabetes",
    "hypertension", "epilepsy", "psychiatric disorder", "others"]

  // createHealthConditionsForm(): FormGroup {
  //   return this.fb.group({
  //     conditionsArray: this.fb.array([]),
  //     otherConditions: new FormControl({ value: null, disabled: true }, [Validators.required]),
  //     notes: this.fb.control<string>("")

  //   })
  // }


  createHealthConditionsForm(healthCondition: Conditions): FormGroup {
    const conditionsArrayControls = healthCondition.conditions.map(condition =>
      this.fb.control(condition)
    );

    return this.fb.group({
      // conditionsArray: this.fb.array(healthCondition.conditions,[]),
      conditionsArray: this.fb.array(conditionsArrayControls, []),
      otherConditions: new FormControl({ value: healthCondition.otherConditions, disabled: true }, [Validators.required]),
      notes: this.fb.control<string>(healthCondition.notes)

    })
  }

  onChange(event: any) {
    console.info("Healthconditions form array: ", this.healthConditionsForm.get("conditionsArray"))

    const healthConditions: FormArray = this.healthConditionsForm.get("conditionsArray") as FormArray
    const isChecked = event.checked
    const value = event.source.value


    const otherConditionsInput: string = this.healthConditionsForm.get("otherConditions")?.value

    if (value == 'others') {
      if (isChecked) {
        this.healthConditionsForm.get("otherConditions")?.enable()

      } else {
        const i = healthConditions.value.findIndex((x: string) => x == otherConditionsInput)
        // console.info("index: ", i)
        if (i >= 0) {
          healthConditions.removeAt(i)
        }
        this.healthConditionsForm.get("otherConditions")?.disable()
        this.healthConditionsForm.get("otherConditions")?.reset()
      }

    } else {
      if (value != 'others') {
        if (isChecked) {
          healthConditions.push(new FormControl(value))
          console.info("healthConditions value: ", event.source.value)
        } else {
          const i = healthConditions.value.findIndex((x: string) => x == value)
          // console.info("index: ", i)
          if (i >= 0) {
            healthConditions.removeAt(i)
          }
        }
      }
    }

  }

  inputOthers(event: any) {

    console.info("input from other conditions: ", event.target.value)

    const healthConditions: FormArray = this.healthConditionsForm.get("conditionsArray") as FormArray
    const value = event.target.value

    healthConditions.push(new FormControl(value))

  }

  addMedicalDetails() {

    if (confirm("Are you sure you want to Save changes?")) {
      // payload: 
      //   ['cardiac disease', 'hypertension']
      console.info("Medical Form input on Submit", this.healthConditionsForm.value.conditionsArray)
      console.info("Medical Form Notes on Submit", this.healthConditionsForm.value.notes)


      const notes: string = this.healthConditionsForm.value.notes

      let conditions: string[] = []
      // let conditions : string = ""
      conditions = this.healthConditionsForm.value.conditionsArray


      // {name: 'Flunil Suspension', medicationType: 'tablet', dosage: '2 tablets', frequency: '2', frequencyUnits: 'weekly', …}
      let medications: Medication[] = []
      // let medications : string = ""
      this.addedMeds$.subscribe((meds: Medication[]) => {
        console.info("Added meds on submit: ", meds)
        medications = meds

      })



      this.medSvc.addMedicationRecord(this.patientId, medications, conditions, notes)
      this.medStore.clearMedicationStore()
      this.router.navigate(['/patients', this.patientId])


    }
  }




  backToPatientDetails() {
    this.router.navigate(['/patients', this.patientId])

  }

}


