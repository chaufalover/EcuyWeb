package com.project.ecuy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Scanner;

@SpringBootApplication
public class EcuyApplication {
    
    public static String SIGHTENGINE_API_KEY;
    public static String SIGHTENGINE_API_SECRET;

    public static void main(String[] args) {
        System.out.println("Iniciando la aplicación...");
        SpringApplication.run(EcuyApplication.class, args);
    }
    
    @Component
    public static class SightEngineInitializer implements ApplicationListener<ApplicationReadyEvent> {
        @Override
        public void onApplicationEvent(ApplicationReadyEvent event) {
            
            if (SIGHTENGINE_API_KEY == null || SIGHTENGINE_API_SECRET == null) {
                solicitarCredenciales();
            }
        }
        
        private void solicitarCredenciales() {
            Scanner scanner = new Scanner(System.in);
            try {
                System.out.println("\n=== Configuración de SightEngine ===");
                System.out.print("Ingrese su API Key de SightEngine: ");
                SIGHTENGINE_API_KEY = scanner.nextLine().trim();
                
                System.out.print("Ingrese su API Secret de SightEngine: ");
                SIGHTENGINE_API_SECRET = scanner.nextLine().trim();
                
                if (SIGHTENGINE_API_KEY.isEmpty() || SIGHTENGINE_API_SECRET.isEmpty()) {
                    System.err.println("Error: Debe proporcionar tanto la API Key como el API Secret.");
                    System.exit(1);
                }
                
                System.out.println("Credenciales configuradas correctamente.\n");
            } finally {
                
            }
        }
    }
}
