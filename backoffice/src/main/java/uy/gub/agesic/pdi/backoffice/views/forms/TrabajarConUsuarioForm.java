package uy.gub.agesic.pdi.backoffice.views.forms;

import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.link.StatelessLink;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.StringValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.agesic.pdi.backoffice.config.BackofficeProperties;
import uy.gub.agesic.pdi.backoffice.dtos.UsuarioDTO;
import uy.gub.agesic.pdi.backoffice.services.UsuarioService;
import uy.gub.agesic.pdi.backoffice.utiles.enumerados.ModoOperacion;
import uy.gub.agesic.pdi.backoffice.utiles.enumerados.PermisoUsuario;
import uy.gub.agesic.pdi.backoffice.utiles.exceptions.BackofficeException;
import uy.gub.agesic.pdi.backoffice.utiles.spring.ApplicationContextProvider;
import uy.gub.agesic.pdi.backoffice.utiles.ui.BackofficeForm;
import uy.gub.agesic.pdi.backoffice.utiles.ui.BackofficePage;
import uy.gub.agesic.pdi.backoffice.utiles.ui.components.BotonAccion;
import uy.gub.agesic.pdi.backoffice.utiles.ui.components.ComponentCustomFeedbackPanel;
import uy.gub.agesic.pdi.backoffice.views.Error;
import uy.gub.agesic.pdi.backoffice.views.Usuarios;

public class TrabajarConUsuarioForm extends BackofficeForm {

    private final static Logger logger = LoggerFactory.getLogger(TrabajarConUsuarioForm.class);

    // Parametros
    private ModoOperacion modo;
    //private Long id;
    private String login;
    private String nombre;
    private String apellido;
    private String password;
    private Boolean admin;

    public TrabajarConUsuarioForm() {
        super("trabajarConUsuarioForm");
    }

    @Override
    public void initForm() {

        BackofficeProperties properties = ((BackofficePage) this.getPage()).getProperties();

        this.setDefaultModel(new CompoundPropertyModel<TrabajarConUsuarioForm>(this));

        final FormComponent<String> login = new TextField<String>("login").setRequired(true);
        login.add(StringValidator.maximumLength(200));
        this.add(new ComponentCustomFeedbackPanel("loginFeedback", new ComponentFeedbackMessageFilter(login)));
        login.setLabel(new Model("Usuario"));
        this.add(login);

        final FormComponent<String> nombre = new TextField<String>("nombre").setRequired(false);
        nombre.add(StringValidator.maximumLength(200));
        this.add(new ComponentCustomFeedbackPanel("nombreFeedback", new ComponentFeedbackMessageFilter(nombre)));
        nombre.setLabel(new Model("Nombre"));
        this.add(nombre);

        final FormComponent<String> apellido = new TextField<String>("apellido").setRequired(false);
        apellido.add(StringValidator.maximumLength(200));
        this.add(new ComponentCustomFeedbackPanel("apellidoFeedback", new ComponentFeedbackMessageFilter(apellido)));
        apellido.setLabel(new Model("Apellido"));
        this.add(apellido);

        final FormComponent<String> password = new PasswordTextField("password").setRequired(true);
        password.add(StringValidator.maximumLength(15));
        this.add(new ComponentCustomFeedbackPanel("passwordFeedback", new ComponentFeedbackMessageFilter(password)));
        password.setLabel(new Model("Contraseña"));
        this.add(password);

        this.add(new Label("labelPassword", "Contraseña *"));

        final CheckBox admin = new CheckBox("admin", new PropertyModel<Boolean>(this, "admin"));
        this.add(new ComponentCustomFeedbackPanel("adminFeedback", new ComponentFeedbackMessageFilter(admin)));
        this.add(admin);

        // Guardamos cambios
        this.add(new TrabajarConUsuarioForm.GuardarButton(this.getFinalMessage(properties.getMensajeConfirmacion())));

        // Se agrega el link de volver
        this.add(new TrabajarConUsuarioForm.LinkVolver());

    }

