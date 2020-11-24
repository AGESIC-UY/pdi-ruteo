package uy.gub.agesic.pdi.backoffice.views;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import uy.gub.agesic.pdi.backoffice.utiles.ui.BackofficeForm;
import uy.gub.agesic.pdi.backoffice.views.forms.IndexForm;

public class Index extends PaginaBase {
	 
	private static final long serialVersionUID = 1L;
	
	private IndexForm form;
 
    public Index() {
    	this.initPage();
    }

    public Index(final PageParameters parameters) {
    	super(parameters);

    	this.initPage();
    	this.form.setParameters(parameters);

	}

    private void initPage() {

    	this.form = new IndexForm();
    	this.add(this.form);
    	this.form.initForm();
    	this.setPageTitle("AGESIC - Backoffice PDI");
    }

	@Override
	protected BackofficeForm getCurrentForm() {
		return this.form;
	}

}