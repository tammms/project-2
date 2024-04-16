package vttp.project.backend.service;

import java.io.StringReader;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.springframework.jdbc.support.rowset.SqlRowSet;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import vttp.project.backend.model.DailyNotification;
import vttp.project.backend.model.EventsNotification;
import vttp.project.backend.model.Guardian;
import vttp.project.backend.model.MapDistance;
import vttp.project.backend.model.MapLocation;
import vttp.project.backend.model.Medication;
import vttp.project.backend.model.MedicationRecord;
import vttp.project.backend.model.NotificationRequest;
import vttp.project.backend.model.Patient;
import vttp.project.backend.model.ScheduledTimings;

public class Utils {

    public static Date convertStringToDate(String dateString) {

        Date date = new Date(Long.valueOf(dateString));

        return date;
    }

    public static Guardian stringToGuardian(String payload) {

        // Payload from frontend:
        // {"guardianId":"","firstName":"freddy","lastName":"tan","email":"freddy@gmail.com","phoneNo":"90823743","password":"poiu1234","patientIds":[]}

        JsonReader reader = Json.createReader(new StringReader(payload));
        JsonObject object = reader.readObject();
        Guardian guardian = new Guardian();

        guardian.setGuardianId(object.getString("guardianId"));
        guardian.setFirstName(object.getString("firstName"));
        guardian.setLastName(object.getString("lastName"));
        guardian.setEmail(object.getString("email"));
        // guardian.setPhoneNo(object.getString("phoneNo"));
        guardian.setPassword(object.getString("password"));

        try {
            guardian.setPhoneNo(object.getString("phoneNo"));

        } catch (Exception e) {
            // TODO: handle exception
            Integer phone = object.getInt("phoneNo");
            guardian.setPhoneNo(phone.toString());
        }

        return guardian;

    }

    public static JsonObject guardianToJson(Guardian guardian) {

        return Json.createObjectBuilder()
                .add("guardianId", guardian.getGuardianId())
                .add("firstName", guardian.getFirstName())
                .add("lastName", guardian.getLastName())
                .add("email", guardian.getEmail())
                .add("phoneNo", guardian.getPhoneNo())
                .add("password", guardian.getPassword())
                .build();

    }

    public static Patient stringToPatient(String payload) {

        // Payload from frontend:
        // {"patientId":"","firstName":"peanutty","lastName":"tan","gender":"male","birthDate":"2024-02-29T16:00:00.000Z","phoneNo":"90204953","guardians":[],"medications":[],"medicalNotes":[]}

        JsonReader reader = Json.createReader(new StringReader(payload));
        JsonObject object = reader.readObject();
        Patient patient = new Patient();

        patient.setPatientId(object.getString("patientId"));
        patient.setFirstName(object.getString("firstName"));
        patient.setLastName(object.getString("lastName"));
        patient.setGender(object.getString("gender"));
        patient.setAge(object.getInt("age"));

        try {
            patient.setPhoneNo(object.getString("phoneNo"));

        } catch (Exception e) {
            // TODO: handle exception
            Integer phone = object.getInt("phoneNo");
            patient.setPhoneNo(phone.toString());
        }

        patient.setBirthDate(convertStringToDate(object.getString("birthDate")));

        // System.out.println("check:"+ patient.getBirthDate());

        return patient;
    }

    public static JsonObject relationsDataToJson(SqlRowSet rs) {

        System.out.println("Role from DB: " + rs.getString("role"));

        String role = rs.getString("role");

        if (role.contains("USERADMIN")) {
            role = "Admin";
        } else {
            role = "User";
        }

        return Json.createObjectBuilder()
                .add("guardianId", rs.getString("guardian_id"))
                .add("patientId", rs.getString("patient_id"))
                .add("patientFirstName", rs.getString("patient_first_name"))
                .add("patientLastName", rs.getString("patient_last_name"))
                .add("patientGender", rs.getString("gender"))
                .add("patientBirthDate", rs.getDate("birth_date").toString())
                .add("age", (int) rs.getDouble("age"))
                .add("patientPhoneNo", rs.getString("patient_phone_no"))
                .add("patientRelation", rs.getString("patient_relation"))
                .add("role", role)
                .build();

    }

