import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { Component, OnDestroy, OnInit, ViewChild, inject } from '@angular/core';
import { AbstractControl, FormArray, FormBuilder, FormControl, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { MatExpansionPanel } from '@angular/material/expansion';
import { Router } from '@angular/router';
import { Subscription, lastValueFrom } from 'rxjs';
import { TokenStorageService } from '../service/token.storage.service';
import { ReminderService } from '../service/reminder.service';
import { eventDetails } from '../models';
import { v4 as uuid } from 'uuid'
import { MatSnackBar } from '@angular/material/snack-bar';
declare var createGoogleEvent: any;


@Component({
  selector: 'app-events',
  templateUrl: './events.component.html',
  styleUrl: './events.component.css'
})
export class EventsComponent implements OnInit, OnDestroy {


  private router = inject(Router)
  private tokenStorage = inject(TokenStorageService)
  private reminderSvc = inject(ReminderService)


  patientName = this.tokenStorage.getPatientNameFromStorage()
  patientId = this.tokenStorage.getPatientIdFromStorage()
  guardianId = this.tokenStorage.getGuardianIdFromStorage()
  fcmToken = this.tokenStorage.getFCMToken()

  private snackbar = inject(MatSnackBar)


  private fb = inject(FormBuilder)
  eventsForm!: FormGroup
  repeatForm!: FormGroup
  dailyReminderForm!: FormGroup


  @ViewChild('panel') panel!: MatExpansionPanel;

  isRepeat: boolean = false
  isExpanded: boolean = false
  sendEmail: boolean = false



  frequencyCategory = [
    { value: "DAILY", displayValue: "Day" },
    { value: "WEEKLY", displayValue: "Week" },
    { value: "MONTHLY", displayValue: "Month" },
    { value: "YEARLY", displayValue: "Year" }
  ];

  days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
  months = ['January', 'Febuary', 'March', 'April',
    'May', 'June', 'July', 'August',
    'September', 'October', 'November', 'December']

  repeatDescription: string = "Don't repeat"



  startDateChange: boolean = false
  endDateChange: boolean = false
  startDateSub!: Subscription | undefined
  endDateSub!: Subscription | undefined
  startTimeSub!: Subscription | undefined


  startDateFormatted: Date = new Date()
  endDateFormatted: Date = new Date()

  startDateTimeFormatted!: Date
  endDateTimeFormatted!: Date



  ngOnInit(): void {

    this.eventsForm = this.createEventsForm()

    this.startDateSub = this.eventsForm.get("startDate")?.valueChanges.subscribe(
      value => {
        console.info("Start Date Input: ", value.c)
        this.startDateChange = true
        this.startDateFormatted = this.formatDate(value.c)

        let endDate
        if (this.endDateChange) {
          endDate = this.formatDate(this.eventsForm.get("endDate")?.value.c)
        } else { endDate = this.eventsForm.get("endDate")?.value }
        if (this.startDateFormatted.getTime() > endDate) {
          this.eventsForm.get('endDate')?.setErrors({ dateValidation: true });
        }
        if (this.isRepeat) { this.setDescription() }

        if (this.startDateFormatted.getTime() == endDate) {
          console.info("start date = end date")
          if (this.eventsForm.get('startTime')?.value > this.eventsForm.get('endTime')?.value) {
            this.eventsForm.get('endTime')?.setErrors({ timeValidation: true })
          };
        }
      })



    this.endDateSub = this.eventsForm.get("endDate")?.valueChanges.subscribe(
      value => {
        this.endDateChange = true
        this.endDateFormatted = this.formatDate(value.c)
      }
    )

    this.startTimeSub = this.eventsForm.get('startTime')?.valueChanges.subscribe(
      value => {
        const isSameDayEvent: boolean =
          (this.startDateFormatted.getTime() == this.endDateFormatted.getTime())

        const endStr: string = this.eventsForm.get('endTime')?.value
        const startStr: string = value

        if (endStr != null && startStr != null) {
          const endTime = this.formatTimeString(endStr)
          const startTime = this.formatTimeString(startStr)
          if (startTime > endTime) {
            this.eventsForm.get('endTime')?.setErrors({ timeValidation: true })
          }
        }
      })
  }

  



  ngOnDestroy(): void {
    this.startDateSub?.unsubscribe()
    this.endDateSub?.unsubscribe()
    this.startTimeSub?.unsubscribe()
  }

  // https://medium.com/@onlinemsr/javascript-string-format-the-best-3-ways-to-do-it-c6a12b4b94ed


  createEventsForm(): FormGroup {
    const timePattern = /^([01]?[0-9]|2[0-3]):[0-5][0-9]$/;

    return this.fb.group({
      summary: this.fb.control<string>("", [Validators.required]),
      location: this.fb.control<string>("", [Validators.required]),
      description: this.fb.control<string>("", [Validators.required]),
      startDate: this.fb.control<Date>(this.startDateFormatted,
        //  [this.startDateValidation]
      ),
      startTime: this.fb.control<string>("", [Validators.required, Validators.pattern(timePattern)]),
      endDate: this.fb.control<Date>(this.endDateFormatted),
      endTime: this.fb.control<string>("", [Validators.required, Validators.pattern(timePattern),
      this.timeValidation("startTime")
      ]),
      attendees: this.fb.array([]),
      frequencyUnits: this.fb.control<string>('DAILY', [Validators.required]),
      frequency: this.fb.control<number>(1)

    })
  }

  // createEventsForm(): FormGroup {
  //   const timePattern = /^([01]?[0-9]|2[0-3]):[0-5][0-9]$/;

  //   return this.fb.group({
  //     summary: this.fb.control<string>("summary", [Validators.required]),
  //     location: this.fb.control<string>("location", [Validators.required]),
  //     description: this.fb.control<string>("description", [Validators.required]),
  //     startDate: this.fb.control<Date>(this.startDateFormatted,
  //       //  [this.startDateValidation]
  //     ),
  //     startTime: this.fb.control<string>("00:00", [Validators.required, Validators.pattern(timePattern)]),
  //     endDate: this.fb.control<Date>(this.endDateFormatted),
  //     endTime: this.fb.control<string>("00:00", [Validators.required, Validators.pattern(timePattern),
  //     this.timeValidation("startTime")
  //     ]),
  //     attendees: this.fb.array([]),
  //     frequencyUnits: this.fb.control<string>('DAILY', [Validators.required]),
  //     frequency: this.fb.control<number>(1)

  //   })
  // }



  isInvalid() {
    let isInvalid = false
    const attendeesEmail: FormArray = this.eventsForm.get("attendees") as FormArray

    // if (this.sendEmail && attendeesEmail.length < 1) {
    //   this.eventsForm.get('attendees')?.setErrors({ isrequired: true });
    //   isInvalid = true
    // }

    if (this.eventsForm.invalid) {
      isInvalid = true
    }
    return isInvalid
  }


  repeatReminder(input: any) {
    console.info("Repeat Reminder: ", input.checked)
    this.isRepeat = input.checked
    // this.repeatForm = this.createRepeatForm()
    if (!this.isRepeat) {
      this.repeatDescription = "Don't Repeat"
    } else {
      if (this.eventsForm.get("frequency")?.pristine) {
        this.repeatDescription = "Set Repeat Schedule"
      } else {
        this.setDescription()
      }
    }
  }

  addAttendees(input: any) {
    console.info("Send calendar invite: ", input.checked)
    this.sendEmail = input.checked
    const attendeesEmail: FormArray = this.eventsForm.get("attendees") as FormArray

    if (attendeesEmail.length < 1 && this.sendEmail) {
      this.eventsForm.get('attendees')?.setErrors({ isrequired: true });
    }

  }

  startDateFilter = (d: Date | null): boolean => {
    const minDate: Date = new Date()
    minDate.setDate(minDate.getDate() - 1)
    const day = d || new Date();

    return day > minDate;
  }

  endDateFilter = (d: Date | null): boolean => {
    const minDate: Date = new Date();
    minDate.setDate(this.startDateFormatted.getDate() - 1)
    const day = d || new Date();
    return day > minDate;
  }

  // startDateValidation: ValidatorFn = (ctrl: AbstractControl): ValidationErrors | null => {
  //   const minDate: Date = new Date()
  //   minDate.setDate(minDate.getDate() - 1)
  //   if (new Date(ctrl.value) < minDate) {
  //     return { startDateValidation: true } as ValidationErrors
  //   }
  //   return (null)
  // }

  // startDateValidationCalendar(endDateCtrl: string): ValidatorFn {
  //   return (ctrl: AbstractControl): ValidationErrors | null => {
  //     // if just init/never change, use ctrl.value
  //     // if change already use ctrl.value.c

  //     const startStr: Date = ctrl.value.c
  //     const endStr: Date = ctrl.root.get(endDateCtrl)?.value.c

  //     let startDate: Date
  //     let endDate: Date

  //     if (startStr == null) {
  //       // measn no need to format
  //       startDate = ctrl.value
  //     } else { startDate = this.formatDate(startStr) }

  //     if (endStr == null) {
  //       // measn no need to format
  //       endDate = ctrl.root.get(endDateCtrl)?.value
  //     } else { endDate = this.formatDate(endStr) }

  //     console.info("Validation Start Date", startDate)
  //     console.info("Validation End Date", endDate)


  //     if (startDate != null && endDate != null) {
  //       const minDate: Date = new Date();
  //       minDate.setDate(startDate.getDate() - 1)

  //       if (endDate < minDate) {
  //         return { startDateValidationCalendar: true } as ValidationErrors
  //       } return null
  //     }
  //     return null
  //   }
  // }



  timeValidation(startTimeCtrl: string): ValidatorFn {
    return (ctrl: AbstractControl): ValidationErrors | null => {

      const isSameDayEvent: boolean =
        (this.startDateFormatted.getTime() == this.endDateFormatted.getTime())

      if (isSameDayEvent) {

        const endStr: string = ctrl.value
        const startStr: string = ctrl.root.get(startTimeCtrl)?.value

        if (endStr != null && startStr != null) {
          const endTime = this.formatTimeString(endStr)
          const startTime = this.formatTimeString(startStr)

          if (startTime > endTime) {
            return { timeValidation: true } as ValidationErrors
          } else { return (null) }
        }
        else { return (null) }
      }
      else { return (null) }
    }
  }





  formatDate(dateString: any): Date {
    // dateString = this.eventsForm.get("startDate")?.value
    const date = new Date()
    date.setFullYear(dateString.year, dateString.month - 1, dateString.day)
    console.info("Formatted Date input: ", date)
    return date
  }


  formatDateTime(dateString: any, timeString: string): string {
    const options = { timeZone: 'Asia/Singapore' };
    const date = new Date();

    date.setFullYear(dateString.year, dateString.month - 1, dateString.day)

    const [hours, minutes] = timeString.split(':').map(value => parseInt(value));
    date.setHours(hours, minutes, 0, 0);

    return new Date(date.toLocaleString('en-US', options)).toISOString()
  }

  formatDefaultDateTime(date: Date, timeString: string): string {
    const options = { timeZone: 'Asia/Singapore' };

    const [hours, minutes] = timeString.split(':').map(value => parseInt(value));
    date.setHours(hours, minutes, 0, 0);

    return new Date(date.toLocaleString('en-US', options)).toISOString()
  }

  formatTimeString(timeString: string): Date {
    const date = new Date()
    const [hours, minutes] = timeString.split(':').map(value => parseInt(value));
    date.setHours(hours, minutes, 0, 0);
    return date
  }

  formatFrequency(frequencyUnits: string, frequency: number) {

    return `RRULE:FREQ=${frequencyUnits};INTERVAL=${frequency}`;
  }

  setDescription() {
    const date = this.startDateFormatted
    const freq = this.eventsForm.get("frequency")?.value
    const frequencyUnits = this.eventsForm.get("frequencyUnits")?.value

    console.info('Date: ', date)

    let schedule = ""

    if (frequencyUnits == 'DAILY') {
      schedule = "This reminder will repeat daily"
    }
    if (frequencyUnits == 'WEEKLY') {
      const day = this.days[date.getDay()]
      schedule = `This reminder will repeat every ${freq} week on ${day}`
    }
    if (frequencyUnits == 'MONTHLY') {
      const day = date.getDate()
      const units = this.getDateSuffix(day)
      schedule = `This reminder will repeat every ${freq} month on the ${day}${units}`
    }

    if (frequencyUnits == 'YEARLY') {
      const day = date.getDate()
      const units = this.getDateSuffix(day)
      const month = this.months[date.getMonth()]
      schedule = `This reminder will repeat every ${freq} year on ${day}${units} ${month}`
    }

    console.info('Description: ', this.repeatDescription)

  }

  setFrequencyUnits() {
    this.setDescription()
  }


  getDateSuffix(input: number) {
    if (input >= 11 && input <= 13) {
      return 'th';
    }
    switch (input % 10) {
      case 1: return 'st';
      case 2: return 'nd';
      case 3: return 'rd';
      default: return 'th';
    }
  }


  addOnBlur = true;
  readonly separatorKeysCodes = [ENTER, COMMA] as const;
  get attendeesArray() {
    return this.eventsForm.get("attendees") as FormArray
  }

  addAttendee(input: any) {
    console.info('input add: ', this.eventsForm.get("attendees")?.value)

    const email = input.value
    const attendeesEmail: FormArray = this.eventsForm.get("attendees") as FormArray

    const index = attendeesEmail.value.findIndex((e: string) => e == email)

    if (index < 0 && email != '') {
      const tempControl = new FormControl(email, Validators.email);
      if (tempControl.valid) {
        attendeesEmail.push(new FormControl(email))
        input.chipInput!.clear()
      } else {

        this.eventsForm.get('attendees')?.setErrors({ invalidEmail: true });
      }
    }

  }

  


  removeAttendee(index: number) {
    console.info('Remove at: ', index)
    const attendeesEmail: FormArray = this.eventsForm.get("attendees") as FormArray
    if (index >= 0) {
      attendeesEmail.removeAt(index)
    }

  }

  checkValid(){
    const attendeesEmail: FormArray = this.eventsForm.get("attendees") as FormArray

    if (this.sendEmail && attendeesEmail.length < 1) {
        this.eventsForm.get('attendees')?.setErrors({ isrequired: true });
      //   isInvalid = true
      }
  }

  saveReminder() {
    console.info("Data from form Input:", this.eventsForm.value)
    const formInput = this.eventsForm.value
    const id: string = uuid().substring(0, 8)

    const details: eventDetails = {
      guardianId: this.guardianId,
      patientId: this.patientId,
      eventId: id,
      reminderType: "event",
      patientName: this.patientName,
      isValid: true,
      summary: formInput.summary,
      location: formInput.location,
      description: formInput.description,
      startDate: formInput.startDate,
      startTime: formInput.startTime,
      endDate: formInput.endDate,
      endTime: formInput.endTime,
      isRepeat: this.isRepeat,
      frequencyUnits: formInput.frequencyUnits,
      frequency: formInput.frequency,
      sendEmail: this.sendEmail,
      attendees: formInput.attendees
    }

    this.reminderSvc.addEvent(details)
      .then(
        message => {
          this.snackbar.open(message.message, undefined, { duration: 3000 })
          if (this.fcmToken != null) {
            lastValueFrom(this.reminderSvc.activateEventReminders(this.guardianId, this.fcmToken))
            // .then(
            //   value=>{
            //     console.info(">>Event activated", value)
            //     if(this.sendEmail){
            //       this.saveToGoogleCalendar(formInput)}
            //     this.router.navigate(['/patients', this.patientId])
            //   }
            // )
          }


        }
      ).catch(
        err => { this.snackbar.open(`Error: ${err.error.message.string}`, undefined, { duration: 3000 }) }
      )


    if (this.sendEmail) {
      this.saveToGoogleCalendar(formInput)
    }
    // this.router.navigate(['/patients', this.patientId])
    this.router.navigate(['/patients'])

  }


  saveToGoogleCalendar(formInput: any) {
    let eventStart: string
    let eventEnd: string

    const startTimeString = formInput.startTime
    const endTimeString = formInput.endTime

    if (this.startDateChange) {
      const startDateString = formInput.startDate.c
      eventStart = this.formatDateTime(startDateString, startTimeString)
    } else {
      const startDateString = formInput.startDate
      eventStart = this.formatDefaultDateTime(startDateString, startTimeString)
    }

    if (this.endDateChange) {
      const endDateString = formInput.endDate.c
      eventEnd = this.formatDateTime(endDateString, endTimeString)
    } else {
      const endDateString = formInput.endDate
      eventEnd = this.formatDefaultDateTime(endDateString, endTimeString)
    }

    let repeatFrequency: string = ""
    if (this.isRepeat) {
      repeatFrequency = this.formatFrequency(formInput.frequencyUnits, formInput.frequency)
    }

    console.info("Recurrence string:", repeatFrequency)

    const inputDescription = formInput.description
    const description = `(Care Aid: Event Reminder for ${this.patientName})\n${inputDescription}`

    const eventDetails = {
      summary: formInput.summary,
      location: formInput.location,
      description: description,
      startTime: eventStart,
      endTime: eventEnd,
      recurrence: repeatFrequency,
      attendees: formInput.attendees
    }

    console.info("Events input from form: ", eventDetails)

    this.eventsForm = this.createEventsForm()

    createGoogleEvent(eventDetails)
  }





}
