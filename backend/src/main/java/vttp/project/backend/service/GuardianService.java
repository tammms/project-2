package vttp.project.backend.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import vttp.project.backend.model.Guardian;
import vttp.project.backend.model.MyPrincipal;
import vttp.project.backend.model.User;
import vttp.project.backend.repository.GuardianRepository;
import vttp.project.backend.repository.UserRepository;

@Service
public class GuardianService {

    @Autowired
    private GuardianRepository guardianRepo;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    JwtService jwtSvc;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    PasswordEncoder passwordEncoder;

    public Optional<User> register(String payload) {

        Guardian guardian = Utils.stringToGuardian(payload);
        String encodedPassword = passwordEncoder.encode(guardian.getPassword());

        Boolean guardianResult = guardianRepo.addGuardian(guardian, encodedPassword);

        String roleId = userRepo.formatRoleId(guardian.getGuardianId(), "");
        Optional<User> user = userRepo.getUser(roleId);

        if (guardianResult == true && user.isPresent()) {
            return user;
        } else {
            return Optional.empty();
        }


    }

    public UserDetails signIn(String guardianId, String password) {

        String roleId = userRepo.formatRoleId(guardianId, "");
        System.out.println("\nroleId: " + roleId);

        
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(roleId, password));
        } catch (Exception e) {
            System.out.println("Error: " + e.getLocalizedMessage());
        }

        UserDetails user = userRepo.getUser(roleId)
                .map(MyPrincipal::new)
                .orElseThrow(() -> new UsernameNotFoundException("Error: User not Found"));
            

        return user;

    }

    public Guardian getGuardian (String email, String password){
        Optional<Guardian> opt = guardianRepo.getGuardian(email, password);
        if (opt.isPresent()){
            Guardian guardian = opt.get();
            if (passwordEncoder.matches(password, guardian.getPassword())){
                return guardian;
            }else{
                return null;
            }  
        }
        return null;
    }

    public boolean guardianEmailExists(String email) {
        return guardianRepo.guardianEmailExists(email);
    }


    public Optional<Guardian> getGuardianByID(String guardianId) {
        return guardianRepo.getGuardianByID(guardianId);
    }
    

    

    
    public boolean editGuardian(String payload) {

        Guardian guardian = Utils.stringToGuardian(payload);
        String encodedPassword = passwordEncoder.encode(guardian.getPassword());

        return guardianRepo.editGuardian(guardian, encodedPassword);


    }

}