    public static JsonObject medicationRecordToJson(MedicationRecord medicationRecord) {

        List<String> conditionsList = medicationRecord.getHealthConditionList();
        JsonArrayBuilder conditionArrBuilder = Json.createArrayBuilder();

        if (conditionsList.size() > 0) {
            for (String c : conditionsList) {
                conditionArrBuilder.add(c);
            }
        }

        List<Medication> medList = medicationRecord.getMedicationList();
        JsonArrayBuilder medArrBuilder = Json.createArrayBuilder();
        if (medList.size() > 0) {
            for (Medication med : medList) {

                JsonArrayBuilder freqArrBuilder = Json.createArrayBuilder();
                List<String> freq = med.getFrequency();
                for (String f : freq) {
                    freqArrBuilder.add(f);
                }

                JsonObject medObj = Json.createObjectBuilder()
                        .add("name", med.getName())
                        .add("medicationType", med.getMedicationType())
                        .add("dosage", med.getDosage())
                        .add("frequency", freqArrBuilder.build())
                        .add("frequencyUnits", med.getFrequencyUnits())
                        .add("notes", med.getNotes())
                        .add("uses", med.getUses())
                        .add("sideEffect", med.getSideEffect())
                        .add("imageUrl", med.getImageUrl())
                        .build();
                medArrBuilder.add(medObj);
            }
        }

        return Json.createObjectBuilder()
                .add("patientId", medicationRecord.getPatientId())
                .add("healthConditionList", conditionArrBuilder.build())
                .add("medicationList", medArrBuilder.build())
                .add("notes", medicationRecord.getNotes())
                .build();

    }

    public static List<String> sortFrequencies(List<String> inputFreqList) {

        List<String> order = Arrays.asList("beforeBreakfast", "afterBreakfast", "beforeLunch", "afterLunch",
                "beforeDinner", "afterDinner");

        List<String> sortedList = new LinkedList<>();
        for (String s : inputFreqList) {
            switch (s) {
                case "Before Breakfast":
                    sortedList.add("beforeBreakfast");
                    break;
                case "After Breakfast":
                    sortedList.add("afterBreakfast");
                    break;
                case "Before Lunch":
                    sortedList.add("beforeLunch");
                    break;
                case "After Lunch":
                    sortedList.add("afterLunch");
                    break;
                case "Before Dinner":
                    sortedList.add("beforeDinner");
                    break;
                case "After Dinner":
                    sortedList.add("afterDinner");
                    break;
            }
        }

        Collections.sort(sortedList, Comparator.comparingInt(order::indexOf));

        // System.out.println("\nsorted list" + inputFreqList.toString());

        return sortedList;
    }

