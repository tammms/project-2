import { HttpClient, HttpParams } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { BehaviorSubject, Observable, Subject, lastValueFrom } from "rxjs";
import { AngularFireMessaging } from "@angular/fire/compat/messaging";
import { MedReminderRequest, dailyDetails, eventDetails } from "../models";
import { TokenStorageService } from "./token.storage.service";

@Injectable()
export class ReminderService {

    private http = inject(HttpClient)
    private tokenStorage = inject(TokenStorageService)
    // private angularFireMessaging = inject(AngularFireMessaging)

    currentMessage = new BehaviorSubject(null);

    constructor(private angularFireMessaging: AngularFireMessaging) {
        navigator.serviceWorker.addEventListener('message', (event) => {
            console.log('Received background message:', event.data);
            this.currentMessage.next(event.data);
        });
    }


    ////////////////////////////////////////////////////////////////////////
    // Medication reminders
    ////////////////////////////////////////////////////////////////////////

    changeMedicationReminder(request: MedReminderRequest): Promise<any> {

        return lastValueFrom(this.http.post("/api/notification/reminder", { request }))
    }

    deleteReminder(guardianId: string, patientId: string): Promise<any> {

        const params = new HttpParams()
            .set("guardianId", guardianId)
            .set("patientId", patientId)

        return lastValueFrom(this.http.delete(`/api/notification/delete/${guardianId}/${patientId}`))

    }

    getPatientReminderSchedule(guardianId: string, patientId: string): Promise<MedReminderRequest> {

        const params = new HttpParams()
            .set("guardianId", guardianId)
            .set("patientId", patientId)

        return lastValueFrom(this.http.get<MedReminderRequest>("/api/notification/schedule", { params }))

    }

    getAllReminderSchedule(guardianId: string): Promise<MedReminderRequest> {

        const params = new HttpParams()
            .set("guardianId", guardianId)

        return lastValueFrom(this.http.get<MedReminderRequest>("/api/notification/all", { params }))

    }

    activateMedicalReminders(guardianId: string, token: string): Observable<any> {

        return this.http.post("/api/notification/send", { guardianId, token })

    }

    ////////////////////////////////////////////////////////////////////////
    // Events reminders
    ////////////////////////////////////////////////////////////////////////

    addEvent(event: eventDetails): Promise<any> {
        return lastValueFrom(this.http.post("/api/events/save", { event }))
    }

    activateEventReminders(guardianId: string, token: string): Observable<any> {

        return this.http.post("/api/events/send", { guardianId, token })

    }

    getAllEvents(guardianId: string): Observable<eventDetails[]> {
        const params = new HttpParams()
            .set("guardianId", guardianId)

        return this.http.get<eventDetails[]>("/api/events/get", { params })

    }

    getAllEventstest(guardianId: string): Promise<any> {
        const params = new HttpParams()
            .set("guardianId", guardianId)

        return lastValueFrom(this.http.get("/api/events/test", { params }))

    }

    // getPatientEvents(guardianId: string, patientId:string): Promise<any> {
    //     const params = new HttpParams()
    //     .set("guardianId", guardianId)
    //     .set("patientId", patientId)

    // return lastValueFrom(this.http.get("/api/events/getEvents", { params }))
    // }


    ////////////////////////////////////////////////////////////////////////
    // Dailys reminders
    ////////////////////////////////////////////////////////////////////////

    addDaily(daily: dailyDetails): Promise<any> {
        return lastValueFrom(this.http.post("/api/daily/save", { daily }))
    }


    activateDailyReminders(guardianId: string, token: string): Observable<any> {

        return this.http.post("/api/daily/send", { guardianId, token })

    }

    getAllDaily(guardianId: string): Observable<dailyDetails[]> {
        const params = new HttpParams()
            .set("guardianId", guardianId)

        return this.http.get<dailyDetails[]>("/api/daily/get", { params })

    }

    deleteEvent(eventId: string): Promise<any> {

        const params = new HttpParams()
            .set("eventId", eventId)

        return lastValueFrom(this.http.delete(`/api/reminder/delete/${eventId}`))

    }



    ////////////////////////////////////////////////////////////////////////



    receiveMessage() {

        this.angularFireMessaging.onMessage(
            (payload) => {

                console.info("Message Recieved from backend: ", payload.data.body)
                this.currentMessage.next(payload)

                const notificationTitle = payload.data.title;
                const notificationOptions = {
                    body: payload.data.body
                };

                new Notification(notificationTitle, notificationOptions)
            })
    }



    requestPermission(): Observable<any> {
        return this.angularFireMessaging.requestToken
    }

    requestNotification(guardianId: string) {
        this.requestPermission().subscribe({
            next: (token: any) => {

                if (token == null) {
                    throw new Error("FCM token is null")
                } else {
                    console.info("FCM token: ", token)
                    this.tokenStorage.saveFCMToken(token)
                    this.activateMedicalReminders(guardianId, token).subscribe({
                        next: (value: any) => {
                            console.info(">>Activated Reminders for MEDICATION");
                            console.info("Response from push notification: ", value);
                        },
                        error: (error: any) => {
                            console.error("Error requesting notification: ", error);
                        }
                    });

                    this.activateEventReminders(guardianId, token).subscribe({
                        next: (value: any) => {
                            console.info(">>Activated Reminders for EVENTS");
                            console.info("Response from push notification: ", value);
                        },
                        error: (error: any) => {
                            console.error("Error requesting notification: ", error);
                        }
                    });

                    this.activateDailyReminders(guardianId, token).subscribe({
                        next: (value: any) => {
                            console.info(">>Activated Reminders for DAILY");
                            console.info("Response from push notification: ", value);
                        },
                        error: (error: any) => {
                            console.error("Error requesting notification: ", error);
                        }
                    });
                }
            },
            error: (error: any) => {
                console.error("Error requesting token: ", error);

            }
        })
    }



}