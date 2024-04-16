package vttp.project.backend.model;

import java.util.List;

public class Medication {

    private String name;
    private String medicationType;
    private String dosage;
    private List<String> frequency;
    private String frequencyUnits;
    private String notes;
    private String uses;
    private String sideEffect;
    private String imageUrl;

    public Medication(){}
    
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public String getMedicationType() {return medicationType;}
    public void setMedicationType(String medicationType) {this.medicationType = medicationType;}

    public String getDosage() {return dosage;}
    public void setDosage(String dosage) {this.dosage = dosage;}

    public List<String> getFrequency() {return frequency;}
    public void setFrequency(List<String> frequency) {this.frequency = frequency;}
    
    public String getFrequencyUnits() {return frequencyUnits;}
    public void setFrequencyUnits(String frequencyUnits) {this.frequencyUnits = frequencyUnits;}

    public String getNotes() {return notes;}
    public void setNotes(String notes) {this.notes = notes;}

    public String getUses() {return uses;}
    public void setUses(String uses) {this.uses = uses;}
    
    public String getSideEffect() {return sideEffect;}
    public void setSideEffect(String sideEffect) {this.sideEffect = sideEffect;}

    public String getImageUrl() {return imageUrl;}
    public void setImageUrl(String imageUrl) {this.imageUrl = imageUrl;}

    @Override
    public String toString() {
        return "Medication [name=" + name + ", medicationType=" + medicationType + ", dosage=" + dosage + ", frequency="
                + frequency + ", frequencyUnits=" + frequencyUnits + ", notes=" + notes + ", uses=" + uses
                + ", sideEffect=" + sideEffect + ", imageUrl=" + imageUrl + "]";
    }

    
    

    
}
