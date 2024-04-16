import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { PatientRegistrationService } from '../service/patient.register.service';
import { AuthService } from '../service/authenticate.service';
import { TokenStorageService } from '../service/token.storage.service';

@Component({
  selector: 'app-sign-in',
  templateUrl: './sign-in.component.html',
  styleUrl: './sign-in.component.css'
})
export class SignInComponent implements OnInit {

  private fb = inject(FormBuilder)
  signInForm!: FormGroup
  hide: Boolean = true;

  private patientSvc = inject(PatientRegistrationService)
  private router = inject(Router)

  private authService = inject(AuthService)
  private tokenStorage = inject(TokenStorageService)

  isLoggedIn = false;
  isLoginFailed = false;

  guardianId: String | undefined

  token!:string


  ngOnInit(): void {
    console.info("sign in on init")


    this.signInForm = this.createSignInForm()
    this.tokenStorage.signOut()

    // from security
    this.isLoggedIn = !!this.tokenStorage.getToken()
    if (this.isLoggedIn) {
      this.guardianId = this.tokenStorage.getGuardianIdFromStorage()
    } 

    if (this.tokenStorage.getToken()) {
      this.isLoggedIn = true;
    }



  }


  createSignInForm(): FormGroup {
    return this.fb.group({
      email: this.fb.control<string>('', [Validators.required, Validators.email]),
      password: this.fb.control<string>('', [Validators.required]),
    })
  }



  login() {

    const loginInput = this.signInForm.value
    console.log("Login Input: ", loginInput)

    const email: string = loginInput['email']
    const password: string = loginInput['password']

    this.authService.login(email, password)
      .then(data => {
        console.log("Data from login obs: ", data)
        console.log("Guardian: ", data.token)

        this.tokenStorage.saveToken(data.token);
        this.tokenStorage.saveAuthority(data.authority)
        this.tokenStorage.saveGuardianIdToStorage(data.guardianId)

        this.isLoginFailed = false;
        this.isLoggedIn = true;
        // this.roles = this.tokenStorage.getUser().roles;
        this.router.navigate(['/patients'])
      })
      .catch(err => {
        alert(`Sign In Failed: ${err.error.message}`)
      })


  }

}
