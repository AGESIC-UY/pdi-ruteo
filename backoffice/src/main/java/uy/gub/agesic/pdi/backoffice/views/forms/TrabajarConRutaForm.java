package uy.gub.agesic.pdi.backoffice.views.forms;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.NumberTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.StatelessLink;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.ValidatorAdapter;
import org.apache.wicket.validation.validator.RangeValidator;
import org.apache.wicket.validation.validator.StringValidator;
import org.apache.wicket.validation.validator.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.agesic.pdi.backoffice.config.BackofficeProperties;
import uy.gub.agesic.pdi.backoffice.dtos.RutaDTO;
import uy.gub.agesic.pdi.backoffice.services.RutaService;
import uy.gub.agesic.pdi.backoffice.utiles.enumerados.ModoOperacion;
import uy.gub.agesic.pdi.backoffice.utiles.exceptions.BackofficeException;
import uy.gub.agesic.pdi.backoffice.utiles.spring.ApplicationContextProvider;
import uy.gub.agesic.pdi.backoffice.utiles.ui.BackofficeForm;
import uy.gub.agesic.pdi.backoffice.utiles.ui.BackofficePage;
import uy.gub.agesic.pdi.backoffice.utiles.ui.components.BotonAccion;
import uy.gub.agesic.pdi.backoffice.utiles.ui.components.ComponentCustomFeedbackPanel;
import uy.gub.agesic.pdi.backoffice.views.Error;
import uy.gub.agesic.pdi.backoffice.views.Rutas;

import javax.xml.validation.Validator;


@SuppressWarnings({"unchecked", "rawtypes"})
public class TrabajarConRutaForm extends BackofficeForm {

    private final static Logger logger = LoggerFactory.getLogger(TrabajarConRutaForm.class);

    // Propiedades del backoffice
    private BackofficeProperties properties;

    // Parametros
    private ModoOperacion modo;

    // Atributos de la ruta
    private String logical;
    private String logicalOld;
    private String physical;
    private String baseURI;
    private Boolean degraded;
    private Long degradeTimeout;
    private Long degradePermits;

    public TrabajarConRutaForm() {
        super("trabajarConRutaForm");
    }

