package vttp.project.backend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import vttp.project.backend.service.MapService;

@SpringBootApplication
@EnableScheduling
public class BackendApplication implements CommandLineRunner {

	@Value("${api.key}")
    private String apiKey;

	@Autowired
	MapService mapSvc;
	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		apiKey = mapSvc.getToken();
		// System.out.println("\nResult:\n"+apiKey );
	}

	

}
