package vttp.project.backend.controller;

import java.io.StringReader;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.servlet.http.HttpServletResponse;
import vttp.project.backend.model.Guardian;
import vttp.project.backend.model.MyPrincipal;
import vttp.project.backend.model.User;
import vttp.project.backend.service.GuardianService;
import vttp.project.backend.service.JwtService;
import vttp.project.backend.service.Utils;

@RestController
@RequestMapping(path = "/api")
public class GuardianController {

    @Autowired
    private GuardianService guardianSvc;

    @Autowired
    private JwtService jwtSvc;

    // to sign in
    @PostMapping(path = "/auth/signIn")
    public ResponseEntity<String> signIn(@RequestBody String payload, HttpServletResponse response) {

        System.out.println("\n Payload:\n" + payload);

        JsonReader reader = Json.createReader(new StringReader(payload));
        JsonObject object = reader.readObject();

        String email = object.getString("email");
        String password = object.getString("password");

        if (guardianSvc.guardianEmailExists(email)) {

            if (guardianSvc.getGuardian(email, password) != null) {

                Guardian guardian = guardianSvc.getGuardian(email, password);
                String guardianId = guardian.getGuardianId();

                var jwtToken = "";
                if (guardianSvc.signIn(guardianId, password) != null) {
                    UserDetails userDetails = guardianSvc.signIn(guardianId, password);
                    jwtToken = jwtSvc.generateToken(userDetails);
                    System.out.println("\njwtTOken: " + jwtToken);

                    JsonObject resp = Json.createObjectBuilder()
                            .add("guardianId", guardianId)
                            .add("authority", userDetails.getAuthorities().toString())
                            .add("token", jwtToken)
                            .add("status", 200)
                            .build();
                    return ResponseEntity.status(200)
                            .body(resp.toString());

                } else {

                    JsonObject err = Json.createObjectBuilder()
                            .add("message", "Unable to Authenticate User")
                            .add("status", 400)
                            .build();
                    return ResponseEntity.status(400)
                            .body(err.toString());

                }

            } else {
                JsonObject err = Json.createObjectBuilder()
                        .add("message", "Incorrect Password")
                        .add("status", 400)
                        .build();
                return ResponseEntity.status(400)
                        .body(err.toString());
            }

        } else {
            JsonObject err = Json.createObjectBuilder()
                    .add("message", "Email does not exist, please Sign up")
                    .add("status", 400)
                    .build();
            return ResponseEntity.status(400)
                    .body(err.toString());
        }

    }

    @PostMapping(path = "/auth/register")
    public ResponseEntity<String> register(@RequestBody String payload) {

        System.out.println("\n Payload:\n" + payload);

        Optional<User> opt = guardianSvc.register(payload);

        if (opt.isPresent()) {
            User user = opt.get();
            MyPrincipal myPrincipal = new MyPrincipal(user);
            var jwtToken = jwtSvc.generateToken(myPrincipal);
            JsonObject resp = Json.createObjectBuilder()
                    .add("token", jwtToken)
                    .add("message", "Guardian added Successfully")
                    .add("status", 200)
                    .build();
            return ResponseEntity.ok(resp.toString());
        } else {

            JsonObject err = Json.createObjectBuilder()
                    .add("message", "Input email exists, please change email input")
                    .add("status", 400)
                    .build();
            return ResponseEntity.status(400)
                    .body(err.toString());

        }

    }

    @GetMapping(path = "/getGuardian")
    public ResponseEntity<String> getGuardianById(@RequestParam String guardianId) {

        Optional<Guardian> opt = guardianSvc.getGuardianByID(guardianId);
        if (opt.isPresent()) {
            Guardian guardian = opt.get();
            JsonObject obj = Utils.guardianToJson(guardian);
            return ResponseEntity.ok(obj.toString());
        }

        return null;
    }

    @PutMapping(path = "/guardian/edit/{guardianId}")
    public ResponseEntity<String> editGuardian(@PathVariable String guardianId,
            @RequestBody String payload) {

        System.out.println("Put mapping is ok");
        System.out.println("\n Payload:" + payload);

        if (guardianSvc.editGuardian(payload)) {
            JsonObject resp = Json.createObjectBuilder()
                    .add("message", "Patient Updated Successfully")
                    .add("status", 200)
                    .build();
            return ResponseEntity.ok(resp.toString());
        } else {
            JsonObject err = Json.createObjectBuilder()
                    .add("message", "Unable to update guardian details")
                    .add("status", 400)
                    .build();
            return ResponseEntity.status(400)
                    .body(err.toString());

        }
    }

    
}
