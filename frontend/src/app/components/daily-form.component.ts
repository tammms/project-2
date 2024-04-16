import { COMMA, ENTER } from '@angular/cdk/keycodes';
import { Component, OnInit, ViewChild, inject } from '@angular/core';
import { FormArray, FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { dailyDetails } from '../models';
import { ReminderService } from '../service/reminder.service';
import { Router } from '@angular/router';
import { TokenStorageService } from '../service/token.storage.service';
import { v4 as uuid } from 'uuid'
import { MatSnackBar } from '@angular/material/snack-bar';
import { lastValueFrom } from 'rxjs';


@Component({
  selector: 'app-daily-form',
  templateUrl: './daily-form.component.html',
  styleUrl: './daily-form.component.css'
})
export class DailyFormComponent implements OnInit {

  private fb = inject(FormBuilder)
  dailyReminderForm!: FormGroup

  private reminderSvc = inject(ReminderService)
  private router = inject(Router)
  private tokenStorage = inject(TokenStorageService)

  private snackbar = inject(MatSnackBar)

  guardianId = this.tokenStorage.getGuardianIdFromStorage()
  patientId = this.tokenStorage.getPatientIdFromStorage()
  patientName = this.tokenStorage.getPatientNameFromStorage()
  fcmToken = this.tokenStorage.getFCMToken()


  ngOnInit(): void {

    this.dailyReminderForm = this.createDailyReminderForm()
  }

  createDailyReminderForm(): FormGroup {
    return this.fb.group({
      description: this.fb.control<string>("", [Validators.required]),
      startTime: this.fb.array([])
    })
  }


  isInvalid(): boolean {
    const timeArray: FormArray = this.dailyReminderForm.get("startTime") as FormArray
    const length = timeArray.controls.length

    return this.dailyReminderForm.invalid || length <= 0
  }

  addOnBlur = true;
  readonly separatorKeysCodes = [ENTER, COMMA] as const;
  get timingArray() {
    return this.dailyReminderForm.get("startTime") as FormArray
  }

  addTime(input: any) {
    const time = input.value
    console.info("Input value: ", time)
    const timeArray: FormArray = this.dailyReminderForm.get("startTime") as FormArray

    const index = timeArray.value.findIndex((t: string) => t == time)

    if (index < 0 && time != '') {
      const timePattern = /^([01]?[0-9]|2[0-3]):[0-5][0-9]$/;
      const tempControl = new FormControl(time, Validators.pattern(timePattern));
      if (tempControl.valid) {
        console.info("Input value: valid")
        timeArray.push(new FormControl(time))
        input.chipInput!.clear()
      } else {
        console.info("Input value: invalid")

        this.dailyReminderForm.get('startTime')?.setErrors({ invalidPattern: true })
      }
    }

  }

  removeTime(index: number) {
    const timeArray: FormArray = this.dailyReminderForm.get("startTime") as FormArray

    timeArray.removeAt(index)
  }

  saveReminder() {
    const input = this.dailyReminderForm.value
    console.info("Value from form: ", input)

    const sortedTime = this.sortDailyStartTime(input.startTime)

    const id: string = uuid().substring(0, 8)


    const details: dailyDetails = {
      guardianId: this.guardianId,
      patientId: this.patientId,
      eventId: id,
      reminderType: "daily",
      isValid: true,
      patientName: this.patientName,
      description: input.description,
      startTime: sortedTime
    }


    this.reminderSvc.addDaily(details)
      .then(
        message => {
          this.snackbar.open(message.message, undefined, { duration: 3000 })
          if (this.fcmToken != null) {
            lastValueFrom(this.reminderSvc.activateDailyReminders(this.guardianId, this.fcmToken))
            .then(
                value=>{this.router.navigate(['/patients'])
              }
            )
          }
        }
      ).catch(
        err => { this.snackbar.open(`Error: ${err.error.message.string}`, undefined, { duration: 3000 }) }
      )

  }


  sortDailyStartTime(timeArray: string[]): string[] {

    timeArray.sort((a, b) => {
      const [hoursA, minutesA] = a.split(':').map(Number);
      const [hoursB, minutesB] = b.split(':').map(Number);

      const dateA = new Date(2000, 0, 1, hoursA, minutesA);
      const dateB = new Date(2000, 0, 1, hoursB, minutesB);

      return dateA.getTime() - dateB.getTime();
    });
    return timeArray
  }

  test() {

    this.reminderSvc.getAllEventstest(this.guardianId)
      .then(
        value => {
          console.info("Message: ", value)
        }
      ).catch(err => {
        alert(`FAILED TO GET EVENT REMINDERS: ${err.error.message}`)
      })
  }

}
