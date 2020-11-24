package uy.gub.agesic.pdi.backoffice.views.forms;

import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.link.StatelessLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import uy.gub.agesic.pdi.backoffice.config.BackofficeProperties;
import uy.gub.agesic.pdi.backoffice.dtos.FiltroUsuarioDTO;
import uy.gub.agesic.pdi.backoffice.dtos.UsuarioDTO;
import uy.gub.agesic.pdi.backoffice.services.UsuarioService;
import uy.gub.agesic.pdi.backoffice.utiles.enumerados.ModoOperacion;
import uy.gub.agesic.pdi.backoffice.utiles.enumerados.PermisoUsuario;
import uy.gub.agesic.pdi.backoffice.utiles.exceptions.BackofficeException;
import uy.gub.agesic.pdi.backoffice.utiles.spring.ApplicationContextProvider;
import uy.gub.agesic.pdi.backoffice.utiles.ui.BackofficeForm;
import uy.gub.agesic.pdi.backoffice.utiles.ui.BackofficePage;
import uy.gub.agesic.pdi.backoffice.utiles.ui.components.BotonAccion;
import uy.gub.agesic.pdi.backoffice.utiles.ui.components.CustomPageNavigator;
import uy.gub.agesic.pdi.backoffice.utiles.ui.sesion.BackofficeAuthenticationSession;
import uy.gub.agesic.pdi.backoffice.views.TrabajarConUsuario;
import uy.gub.agesic.pdi.common.utiles.dtos.ResultadoPaginadoDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class UsuariosForm extends BackofficeForm {

    private static final long serialVersionUID = 1L;

    private String login;
    private String nombre;
    private String apellido;

    private Integer nroPagina;

    String permiso;
    String logedUser;

    // Rutas recuperadas y seleccionados
    private List<UsuarioDTO> usuarios;
    private List<UsuarioDTO> usuariosSeleccion;

    private CustomPageNavigator<UsuarioDTO> navigator;

    public UsuariosForm() {
        super("usuariosForm");
    }

    @Override
    public void initForm() {

        this.setDefaultModel(new CompoundPropertyModel<UsuariosForm>(this));

        this.logedUser = ((BackofficeAuthenticationSession)AuthenticatedWebSession.get()).getUsername();

        try{

            if(this.logedUser != null){
                permiso = this.obtenerUsuarioService().permisoUsuario(this.logedUser);
            }

        }
        catch (BackofficeException e){
            showError("Error.General");
        }

        this.usuarios = new ArrayList<UsuarioDTO>();
        this.usuariosSeleccion = new ArrayList<UsuarioDTO>();

        FormComponent<String> loginField = new TextField<String>("login");
        loginField.setLabel(new Model("Usuario"));
        this.add(loginField);

        FormComponent<String> nombreField = new TextField<String>("nombre");
        nombreField.setLabel(new Model("Nombre"));
        this.add(nombreField);

        FormComponent<String> apellidoField = new TextField<String>("apellido");
        apellidoField.setLabel(new Model("Apellido"));
        this.add(apellidoField);

        this.add(new Button("buscar") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit(){
                UsuariosForm.this.nroPagina = 0;
                buscarUsuarios(null);
            }
        });


        this.add(new Button("agregar") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit() {
                agregar();
            }
        });

        this.add(new Button("modificar") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit() {
                modificar();
            }
        });


        BackofficeProperties properties = ((BackofficePage)this.getPage()).getProperties();

        this.add(new UsuariosForm.EliminarUsuariosButton(this.getFinalMessage(properties.getMensajeConfirmacion()), this.getFinalMessage(properties.getMensajeReconfirmacion())));

        // Agregamos el cuerpo de la grilla
        CheckGroup<UsuarioDTO> group = new CheckGroup<UsuarioDTO>("group", this.usuariosSeleccion);
        group.setOutputMarkupId(true);
        this.add(group);

        CheckGroupSelector groupSelector = new CheckGroupSelector("groupSelector");
        group.add(groupSelector);
        UsuariosForm.UsuariosDataView dataView = new UsuariosForm.UsuariosDataView("rows", new ListDataProvider<UsuarioDTO>(this.usuarios));
        group.add(dataView);

        // Agregamos un navigator
        this.navigator = new CustomPageNavigator<UsuarioDTO>("pagingNavigator") {
            private static final long serialVersionUID = 1L;

            public void gotoPage(Integer pagina) {
                UsuariosForm.this.nroPagina = pagina;
                buscarUsuarios(pagina);
            }
        };
        this.navigator.setCurrentPage(0);
        this.navigator.setTotalRows(0L);
        this.add(navigator);

        if(permiso != null) {

            if (permiso.equals((PermisoUsuario.LECTURA).toString())){

                this.get("agregar").setVisible(false);
                this.get("modificar").setVisible(false);
                this.get("eliminar").setVisible(false);
            }
        }

        buscarUsuarios(null);

    }

