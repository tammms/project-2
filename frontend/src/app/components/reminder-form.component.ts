import { Component, Input, OnDestroy, OnInit, ViewChild, inject, input } from '@angular/core';
import { FormBuilder, FormGroup, ValidatorFn, AbstractControl, ValidationErrors, Validators, FormArray, FormControl } from '@angular/forms';
import { DEFAULT_REMINDER_SCHEDULE, MedReminderRequest, ScheduleTiming } from '../models';
import { Subscription, lastValueFrom } from 'rxjs';
import { MedicationService } from '../service/medication.service';
import { TokenStorageService } from '../service/token.storage.service';
import { ReminderService } from '../service/reminder.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-reminder-form',
  templateUrl: './reminder-form.component.html',
  styleUrl: './reminder-form.component.css'
})
export class ReminderFormComponent implements OnInit {

  private reminderSvc = inject(ReminderService)
  private tokenStore = inject(TokenStorageService)
  patientId = this.tokenStore.getPatientIdFromStorage()
  guardianId = this.tokenStore.getGuardianIdFromStorage()
  fcmToken = this.tokenStore.getFCMToken()

  private snackbar = inject(MatSnackBar)

  private fb = inject(FormBuilder)
  reminderTimingsForm!: FormGroup

  @Input()
  patientFirstName!: string

  @Input()
  medFrequencies: string[] = []

  hasWeekly: boolean = false
  weeklyStart: number = 1

  @Input()
  frequencyStatus!: string


  sub!: Subscription

  reminderList: string[] = []
  reminderSchedule: ScheduleTiming = DEFAULT_REMINDER_SCHEDULE
  sendReminder: Boolean = false

  // token!: string


  frequencyCategory = [
    { value: 'beforeBreakfast', displayValue: 'Before Breakfast' },
    { value: 'afterBreakfast', displayValue: 'After Breakfast' },
    { value: 'beforeLunch', displayValue: 'Before Lunch' },
    { value: 'afterLunch', displayValue: 'After Lunch' },
    { value: 'beforeDinner', displayValue: 'Before Dinner' },
    { value: 'afterDinner', displayValue: 'After Dinner' }
  ];

  weekOptions = [
    { value: 1, displayValue: 'Monday' },
    { value: 2, displayValue: 'Tuesday' },
    { value: 3, displayValue: 'Wednesday' },
    { value: 4, displayValue: 'Thursday' },
    { value: 5, displayValue: 'Friday' },
    { value: 6, displayValue: 'Saturday' },
    { value: 7, displayValue: 'Sunday' }

  ]

  ngOnInit(): void {

    console.info("Form oninit")
    this.reminderTimingsForm = this.createReminderTimingsForm(this.reminderSchedule)


    this.reminderSvc.getPatientReminderSchedule(this.guardianId, this.patientId)
      .then(
        resp => {
          console.info("Reminder Schedule from backend: ", resp)
          if (resp != null) {
            this.reminderList = resp['reminderFrequencies']
            // edit here
            if (resp['hasWeekly'] == true) {
              this.hasWeekly = true
            }
            this.weeklyStart = resp['weeklyStart']
            const timings: ScheduleTiming = resp['scheduleTimings']
            this.reminderSchedule = timings
            this.sendReminder = true
            console.info("Reminder Schedule from backend: ", this.reminderSchedule)
          }
          return this.reminderSchedule
        })
      .then(
        schedule => {
          this.reminderTimingsForm = this.createReminderTimingsForm(schedule)
          this.disableControls()
          console.info("schedule from then: ", schedule)
        })


  }





  timeValidationPrev(prevTimeCtrl: string): ValidatorFn {
    return (ctrl: AbstractControl): ValidationErrors | null => {

      const currStr: string = ctrl.value
      const prevStr: string = ctrl.root.get(prevTimeCtrl)?.value

      // console.info("Current value: ", currStr)
      // console.info("Previous value: ", prevStr)

      if (currStr != null && prevStr != null) {
        const currTime = this.parseTimeString(currStr)
        const prevTime = this.parseTimeString(prevStr)

        // console.info("Current time: ", currTime)
        // console.info("Previous time: ", prevTime)

        if (currTime != null && prevTime != null) {
          if (currTime < prevTime) {
            return { timeValidationPrev: true } as ValidationErrors
          }
          return (null)
        }
      } return (null)
    }
  }


  timeValidationNext(nextTimeCtrl: string): ValidatorFn {
    return (ctrl: AbstractControl): ValidationErrors | null => {

      const currStr: string = ctrl.value
      const nextStr: string = ctrl.root.get(nextTimeCtrl)?.value

      // console.info("Current value: ", currStr)
      // console.info("Previous value: ", prevStr)

      if (currStr != null && nextStr != null) {
        const currTime = this.parseTimeString(currStr)
        const nextTime = this.parseTimeString(nextStr)

        // console.info("Current time: ", currTime)
        // console.info("Previous time: ", prevTime)

        if (currTime != null && nextTime != null) {
          if (currTime > nextTime) {
            return { timeValidationNext: true } as ValidationErrors
          }
          return (null)
        }
      } return (null)
    }
  }


  parseTimeString(timeString: string): Date | null {
    const [hours, minutes] = timeString.split(':').map(value => parseInt(value));
    if (hours != null && minutes != null) {
      const date = new Date();
      date.setHours(hours, minutes);
      return date;
    }
    return null;
  }

