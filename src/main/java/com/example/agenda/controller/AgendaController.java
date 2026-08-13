package com.example.agenda.controller;

import com.example.agenda.dao.PersonaDAO;
import com.example.agenda.model.Persona;
import com.example.agenda.model.Telefono;

import java.util.List;

public class AgendaController {
    private PersonaDAO personaDAO;

    public AgendaController() {
        this.personaDAO = new PersonaDAO();
    }

    public List<Persona> obtenerPersonas() {
        return personaDAO.obtenerTodas();
    }

    public boolean guardarPersona(String nombre, String direccion, List<String> telefonosText) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return false;
        }

        Persona p = new Persona(0, nombre, direccion);
        for (String tel : telefonosText) {
            if (!tel.trim().isEmpty()) {
                p.getTelefonos().add(new Telefono(0, 0, tel));
            }
        }
        return personaDAO.insertar(p);
    }

    public boolean modificarPersona(int id, String nombre, String direccion, List<String> telefonosText) {
        if (id <= 0 || nombre == null || nombre.trim().isEmpty()) {
            return false;
        }

        Persona p = new Persona(id, nombre, direccion);
        for (String tel : telefonosText) {
            if (!tel.trim().isEmpty()) {
                p.getTelefonos().add(new Telefono(0, id, tel));
            }
        }
        return personaDAO.actualizar(p);
    }

    public boolean eliminarPersona(int id) {
        if (id <= 0) return false;
        return personaDAO.eliminar(id);
    }
}