    @Override
    public void initForm() {
        this.properties = ((BackofficePage)this.getPage()).getProperties();

        this.setDefaultModel(new CompoundPropertyModel<TrabajarConRutaForm>(this));

        final FormComponent<String> logical = new TextField<String>("logical").setRequired(true);
        logical.add(StringValidator.maximumLength(200));
        this.add(new ComponentCustomFeedbackPanel("logicalFeedback", new ComponentFeedbackMessageFilter(logical)));
        logical.setLabel(new Model("Direcci\u00F3n L\u00F3gica"));
        this.add(logical);

        final FormComponent<String> physical = new TextField<String>("physical").setRequired(true);
        physical.add(StringValidator.maximumLength(200));
        this.add(new ComponentCustomFeedbackPanel("physicalFeedback", new ComponentFeedbackMessageFilter(physical)));
        physical.setLabel(new Model("Direcci\u00F3n F\u00EDsica"));
        this.add(physical);

        final FormComponent<String> baseURI = new TextField<String>("baseURI").setRequired(false);
        baseURI.add(StringValidator.maximumLength(200));
        this.add(new ComponentCustomFeedbackPanel("baseURIFeedback", new ComponentFeedbackMessageFilter(baseURI)));
        baseURI.setLabel(new Model("Direcci\u00F3n Base"));
        this.add(baseURI);

        final FormComponent<Long> degradeTimeout = new NumberTextField<Long>("degradeTimeout").setMinimum(new Long(0)).setRequired(false);
        this.add(new ComponentCustomFeedbackPanel("degradeTimeoutFeedback", new ComponentFeedbackMessageFilter(degradeTimeout)));
        degradeTimeout.setLabel(new Model("Tiempo"));
        this.add(degradeTimeout);

        final FormComponent<Long> degradePermits = new NumberTextField<Long>("degradePermits").setMinimum(new Long(0)).setRequired(false);
        this.add(new ComponentCustomFeedbackPanel("degradePermitsFeedback", new ComponentFeedbackMessageFilter(degradePermits)));
        degradePermits.setLabel(new Model("Permitidos"));
        this.add(degradePermits);

        final CheckBox degraded = new  CheckBox("degraded", new PropertyModel<Boolean>(this, "degraded"));
        this.add(new ComponentCustomFeedbackPanel("degradedFeedback", new ComponentFeedbackMessageFilter(degraded)));
        degraded.setLabel(new Model("Degradable"));
        this.add(degraded);

        degraded.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
               if (target != null) {
                   if (degraded.getConvertedInput().booleanValue()){
                       degradePermits.setRequired(true);
                       degradeTimeout.setRequired(true);
                   }
                   else{
                       degradePermits.setRequired(false);
                       degradeTimeout.setRequired(false);
                   }
                }
            }
        });

        // Guardamos cambios
        this.add(new GuardarButton(this.getFinalMessage(properties.getMensajeConfirmacion())));

        // Se agrega el link de volver
        this.add(new LinkVolver());
    }

    @Override
    public void setParametersInner(PageParameters parameters) {
        // Se cargan los datos del filtro de la pagina anterior en el link de volver, por si se quiere volver al mismo lugar de donde se vino
        ((LinkVolver) this.get("linkVolver")).setParametersCallback(this.buscarParametrosDeFiltro(parameters));

        if (parameters.getNamedKeys().contains("modo")) {
            this.modo = parameters.get("modo").toEnum(ModoOperacion.class);
        }
        if (parameters.getNamedKeys().contains("logicalParameter")) {
            this.logical = parameters.get("logicalParameter").toString();
        }

        if (!this.modo.equals(ModoOperacion.ALTA)) {
            this.definirValoresIniciales();
        } else {
            // Para el caso de la direccion base, en modo alta ponemos el valor de la propiedad por defecto
            this.baseURI = this.properties.getDireccionBasePorDefecto();
        }

        // Si el modo es consulta, definir componentes como Readonly
        if (this.modo.equals(ModoOperacion.CONSULTA)) {
            this.get("logical").setEnabled(false);
            this.get("physical").setEnabled(false);
            this.get("baseURI").setEnabled(false);
            this.get("degraded").setEnabled(false);
            this.get("degradeTimeout").setEnabled(false);
            this.get("degradePermits").setEnabled(false);
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
            return !TrabajarConRutaForm.this.modo.equals(ModoOperacion.CONSULTA);
        }

        @Override
        public void ejecutar() {
            guardarRuta();
        }
    }

    private class LinkVolver extends StatelessLink {

        private PageParameters parametersCallback;

        public LinkVolver() {
            super("linkVolver");
        }

        @Override
        public void onClick() {
            setResponsePage(Rutas.class, parametersCallback);
        }

        public PageParameters getParametersCallback() {
            return parametersCallback;
        }

        public void setParametersCallback(PageParameters parametersCallback) {
            this.parametersCallback = parametersCallback;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected void guardarRuta () {
        try {
            RutaService rutaService = this.obtenerRutaService();

            if (ModoOperacion.ALTA.equals(this.modo) || ModoOperacion.MODIFICACION.equals(this.modo)) {
                RutaDTO ruta = ModoOperacion.ALTA.equals(this.modo) ? new RutaDTO() : rutaService.obtenerRuta(this.logicalOld);

                ruta.setLogical(this.logical);
                ruta.setPhysical(this.physical);
                ruta.setBaseURI(this.baseURI);
                ruta.setDegraded(this.degraded);
                ruta.setDegradePermits(this.degradePermits);
                ruta.setDegradeTimeout(this.degradeTimeout);

                if (ModoOperacion.ALTA.equals(this.modo)) {

                    if(rutaService.obtenerRuta(this.logical) != null) {
                        this.showError("Ya existe una ruta con la direcci\u00F3n l\u00F3gica: " + this.logical);
                        return;
                    }

                    rutaService.crearRuta(ruta, this.logicalOld);
                    getSession().success("Ruta creada exitosamente");
                    setResponsePage(Rutas.class);

                } else {

                    if((this.degraded) && ((this.degradeTimeout==null) || (this.degradePermits==null)))
                        this.showError("Ruta.modificar.required");

                    else{

                        rutaService.modificarRuta(ruta, this.logicalOld);
                        getSession().success("Ruta modificada exitosamente");
                        setResponsePage(Rutas.class);
                    }

                }
                // Cambio al modo a edicion
                this.modo = ModoOperacion.MODIFICACION;
                this.logical = ruta.getLogical();
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
            RutaDTO ruta = this.obtenerRutaService().obtenerRuta(this.logical);

            this.logical = ruta.getLogical();
            this.logicalOld = ruta.getLogical();
            this.physical = ruta.getPhysical();
            this.baseURI = ruta.getBaseURI();
            this.degraded = ruta.getDegraded();
            this.degradePermits = ruta.getDegradePermits();
            this.degradeTimeout = ruta.getDegradeTimeout();

        } catch (BackofficeException e) {
            this.showError(e);
        }
    }

    private RutaService obtenerRutaService() {
        RutaService rutaService = ApplicationContextProvider.getBean("rutaServiceImpl", RutaService.class);
        return rutaService;
    }
}

