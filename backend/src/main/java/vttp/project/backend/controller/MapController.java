package vttp.project.backend.controller;

import java.io.StringReader;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import vttp.project.backend.model.MapDistance;
import vttp.project.backend.model.MapLocation;
import vttp.project.backend.service.MapService;
import vttp.project.backend.service.Utils;

@RestController
@RequestMapping(path = "/api")
public class MapController {

    @Autowired
    MapService mapSvc;

    @GetMapping(path = "/checkPostCode")
    public ResponseEntity<String> getPostalCode(@RequestParam String postalCode) {

        // System.out.println("\npostal code: " + postalCode);

        MapLocation location = mapSvc.searchPostalCode(postalCode);

        if (location != null) {
            JsonObject resp = Json.createObjectBuilder()
                    .add("message", "Postal Code is Valid")
                    .add("status", 200)
                    .build();
            return ResponseEntity.ok(resp.toString());
        } else {
            JsonObject err = Json.createObjectBuilder()
                    .add("message", "Invalid Postal Code")
                    .add("status", 400)
                    .build();
            return ResponseEntity.status(400)
                    .body(err.toString());

        }

    }

    @GetMapping(path = "/getDistances")
    public ResponseEntity<String> getDistances(@RequestParam String postalCode, @RequestParam String amenities,
            @RequestParam Double distance) {

        MapLocation location = mapSvc.searchPostalCode(postalCode);

        List<MapDistance> distanceList = mapSvc.saveDistances(amenities, location, distance);

        if (distanceList.size() > 0) {
            System.out.println("Number of amenities found within distance: " + distanceList.size());
            JsonArrayBuilder arrBuilder = Json.createArrayBuilder();
            for (MapDistance dist : distanceList) {
                arrBuilder.add(Utils.distanceToJson(dist, location));
            }

            return ResponseEntity.ok(arrBuilder.build().toString());

        } else {
            return null;
        }
    }

    @PostMapping(path="/getMap")
    public ResponseEntity<String> getMap(@RequestBody String distance){
        System.out.println("\nPayload: " + distance);

        // Payload: {"distance":{"distance":0.31,"startAddress":"786C WOODLANDS DRIVE 60 SINGAPORE 733786","endAddress":"789 Woodlands Avenue 6 Blk 797, 788 #01-663 S(730789)","endName":"PCF Sparkletots Preschool @ Sembawang West Blk 789 (KN)","endPostalCode":"730789","startPostalCode":"733786","latEnd":1.44333483207998,"lngEnd":103.80237840904,"latStart":1.44581867717204,"lngStart":103.80106885347}}
        JsonReader reader = Json.createReader(new StringReader(distance));
        JsonObject obj = reader.readObject().getJsonObject("distance");

        byte[] map = mapSvc.staticMap(distance);
        String mapImg = Base64.getEncoder().encodeToString(map);

        JsonObject resp = Json.createObjectBuilder()
                            .add("startPostalCode", obj.getString("startAddress"))
                            .add("startAddress", obj.getString("startAddress"))
                            .add("endPostalCode", obj.getString("startAddress"))
                            .add("endAddress", obj.getString("startAddress"))
                            .add("img", mapImg)
                            .build();
        
        return ResponseEntity.ok(resp.toString());
    }


}
