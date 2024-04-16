package vttp.project.backend.repository;

import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import vttp.project.backend.model.Medication;
import vttp.project.backend.model.MedicationRecord;
import vttp.project.backend.service.Utils;

@Repository
public class MedicationRepository {

    @Autowired
    MongoTemplate template;

    /*
     * db.medicine.find(
     * {"Medicine Name":{$ne:null}},
     * {"Medicine Name":1},
     * {$sort:{"Medicine Name":1}}
     * );
     */
    public List<String> getAllMedicineNames() {

        Query query = Query.query(Criteria.where("Medicine Name").ne(null));

        query.fields()
                .include("Medicine Name");

        List<Document> results = template.find(query, Document.class, "medicine");
        List<String> medicationList = new LinkedList<>();

        for (Document d : results) {
            medicationList.add(d.getString("Medicine Name"));
        }
        return medicationList;
    }

    public boolean addNewMedicationRecord(String payload) {

        // Payload from frontend:
        // {"patientId":"5929db78","medications":[{"name":"Remicade
        // Injection","medicationType":"injection","dosage":"2ml","frequency":4,"frequencyUnits":"daily","notes":"some
        // notes"},{"name":"Remicade
        // Injection","medicationType":"injection","dosage":"2ml","frequency":4,"frequencyUnits":"daily","notes":"some
        // notes"}],"conditions":["cardiac disease","asthma"]}

        // {"patientId":"zxcv0995","medications":[{"name":"Wysolone 10 Tablet
        // DT","medicationType":"ointment","dosage":"-","frequency":["After
        // Breakfast"],"frequencyUnits":"daily","notes":""},{"name":"Glycomet-GP 1
        // Tablet PR","medicationType":"tablet","dosage":"3
        // tablets","frequency":["Before Breakfast","After
        // Lunch"],"frequencyUnits":"daily","notes":""}],"conditions":["diabetes","epilepsy"],"notes":""}

        MedicationRecord medRec = new MedicationRecord();
        System.out.println("\n\nPayload from frontend: " + payload);

        JsonReader reader = Json.createReader(new StringReader(payload));
        JsonObject obj = reader.readObject();

        List<Medication> medicationList = new LinkedList<>();
        JsonArray medicationArr = obj.getJsonArray("medications");

        for (int i = 0; i < medicationArr.size(); i++) {
            JsonObject medObj = medicationArr.getJsonObject(i);

            Medication med = new Medication();
            med.setName(medObj.getString("name"));
            med.setDosage(medObj.getString("dosage"));
            med.setMedicationType(medObj.getString("medicationType"));
            med.setFrequencyUnits(medObj.getString("frequencyUnits"));
            med.setNotes(medObj.getString("notes"));

            JsonArray frequencyArr = medObj.getJsonArray("frequency");
            List<String> frequencies = new LinkedList<>();
            for (int j = 0; j < frequencyArr.size(); j++) {
                String freq = frequencyArr.getString(j);
                frequencies.add(freq);
            }

            med.setFrequency(frequencies);
            medicationList.add(med);
        }

        List<String> conditions = new LinkedList<>();
        JsonArray conditionsArr = obj.getJsonArray("conditions");
        for (int i = 0; i < conditionsArr.size(); i++) {
            conditions.add(conditionsArr.getString(i));
        }

        medRec.setPatientId(obj.getString("patientId"));
        medRec.setMedicationList(medicationList);
        medRec.setHealthConditionList(conditions);
        medRec.setNotes(obj.getString("notes"));

        deleteMedicationRecord(obj.getString("patientId"));

        Boolean insertResult = template.insert(medRec) != null;

        return insertResult;
    }

    public boolean deleteMedicationRecord(String patientId) {

        Query query = Query.query(
                Criteria.where("patientId").is(patientId));

        return template.remove(query, "medicationRecord").getDeletedCount() > 0;
    }

