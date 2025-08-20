package com.example.sigelic.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;

@Component
public class PasswordTestUtil implements CommandLineRunner {
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        // Hash encontrado en la base de datos
        String storedHash = "$2a$12$l7UZ3PqAqqj8Lrb8M.3oXOm4/nSzZg5OhUmZELJhv8rQCR3BvWHFe";
        
        // Contraseñas que queremos probar
        String[] passwordsToTest = {
            "Admin123!",
            "admin123!",
            "ADMIN123!",
            "Admin123",
            "admin123",
            "admin",
            "password"
        };
        
        System.out.println("========== PASSWORD TESTING ==========");
        System.out.println("Hash stored: " + storedHash);
        
        for (String password : passwordsToTest) {
            boolean matches = passwordEncoder.matches(password, storedHash);
            System.out.println("Password '" + password + "' matches: " + matches);
        }
        
        // También vamos a generar un nuevo hash para Admin123! y comparar
        String newHash = passwordEncoder.encode("Admin123!");
        System.out.println("\nNew hash for 'Admin123!': " + newHash);
        System.out.println("New hash matches 'Admin123!': " + passwordEncoder.matches("Admin123!", newHash));
        
        System.out.println("========================================");
    }
}
