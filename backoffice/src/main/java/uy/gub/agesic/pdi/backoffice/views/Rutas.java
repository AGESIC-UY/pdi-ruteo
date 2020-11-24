package uy.gub.agesic.pdi.backoffice.views;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import uy.gub.agesic.pdi.backoffice.utiles.ui.BackofficeForm;
import uy.gub.agesic.pdi.backoffice.views.forms.RutasForm;

public class Rutas extends PaginaBase {

	private static final long serialVersionUID = 1L;

	private RutasForm form;

    public Rutas() {
    	this.initPage();
    }

    public Rutas(final PageParameters parameters) {
    	super(parameters);

    	this.initPage();
    	this.form.setParameters(parameters);
    }

    private void initPage() {
    	this.form = new RutasForm();
    	this.add(this.form);
    	this.form.initForm();
    	
    	this.setPageTitle("AGESIC - Backoffice de la PDI - Rutas");
    }

	@Override
	protected BackofficeForm getCurrentForm() {
		return this.form;
	}
}