    /*
     * db.medicationRecord.aggregate([
     * {$match: {patientId : "5929db78"}},
     * {$unwind:"$medicationList"},
     * {$lookup:{
     * from:'medicine',
     * foreignField: 'Medicine Name',
     * localField: 'medicationList.name',
     * as:"medicationList.details"
     * }},
     * {$project:{"medicationList.details._id":0,
     * "medicationList.details.Medicine Name":0,
     * "medicationList.details.Composition":0,
     * "medicationList.details.Manufacturer":0,
     * "medicationList.details.Excellent Review %":0,
     * "medicationList.details.Average Review %":0,
     * "medicationList.details.Poor Review %":0,}},
     * {$group: {
     * _id: "$_id",
     * patientId: { $first: "$patientId" },
     * healthConditionList: { $first: "$healthConditionList" },
     * medicationList: { $push: "$medicationList" }
     * }
     * }
     * 
     * ]);
     * 
     */

    public Optional<MedicationRecord> getNewMedicationRecord(String patientId) {

        Query query = Query.query(
                Criteria.where("patientId").is(patientId));
        query.fields().include("healthConditionList", "notes")
                .exclude("_id");

        Document healthDoc = template.findOne(query, Document.class, "medicationRecord");

        if (healthDoc == null) {
            return Optional.empty();
        } else {

            List<String> healthConditions = healthDoc.getList("healthConditionList", String.class);

            String notes = healthDoc.getString("notes");

            MatchOperation matchPatientId = Aggregation.match(Criteria.where("patientId").is(patientId));

            ProjectionOperation projectFields = Aggregation.project("_id", "medicationList");

            AggregationOperation unwindMedicationList = Aggregation.unwind("medicationList");
            LookupOperation lookupMedicine = Aggregation.lookup("medicine",
                    "medicationList.name",
                    "Medicine Name",
                    "medicationList.details");

            AggregationOperation unwindMedicationDetails = Aggregation.unwind("medicationList.details");

            GroupOperation groupRecords = Aggregation.group("_id")
                    .push("medicationList").as("medicationList");

            Aggregation pipeline = Aggregation.newAggregation(matchPatientId,
                    projectFields,
                    unwindMedicationList,
                    lookupMedicine,
                    unwindMedicationDetails,
                    groupRecords,
                    unwindMedicationList);

            AggregationResults<Document> results = template.aggregate(pipeline, "medicationRecord", Document.class);

            MedicationRecord medicalRecord = new MedicationRecord();
            medicalRecord.setPatientId(patientId);
            medicalRecord.setNotes(notes);
            medicalRecord.setHealthConditionList(healthConditions);
            List<Medication> medicationList = new LinkedList<>();

            for (Document d : results) {
                // System.out.println("\n\naggregation results:\n" + d);

                Document medObj = d.get("medicationList", Document.class);
                Medication med = new Medication();
                med.setName(medObj.getString("name"));
                med.setDosage(medObj.getString("dosage"));
                med.setMedicationType(medObj.getString("medicationType"));
                med.setFrequencyUnits(medObj.getString("frequencyUnits"));
                med.setNotes(medObj.getString("notes"));

                List<String> frequencies = medObj.getList("frequency", String.class);
                med.setFrequency(frequencies);

                Document detailsObj = medObj.get("details", Document.class);
                med.setUses(detailsObj.getString("Uses"));
                med.setSideEffect(detailsObj.getString("Side_effects"));
                med.setImageUrl(detailsObj.getString("Image URL"));

                medicationList.add(med);

                // System.out.println("\nMedicine: " + med.toString());

            }

            medicalRecord.setMedicationList(medicationList);
            // System.out.println("\nMedical Record: " + medicalRecord.toString());

            return Optional.of(medicalRecord);

        }

    }

    // Get medication frequencies
    /*
     * db.medicationRecord.aggregate([
     * {$match : {"patientId" : "zxcv0995"}},
     * {$unwind : "$medicationList"},
     * {$unwind : "$medicationList.frequency"},
     * {$group: {
     * _id:"$medicationList.frequency",
     * count:{$sum:1}
     * }}
     * ]);
     */

