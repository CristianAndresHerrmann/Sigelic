package com.example.sigelic.views;

import com.example.sigelic.dto.usuario.CrearUsuarioDTO;
import com.example.sigelic.dto.usuario.UsuarioDTO;
import com.example.sigelic.model.RolSistema;
import com.example.sigelic.model.Usuario;
import com.example.sigelic.service.UsuarioService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Vista para gestión de usuarios del sistema
 */
@Route(value = "usuarios", layout = MainLayout.class)
@PageTitle("Usuarios | SIGELIC")
@RolesAllowed({"ADMINISTRADOR", "SUPERVISOR"})
public class UsuariosView extends VerticalLayout {

    private final UsuarioService usuarioService;
    private final AuthenticationContext authContext;
    
    private Grid<UsuarioDTO> grid;
    private ListDataProvider<UsuarioDTO> dataProvider;
    private TextField searchField;

    public UsuariosView(UsuarioService usuarioService, AuthenticationContext authContext) {
        this.usuarioService = usuarioService;
        this.authContext = authContext;

        addClassName("usuarios-view");
        setSizeFull();

        createHeader();
        createSearchBar();
        createGrid();
        refreshGrid();
    }

    private void createHeader() {
        H2 title = new H2("Gestión de Usuarios");
        title.addClassNames(LumoUtility.Margin.Bottom.MEDIUM, LumoUtility.Margin.Top.NONE);

        Button addUserButton = new Button("Nuevo Usuario", new Icon(VaadinIcon.PLUS));
        addUserButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addUserButton.addClickListener(e -> openUserDialog(null));

        HorizontalLayout header = new HorizontalLayout(title, addUserButton);
        header.setAlignItems(Alignment.CENTER);
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setWidthFull();

        add(header);
    }

    private void createSearchBar() {
        searchField = new TextField();
        searchField.setPlaceholder("Buscar usuarios...");
        searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
        searchField.setWidthFull();
        searchField.setMaxWidth("400px");
        
        searchField.addValueChangeListener(e -> {
            dataProvider.setFilter(user -> {
                String searchTerm = e.getValue().toLowerCase();
                return user.getUsername().toLowerCase().contains(searchTerm) ||
                       user.getNombre().toLowerCase().contains(searchTerm) ||
                       user.getApellido().toLowerCase().contains(searchTerm) ||
                       user.getEmail().toLowerCase().contains(searchTerm);
            });
        });

        add(searchField);
    }

    private void createGrid() {
        grid = new Grid<>(UsuarioDTO.class, false);
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setSizeFull();

        // Columnas
        grid.addColumn(UsuarioDTO::getUsername).setHeader("Usuario").setSortable(true);
        grid.addColumn(user -> user.getNombre() + " " + user.getApellido())
            .setHeader("Nombre Completo").setSortable(true);
        grid.addColumn(UsuarioDTO::getEmail).setHeader("Email").setSortable(true);
        grid.addColumn(user -> user.getRol().toString()).setHeader("Rol").setSortable(true);
        
        // Columna de estado
        grid.addColumn(new ComponentRenderer<>(user -> {
            Span badge = new Span();
            if (user.getActivo() && !user.getCuentaBloqueada()) {
                badge.setText("Activo");
                badge.getElement().getThemeList().add("badge success");
            } else if (user.getCuentaBloqueada()) {
                badge.setText("Bloqueado");
                badge.getElement().getThemeList().add("badge error");
            } else {
                badge.setText("Inactivo");
                badge.getElement().getThemeList().add("badge contrast");
            }
            return badge;
        })).setHeader("Estado").setSortable(true);

        // Columna de acciones
        grid.addColumn(new ComponentRenderer<>(this::createActionButtons))
            .setHeader("Acciones").setFlexGrow(0);

        add(grid);
    }

    private HorizontalLayout createActionButtons(UsuarioDTO user) {
        Button editButton = new Button(new Icon(VaadinIcon.EDIT));
        editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
        editButton.getElement().setAttribute("aria-label", "Editar usuario");
        editButton.addClickListener(e -> openUserDialog(user));

        Button toggleButton = new Button();
        if (user.getActivo()) {
            toggleButton.setIcon(new Icon(VaadinIcon.LOCK));
            toggleButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
            toggleButton.getElement().setAttribute("aria-label", "Desactivar usuario");
            toggleButton.addClickListener(e -> toggleUserStatus(user, false));
        } else {
            toggleButton.setIcon(new Icon(VaadinIcon.UNLOCK));
            toggleButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
            toggleButton.getElement().setAttribute("aria-label", "Activar usuario");
            toggleButton.addClickListener(e -> toggleUserStatus(user, true));
        }

        Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
        deleteButton.getElement().setAttribute("aria-label", "Eliminar usuario");
        deleteButton.addClickListener(e -> confirmDeleteUser(user));

        return new HorizontalLayout(editButton, toggleButton, deleteButton);
    }

