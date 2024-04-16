import { Injectable } from "@angular/core";

const TOKEN_KEY = 'auth-token'
const ROLE_KEY = 'auth-role'
const ORIGINAL_TOKEN_KEY = 'cache-token'
const ORIGINAL_ROLE_KEY = 'cache-role'
const FCM_TOKEN = 'fcm-token'
@Injectable()
export class TokenStorageService {

  signOut(): void {
    window.sessionStorage.clear()
  }

  public saveToken(token: string): void {
    window.sessionStorage.removeItem(TOKEN_KEY);
    window.sessionStorage.setItem(TOKEN_KEY, token);
  }

  public getToken() {
    return sessionStorage.getItem(TOKEN_KEY);
  }


  public saveAuthority(role: string){
    window.sessionStorage.removeItem(ROLE_KEY)
    window.sessionStorage.setItem(ROLE_KEY, role)
  }

  
  public getAuthority(): string | undefined {
    const role = window.sessionStorage.getItem(ROLE_KEY)
    if (!!role) {
      return role
    } else {
      return undefined
    }
  }


  public cacheToken(newToken: string){
    const cacheToken = this.getToken()
    if (cacheToken!= undefined){
      window.sessionStorage.setItem(ORIGINAL_TOKEN_KEY, cacheToken)
      this.saveToken(newToken)
    }
  }

  public setOriginalToken(){
    //  assuming come back from individual patient
    const cacheToken = window.sessionStorage.getItem(ORIGINAL_TOKEN_KEY)
    const cacheRole = window.sessionStorage.getItem(ORIGINAL_ROLE_KEY)

      if(cacheToken != undefined && cacheRole!=undefined){
        this.saveToken(cacheToken)
        this.saveAuthority(cacheRole)

      } else{
        console.info("Original token and role")
      }
  }

  public cacheAuthority(newRole: string){
    const cacheRole = this.getAuthority()
    if (cacheRole!= undefined){
      window.sessionStorage.setItem(ORIGINAL_ROLE_KEY, cacheRole)
      this.saveAuthority(newRole)
    }
  }

  public saveGuardianIdToStorage(guardianId: string) {
    window.sessionStorage.removeItem("guardianId")
    window.sessionStorage.setItem("guardianId", guardianId)
  }

  public savePatientIdToStorage(patientId: string) {
    window.sessionStorage.removeItem("patientId")
    window.sessionStorage.setItem("patientId", patientId)
  }

  public getGuardianIdFromStorage():any {
    const id = window.sessionStorage.getItem("guardianId")
    if (!!id) {
      return id
    } else {
      return undefined
    }
  }

  public getPatientIdFromStorage(): any{
    const id = window.sessionStorage.getItem("patientId")
    if (!!id) {
      return id
    } else {
      return undefined
    }
  }


  public saveFCMToken(token: string): void {
    window.sessionStorage.removeItem(FCM_TOKEN);
    window.sessionStorage.setItem(FCM_TOKEN, token);
  }

  public getFCMToken() {
    return sessionStorage.getItem(FCM_TOKEN);
  }
  

  public savePatientNameToStorage(patientName: string): void {
    window.sessionStorage.removeItem("patientName");
    window.sessionStorage.setItem("patientName", patientName);
  }

  public getPatientNameFromStorage():any{
    const id = window.sessionStorage.getItem("patientName")
    if (!!id) {
      return id
    } else {
      return undefined
    }
  }

  



}