    public List<String> getMedicationFrequencies(String patientId) {

        MatchOperation matchPatientId = Aggregation.match(Criteria.where("patientId").is(patientId));
        AggregationOperation unwindMedicationList = Aggregation.unwind("medicationList");
        AggregationOperation unwindFreqeuency = Aggregation.unwind("medicationList.frequency");
        GroupOperation groupByFrequency = Aggregation.group("medicationList.frequency")
                .count().as("count");

        Aggregation pipeline = Aggregation.newAggregation(matchPatientId, unwindMedicationList,
                unwindFreqeuency, groupByFrequency);

        List<Document> results = template.aggregate(pipeline, "medicationRecord", Document.class).getMappedResults();

        List<String> frequencyList = new LinkedList<>();
        if (results.size() > 0) {
            for (Document doc : results) {
                // System.out.println("\nDocument: " + doc);
                frequencyList.add(doc.getString("_id"));
            }
            frequencyList = Utils.sortFrequencies(frequencyList);
        }

        System.out.println("\nFrequencies " + frequencyList.toString());

        return frequencyList;
    }

    /*
     * db.medicationRecord.find({
     * "patientId":{$regex:"zxcv0995"},
     * "medicationList.frequencyUnits": "weekly"
     * 
     * })
     */

    // public boolean containsWeekly(String patientId) {
    // Query query = new Query();

    // query.addCriteria(Criteria.where("patientId").is(patientId));
    // query.addCriteria(Criteria.where("medicationList.frequencyUnits").is("weekly"));

    // Boolean result = template.find(query, Document.class,
    // "medicationRecord").size() > 0;
    // // System.out.println("\nContains weekly: " + result);

    // return result;

    // }

    /*
     * db.medicationRecord.aggregate([
     * {$match: {patientId : "zxcv0995"}},
     * {$unwind:"$medicationList"},
     * {$unwind:"$medicationList.frequency"},
     * {$match: {"medicationList.frequencyUnits" : "daily"}},
     * ]);
     */

    public String frequencyUnitsStatus(String patientId) {
        MatchOperation matchPatientId = Aggregation.match(Criteria.where("patientId").is(patientId));
        AggregationOperation unwindMedicationList = Aggregation.unwind("medicationList");
        AggregationOperation unwindFreqeuency = Aggregation.unwind("medicationList.frequency");

        MatchOperation matchDailyUnits = Aggregation.match(Criteria.where("medicationList.frequencyUnits").is("daily"));
        MatchOperation matchWeeklyUnits = Aggregation
                .match(Criteria.where("medicationList.frequencyUnits").is("weekly"));

        Aggregation pipelineDaily = Aggregation.newAggregation(matchPatientId, unwindMedicationList,
                unwindFreqeuency, matchDailyUnits);
        Integer dailyResults = template.aggregate(pipelineDaily, "medicationRecord", Document.class).getMappedResults()
                .size();

        Aggregation pipelineWeekly = Aggregation.newAggregation(matchPatientId, unwindMedicationList,
                unwindFreqeuency, matchWeeklyUnits);
        Integer weeklyResults = template.aggregate(pipelineWeekly, "medicationRecord", Document.class)
                .getMappedResults().size();

        if (dailyResults > 0 && weeklyResults > 0) {
            return "both";
        }

        if (dailyResults > 0 && weeklyResults <= 0) {
            return "daily";
        }

        if (dailyResults <= 0 && weeklyResults > 0) {
            return "weekly";
        }

        return "daily";
    }

    // db.medicationRecord.aggregate([
    // {$match: {patientId : "zxcv0995"}},
    // {$unwind:"$medicationList"},
    // {$unwind:"$medicationList.frequency"},
    // {$group:{
    // _id:{"frequencyUnits":"$medicationList.frequencyUnits"},
    // frequency: {$push:"$medicationList.frequency"}
    // }},
    // {$match: {"_id.frequencyUnits" : "weekly"}},
    // ]);

