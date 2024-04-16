import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { EMPTY_GUARDIAN, Guardian } from "../models"
import { Router } from '@angular/router';
import { v4 as uuid } from 'uuid'
import { AuthService } from '../service/authenticate.service';
import { TokenStorageService } from '../service/token.storage.service';

@Component({
  selector: 'app-guardian',
  templateUrl: './guardian.component.html',
  styleUrl: './guardian.component.css'
})
export class GuardianComponent implements OnInit {

  private fb = inject(FormBuilder)
  signUpForm!: FormGroup
  hide: Boolean = true;


  isSuccessful = false;
  isSignUpfailed = false;
  isEdit = false

  guardian: Guardian = EMPTY_GUARDIAN

  private authSvc = inject(AuthService)
  private router = inject(Router)
  private tokenStorage = inject(TokenStorageService)
  guardianId = this.tokenStorage.getGuardianIdFromStorage()


  ngOnInit(): void {
    this.signUpForm = this.createsignUpForm()

    if (this.guardianId != undefined) {
      this.authSvc.getGuardianById(this.guardianId)
        .then(
          value => {
            this.guardian = value
            this.isEdit = true
            console.info("guardian value from back end: ", value)
            return this.isEdit
          }
        ).
        then(
          value => {
            this.signUpForm = this.editGuardianForm(this.guardian)

          }
        )
        .catch(err => {
          alert(`FAILED TO LOAD GUARDIAN: ${err}`)
        })
    }

   

  }


  createsignUpForm(): FormGroup {
    return this.fb.group({

      firstName: this.fb.control<string>('', [Validators.required, Validators.minLength(3)]),
      lastName: this.fb.control<string>('', [Validators.required, Validators.minLength(3)]),
      email: this.fb.control<string>('', [Validators.required, Validators.email]),
      phoneNo: this.fb.control<string>('', [Validators.required, Validators.pattern("(8|9)[0-9]{7}")]),
      password: this.fb.control<string>('', [Validators.required, Validators.minLength(8)])
    })
  }

  editGuardianForm(guardian: Guardian): FormGroup {
    return this.fb.group({

      firstName: this.fb.control<string>(guardian.firstName, [Validators.required, Validators.minLength(3)]),
      lastName: this.fb.control<string>(guardian.lastName, [Validators.required, Validators.minLength(3)]),
      email: this.fb.control({value: guardian.email, disabled:true}, [Validators.required, Validators.email]),
      phoneNo: this.fb.control<string>(guardian.phoneNo, [Validators.required, Validators.pattern("(8|9)[0-9]{7}")]),
      password: this.fb.control<string>('', [Validators.required, Validators.minLength(8)])
    })
  }


  signUp() {

    const signupInput = this.signUpForm.value
    console.log("Login Input: ", signupInput)

    const id: string = uuid().substring(0, 8)

    const guardian: Guardian = {

      guardianId: id,
      firstName: signupInput['firstName'],
      lastName: signupInput['lastName'],
      email: signupInput['email'],
      phoneNo: signupInput['phoneNo'],
      password: signupInput['password'],
      patients: [],
      role: "ROLE_USER"

    }

    this.authSvc.register(guardian)
      .then(resp => {
        console.info("response: ", resp)
        this.router.navigate(['/'])
        this.isSuccessful = true;
        this.isSignUpfailed = false;
      }).catch(err => {
        this.isSignUpfailed = true
        alert(`ACCOUNT FAILED TO CREATE: ${err.error.message}`)
      })


  }

  saveChanges(){
    const signupInput = this.signUpForm.value
    console.info("Form input value = ", signupInput)

    const guardian: Guardian = {

      guardianId: this.guardianId,
      firstName: signupInput['firstName'],
      lastName: signupInput['lastName'],
      email: this.guardian.email,
      phoneNo: signupInput['phoneNo'],
      password: signupInput['password'],
      patients: [],
      role: "ROLE_USER"

    }

    this.authSvc.editGuardianDetails(guardian)
    .then(resp => {
      alert(resp.message)
      console.info("response: ", resp)
      this.signUpForm = this.createsignUpForm()
      this.router.navigate(['/patients'])
    }).catch(err => {
      alert(`Error: ${err.error.message}`)
    })

    
  }


}
