import { HttpClient, HttpParams } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { Observable, lastValueFrom } from "rxjs";
import { Medication, MedicationRecord } from "../models";

@Injectable()
export class MedicationService {


    private http = inject(HttpClient)

    getMedicines():Observable<string[]>{

        return this.http.get<string[]>("/api/medicines")
    }


    addMedicationRecord(patientId: string, medications: Medication[],
                                     conditions: string[], notes: string):Promise<any>{

        const payload = {
            patientId: patientId,
            medications: medications,
            conditions: conditions,
            notes:notes
        }

        return lastValueFrom(this.http.post<string>("/api/medicationRecord", payload))
    }

    getMedicationRecord(patientId: string):Observable<MedicationRecord>{

        const params = new HttpParams().set("patientId", patientId)
        // console.info("\n\ngetmedicationrecords")

        return this.http.get<MedicationRecord>("/api/listMedicalRecords", {params})
        // return lastValueFrom(this.http.get<string[]>("/api/medicines"))


    }

    deleteMedicationRecord(patientId: string):Observable<any>{
    console.info("delete record here")
        
        // const params = new HttpParams().set("patientId", patientId)
        // console.info("\n\ngetmedicationrecords")

        return this.http.delete<string>(`/api/patients/${patientId}`)

    }

    getMedicatioinFrequencies(patientId: string):Promise<any>{

        const params = new HttpParams().set("patientId", patientId)

        return lastValueFrom(this.http.get("/api/medicationFrequencies", {params}))

    }

}