    public static NotificationRequest stringToNotificationRequest(String payload) {

        // Payload
        // {"request":{"guardianId":"qwer1235","patientId":"zxcv0995","patientFirstName":"belly","scheduleTimings":{"beforeBreakfast":"08:00","afterBreakfast":"10:00","beforeLunch":"12:00","afterLunch":"14:00","beforeDinner":"18:00","afterDinner":"20:00"},"reminderFrequencies":["beforeBreakfast","beforeDinner","beforeLunch"],"hasWeekly":true,"weeklyStart":1},"token":"fUMD9sjs657eA8uBe8FTd7:APA91bEf-qlYPpYvsowgChc0fF6cnPXQmc8kHPAVzuy6ng2w2glG3k_r4HGoQt3zqEGtRiVrWz_CPlMmfqXMcH93QADA4HZr2ulozVpwX0s2DhZHqpgG9S9VIYHTkFPJgVbZtzIVnNpT"}

        JsonReader reader = Json.createReader(new StringReader(payload));
        JsonObject object = reader.readObject();

        // JsonObject object = obj.getJsonObject("request");

        NotificationRequest request = new NotificationRequest();
        request.setGuardianId(object.getString("guardianId"));
        request.setPatientId(object.getString("patientId"));
        request.setPatientFirstName(object.getString("patientFirstName"));

        ScheduledTimings timing = new ScheduledTimings();
        JsonObject timingObj = object.getJsonObject("scheduleTimings");
        timing.setBeforeBreakfast(timingObj.getString("beforeBreakfast", "08:00"));
        timing.setAfterBreakfast(timingObj.getString("afterBreakfast", "10:00"));
        timing.setBeforeLunch(timingObj.getString("beforeLunch", "12:00"));
        timing.setAfterLunch(timingObj.getString("afterLunch", "14:00"));
        timing.setBeforeDinner(timingObj.getString("beforeDinner", "18:00"));
        timing.setAfterDinner(timingObj.getString("afterDinner", "20:00"));
        request.setScheduleTimings(timing);

        JsonArray freqArr = object.getJsonArray("reminderFrequencies");
        List<String> frequencyReminderList = new LinkedList<>();
        for (int i = 0; i < freqArr.size(); i++) {
            frequencyReminderList.add(freqArr.getString(i));
        }
        request.setReminderFrequencies(frequencyReminderList);
        request.setHasWeekly(object.getBoolean("hasWeekly"));
        request.setWeeklyStart(object.getInt("weeklyStart"));

        return request;
    }

    public static String getTokenFromString(String payload) {

        JsonReader reader = Json.createReader(new StringReader(payload));
        JsonObject object = reader.readObject();
        String token = object.getString("token");

        return token;

    }

    public static String convertFrequencyValue(String value) {

        switch (value) {
            case "beforeBreakfast":
                return "Before Breakfast";
            case "afterBreakfast":
                return "After Breakfast";
            case "beforeLunch":
                return "Before Lunch";
            case "afterLunch":
                return "After Lunch";
            case "beforeDinner":
                return "Before Dinner";
            case "afterDinner":
                return "After Dinner";
            default:
                return "";
        }
    }

    public static String getScheduledTiming(String frequencyUnit, ScheduledTimings inputScheduledTimings) {

        switch (frequencyUnit) {
            case "beforeBreakfast":
                return inputScheduledTimings.getBeforeBreakfast();
            case "afterBreakfast":
                return inputScheduledTimings.getAfterBreakfast();

            case "beforeLunch":
                return inputScheduledTimings.getBeforeLunch();

            case "afterLunch":
                return inputScheduledTimings.getAfterLunch();

            case "beforeDinner":
                return inputScheduledTimings.getBeforeDinner();

            case "afterDinner":
                return inputScheduledTimings.getAfterDinner();

            default:
                return "";
        }
    }

    public static String formatUpperCase(String input) {
        String firstLetter = input.substring(0, 1).toUpperCase();
        String remaining = input.substring(1);
        return ("%s%s").formatted(firstLetter, remaining);
    }

    public static String convertTimeStringToISO(String timeString) {
        ZoneId zoneId = ZoneId.of("Asia/Singapore");
        ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);

        String[] timeArr = timeString.split(":");
        Integer hours = Integer.parseInt(timeArr[0]);
        Integer minutes = Integer.parseInt(timeArr[1]);

        zonedDateTime = zonedDateTime.withHour(hours).withMinute(minutes).withSecond(0).withNano(0);

        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;

        String formatted = zonedDateTime.format(formatter);