    private void openUserDialog(UsuarioDTO user) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(user == null ? "Nuevo Usuario" : "Editar Usuario");
        dialog.setModal(true);
        dialog.setCloseOnEsc(true);
        dialog.setCloseOnOutsideClick(false);

        // Formulario
        FormLayout formLayout = new FormLayout();
        
        TextField usernameField = new TextField("Usuario");
        PasswordField passwordField = new PasswordField("Contraseña");
        EmailField emailField = new EmailField("Email");
        TextField nombreField = new TextField("Nombre");
        TextField apellidoField = new TextField("Apellido");
        TextField telefonoField = new TextField("Teléfono");
        TextField dniField = new TextField("DNI");
        TextField direccionField = new TextField("Dirección");
        
        ComboBox<RolSistema> rolComboBox = new ComboBox<>("Rol");
        rolComboBox.setItems(Arrays.asList(RolSistema.values()));
        rolComboBox.setItemLabelGenerator(RolSistema::toString);

        Checkbox activoCheckbox = new Checkbox("Usuario Activo");
        Checkbox cambioPasswordCheckbox = new Checkbox("Requiere Cambio de Contraseña");

        formLayout.add(
            usernameField, passwordField, emailField,
            nombreField, apellidoField, telefonoField,
            dniField, direccionField, rolComboBox,
            activoCheckbox, cambioPasswordCheckbox
        );

        // Binder para validación (no es necesario bindInstanceFields)
        Binder<CrearUsuarioDTO> binder = new Binder<>(CrearUsuarioDTO.class);
        
        // Configurar validaciones básicas si es necesario
        binder.forField(usernameField)
            .asRequired("El nombre de usuario es requerido")
            .bind(CrearUsuarioDTO::getUsername, CrearUsuarioDTO::setUsername);
        
        binder.forField(emailField)
            .asRequired("El email es requerido")
            .bind(CrearUsuarioDTO::getEmail, CrearUsuarioDTO::setEmail);

        // Si es edición, cargar datos
        if (user != null) {
            usernameField.setValue(user.getUsername());
            usernameField.setEnabled(false); // No permitir cambiar username
            passwordField.setVisible(false); // No mostrar password en edición
            emailField.setValue(user.getEmail());
            nombreField.setValue(user.getNombre());
            apellidoField.setValue(user.getApellido());
            telefonoField.setValue(user.getTelefono() != null ? user.getTelefono() : "");
            dniField.setValue(user.getDni() != null ? user.getDni() : "");
            direccionField.setValue(user.getDireccion() != null ? user.getDireccion() : "");
            rolComboBox.setValue(user.getRol());
            activoCheckbox.setValue(user.getActivo());
            cambioPasswordCheckbox.setValue(user.getCambioPasswordRequerido());
        } else {
            activoCheckbox.setValue(true);
            cambioPasswordCheckbox.setValue(true);
        }