    @Override
    public void setParametersInner(PageParameters parameters) {

        // Se cargan los datos del filtro de la pagina anterior en el link de volver, por si se quiere volver al mismo lugar de donde se vino
        ((TrabajarConUsuarioForm.LinkVolver) this.get("linkVolver")).setParametersCallback(this.buscarParametrosDeFiltro(parameters));

        if (parameters.getNamedKeys().contains("modo")) {
            this.modo = parameters.get("modo").toEnum(ModoOperacion.class);
        }
        if (parameters.getNamedKeys().contains("loginParameter")) {
            this.login = parameters.get("loginParameter").toString();
        }

        if (!this.modo.equals(ModoOperacion.ALTA)) {
            this.definirValoresIniciales();
        }

        // Si el modo es consulta, definir componentes como Readonly
        if (this.modo.equals(ModoOperacion.CONSULTA)) {
            this.get("login").setEnabled(false);
            this.get("nombre").setEnabled(false);
            this.get("apellido").setEnabled(false);
            this.get("password").setVisible(false);
            this.get("labelPassword").setVisible(false);
            this.get("admin").setEnabled(false);
        }

    }

    private class GuardarButton extends BotonAccion {

        public GuardarButton(String mensajeConfirmacion) {
            super("btnGuardar", Error.class, true, mensajeConfirmacion);
        }

        @Override
        public boolean poseePermisoEjecucion() {
            // El control en este caso ya fue realizado al ingresar a la pagina, segun el modo apropiado
            return true;
        }

        @Override
        public boolean isVisible() {
            return !TrabajarConUsuarioForm.this.modo.equals(ModoOperacion.CONSULTA);
        }

        @Override
        public void ejecutar() {
          guardarUsuario();
        }
    }


    private class LinkVolver extends StatelessLink {

        private PageParameters parametersCallback;

        public LinkVolver() {
            super("linkVolver");
        }

        @Override
        public void onClick() {
            setResponsePage(Usuarios.class, parametersCallback);
        }

        public PageParameters getParametersCallback() {
            return parametersCallback;
        }

        public void setParametersCallback(PageParameters parametersCallback) {
            this.parametersCallback = parametersCallback;
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void guardarUsuario () {
        try {
            UsuarioService usuarioService = this.obtenerUsuarioService();

            if (ModoOperacion.ALTA.equals(this.modo) || ModoOperacion.MODIFICACION.equals(this.modo)) {
                UsuarioDTO usuario = ModoOperacion.ALTA.equals(this.modo) ? new UsuarioDTO() : usuarioService.obtenerUsuario(this.login);

                usuario.setLogin(this.login);
                usuario.setNombre(this.nombre);
                usuario.setApellido(this.apellido);
                usuario.setPassword(this.password);

                if(admin)
                    usuario.setPermiso((PermisoUsuario.ESCRITURA).toString());
                else
                    usuario.setPermiso((PermisoUsuario.LECTURA).toString());


                if (ModoOperacion.ALTA.equals(this.modo)) {

                    if(usuarioService.obtenerUsuario(this.login) != null) {
                        this.showError("Ya existe el usuario: " + this.login);
                        return;
                    }

                    usuarioService.crearUsuario (usuario);
                    getSession().success("Usuario creado exitosamente");
                    setResponsePage(Usuarios.class);

                } else {
                    getSession().success("Usuario modificado exitosamente");
                    setResponsePage(Usuarios.class);
                    usuarioService.modificarUsuario (usuario);
                }
                // Cambio al modo a edicion
                this.modo = ModoOperacion.MODIFICACION;
                this.login = usuario.getLogin();
                this.definirValoresIniciales();
            }
        } catch (BackofficeException ex) {
            this.showError(ex);
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // AUXILIARES

    private void definirValoresIniciales() {
        try {
            UsuarioDTO usuario = this.obtenerUsuarioService().obtenerUsuario(this.login);

            //this.id = usuario.getId();
            this.login = usuario.getLogin();
            this.nombre = usuario.getNombre();
            this.apellido = usuario.getApellido();
            this.password = usuario.getPassword();

            if(usuario.getPermiso().equals((PermisoUsuario.ESCRITURA).toString()))
                this.admin = true;
            else
                this.admin = false;

            this.get("login").setEnabled(false);
            this.get("password").setVisible(false);
            this.get("labelPassword").setVisible(false);

        } catch (BackofficeException e) {
            this.showError(e);
        }

    }


    private UsuarioService obtenerUsuarioService() {
        UsuarioService usuarioService = ApplicationContextProvider.getBean("usuarioServiceImpl", UsuarioService.class);

        return usuarioService;
    }
}


