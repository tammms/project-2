import { Injectable } from "@angular/core";
import { ComponentStore } from "@ngrx/component-store";
import { Medication, MedicationDetails, MedicationSlice } from "../models";
import { Observable, Subscription } from "rxjs";

const INIT_STORE: MedicationSlice={
    medication:[],
    healthConditions: [],
    notes: ""
    
}

@Injectable()
export class MedicationIntakeStore extends ComponentStore<MedicationSlice>{

    constructor(){super(INIT_STORE)}


    readonly addMedication = this.updater<Medication>(
        (slice: MedicationSlice, med: Medication)=>{
            return{
                medication:[...slice.medication, med],
                healthConditions: slice.healthConditions,
                notes: slice.notes
            }
        }
    )

   

    readonly deleteMedication = this.updater<Medication>(
        (slice: MedicationSlice, med: Medication)=>{
            return{
                medication: slice.medication.filter(m => med != m),
                healthConditions: slice.healthConditions,
                notes: slice.notes

            }
        }

    )

    readonly clearMedicationStore = this.updater(
        (slice: MedicationSlice)=>{
            return{
                medication: [],
                healthConditions: [],
                notes: "",
                medicationDetails: []

            }
        }

    )

    readonly medicationExist = (medicationName: string)=>{
        return this.select<boolean>(
            (slice: MedicationSlice)=>{
                return slice.medication.filter(med => medicationName == med.name).length>0
            }
        )
    }

    readonly updateMedication = this.updater<Medication>(
        (slice:MedicationSlice, med:Medication)=>{
            
            return{
                medication:[... slice.medication.filter(m => med.name != m.name), med],
                healthConditions: slice.healthConditions,
                notes: slice.notes
            }
        }
    )

    readonly getMedications = this.select<Medication[]>(
            (slice:MedicationSlice)=> slice.medication
    )
    


    

}