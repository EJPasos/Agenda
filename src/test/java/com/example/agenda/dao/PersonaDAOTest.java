package com.example.agenda.dao;

import com.example.agenda.model.Persona;
import com.example.agenda.model.Telefono;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PersonaDAOTest {

    private PersonaDAO personaDAO;
    private int idPersonaPrueba;

    @BeforeEach
    public void setUp() {
        personaDAO = new PersonaDAO();
    }

    @Test
    public void testCicloCompletoCRUD() {
        // -----------------------------
        // 1. CREATE (Alta)
        // -----------------------------
        Persona persona = new Persona(0, "Usuario Test", "Calle Falsa Test");
        persona.getTelefonos().add(new Telefono(0, 0, "555-0000"));
        persona.getTelefonos().add(new Telefono(0, 0, "555-1111"));

        boolean insertado = personaDAO.insertar(persona);
        assertTrue(insertado, "El método insertar debe devolver true");
        assertTrue(persona.getId() > 0, "El ID de la persona debe haberse actualizado tras la inserción");

        idPersonaPrueba = persona.getId(); // Guardamos el ID para borrarlo después

        // -----------------------------
        // 2. READ (Consulta)
        // -----------------------------
        List<Persona> personas = personaDAO.obtenerTodas();
        boolean encontrado = false;
        Persona personaRecuperada = null;

        for (Persona p : personas) {
            if (p.getId() == idPersonaPrueba) {
                encontrado = true;
                personaRecuperada = p;
                break;
            }
        }

        assertTrue(encontrado, "La persona insertada debe estar en la lista de la BD");
        assertNotNull(personaRecuperada);
        assertEquals("Usuario Test", personaRecuperada.getNombre());
        assertEquals(2, personaRecuperada.getTelefonos().size(), "Debe recuperar los 2 teléfonos");

        // -----------------------------
        // 3. UPDATE (Modificación)
        // -----------------------------
        personaRecuperada.setNombre("Usuario Modificado");
        personaRecuperada.getTelefonos().clear();
        personaRecuperada.getTelefonos().add(new Telefono(0, idPersonaPrueba, "999-9999"));

        boolean actualizado = personaDAO.actualizar(personaRecuperada);
        assertTrue(actualizado, "El método actualizar debe devolver true");

        // -----------------------------
        // 4. DELETE (Baja)
        // -----------------------------
        boolean eliminado = personaDAO.eliminar(idPersonaPrueba);
        assertTrue(eliminado, "El método eliminar debe devolver true");

        // Verificamos que ya no exista
        boolean aunExiste = personaDAO.obtenerTodas().stream().anyMatch(p -> p.getId() == idPersonaPrueba);
        assertFalse(aunExiste, "La persona ya no debe existir en la BD");
    }

    // Este método se asegura de limpiar la BD si la prueba falla a la mitad y no llega al DELETE
    @AfterEach
    public void tearDown() {
        if (idPersonaPrueba > 0) {
            personaDAO.eliminar(idPersonaPrueba);
        }
    }
}