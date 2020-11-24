package uy.gub.agesic.pdi.backoffice.views;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import uy.gub.agesic.pdi.backoffice.utiles.ui.BackofficeForm;
import uy.gub.agesic.pdi.backoffice.views.forms.TrabajarConUsuarioForm;

public class TrabajarConUsuario extends PaginaBase {

    private TrabajarConUsuarioForm form;

    public TrabajarConUsuario() {
        this.initPage();
    }

    public TrabajarConUsuario(final PageParameters parameters) {
        super(parameters);
        this.initPage();
        this.form.setParameters(parameters);
    }

    private void initPage() {
        this.form = new TrabajarConUsuarioForm();
        this.add(this.form);
        this.form.initForm();
        this.setPageTitle("AGESIC - Backoffice de la PDI - Trabajar con usuario");
    }

    @Override
    protected BackofficeForm getCurrentForm() {
        return this.form;
    }


}
