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
import org.apache.wicket.request.mapper.parameter.PageParameters;
import uy.gub.agesic.pdi.backoffice.config.BackofficeProperties;
import uy.gub.agesic.pdi.backoffice.dtos.FiltroRutaDTO;
import uy.gub.agesic.pdi.backoffice.dtos.RutaDTO;
import uy.gub.agesic.pdi.backoffice.services.RutaService;
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
import uy.gub.agesic.pdi.backoffice.views.TrabajarConRuta;
import uy.gub.agesic.pdi.common.utiles.dtos.ResultadoPaginadoDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RutasForm extends BackofficeForm {

	private static final long serialVersionUID = 1L;

	// Filtros
	private String logical;
	private String physical;
	private String baseURI;
	
	private Integer nroPagina;

	String permiso;
	String logedUser;

    // Rutas recuperadas y seleccionados
    private List<RutaDTO> rutas;
    private List<RutaDTO> rutasSeleccion;

    // Componentes visuales
	private CustomPageNavigator<RutaDTO> navigator;

	public RutasForm() {
		super("rutasForm");
	}
	
	@Override
	public void initForm() {
		this.setDefaultModel(new CompoundPropertyModel<RutasForm>(this));

		this.logedUser = ((BackofficeAuthenticationSession) AuthenticatedWebSession.get()).getUsername();

		try{
			if(this.logedUser != null){
				permiso = this.obtenerUsuarioService().permisoUsuario(this.logedUser);
			}
		}
		catch (BackofficeException e){
			showError("Error.General");
		}

		this.rutas = new ArrayList<RutaDTO>();
		this.rutasSeleccion = new ArrayList<RutaDTO>();

		FormComponent<String> logicalField = new TextField<String>("logical");
		this.add(logicalField);

		FormComponent<String> physicalField = new TextField<String>("physical");
		this.add(physicalField);

		FormComponent<String> baseURIField = new TextField<String>("baseURI");
		this.add(baseURIField);
		
        this.add(new Button("buscar") {
    		private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit(){
            	RutasForm.this.nroPagina = 0;
				buscarRutas(null);
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


		this.add(new Button("limpiarFiltros") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(){
				limpiarFiltros();
			}
		});


		BackofficeProperties properties = ((BackofficePage)this.getPage()).getProperties();

		this.add(new EliminarRutasButton(this.getFinalMessage(properties.getMensajeConfirmacion()), this.getFinalMessage(properties.getMensajeReconfirmacion())));

		// Agregamos el cuerpo de la grilla
        CheckGroup<RutaDTO> group = new CheckGroup<RutaDTO>("group", this.rutasSeleccion);
        group.setOutputMarkupId(true);
        this.add(group);

        CheckGroupSelector groupSelector = new CheckGroupSelector("groupSelector");
        group.add(groupSelector);
		RutasDataView dataView = new RutasDataView("rows", new ListDataProvider<RutaDTO>(this.rutas));
        group.add(dataView);

		// Agregamos un navigator
		this.navigator = new CustomPageNavigator<RutaDTO>("pagingNavigator") {
			private static final long serialVersionUID = 1L;

			public void gotoPage(Integer pagina) {
				RutasForm.this.nroPagina = pagina;
				buscarRutas(pagina);
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

		buscarRutas(null);

	}

	@Override
	public void setParametersInner(PageParameters parameters) {
		Boolean aplicarFiltros = this.cargarFiltrosPorParametrosPagina(parameters);

		if (aplicarFiltros) {
			this.buscarRutas(null);

			if (this.nroPagina > 0) {
				this.buscarRutas(this.nroPagina);
			}
		}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// Clases auxiliares 

	private class EliminarRutasButton extends BotonAccion {

		private static final long serialVersionUID = 1L;

		public EliminarRutasButton(String mensajeConfirmacion, String mensajeReconfirmacion) {
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
        	buscarRutas(null);
        }
	}

    private class RutasDataView extends DataView<RutaDTO> {

		public RutasDataView(String id, ListDataProvider<RutaDTO> dataProvider) {
			super(id, dataProvider);
		}

		@SuppressWarnings("rawtypes")
		@Override 
		protected void populateItem(Item<RutaDTO> item) {
			final RutaDTO info = item.getModelObject();

			// Checkbox de seleccion
			Check<RutaDTO> chkSelector = new Check<RutaDTO>("dataRowSelector", item.getModel());
			item.add(chkSelector);
			
			// Enlace para acceder a la consulta/modificacion del servicio
			StatelessLink dataRowLink = new StatelessLink("dataRowLink") {
				private static final long serialVersionUID = 1L;

				@Override
	            public void onClick() {
		            PageParameters parameters = new PageParameters();
	        		parameters.add("modo", ModoOperacion.CONSULTA);
	        		parameters.add("logicalParameter", info.getLogical());

					// Se cargan los filtros en forma de parametros para utilizarlos a la vuelta y se anexan a los parametros pertenecientes a la siguiente pagina
					PageParameters parametrosFiltro = RutasForm.this.filtrosAParametrosPagina();
					parameters.mergeWith(parametrosFiltro);

	            	setResponsePage(TrabajarConRuta.class, parameters);
	            }			
			};			
			item.add(dataRowLink); 

    		dataRowLink.add(new Label("dataRowLogical", info.getLogical()));

			// Datos puramente visuales
			RepeatingView repeatingView = new RepeatingView("dataRow");
			repeatingView.add(new Label(repeatingView.newChildId(), info.getPhysical()));
			repeatingView.add(new Label(repeatingView.newChildId(), info.getBaseURI()));

			String degradedText = "NO";

			if (info.getDegraded())
				degradedText = "SI";

			repeatingView.add(new Label(repeatingView.newChildId(),degradedText));
			repeatingView.add(new Label(repeatingView.newChildId(), info.getDegradeTimeout()));
			repeatingView.add(new Label(repeatingView.newChildId(), info.getDegradePermits()));
			item.add(repeatingView);
		} 
    }

	////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Handlers de eventos
    
	public void buscarRutas(Integer pagina) {
		try {
			FiltroRutaDTO filtro = new FiltroRutaDTO(pagina, this.navigator.getPageSize());
			filtro.setLogical(this.logical);
			filtro.setPhysical(this.physical);
			filtro.setBaseURI(this.baseURI);

			ResultadoPaginadoDTO<RutaDTO> resultado = this.obtenerRutaService().buscarRutas(filtro);
			
			this.rutas.clear();
			
			if (pagina == null) {
				this.rutasSeleccion.clear();
			}

			if (resultado != null) {
				this.rutas.addAll(resultado.getResultado());
			}

			this.navigator.setState(pagina, resultado == null ? 0 : resultado.getTotalTuplas());

			if(rutas.size() == 0){
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
		parameters.add("logicalParameter", "");

		// Se cargan los filtros en forma de parametros para utilizarlos a la vuelta y se anexan a los parametros pertenecientes a la siguiente pagina
		PageParameters parametrosFiltro = this.filtrosAParametrosPagina();
		parameters.mergeWith(parametrosFiltro);

		setResponsePage(TrabajarConRuta.class, parameters);
	}

	public void modificar() {
		if (this.rutasSeleccion == null || this.rutasSeleccion.size() == 0) {
			showError("Rutas.seleccion.vacia");
			return;
		}

		RutaDTO ruta = this.rutasSeleccion.get(0);

		PageParameters parameters = new PageParameters();
		parameters.add("modo", ModoOperacion.MODIFICACION);
		parameters.add("logicalParameter", ruta.getLogical());

		// Se cargan los filtros en forma de parametros para utilizarlos a la vuelta y se anexan a los parametros pertenecientes a la siguiente pagina
		PageParameters parametrosFiltro = this.filtrosAParametrosPagina();
		parameters.mergeWith(parametrosFiltro);

		setResponsePage(TrabajarConRuta.class, parameters);
	}

	public void eliminar() {
		if (this.rutasSeleccion == null || this.rutasSeleccion.size() == 0) {
			showError("Rutas.seleccion.vacia");
			return;
		}

		List<String> ids = this.rutasSeleccion.stream().map(RutaDTO::getLogical).collect(Collectors.toList());

    	try {
			this.obtenerRutaService().eliminarRutas(ids);
			
			this.showSuccess("Operacion.exitosa");
		} catch (BackofficeException e) {
			showError("Rutas.error.eliminar");
		}
	}

	@Override
	protected PageParameters filtrosAParametrosPagina() {
		PageParameters parameters = new PageParameters();

		if (this.nroPagina != null) {
			parameters.add("filtro_nroPagina", this.nroPagina);
		}

		if (this.logical != null && !this.logical.trim().isEmpty()) {
			parameters.add("filtro_logical", this.logical);
		}

		if (this.physical != null && !this.physical.trim().isEmpty()) {
			parameters.add("filtro_physical", this.physical);
		}

		if (this.baseURI != null && !this.baseURI.trim().isEmpty()) {
			parameters.add("filtro_baseURI", this.baseURI);
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

			if (parameters.getNamedKeys().contains("filtro_logical")) {
				this.logical = parameters.get("filtro_logical").toString();
				filtroCargado = true;
			}

			if (parameters.getNamedKeys().contains("filtro_physical")) {
				this.physical = parameters.get("filtro_physical").toString();
				filtroCargado = true;
			}

			if (parameters.getNamedKeys().contains("filtro_baseURI")) {
				this.baseURI = parameters.get("filtro_baseURI").toString();
				filtroCargado = true;
			}
		} catch (Exception ex) {
			filtroCargado = false;
		}

		return (filtroCargado);
	}

	public void limpiarFiltros(){
		this.get("logical").getDefaultModel().setObject(null);
		this.get("physical").getDefaultModel().setObject(null);
		this.get("baseURI").getDefaultModel().setObject(null);
	}

	private RutaService obtenerRutaService() {
		RutaService rutaService = ApplicationContextProvider.getBean("rutaServiceImpl", RutaService.class);
		return rutaService;
	}

	private UsuarioService obtenerUsuarioService() {
		UsuarioService usuarioService = ApplicationContextProvider.getBean("usuarioServiceImpl", UsuarioService.class);

		return usuarioService;
	}

}

