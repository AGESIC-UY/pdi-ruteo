package uy.gub.agesic.pdi.backoffice.utiles.ui;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uy.gub.agesic.pdi.backoffice.utiles.exceptions.BackofficeException;

import java.util.List;
import java.util.MissingResourceException;

public abstract class BackofficeForm extends StatelessForm<CompoundPropertyModel<BackofficeForm>> {

    private final static Logger logger = LoggerFactory.getLogger(BackofficeForm.class);

    public static final String PARAM_MSJ_EXITO = "msjExito";
    public static final String PARAM_MSJ_ERROR = "msjError";
    public static final String PARAM_MSJ_WARNING = "msjWarning";

    public BackofficeForm(String id) {
        super(id);
        this.validateComponents();
    }

    /**
     * Esta funcion inicializa el formulario. Debe ser llamada manualmente por quien instancie este formulario, LUEGO de que el mismo
     * fue agregado al a pagina que lo contiene. Si esto no se hace asi, se generan errores con el Localizer de recursos al invocar a 
     * la funcion getFinalMessage(key)
     */
	public abstract void initForm();

    protected void logout() throws BackofficeException {
    	BackofficePage ownerPage = (BackofficePage)this.getPage();
    	ownerPage.logout();
    }

    public final void setParameters(PageParameters parameters) {
        if (parameters.getNamedKeys().contains(BackofficeForm.PARAM_MSJ_EXITO)) {
            showSuccess(parameters.get(BackofficeForm.PARAM_MSJ_EXITO).toString());
        } else if (parameters.getNamedKeys().contains(BackofficeForm.PARAM_MSJ_ERROR)) {
            showError(parameters.get(BackofficeForm.PARAM_MSJ_ERROR).toString());
        } else if (parameters.getNamedKeys().contains(BackofficeForm.PARAM_MSJ_WARNING)) {
            showWarning(parameters.get(BackofficeForm.PARAM_MSJ_WARNING).toString());
        }

        try {
            this.setParametersInner(parameters);
        } catch (Exception e) {
            logger.error("Error al inicializar los parametros del formulario", e);
        }
    }

    public abstract void setParametersInner(PageParameters parameters);
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Mensajeria
    
    public void showSuccess(String message) {
        message = this.getFinalMessage(message);
        this.success(message);
    }
    
    public void showWarning(String message) {
        message = this.getFinalMessage(message);
        this.warn(message);
    }
    
    public void showError(String message) {
        message = this.getFinalMessage(message);
        this.error(message);
    }

    public void showError(Throwable exception) {
        String message = this.getFinalMessage(exception.getMessage());
        this.error(message);
    }

    protected String getFinalMessage(String messageOrKey) {
        if (messageOrKey == null) {
            // Si la clave o el mensaje es nulo, ha ocurrido un error general.
            messageOrKey = "Error.General";
        }

        String message = null;
    	try {
    		message = this.getString(messageOrKey.replace("\n", ""));
    	} catch (MissingResourceException ex) {}
    	
        if (message != null) {
            return message;
        } else {
            return messageOrKey;
        }
    }

    /**
     * Este método devuelve un objeto <code>org.apache.ui.request.mapper.parameter.PageParameter</code> en base a los filtros cargados del formulario
     */
    protected PageParameters filtrosAParametrosPagina(){
        return null;
    }

    /**
     * Este método permite cargar los filtros del formulario (si tiene) a partir de los parametros que recibe
     * Devuelve TRUE si y solo si se cargó al menos un campo de filtro
     */
    protected Boolean cargarFiltrosPorParametrosPagina(PageParameters parameters) {
        return null;
    }

    protected PageParameters buscarParametrosDeFiltro(PageParameters parameters) {
        PageParameters parametrosFiltro = null;

        if (parameters != null) {
            parametrosFiltro = new PageParameters();

            for (String param : parameters.getNamedKeys()) {
                if (param.startsWith("filtro_")) {
                    parametrosFiltro.add(param, parameters.get(param));
                }
            }
        }

        return (parametrosFiltro);
    }

    protected Boolean contieneParametrosDeFiltro(PageParameters parameters) {
        if (parameters != null) {
            for (String param : parameters.getNamedKeys()) {
                if (param.startsWith("filtro_")) {
                    return true;
                }
            }
        }

        return false;
    }

    protected void injectHeadJavascript(String jsCode) {
        add(new Behavior() {
            public boolean isTemporary(Component component) {
                return true;
            }

            public void renderHead(Component component, IHeaderResponse response) {
                response.render(OnDomReadyHeaderItem.forScript(jsCode));
            }
        });
    }

}