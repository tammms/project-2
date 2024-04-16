import { Component, Input, OnInit, inject } from '@angular/core';
import { ReminderService } from '../service/reminder.service';
import { dailyDetails, eventDetails } from '../models';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';

@Component({
  selector: 'app-reminder-summary',
  templateUrl: './reminder-summary.component.html',
  styleUrl: './reminder-summary.component.css'
})
export class ReminderSummaryComponent implements OnInit {


  private reminderSvc = inject(ReminderService)
  private snackbar = inject(MatSnackBar)
  private router = inject(Router)


  @Input()
  eventList: eventDetails[] = []

  @Input()
  dailyList: dailyDetails[] = []
  

  ngOnInit(): void {
      
  }

  days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
  months = ['January', 'Febuary', 'March', 'April',
    'May', 'June', 'July', 'August',
    'September', 'October', 'November', 'December']

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

  formatDateString(dateString: string): Date {

    let stringArray: string[] = dateString.split("-")
    let date = new Date()
    date.setFullYear(Number(stringArray[0]), Number(stringArray[1]) - 1, Number(stringArray[2]))
    return date
  }

  setDescription(event: eventDetails): string {


    const date = this.formatDateString(event.startDate)
    const freq = event.frequency
    const frequencyUnits = event.frequencyUnits

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

    return schedule

  }

  deleteReminder(eventId: string, reminderType: string){
    let confirmString = "";
    if(reminderType == "event"){
      confirmString = "Are you certain you wish to Delete reminders?\nDeleted Reminders cannot be retrieved. \n*NOTE: Deleting a reminder here will not remove the event from your Google Calendar if you have previously sent a Google invite."
    } else{
      confirmString = "Are you certain you wish to Delete reminders?\nDeleted Reminders cannot be retrieved."
    }
    if(confirm(confirmString)){
      this.reminderSvc.deleteEvent(eventId)
      .then(
        message => {
          this.snackbar.open(message.message, undefined, { duration: 3000 })
      this.router.navigate(['/patients'])

          // const fcmToken = this.tokenStorage.getFCMToken()
          // if (fcmToken != null) {
          //   lastValueFrom(this.reminderSvc.activateMedicalReminders(this.guardianId, fcmToken))
          // }
        }
      ).catch(
        err => { this.snackbar.open(`Error: ${err.error.message}`, undefined, { duration: 3000 }) }
      )
      
    }
  }
  

 
}