@Override
    public void setParametersInner(PageParameters parameters) {
    Boolean aplicarFiltros = this.cargarFiltrosPorParametrosPagina(parameters);

    if (aplicarFiltros) {
        this.buscarUsuarios(null);

        if (this.nroPagina > 0) {
            this.buscarUsuarios(this.nroPagina);
        }
    }

    }


    private class EliminarUsuariosButton extends BotonAccion {

        private static final long serialVersionUID = 1L;

        public EliminarUsuariosButton(String mensajeConfirmacion, String mensajeReconfirmacion) {
            super("eliminar",true, mensajeConfirmacion);
        }

        @Override
        public boolean poseePermisoEjecucion() {
            return true;
        }

        @Override
        public void ejecutar() {
            eliminar();
            limpiarFiltros();
            buscarUsuarios(null);
        }
    }


    private class UsuariosDataView extends DataView<UsuarioDTO> {

        public UsuariosDataView(String id, ListDataProvider<UsuarioDTO> dataProvider) {
            super(id, dataProvider);
        }

        @SuppressWarnings("rawtypes")
        @Override
        protected void populateItem(Item<UsuarioDTO> item) {
            final UsuarioDTO info = item.getModelObject();

            // Checkbox de seleccion
            Check<UsuarioDTO> chkSelector = new Check<UsuarioDTO>("dataRowSelector", item.getModel());
            item.add(chkSelector);

            // Enlace para acceder a la consulta/modificacion del servicio
            StatelessLink dataRowLink = new StatelessLink("dataRowLink") {
                private static final long serialVersionUID = 1L;

                @Override
                public void onClick() {
                    PageParameters parameters = new PageParameters();
                    parameters.add("modo", ModoOperacion.CONSULTA);
                    parameters.add("loginParameter", info.getLogin());

                    // Se cargan los filtros en forma de parametros para utilizarlos a la vuelta y se anexan a los parametros pertenecientes a la siguiente pagina
                    PageParameters parametrosFiltro = UsuariosForm.this.filtrosAParametrosPagina();
                    parameters.mergeWith(parametrosFiltro);

                    setResponsePage(TrabajarConUsuario.class, parameters);
                }
            };
            item.add(dataRowLink);

            dataRowLink.add(new Label("dataRowLogin", info.getLogin()));

            // Datos puramente visuales
            RepeatingView repeatingView = new RepeatingView("dataRow");
            repeatingView.add(new Label(repeatingView.newChildId(), info.getNombre()));
            repeatingView.add(new Label(repeatingView.newChildId(), info.getApellido()));

            String admin = "NO";

            if (info.getPermiso().equals((PermisoUsuario.ESCRITURA).toString()))
                admin = "SI";

            repeatingView.add(new Label(repeatingView.newChildId(),admin));



            item.add(repeatingView);
        }
    }

    public void buscarUsuarios (Integer pagina) {
        try {
            FiltroUsuarioDTO filtro = new FiltroUsuarioDTO(pagina, this.navigator.getPageSize());
            filtro.setLogin(this.login);
            filtro.setNombre(this.nombre);
            filtro.setApellido(this.apellido);

            ResultadoPaginadoDTO<UsuarioDTO> resultado = this.obtenerUsuarioService().buscarUsuarios(filtro);

            this.usuarios.clear();

            if (pagina == null) {
                this.usuariosSeleccion.clear();
            }
            if (resultado != null) {
                this.usuarios.addAll(resultado.getResultado());
            }

            this.navigator.setState(pagina, resultado.getTotalTuplas());

            if(usuarios.size() == 0){
                getSession().success("No se encontraron resultados para la b\u00FAsqueda");
                return;
            }

        } catch (BackofficeException ex) {
            this.showError(ex);
        }
    }

    public void agregar() {
        PageParameters parameters = new PageParameters();
        parameters.add("modo", ModoOperacion.ALTA);
        parameters.add("loginParameter", "");

        // Se cargan los filtros en forma de parametros para utilizarlos a la vuelta y se anexan a los parametros pertenecientes a la siguiente pagina
        PageParameters parametrosFiltro = this.filtrosAParametrosPagina();
        parameters.mergeWith(parametrosFiltro);

        setResponsePage(TrabajarConUsuario.class, parameters);
    }

    public void modificar() {
        if (this.usuariosSeleccion == null || this.usuariosSeleccion.size() == 0) {
            showError("Usuarios.seleccion.vacia");
            return;
        }

        UsuarioDTO usuario = this.usuariosSeleccion.get(0);

        PageParameters parameters = new PageParameters();
        parameters.add("modo", ModoOperacion.MODIFICACION);
        parameters.add("loginParameter", usuario.getLogin());

        // Se cargan los filtros en forma de parametros para utilizarlos a la vuelta y se anexan a los parametros pertenecientes a la siguiente pagina
        PageParameters parametrosFiltro = this.filtrosAParametrosPagina();
        parameters.mergeWith(parametrosFiltro);

        setResponsePage(TrabajarConUsuario.class, parameters);

    }

    public void eliminar() {
        if (this.usuariosSeleccion == null || this.usuariosSeleccion.size() == 0) {
            showError("Usuarios.seleccion.vacia");
            return;
        }


        List<String> logins = this.usuariosSeleccion.stream().map(UsuarioDTO::getLogin).collect(Collectors.toList());

        try {
            this.obtenerUsuarioService().eliminarUsuarios(logins);

            this.showSuccess("Operacion.exitosa");
        } catch (BackofficeException e) {
            showError("Usuarios.error.eliminar");
        }
    }



    @Override
    protected PageParameters filtrosAParametrosPagina() {
        PageParameters parameters = new PageParameters();

        if (this.nroPagina != null) {
            parameters.add("filtro_nroPagina", this.nroPagina);
        }

        if (this.login != null && !this.login.trim().isEmpty()) {
            parameters.add("filtro_login", this.login);
        }

        if (this.nombre != null && !this.nombre.trim().isEmpty()) {
            parameters.add("filtro_nombre", this.nombre);
        }

        if (this.apellido != null && !this.apellido.trim().isEmpty()) {
            parameters.add("filtro_apellido", this.apellido);
        }

        return (parameters);
    }


    @Override
    protected Boolean cargarFiltrosPorParametrosPagina(PageParameters parameters) {
        Boolean filtroCargado = false;

        try {
            if (parameters.getNamedKeys().contains("filtro_nroPagina")) {
                this.nroPagina = parameters.get("filtro_nroPagina").toInteger();
                filtroCargado = true;
            }

            if (parameters.getNamedKeys().contains("filtro_login")) {
                this.login = parameters.get("filtro_login").toString();
                filtroCargado = true;
            }

            if (parameters.getNamedKeys().contains("filtro_nombre")) {
                this.nombre = parameters.get("filtro_nombre").toString();
                filtroCargado = true;
            }

            if (parameters.getNamedKeys().contains("filtro_apellido")) {
                this.apellido = parameters.get("filtro_apellido").toString();
                filtroCargado = true;
            }
        } catch (Exception ex) {
            filtroCargado = false;
        }

        return (filtroCargado);
    }

    public void limpiarFiltros(){
        this.get("login").getDefaultModel().setObject(null);
        this.get("nombre").getDefaultModel().setObject(null);
        this.get("apellido").getDefaultModel().setObject(null);
    }


    private UsuarioService obtenerUsuarioService() {
        UsuarioService usuarioService = ApplicationContextProvider.getBean("usuarioServiceImpl", UsuarioService.class);

        return usuarioService;
    }


}