        // "2024-04-12T13:00:00Z"
        // System.out.println("FOrmatted time: "+ formatted);
        // System.out.println("FOrmatted hour: "+ formatted.substring(11,13));
        // System.out.println("FOrmatted mins: "+ formatted.substring(14,16));
        return formatted;
    }

    public static String convertDateStringToISO(String dateString) {
        ZoneId zoneId = ZoneId.of("Asia/Singapore");
        ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);

        String[] startDateArr = dateString.split("-");
        Integer year = Integer.parseInt(startDateArr[0]);
        Integer month = Integer.parseInt(startDateArr[1]);
        Integer day = Integer.parseInt(startDateArr[2]);

        zonedDateTime.withYear(year).withMonth(month).withDayOfMonth(day);

        DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;

        String formatted = zonedDateTime.format(formatter);

        // "2024-04-13T13:04:13.140929400Z"
        // System.out.println("FOrmatted time: "+ formatted);
        // System.out.println("FOrmatted year: "+ formatted.substring(0,4));
        // System.out.println("FOrmatted month: "+ formatted.substring(5,7));
        // System.out.println("FOrmatted day: "+ formatted.substring(8,10));

        return formatted;
    }

    public static boolean checkEventDateValidity(String dateString, String timeString) {

        String[] startDateArr = dateString.split("-");
        Integer year = Integer.parseInt(startDateArr[0]);
        Integer month = Integer.parseInt(startDateArr[1]);
        Integer day = Integer.parseInt(startDateArr[2]);

        String[] timeArr = timeString.split(":");
        Integer hours = Integer.parseInt(timeArr[0]);
        Integer minutes = Integer.parseInt(timeArr[1]);

        // Create a ZonedDateTime object for the reminder time
        ZoneId zoneId = ZoneId.of("Asia/Singapore");
        ZonedDateTime reminderTime = ZonedDateTime.of(year, month, day, hours, minutes, 0, 0, zoneId);

        // Format the current time to ISO format
        ZonedDateTime currentTime = ZonedDateTime.now(zoneId);

        System.out.println("\nScheduled Time: " + reminderTime);
        System.out.println("Current Time: " + currentTime);

        return reminderTime.isBefore(currentTime);
    }

    public static EventsNotification stringToEventNotification(String payload) {

        // {"event":{"guardianId":"qwer1235","patientId":"zxcv0995","eventId":"bdebcd81","reminderType":"event","summary":"summary","location":"location","description":"description","startDate":"2024-04-13T08:14:37.108Z","startTime":"9:00","endDate":"2024-04-13T08:14:37.108Z","endTime":"10:00","isRepeat":false,"frequencyUnits":"DAILY","frequency":1,"duration":"forever","times":1,"sendEmail":false,"attendees":[]}}

        JsonReader reader = Json.createReader(new StringReader(payload));
        JsonObject object = reader.readObject();

        String startDate = object.getString("startDate").substring(0, 10);
        String endDate = object.getString("endDate").substring(0, 10);
        // System.out.println("\nDate string: "+ startDate);

        EventsNotification event = new EventsNotification();
        event.setGuardianId(object.getString("guardianId"));
        event.setPatientId(object.getString("patientId"));
        event.setEventId(object.getString("eventId"));
        event.setReminderType(object.getString("reminderType"));
        event.setPatientName(object.getString("patientName"));
        event.setIsValid(object.getBoolean("isValid"));
        event.setSummary(object.getString("summary"));
        event.setLocation(object.getString("location"));
        event.setDescription(object.getString("description"));
        event.setStartDate(startDate);
        event.setStartTime(object.getString("startTime"));
        event.setEndDate(endDate);
        event.setEndTime(object.getString("endTime"));
        event.setRepeat(object.getBoolean("isRepeat"));
        event.setFrequencyUnits(object.getString("frequencyUnits"));
        event.setFrequency(object.getInt("frequency"));
        event.setSendEmail(object.getBoolean("sendEmail"));

        JsonArray attendeesArr = object.getJsonArray("attendees");
        List<String> attendees = new LinkedList<>();
        for (int i = 0; i < attendeesArr.size(); i++) {
            attendees.add(attendeesArr.getString(i));
        }

        event.setAttendees(attendees);

        return event;
    }

    public static DailyNotification stringToDailyNotification(String payload) {

        // {"daily":{"guardianId":"qwer1235","patientId":"zxcv0995","eventId":"160e6453","reminderType":"daily","description":"daily
        // description","startTime":["12:11","12:13","12:14"]}}

        JsonReader reader = Json.createReader(new StringReader(payload));
        JsonObject object = reader.readObject();

        DailyNotification daily = new DailyNotification();
        daily.setGuardianId(object.getString("guardianId"));
        daily.setPatientId(object.getString("patientId"));
        daily.setEventId(object.getString("eventId"));
        daily.setReminderType(object.getString("reminderType"));
        daily.setIsValid(object.getBoolean("isValid"));
        daily.setPatientName(object.getString("patientName"));
        daily.setDescription(object.getString("description"));

        JsonArray timeArr = object.getJsonArray("startTime");
        List<String> startTime = new LinkedList<>();
        for (int i = 0; i < timeArr.size(); i++) {
            startTime.add(timeArr.getString(i));
        }
        daily.setStartTime(startTime);

        return daily;

    }

    public static JsonObject eventsNotificationToJson(EventsNotification event) {

        // {"event":{"guardianId":"qwer1235","patientId":"zxcv0995","eventId":"bdebcd81","reminderType":"event","summary":"summary","location":"location","description":"description","startDate":"2024-04-13T08:14:37.108Z","startTime":"9:00","endDate":"2024-04-13T08:14:37.108Z","endTime":"10:00","isRepeat":false,"frequencyUnits":"DAILY","frequency":1,"duration":"forever","times":1,"sendEmail":false,"attendees":[]}}

        List<String> attendees = event.getAttendees();
        JsonArrayBuilder arrBuilder = Json.createArrayBuilder();

        if (attendees.size() > 0) {
            for (String email : attendees) {
                arrBuilder.add(email);
            }
        }

        return Json.createObjectBuilder()
                .add("guardianId", event.getGuardianId())
                .add("patientId", event.getPatientId())
                .add("eventId", event.getEventId())
                .add("reminderType", event.getReminderType())
                .add("patientName", event.getPatientName())
                .add("isValid", event.getIsValid())
                .add("summary", event.getSummary())
                .add("location", event.getLocation())
                .add("description", event.getDescription())
                .add("startDate", event.getStartDate())
                .add("startTime", event.getStartTime())
                .add("endDate", event.getEndDate())
                .add("endTime", event.getEndTime())
                .add("isRepeat", event.isRepeat())
                .add("frequencyUnits", event.getFrequencyUnits())
                .add("frequency", event.getFrequency())
                .add("sendEmail", event.isSendEmail())
                .add("attendees", arrBuilder.build())
                .build();
    }

    public static JsonObject DailyNotificationtoJson(DailyNotification daily) {

        List<String> startTimings = daily.getStartTime();
        JsonArrayBuilder arrBuilder = Json.createArrayBuilder();

        if (startTimings.size() > 0) {
            for (String time : startTimings) {
                arrBuilder.add(time);
            }
        }
        return Json.createObjectBuilder()
                .add("guardianId", daily.getGuardianId())
                .add("patientId", daily.getPatientId())
                .add("eventId", daily.getEventId())
                .add("reminderType", daily.getReminderType())
                .add("patientName", daily.getPatientName())
                .add("isValid", daily.getIsValid())
                .add("description", daily.getDescription())
                .add("startTime", arrBuilder.build())
                .build();

    }

    public static JsonObject distanceToJson(MapDistance dist, MapLocation loc) {

        // distance: string
        // startAddress: string
        // endName: string
        // endPostalCode:string
        return Json.createObjectBuilder()
                .add("distance", dist.getDistance())
                .add("startAddress", dist.getStartAddress())
                .add("endAddress", dist.getEndAddress())
                .add("endName", dist.getEndName())
                .add("endPostalCode", dist.getEndPostalCode())
                .add("startPostalCode", loc.getPostalCode())
                .add("latEnd", dist.getLatEnd())
                .add("lngEnd", dist.getLngEnd())
                .add("latStart", loc.getLatitude())
                .add("lngStart", loc.getLongtitude())
                .build();

    }

}