        // Botones
        Button saveButton = new Button("Guardar", e -> {
            try {
                if (user == null) {
                    // Crear nuevo usuario
                    CrearUsuarioDTO dto = new CrearUsuarioDTO();
                    dto.setUsername(usernameField.getValue());
                    dto.setPassword(passwordField.getValue());
                    dto.setEmail(emailField.getValue());
                    dto.setNombre(nombreField.getValue());
                    dto.setApellido(apellidoField.getValue());
                    dto.setTelefono(telefonoField.getValue());
                    dto.setDni(dniField.getValue());
                    dto.setDireccion(direccionField.getValue());
                    dto.setRol(rolComboBox.getValue());
                    dto.setCambioPasswordRequerido(cambioPasswordCheckbox.getValue());

                    // Obtener usuario actual para auditoría
                    String currentUser = authContext.getAuthenticatedUser(UserDetails.class)
                            .map(UserDetails::getUsername).orElse("SYSTEM");

                    usuarioService.crearUsuario(
                        dto.getUsername(), dto.getPassword(), dto.getEmail(),
                        dto.getNombre(), dto.getApellido(), dto.getTelefono(),
                        dto.getDni(), dto.getDireccion(), dto.getRol(),
                        dto.isCambioPasswordRequerido(), currentUser
                    );

                    showNotification("Usuario creado exitosamente", NotificationVariant.LUMO_SUCCESS);
                } else {
                    // Actualizar usuario existente
                    String currentUser = authContext.getAuthenticatedUser(UserDetails.class)
                            .map(UserDetails::getUsername).orElse("SYSTEM");

                    usuarioService.actualizarUsuario(
                        user.getId(), emailField.getValue(), nombreField.getValue(),
                        apellidoField.getValue(), telefonoField.getValue(),
                        dniField.getValue(), direccionField.getValue(),
                        rolComboBox.getValue(), currentUser
                    );

                    showNotification("Usuario actualizado exitosamente", NotificationVariant.LUMO_SUCCESS);
                }

                dialog.close();
                refreshGrid();

            } catch (Exception ex) {
                showNotification("Error: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancelar", e -> dialog.close());

        HorizontalLayout buttonLayout = new HorizontalLayout(saveButton, cancelButton);
        
        dialog.add(formLayout);
        dialog.getFooter().add(buttonLayout);
        dialog.open();
    }

    private void toggleUserStatus(UsuarioDTO user, boolean activate) {
        try {
            String currentUser = authContext.getAuthenticatedUser(UserDetails.class)
                    .map(UserDetails::getUsername).orElse("SYSTEM");

            if (activate) {
                usuarioService.activarUsuario(user.getId(), currentUser);
                showNotification("Usuario activado exitosamente", NotificationVariant.LUMO_SUCCESS);
            } else {
                usuarioService.desactivarUsuario(user.getId(), currentUser);
                showNotification("Usuario desactivado exitosamente", NotificationVariant.LUMO_SUCCESS);
            }

            refreshGrid();
        } catch (Exception e) {
            showNotification("Error: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private void confirmDeleteUser(UsuarioDTO user) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Confirmar eliminación");
        dialog.setText("¿Está seguro que desea eliminar el usuario " + user.getUsername() + "?");
        dialog.setCancelable(true);
        dialog.setConfirmText("Eliminar");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> {
            try {
                usuarioService.eliminarUsuario(user.getId());
                showNotification("Usuario eliminado exitosamente", NotificationVariant.LUMO_SUCCESS);
                refreshGrid();
            } catch (Exception ex) {
                showNotification("Error: " + ex.getMessage(), NotificationVariant.LUMO_ERROR);
            }
        });

        dialog.open();
    }

    private void refreshGrid() {
        try {
            List<Usuario> usuarios = usuarioService.findAll();
            List<UsuarioDTO> usuariosDTO = usuarios.stream()
                    .map(this::convertirADTO)
                    .collect(Collectors.toList());

            dataProvider = new ListDataProvider<>(usuariosDTO);
            grid.setDataProvider(dataProvider);
        } catch (Exception e) {
            showNotification("Error al cargar usuarios: " + e.getMessage(), NotificationVariant.LUMO_ERROR);
        }
    }

    private UsuarioDTO convertirADTO(Usuario usuario) {
        UsuarioDTO dto = new UsuarioDTO();
        dto.setId(usuario.getId());
        dto.setUsername(usuario.getUsername());
        dto.setEmail(usuario.getEmail());
        dto.setNombre(usuario.getNombre());
        dto.setApellido(usuario.getApellido());
        dto.setTelefono(usuario.getTelefono());
        dto.setDni(usuario.getDni());
        dto.setDireccion(usuario.getDireccion());
        dto.setRol(usuario.getRol());
        dto.setActivo(usuario.getActivo());
        dto.setCuentaBloqueada(usuario.getCuentaBloqueada());
        dto.setCambioPasswordRequerido(usuario.getCambioPasswordRequerido());
        dto.setIntentosFallidos(usuario.getIntentosFallidos());
        dto.setUltimoAcceso(usuario.getUltimoAcceso());
        dto.setFechaCreacion(usuario.getFechaCreacion());
        dto.setFechaActualizacion(usuario.getFechaActualizacion());
        dto.setCreadoPor(usuario.getCreadoPor());
        dto.setActualizadoPor(usuario.getActualizadoPor());
        return dto;
    }

    private void showNotification(String message, NotificationVariant variant) {
        Notification notification = Notification.show(message);
        notification.addThemeVariants(variant);
        notification.setPosition(Notification.Position.TOP_CENTER);
    }
}
