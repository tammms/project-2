import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable, lastValueFrom } from "rxjs";
import { Guardian } from "../models";

const httpOptions = {
    headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable()
export class AuthService {

    private http = inject(HttpClient)

    login(email: string, password: string): Promise<any> {

        return lastValueFrom(this.http.post("/api/auth/signIn", { email, password }, httpOptions))

    }


    register(guardian: Guardian): Promise<any> {

        return lastValueFrom(this.http.post("/api/auth/register", guardian, httpOptions))

    }

    selectPatient(guardianId: String, patientId: String): Promise<any> {

        return lastValueFrom(this.http.post("/api/auth/selectPatient", { guardianId, patientId }, httpOptions))

    }


    getGuardianById(guardianId: string): Promise<any> {

        const params = new HttpParams().set("guardianId", guardianId);
        return lastValueFrom(this.http.get<any>("/api/getGuardian", { params }))
    }

    editGuardianDetails(guardian: Guardian): Promise<any> {

        const guardianId = guardian.guardianId
        

        return lastValueFrom(this.http.put<any>(`/api/guardian/edit/${guardianId}`, guardian))
    }

    // updatePatientDetails(patient: Patient, relationship: string, guardianId: string): Promise<any> {
    //     const patientId = patient.patientId
    //     const payload = {
    //         patient: patient,
    //         relationship: relationship,
    //         guardianId: guardianId
    //     }

    //     return lastValueFrom(this.http.put(`/api/patient/update/${patientId}`, payload))
    // }






}