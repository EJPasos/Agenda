package com.example.agenda.view;

import com.example.agenda.controller.AgendaController;
import com.example.agenda.model.Persona;
import com.example.agenda.model.Telefono;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AgendaViewController {

    // --- Componentes del Panel Izquierdo ---
    @FXML private ListView<Persona> listaPersonas;

    // --- Paneles de Estado ---
    @FXML private VBox panelBienvenida;
    @FXML private VBox panelVista;
    @FXML private VBox panelEdicion;

    // --- Componentes Panel Vista ---
    @FXML private Label lblNombreVista;
    @FXML private Label lblDireccionVista;
    @FXML private ListView<String> listaTelefonosVista;

    // --- Componentes Panel Edición ---
    @FXML private Label lblTituloEdicion;
    @FXML private TextField txtNombre;
    @FXML private TextField txtDireccion;
    @FXML private TextField txtNuevoTelefono;
    @FXML private ListView<String> listaTelefonosEdicion;

    // --- Variables de Estado y Controladores ---
    private AgendaController controller;
    private ObservableList<Persona> personasData;
    private ObservableList<String> telefonosEdicionData;
    private Persona personaEnPantalla = null; // null si es una nueva persona

    @FXML
    public void initialize() {
        controller = new AgendaController();
        personasData = FXCollections.observableArrayList();
        telefonosEdicionData = FXCollections.observableArrayList();

        listaPersonas.setItems(personasData);
        listaTelefonosEdicion.setItems(telefonosEdicionData);

        cargarContactos();
        mostrarPanelBienvenida();

        // Listener: Cuando se hace click en un nombre de la lista izquierda
        listaPersonas.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                mostrarPanelVista(newVal);
            }
        });
    }

    private void cargarContactos() {
        personasData.clear();
        personasData.addAll(controller.obtenerPersonas());
    }

    // --- MÉTODOS DE CAMBIO DE ESTADO (VISIBILIDAD) ---

    private void mostrarPanelBienvenida() {
        panelBienvenida.setVisible(true);
        panelVista.setVisible(false);
        panelEdicion.setVisible(false);
    }

    private void mostrarPanelVista(Persona persona) {
        personaEnPantalla = persona;

        lblNombreVista.setText(persona.getNombre());
        lblDireccionVista.setText(persona.getDireccion());

        ObservableList<String> telsVista = FXCollections.observableArrayList();
        for (Telefono t : persona.getTelefonos()) {
            telsVista.add(t.getTelefono());
        }
        listaTelefonosVista.setItems(telsVista);

        panelBienvenida.setVisible(false);
        panelVista.setVisible(true);
        panelEdicion.setVisible(false);
    }

    private void mostrarPanelEdicion(Persona persona) {
        personaEnPantalla = persona;
        telefonosEdicionData.clear();
        txtNuevoTelefono.clear();

        if (persona == null) {
            // Es un ALTA
            lblTituloEdicion.setText("Nueva Persona");
            txtNombre.clear();
            txtDireccion.clear();
        } else {
            // Es una MODIFICACIÓN
            lblTituloEdicion.setText("Modificar Persona");
            txtNombre.setText(persona.getNombre());
            txtDireccion.setText(persona.getDireccion());
            for (Telefono t : persona.getTelefonos()) {
                telefonosEdicionData.add(t.getTelefono());
            }
        }

        panelBienvenida.setVisible(false);
        panelVista.setVisible(false);
        panelEdicion.setVisible(true);
    }

    // --- MÉTODOS DE ACCIÓN (BOTONES) ---

    @FXML
    private void handleNuevaPersona() {
        listaPersonas.getSelectionModel().clearSelection();
        mostrarPanelEdicion(null);
    }

    @FXML
    private void handleModificar() {
        if (personaEnPantalla != null) {
            mostrarPanelEdicion(personaEnPantalla);
        }
    }

    @FXML
    private void handleEliminar() {
        if (personaEnPantalla != null) {
            Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
            alerta.setTitle("Confirmar Eliminación");
            alerta.setHeaderText("Vas a eliminar a " + personaEnPantalla.getNombre());
            alerta.setContentText("¿Estás seguro? Esta acción borrará también sus teléfonos.");

            Optional<ButtonType> resultado = alerta.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                boolean exito = controller.eliminarPersona(personaEnPantalla.getId());
                if (exito) {
                    cargarContactos();
                    mostrarPanelBienvenida();
                } else {
                    mostrarError("No se pudo eliminar a la persona en la base de datos.");
                }
            }
        }
    }

    @FXML
    private void handleAgregarTelefono() {
        String tel = txtNuevoTelefono.getText();
        if (tel != null && !tel.trim().isEmpty()) {
            telefonosEdicionData.add(tel);
            txtNuevoTelefono.clear();
        }
    }

    @FXML
    private void handleGuardar() {
        String nombre = txtNombre.getText();
        String direccion = txtDireccion.getText();
        List<String> telefonos = new ArrayList<>(telefonosEdicionData);

        // Validación básica exigiendo al menos nombre y un teléfono
        if (nombre == null || nombre.trim().isEmpty()) {
            mostrarError("El nombre es obligatorio.");
            return;
        }
        if (telefonos.isEmpty()) {
            mostrarError("Debes agregar al menos un teléfono.");
            return;
        }

        boolean exito;
        if (personaEnPantalla == null) {
            exito = controller.guardarPersona(nombre, direccion, telefonos);
        } else {
            exito = controller.modificarPersona(personaEnPantalla.getId(), nombre, direccion, telefonos);
        }

        if (exito) {
            cargarContactos();
            mostrarPanelBienvenida();
        } else {
            mostrarError("Error al guardar en la base de datos.");
        }
    }

    @FXML
    private void handleCancelar() {
        if (personaEnPantalla == null) {
            mostrarPanelBienvenida();
        } else {
            mostrarPanelVista(personaEnPantalla);
            // Reseleccionar en la lista
            listaPersonas.getSelectionModel().select(personaEnPantalla);
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}