package com.example.sigelic.controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/data")
public class DataController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/titulares")
    public List<Map<String, Object>> getTitulares() {
        List<Map<String, Object>> titulares = new ArrayList<>();
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                 "SELECT id, dni, nombre, apellido, fecha_nacimiento, email, telefono FROM titulares LIMIT 10");
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> titular = new HashMap<>();
                titular.put("id", rs.getLong("id"));
                titular.put("dni", rs.getString("dni"));
                titular.put("nombre", rs.getString("nombre"));
                titular.put("apellido", rs.getString("apellido"));
                titular.put("fecha_nacimiento", rs.getDate("fecha_nacimiento"));
                titular.put("email", rs.getString("email"));
                titular.put("telefono", rs.getString("telefono"));
                titulares.add(titular);
            }
        } catch (SQLException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            titulares.add(error);
        }
        
        return titulares;
    }

    @GetMapping("/recursos")
    public List<Map<String, Object>> getRecursos() {
        List<Map<String, Object>> recursos = new ArrayList<>();
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                 "SELECT id, tipo_recurso, descripcion, cilindraje, combustible FROM recursos");
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> recurso = new HashMap<>();
                recurso.put("id", rs.getLong("id"));
                recurso.put("tipo_recurso", rs.getString("tipo_recurso"));
                recurso.put("descripcion", rs.getString("descripcion"));
                recurso.put("cilindraje", rs.getInt("cilindraje"));
                recurso.put("combustible", rs.getString("combustible"));
                recursos.add(recurso);
            }
        } catch (SQLException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            recursos.add(error);
        }
        
        return recursos;
    }

    @GetMapping("/tramites")
    public List<Map<String, Object>> getTramites() {
        List<Map<String, Object>> tramites = new ArrayList<>();
        
        try (Connection connection = dataSource.getConnection();
             PreparedStatement stmt = connection.prepareStatement(
                 "SELECT t.id, t.numero_tramite, t.tipo_tramite, t.estado, " +
                 "t.fecha_solicitud, ti.nombre, ti.apellido " +
                 "FROM tramites t " +
                 "JOIN titulares ti ON t.titular_id = ti.id " +
                 "ORDER BY t.fecha_solicitud DESC LIMIT 10");
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Map<String, Object> tramite = new HashMap<>();
                tramite.put("id", rs.getLong("id"));
                tramite.put("numero_tramite", rs.getString("numero_tramite"));
                tramite.put("tipo_tramite", rs.getString("tipo_tramite"));
                tramite.put("estado", rs.getString("estado"));
                tramite.put("fecha_solicitud", rs.getDate("fecha_solicitud"));
                tramite.put("titular_nombre", rs.getString("nombre"));
                tramite.put("titular_apellido", rs.getString("apellido"));
                tramites.add(tramite);
            }
        } catch (SQLException e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            tramites.add(error);
        }
        
        return tramites;
    }
}
