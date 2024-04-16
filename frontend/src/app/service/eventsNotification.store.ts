import { Injectable } from "@angular/core";
import { ComponentStore } from "@ngrx/component-store";
import { NotificationSlice, dailyDetails, eventDetails } from "../models";

const INIT_STORE: NotificationSlice = {
    event: [],
    daily: []
}
@Injectable()
export class EventsNotificationStore extends ComponentStore<NotificationSlice> {

    constructor() { super(INIT_STORE) }

    readonly addEvent = this.updater<eventDetails>(
        (slice: NotificationSlice, event: eventDetails) => {
            return {
                event: [...slice.event, event],
                daily: slice.daily
            }
        }
    )

    readonly addDaily = this.updater<dailyDetails>(
        (slice: NotificationSlice, daily: dailyDetails) => {
            
            return {
                event: slice.event,
                daily: [...slice.daily, daily]
            }
        }
    )

    readonly clearEventsFromStore = this.updater(
        (slice: NotificationSlice) => {
            return {
                event: [],
                daily: slice.daily
            }
        }
    )

    readonly clearDailyFromStore = this.updater(
        (slice: NotificationSlice) => {
            return {
                event: slice.event,
                daily: []
            }
        }
    )

    readonly clearNotificationStore = this.updater(
        (slice: NotificationSlice) => {
            return {
                event: [],
                daily: []
            }
        }
    )


    readonly getEventbyID = (patientId: string)=>{
        return this.select<eventDetails[]>(
            (slice: NotificationSlice)=>{
                return slice.event.filter(e => e.patientId == patientId)
            }
        )
    }


    readonly getDailybyID = (patientId: string)=>{
        return this.select<dailyDetails[]>(
            (slice: NotificationSlice)=>{
                return slice.daily.filter(d => d.patientId == patientId)
            }
        )
    }


}