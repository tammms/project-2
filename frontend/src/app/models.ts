export interface Guardian {

    guardianId: string
    firstName: string
    lastName: string
    email: string
    phoneNo: string
    password: string
    patients: Patient[],
    role: string

}

export const EMPTY_GUARDIAN: Guardian= {

    guardianId: "",
    firstName: "",
    lastName: "",
    email: "",
    phoneNo: "",
    password: "",
    patients: [],
    role: ""

}

export interface Patient {

    patientId: string
    firstName: string
    lastName: string
    gender: string
    birthDate: string
    phoneNo: string
    age: number
    guardians: string[]
    medications: Medication[]
    medicalNotes: String[]

}

export interface PatientDetails {

    age: number
    guardianId: string
    patientBirthDate: string
    patientFirstName: string
    patientGender: string
    patientId: string
    patientLastName: string
    patientPhoneNo: string
    patientRelation: string
    role: string
}


export const EMPTY_PATIENT_DETAILS: PatientDetails = {

    age: 0,
    guardianId: "",
    patientBirthDate: "",
    patientFirstName: "",
    patientGender: "",
    patientId: "",
    patientLastName: "",
    patientPhoneNo: "",
    patientRelation: "",
    role: "",
}

export interface Medication {

    name: string
    medicationType: string
    dosage: string
    frequency: string[]
    frequencyUnits: string
    notes: string

}

export interface MedicationSlice {
    medication: Medication[],
    healthConditions: string[],
    notes: string
}


export interface MedicationDetails {
    name: string
    medicationType: string
    dosage: string
    frequency: string[]
    frequencyUnits: string
    notes: string
    uses: string
    sideEffect: string
    imageUrl: string

}

export interface MedicationRecord {

    patientId: string
    healthConditionList: string[]
    medicationList: MedicationDetails[]
    notes: string
}

export const EMPTY_MEDICATION: Medication = {

    name: "",
    medicationType: "",
    dosage: "",
    frequency: [],
    frequencyUnits: "",
    notes: ""
}

export interface Conditions {
    conditions: string[]
    otherConditions: string
    notes: string
}

export const EMPTY_HEALTHCONDITIONS: Conditions = {
    conditions: [],
    otherConditions: "",
    notes: ""
}

export interface ScheduleTiming {
    beforeBreakfast: string
    afterBreakfast: string
    beforeLunch: string
    afterLunch: string
    beforeDinner: string
    afterDinner: string
}

export const DEFAULT_REMINDER_SCHEDULE: ScheduleTiming = {

    beforeBreakfast: "08:00",
    afterBreakfast: "10:00",
    beforeLunch: "12:00",
    afterLunch: "14:00",
    beforeDinner: "18:00",
    afterDinner: "20:00"

}



export interface MedReminderRequest {


    guardianId: string
    patientId: string
    patientFirstName: string
    scheduleTimings: ScheduleTiming
    reminderFrequencies: string[]
    hasWeekly: boolean
    weeklyStart: number

}


export interface eventDetails {

    guardianId: string
    patientId: string
    eventId: string
    reminderType: string
    patientName: string
    isValid: boolean
    summary: string
    location: string
    description: string
    startDate: string
    startTime: string
    endDate: string
    endTime: string
    isRepeat: boolean
    frequencyUnits: string
    frequency: number
    sendEmail: boolean
    attendees: string[];

}


export interface dailyDetails {

    guardianId: string
    patientId: string
    eventId: string
    reminderType: string
    isValid: boolean
    patientName: string
    description: string
    startTime: string[]
}

export interface NotificationSlice {

    event: eventDetails[],
    daily: dailyDetails[]
}

export interface Distance{

    distance: number
    startAddress: string
    endAddress: string
    endName: string
    endPostalCode: string
    startPostalCode: string
    latEnd: number
    lngEnd: number
    latStart: number
    lngStart: number
}