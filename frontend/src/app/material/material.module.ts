import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatIconModule} from '@angular/material/icon';
import {MatButtonModule} from '@angular/material/button';
import {MatSelectModule} from '@angular/material/select';
import {MatRadioModule} from '@angular/material/radio';
import {MatCardModule} from '@angular/material/card';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatTableModule} from '@angular/material/table';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {MatMenuModule} from '@angular/material/menu';
import {MatSlideToggleModule} from '@angular/material/slide-toggle';
import {MatExpansionModule, MatAccordion} from '@angular/material/expansion';
import {MatRippleModule} from '@angular/material/core';
import {MatDividerModule} from '@angular/material/divider';
import {MatListModule} from '@angular/material/list';
import {MatTabsModule} from '@angular/material/tabs';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {MatTooltipModule} from '@angular/material/tooltip';
import {MatChipsModule, MAT_CHIPS_DEFAULT_OPTIONS } from '@angular/material/chips';
import {MatDialogModule} from '@angular/material/dialog';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatPaginatorModule} from '@angular/material/paginator';

import { COMMA, SPACE } from '@angular/cdk/keycodes';

@NgModule({
  declarations: [],
  imports: [
            CommonModule, MatFormFieldModule, MatInputModule, MatIconModule,
            MatButtonModule, MatSelectModule, MatRadioModule, MatCardModule,
            MatDatepickerModule, MatTableModule, MatCheckboxModule, MatMenuModule,
            MatSlideToggleModule, MatExpansionModule,
            MatAccordion, MatRippleModule, MatDividerModule, MatListModule,
            MatTabsModule, MatAutocompleteModule, MatTooltipModule,MatChipsModule,
            MatDialogModule, MatSnackBarModule, MatToolbarModule, MatSidenavModule,
            MatPaginatorModule
  ],
  exports:[ 
    CommonModule, MatFormFieldModule, MatInputModule, MatIconModule,
    MatButtonModule, MatSelectModule, MatRadioModule, MatCardModule,
    MatDatepickerModule, MatTableModule, MatCheckboxModule, MatMenuModule,
    MatSlideToggleModule,MatExpansionModule,
    MatAccordion, MatRippleModule, MatDividerModule, MatListModule,
    MatTabsModule, MatAutocompleteModule, MatTooltipModule,MatChipsModule,
    MatDialogModule, MatSnackBarModule, MatToolbarModule, MatSidenavModule,
    MatPaginatorModule
  ],
  providers: [
    {
      provide: MAT_CHIPS_DEFAULT_OPTIONS,
      useValue: {
        separatorKeyCodes: [COMMA, SPACE]
      }
    }
  ]
})
export class MaterialModule { }
