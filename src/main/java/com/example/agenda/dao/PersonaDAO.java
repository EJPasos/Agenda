package com.example.agenda.dao;

import com.example.agenda.model.Persona;
import com.example.agenda.model.Telefono;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PersonaDAO {

    public boolean insertar(Persona persona) {
        String sqlPersona = "INSERT INTO Personas (nombre, direccion) VALUES (?, ?)";
        String sqlTelefono = "INSERT INTO Telefonos (personaId, telefono) VALUES (?, ?)";

        try (Connection conn = ConexionDB.getConnection()) {
            conn.setAutoCommit(false); // Transacción para asegurar consistencia

            try (PreparedStatement psPersona = conn.prepareStatement(sqlPersona, Statement.RETURN_GENERATED_KEYS)) {
                psPersona.setString(1, persona.getNombre());
                psPersona.setString(2, persona.getDireccion());
                psPersona.executeUpdate();

                ResultSet rs = psPersona.getGeneratedKeys();
                if (rs.next()) {
                    int personaId = rs.getInt(1);
                    persona.setId(personaId);

                    try (PreparedStatement psTel = conn.prepareStatement(sqlTelefono)) {
                        for (Telefono tel : persona.getTelefonos()) {
                            psTel.setInt(1, personaId);
                            psTel.setString(2, tel.getTelefono());
                            psTel.addBatch();
                        }
                        psTel.executeBatch();
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Persona> obtenerTodas() {
        List<Persona> lista = new ArrayList<>();
        String sql = "SELECT p.id, p.nombre, p.direccion, t.id as id_tel, t.telefono " +
                "FROM Personas p LEFT JOIN Telefonos t ON p.id = t.personaId";

        try (Connection conn = ConexionDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            Map<Integer, Persona> mapaPersonas = new LinkedHashMap<>();

            while (rs.next()) {
                int idPersona = rs.getInt("id");
                Persona p = mapaPersonas.getOrDefault(idPersona,
                        new Persona(idPersona, rs.getString("nombre"), rs.getString("direccion")));

                mapaPersonas.putIfAbsent(idPersona, p);

                int idTel = rs.getInt("id_tel");
                if (!rs.wasNull()) {
                    p.getTelefonos().add(new Telefono(idTel, idPersona, rs.getString("telefono")));
                }
            }
            lista.addAll(mapaPersonas.values());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean actualizar(Persona persona) {
        String sqlUpdatePersona = "UPDATE Personas SET nombre = ?, direccion = ? WHERE id = ?";
        String sqlDeleteTelefonos = "DELETE FROM Telefonos WHERE personaId = ?";
        String sqlInsertTelefono = "INSERT INTO Telefonos (personaId, telefono) VALUES (?, ?)";

        try (Connection conn = ConexionDB.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psPersona = conn.prepareStatement(sqlUpdatePersona);
                 PreparedStatement psDelTel = conn.prepareStatement(sqlDeleteTelefonos);
                 PreparedStatement psInsTel = conn.prepareStatement(sqlInsertTelefono)) {

                psPersona.setString(1, persona.getNombre());
                psPersona.setString(2, persona.getDireccion());
                psPersona.setInt(3, persona.getId());
                psPersona.executeUpdate();

                psDelTel.setInt(1, persona.getId());
                psDelTel.executeUpdate();

                for (Telefono tel : persona.getTelefonos()) {
                    psInsTel.setInt(1, persona.getId());
                    psInsTel.setString(2, tel.getTelefono());
                    psInsTel.addBatch();
                }
                psInsTel.executeBatch();

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                ex.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM Personas WHERE id = ?";
        try (Connection conn = ConexionDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}