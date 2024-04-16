import { HTTP_INTERCEPTORS, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { TokenStorageService } from "./token.storage.service";
import { Observable } from "rxjs";

const TOKEN_HEADER_KEY = 'Authorization'
@Injectable()
export class AuthInterceptor implements HttpInterceptor {

    private token = inject(TokenStorageService)

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        let authReq = req
        const token = this.token.getToken();
        console.log("Token: ", token)
        if (token != null) {
            authReq = req.clone({ headers: req.headers.set(TOKEN_HEADER_KEY, "Bearer " + token) })

        }
        return next.handle(authReq)
    }

}

export const authInterceptorProviders = [
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true }
];