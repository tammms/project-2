import { HttpClient, HttpParams } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable, Subject, lastValueFrom } from "rxjs";
import { Patient } from "../models";

@Injectable()
export class PatientRegistrationService {

    private http = inject(HttpClient)

    patientDetailsExists(patient: Patient, birthDate: string): Promise<any> {

        const params = new HttpParams().set("firstName", patient.firstName)
            .set("lastName", patient.lastName)
            .set("birthDate", birthDate);


        return lastValueFrom(this.http.get("/api/checkPatient", { params }))
    }



    patientAddedEvent = new Subject<void>()
    addNewPatient(patient: Patient, relationship: string, guardianId: string): Promise<any> {

        const payload = {
            patient: patient,
            relationship: relationship,
            guardianId: guardianId
        }

        const promise = lastValueFrom(this.http.post("/api/newPatient", payload))
            .then(value => {
                this.patientAddedEvent.next()
                return value
            })
        return promise
    }



    addExistingPatient(patientId: string, relationship: string, guardianId: string): Promise<any> {

        const payload = {
            patientId: patientId,
            relationship: relationship,
            guardianId: guardianId
        }

        const promise = lastValueFrom(this.http.post("/api/addExisting", payload))
            .then(value => {
                this.patientAddedEvent.next()
                return value
            })

        return promise

    }


    patientRelationsExists(guardianId: string, patientId: string): Promise<any> {

        const params = new HttpParams()
            .set("guardianId", guardianId)
            .set("patientId", patientId)

        return lastValueFrom(this.http.get("/api/checkRelations", { params }))
    }



    getPatientRelationDetails(guardianId: string): Observable<any> {

        const params = new HttpParams().set("guardianId", guardianId);

        return this.http.get<any>("/api/relationDetails", { params })
    }



    getPatientDetails(guardianId: string, patientId: string): Observable<any> {

        const params = new HttpParams().set("guardianId", guardianId)
            .set("patientId", patientId);

        return this.http.get<any>("/api/getDetailsFromRelation", { params })
    }

    deleteRelation(guardianId: string, patientId: string): Promise<any> {

        
        return lastValueFrom(this.http.delete(`/api/deleteRelation/user/${guardianId}/${patientId}`))

    }

    deleteRelationAdmin(guardianId: string, patientId: string): Promise<any> {
        console.info(">guardian id", guardianId)
    console.info(">patient id", patientId)

        return lastValueFrom(this.http.delete(`/api/deleteRelation/admin/${guardianId}/${patientId}`))

    }

    isEditingEvent = new Subject<boolean>()
    updatePatientDetails(patient: Patient, relationship: string, guardianId: string): Promise<any> {
        const patientId = patient.patientId
        const payload = {
            patient: patient,
            relationship: relationship,
            guardianId: guardianId
        }

        return lastValueFrom(this.http.put(`/api/patient/update/${patientId}`, payload))
    }



}