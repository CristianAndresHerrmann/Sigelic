package com.example.sigelic.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/verification")
public class VerificationController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/database-status")
    public Map<String, Object> getDatabaseStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            status.put("connected", true);
            status.put("url", connection.getMetaData().getURL());
            
            // Verificar tablas principales y contar registros
            status.put("titulares_count", getTableCount("titulares"));
            status.put("recursos_count", getTableCount("recursos"));
            status.put("costos_tramite_count", getTableCount("costos_tramite"));
            status.put("tramites_count", getTableCount("tramites"));
            status.put("licencias_count", getTableCount("licencias"));
            status.put("pagos_count", getTableCount("pagos"));
            status.put("examenes_count", getTableCount("examenes"));
            status.put("turnos_count", getTableCount("turnos"));
            
        } catch (SQLException e) {
            status.put("connected", false);
            status.put("error", e.getMessage());
        }
        
        return status;
    }
    
    private int getTableCount(String tableName) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM " + tableName);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error counting table " + tableName + ": " + e.getMessage());
        }
        return -1;
    }
}
