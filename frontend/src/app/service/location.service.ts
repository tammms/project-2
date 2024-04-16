import { HttpClient, HttpParams } from "@angular/common/http";
import { Injectable, inject } from "@angular/core";
import { lastValueFrom } from "rxjs";
import { Distance } from "../models";

@Injectable()
export class LocationService {

    private http = inject(HttpClient)

    checkPostalCode(postalCode: string):Promise<any> {

        const params = new HttpParams()
            .set("postalCode", postalCode)

        return lastValueFrom(this.http.get("/api/checkPostCode", {params}))
    }

    getDistances(postalCode: string, amenities: string, distance: number):Promise<Distance[]> {

        const params = new HttpParams()
            .set("postalCode", postalCode)
            .set("amenities", amenities)
            .set("distance", distance)


        return lastValueFrom(this.http.get<Distance[]>("/api/getDistances", {params}))
    }

    getMap(distance:any):Promise<any>{

        const params = new HttpParams()
        .set("distance", distance)
    
        return lastValueFrom(this.http.post<any>("/api/getMap", {distance}))


    }
}