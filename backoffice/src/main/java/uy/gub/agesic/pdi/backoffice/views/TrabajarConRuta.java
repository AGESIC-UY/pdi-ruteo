package uy.gub.agesic.pdi.backoffice.views;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import uy.gub.agesic.pdi.backoffice.utiles.enumerados.ModoOperacion;
import uy.gub.agesic.pdi.backoffice.utiles.ui.BackofficeForm;
import uy.gub.agesic.pdi.backoffice.views.forms.TrabajarConRutaForm;

public class TrabajarConRuta extends PaginaBase {

	private TrabajarConRutaForm form;

    public TrabajarConRuta() {
    	this.initPage();
    }

    public TrabajarConRuta(final PageParameters parameters) {
    	super(parameters);
    	this.initPage();
    	this.form.setParameters(parameters);
    }

    private void initPage() {
    	this.form = new TrabajarConRutaForm();
    	this.add(this.form);
    	this.form.initForm();
    	this.setPageTitle("AGESIC - Backoffice de la PDI - Trabajar con ruta");
    }

	@Override
	protected BackofficeForm getCurrentForm() {
		return this.form;
	}

}