    public List<String> getWeeklyFrequencyUnits(String patientId) {

        MatchOperation matchPatientId = Aggregation.match(Criteria.where("patientId").is(patientId));
        AggregationOperation unwindMedicationList = Aggregation.unwind("medicationList");
        AggregationOperation unwindFreqeuency = Aggregation.unwind("medicationList.frequency");

        GroupOperation groupByFrequency = Aggregation.group("medicationList.frequencyUnits")
                .push("medicationList.frequency").as("frequency");

        MatchOperation matchWeeklyUnits = Aggregation.match(Criteria.where("_id").is("weekly"));

        Aggregation pipeline = Aggregation.newAggregation(matchPatientId, unwindMedicationList,
                unwindFreqeuency, groupByFrequency, matchWeeklyUnits);

        List<Document> results = template.aggregate(pipeline, "medicationRecord", Document.class).getMappedResults();

        // System.out.println("\nWeekly frequency units: " + results.get(0).toJson());

        JsonReader reader = Json.createReader(new StringReader(results.get(0).toJson()));
        JsonObject object = reader.readObject();

        JsonArray freqArr = object.getJsonArray("frequency");
        List<String> frequencyReminderList = new LinkedList<>();
        for (int i = 0; i < freqArr.size(); i++) {
            frequencyReminderList.add(freqArr.getString(i));
        }

        // System.out.println("\ngetWeeklyFrequencyUnits from repo: " + results.size());
        // System.out.println("\nWeekly frequencies from repo: " + Utils.sortFrequencies(frequencyReminderList).toString());

        return Utils.sortFrequencies(frequencyReminderList);

    }

    // db.medicationRecord.aggregate([
    // {$match: {patientId : "zxcv0995"}},
    // {$unwind:"$medicationList"},
    // {$unwind:"$medicationList.frequency"},
    // {$group:{
    // _id:{"frequency":"$medicationList.frequency"},
    // frequencyUnits: {$push:"$medicationList.frequencyUnits"},
    // }},
    // {$match: {"frequencyUnits" : "weekly"}},
    // ]);

    // db.medicationRecord.aggregate([
    // {$match: {patientId : "zxcv0995"}},
    // {$unwind:"$medicationList"},
    // {$unwind:"$medicationList.frequency"},
    // {$group:{

    // _id:{"frequencyUnits":"$medicationList.frequencyUnits"},

    // frequency: {$push:"$medicationList.frequency"},

    // }},
    // ]);

    public List<String> getDailyFrequencies(String patientId) {
        // should return as optional
        MatchOperation matchPatientId = Aggregation.match(Criteria.where("patientId").is(patientId));
        AggregationOperation unwindMedicationList = Aggregation.unwind("medicationList");
        AggregationOperation unwindFreqeuency = Aggregation.unwind("medicationList.frequency");

        GroupOperation groupByFrequency = Aggregation.group("medicationList.frequencyUnits")
                .push("medicationList.frequency").as("frequency");

        MatchOperation matchWeeklyUnits = Aggregation
                .match(Criteria.where("medicationList.frequencyUnits").is("daily"));
        Aggregation pipeline = Aggregation.newAggregation(matchPatientId, unwindMedicationList,
                unwindFreqeuency, matchWeeklyUnits, groupByFrequency
                );

        List<Document> results = template.aggregate(pipeline, "medicationRecord", Document.class).getMappedResults();

        //  {"_id": "weekly", "frequency": ["After Breakfast", "After Lunch", "After Dinner"]}
        JsonReader reader = Json.createReader(new StringReader(results.get(0).toJson()));
        JsonObject object = reader.readObject();

        JsonArray freqArr = object.getJsonArray("frequency");
        List<String> frequencyReminderList = new LinkedList<>();
        for (int i = 0; i < freqArr.size(); i++) {
            frequencyReminderList.add(freqArr.getString(i));
        }
        
        // System.out.println("\ngetDailyFrequencies from repo: " + results.size());
        // System.out.println("\nDaily frequencies from repo: " + Utils.sortFrequencies(frequencyReminderList).toString());


        return Utils.sortFrequencies(frequencyReminderList);
    }

}
