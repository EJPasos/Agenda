package com.example.agenda.view;

import com.example.agenda.controller.AgendaController;
import com.example.agenda.model.Persona;
import com.example.agenda.model.Telefono;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AgendaViewController {

    @FXML private ListView<Persona> listaPersonas;

    @FXML private VBox panelBienvenida;
    @FXML private VBox panelVista;
    @FXML private VBox panelEdicion;

    @FXML private Label lblNombreVista;
    @FXML private Label lblDireccionVista;
    @FXML private ListView<String> listaTelefonosVista;

    @FXML private Label lblTituloEdicion;
    @FXML private TextField txtNombre;
    @FXML private TextField txtDireccion;

    // Nuevo contenedor dinámico
    @FXML private VBox vboxTelefonosContenedor;

    private AgendaController controller;
    private ObservableList<Persona> personasData;
    private Persona personaEnPantalla = null;

    @FXML
    public void initialize() {
        controller = new AgendaController();
        personasData = FXCollections.observableArrayList();
        listaPersonas.setItems(personasData);

        cargarContactos();
        mostrarPanelBienvenida();

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
        vboxTelefonosContenedor.getChildren().clear(); // Limpiamos los campos anteriores

        if (persona == null) {
            lblTituloEdicion.setText("Nueva Persona");
            txtNombre.clear();
            txtDireccion.clear();
            agregarCampoTelefono("", true); // Agrega 1 campo vacío con el botón "+"
        } else {
            lblTituloEdicion.setText("Modificar Persona");
            txtNombre.setText(persona.getNombre());
            txtDireccion.setText(persona.getDireccion());

            List<Telefono> tels = persona.getTelefonos();
            if (tels.isEmpty()) {
                agregarCampoTelefono("", true);
            } else {
                // Iteramos los teléfonos existentes para crear sus campos
                for (int i = 0; i < tels.size(); i++) {
                    boolean esElUltimo = (i == tels.size() - 1);
                    agregarCampoTelefono(tels.get(i).getTelefono(), esElUltimo);
                }
            }
        }

        panelBienvenida.setVisible(false);
        panelVista.setVisible(false);
        panelEdicion.setVisible(true);
    }

    // --- LÓGICA DE CAMPOS DINÁMICOS ---
    private void agregarCampoTelefono(String texto, boolean incluirBotonPlus) {
        HBox hbox = new HBox(10);
        TextField txtTel = new TextField(texto);
        txtTel.setPromptText("Número de teléfono");
        HBox.setHgrow(txtTel, Priority.ALWAYS); // Para que el input ocupe todo el ancho

        hbox.getChildren().add(txtTel);

        if (incluirBotonPlus) {
            Button btnPlus = new Button("+");
            // Acción al presionar el "+"
            btnPlus.setOnAction(e -> {
                hbox.getChildren().remove(btnPlus);
                agregarCampoTelefono("", true);
            });
            hbox.getChildren().add(btnPlus);
        }

        vboxTelefonosContenedor.getChildren().add(hbox);
    }

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
            alerta.setContentText("¿Estás seguro?");

            Optional<ButtonType> resultado = alerta.showAndWait();
            if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
                boolean exito = controller.eliminarPersona(personaEnPantalla.getId());
                if (exito) {
                    cargarContactos();
                    mostrarPanelBienvenida();
                } else {
                    mostrarError("No se pudo eliminar a la persona.");
                }
            }
        }
    }

    @FXML
    private void handleGuardar() {
        String nombre = txtNombre.getText();
        String direccion = txtDireccion.getText();
        List<String> telefonos = new ArrayList<>();

        // Recorremos los campos dinámicos para extraer los números
        for (Node nodo : vboxTelefonosContenedor.getChildren()) {
            if (nodo instanceof HBox) {
                HBox hbox = (HBox) nodo;
                TextField txt = (TextField) hbox.getChildren().get(0); // El TextField siempre es el índice 0
                String tel = txt.getText();

                // Si el usuario dejó el campo vacío, simplemente lo ignoramos
                if (tel != null && !tel.trim().isEmpty()) {
                    telefonos.add(tel.trim());
                }
            }
        }

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