  freqFormArray!: FormArray
  createReminderTimingsForm(defaultTiming: ScheduleTiming): FormGroup {

    const timePattern = /^([01]?[0-9]|2[0-3]):[0-5][0-9]$/;


    return this.fb.group({

      // weeklyStart: this.fb.control<number>(1),
        beforeBreakfast: this.fb.control({ value: defaultTiming.beforeBreakfast, disabled: true },
          [Validators.required, Validators.pattern(timePattern)]),

        afterBreakfast: this.fb.control({ value: defaultTiming.afterBreakfast, disabled: true },
          [Validators.required, Validators.pattern(timePattern)]),

        beforeLunch: this.fb.control({ value: defaultTiming.beforeLunch, disabled: true },
          [Validators.required, Validators.pattern(timePattern)]),

        afterLunch: this.fb.control({ value: defaultTiming.afterLunch, disabled: true },
          [Validators.required, Validators.pattern(timePattern)]),

        beforeDinner: this.fb.control({ value: defaultTiming.beforeDinner, disabled: true },
          [Validators.required, Validators.pattern(timePattern)]),

        afterDinner: this.fb.control({ value: defaultTiming.afterDinner, disabled: true },
          [Validators.required, Validators.pattern(timePattern)])

    })
  }

  isReminderSelected(frequency: string): boolean {
    if (this.reminderList.length > 0) {
      const index = this.reminderList.findIndex(freq => (freq == frequency))
      if (index < 0) { return false }
      else { return true }
    }
    else return false
  }

  isDisabled(value: string): boolean {
    const index = this.medFrequencies.findIndex(freq => (freq == value))
    if (index >= 0) {
      return false
    } else
      return true
  }


  disableControls() {
    const controls = this.reminderTimingsForm.controls;
    if (this.reminderList.length > 0) {
      this.reminderList.forEach(freq => {
        if (controls[freq]) {
          controls[freq].enable();
        }
      });
    }
  }

  chipChange(event: any, frequency: string) {

    console.info("chip selected: ", event.selected)
    console.info("chip value: ", frequency)

    let isSelected: boolean = event.selected
    let inputValue: string = frequency

    const index = this.reminderList.findIndex(value => (value == inputValue))

    if (isSelected && index < 0) {
      this.reminderList.push(inputValue)
      this.reminderTimingsForm.get(frequency)?.enable()
    }

    if (!isSelected && index >= 0) {
      this.reminderList.splice(index, 1)
      this.reminderTimingsForm.get(frequency)?.disable()
    }
    console.info("selected frequencies count: ", this.reminderList)
  }

  changeWeeklyStart(input: any) {
    console.info("Event from value change: ", input)
    this.weeklyStart = input

  }

  containsWeekly(): boolean {
    if (this.frequencyStatus == "weekly" || this.frequencyStatus == "both") {
      return true
    } else {
      return false
    }
  }

  containsBoth(): boolean {
    if (this.frequencyStatus == "both") {
      return true
    } else {
      return false
    }
  }

  setWeekly(event: any) {
    this.hasWeekly = event.checked
  }

  isInvalid(): boolean {
    return this.reminderTimingsForm.invalid || this.reminderList.length < 1
  }

  setTime() {
    console.log("Form values:", this.reminderTimingsForm.value)
    const timing: ScheduleTiming = this.reminderTimingsForm.value

    const request: MedReminderRequest = {
      guardianId: this.tokenStore.getGuardianIdFromStorage(),
      patientId: this.patientId,
      patientFirstName: this.patientFirstName,
      scheduleTimings: timing,
      reminderFrequencies: this.reminderList,
      hasWeekly: this.hasWeekly,
      weeklyStart: this.weeklyStart,
    }

    this.reminderSvc.changeMedicationReminder(request)
      .then(
        message => {
          this.snackbar.open(message.message, undefined, { duration: 3000 })
          if (this.fcmToken != null) {
            lastValueFrom(this.reminderSvc.activateMedicalReminders(this.guardianId, this.fcmToken))
          }
        }
      ).catch(
        err => { this.snackbar.open(`Error: ${err.error.message.string}`, undefined, { duration: 3000 }) }
      )

  }

  toRemind(input: any) {
    console.info("Event from value change: ", input.checked)
    this.sendReminder = input.checked
    if (this.sendReminder == false) {
      if (confirm("Are you certain you wish to disable reminders?\nDisabling reminders will discard all reminder settings.\nYou can re-enable them later if needed.")) {
        this.reminderSvc.deleteReminder(this.guardianId, this.patientId)
          .then(
            message => {
              this.resetForm()
              this.snackbar.open(message.message, undefined, { duration: 3000 })
              if (this.fcmToken != null) {
                lastValueFrom(this.reminderSvc.activateMedicalReminders(this.guardianId, this.fcmToken))
              }
            }
          ).catch(
            err => { this.snackbar.open(`Error: ${err.error.message}`, undefined, { duration: 3000 }) }
          )
      }
    }
  }

  resetForm() {
    this.reminderTimingsForm = this.createReminderTimingsForm(DEFAULT_REMINDER_SCHEDULE)
    this.reminderList = []
    this.weeklyStart = 1
  }

}
