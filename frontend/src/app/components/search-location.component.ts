import { AfterViewInit, Component, OnInit, ViewChild, inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { LocationService } from '../service/location.service';
import { Distance } from '../models';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';

@Component({
  selector: 'app-search-location',
  templateUrl: './search-location.component.html',
  styleUrl: './search-location.component.css'
})
export class SearchLocationComponent implements OnInit {


  private fb = inject(FormBuilder)
  locationSearchForm!: FormGroup

  distanceList: Distance[] = []
  imgUrl!: SafeResourceUrl;
  hasSearched = false
  showMap = false


  private locationSvc = inject(LocationService)
  private sanitizer = inject(DomSanitizer)


  ngOnInit(): void {

    console.info("map on init")
    this.locationSearchForm = this.createSearchLocationForm()
  }



  createSearchLocationForm(): FormGroup {
    return this.fb.group({

      postalCode: this.fb.control<string>('', [Validators.required, Validators.pattern('[0-9]{6}')]),
      amenities: this.fb.control<string>('', [Validators.required]),
      distance: this.fb.control<number>(0, [Validators.required])

    })

  }


  distanceFromLocation = [
    { value: 0.5, displayValue: "0.5km" },
    { value: 1.5, displayValue: "1.5km" },
    { value: 2.5, displayValue: "2.5km" },
    { value: 5, displayValue: "5km" },
    { value: 10, displayValue: "10km" }

  ];

  amentitiesCategory = [
    { value: "kindergartens", displayValue: "Kindergartens" },
    { value: "eldercare", displayValue: "Elder Care Centres" },
    { value: "disability", displayValue: "Disability Care Centres" },
    { value: "dfc_gtp", displayValue: "Dementia Friendly Go-To-Points" },
    { value: "registered_pharmacy", displayValue: "Registered Pharmacies" },


  ];

  tableHeaders = ['location', 'address', 'postalCode', 'distance', 'map'];
  searchAddress: string =""
  search() {
    const input = this.locationSearchForm.value
    console.info("form input: ", input)
    this.showMap=false
    this.locationSvc.checkPostalCode(input['postalCode'])
      .then(
        value => {
          console.info("Value from backend", value)
          this.hasSearched= true
          this.locationSvc.getDistances(input['postalCode'], input['amenities'], input['distance'])
            .then(
              value => {
                console.info("Value from backend", value)
                if (value != null) 
                  { this.distanceList = value
                    this.searchAddress = this.distanceList[0].startAddress
                  }})
            .catch(err => {
              alert(`FAILED TO GET DISTANCES: ${err.error.message}`)
            })
        }
      ).catch(err => {
        alert(`Error: ${err.error.message}`)
      })
  }

  distance!:Distance
  getMap(input: Distance) {

    this.locationSvc.getMap(input)
      .then(
        value => {
          console.info("Map controller value: ", value);
          this.distance = input
          // this.imgUrl = value.img;
          this.showMap =true
          this.imgUrl = this.sanitizer.bypassSecurityTrustUrl('data:image/jpeg;base64,' + value.img)
          return this.imgUrl
          
        }
      ).catch(
        err => alert(`error: ${err}`)
      )

  }



}
