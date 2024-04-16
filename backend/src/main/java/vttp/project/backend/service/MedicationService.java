package vttp.project.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import vttp.project.backend.model.MedicationRecord;
import vttp.project.backend.repository.MedicationRepository;

@Service
public class MedicationService {

    @Autowired
    MedicationRepository medRepo;

    public List<String> getAllMedicineNames(){
        return medRepo.getAllMedicineNames();
    }

    public boolean addNewMedicationRecord(String payload){
        return medRepo.addNewMedicationRecord(payload);
    }
    
    public Optional<MedicationRecord> getNewMedicationRecord(String patientId) {
       return medRepo.getNewMedicationRecord(patientId);
    }

    public boolean deleteMedicationRecord(String patientId) {
        return medRepo.deleteMedicationRecord(patientId);
    }

    public List<String> getMedicationFrequencies(String patientId) {
        return medRepo.getMedicationFrequencies(patientId);
    }

    // public boolean containsWeekly(String patientId) {
    //     return medRepo.containsWeekly(patientId);

    // }

    public String frequencyUnitsStatus(String patientId) {
        return medRepo.frequencyUnitsStatus(patientId);
    }


    
    
}
