package com.example.agenda.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PersonaTest {

    @Test
    public void testInicializacionPersona() {
        // Preparación y Ejecución
        Persona persona = new Persona(1, "Prueba", "Direccion 1");

        // Verificación
        assertNotNull(persona.getTelefonos(), "La lista de teléfonos no debe ser nula al instanciar");
        assertEquals(0, persona.getTelefonos().size(), "La lista de teléfonos debe estar vacía inicialmente");
    }

    @Test
    public void testAgregarTelefonoAPersona() {
        // Preparación
        Persona persona = new Persona();
        Telefono tel1 = new Telefono(0, 0, "1234567890");
        Telefono tel2 = new Telefono(0, 0, "0987654321");

        // Ejecución
        persona.getTelefonos().add(tel1);
        persona.getTelefonos().add(tel2);

        // Verificación
        assertEquals(2, persona.getTelefonos().size(), "La persona debería tener 2 teléfonos");
        assertEquals("1234567890", persona.getTelefonos().get(0).getTelefono());
    }
}