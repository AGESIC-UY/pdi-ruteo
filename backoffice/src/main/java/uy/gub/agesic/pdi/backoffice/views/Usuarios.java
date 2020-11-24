package uy.gub.agesic.pdi.backoffice.views;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import uy.gub.agesic.pdi.backoffice.utiles.ui.BackofficeForm;
import uy.gub.agesic.pdi.backoffice.views.forms.UsuariosForm;

public class Usuarios extends PaginaBase {

    private static final long serialVersionUID = 1L;

    private UsuariosForm form;

    public Usuarios() {
        this.initPage();
    }

    public Usuarios(final PageParameters parameters) {
        super(parameters);

        this.initPage();
        this.form.setParameters(parameters);
    }

    private void initPage() {
        this.form = new UsuariosForm();
        this.add(this.form);
        this.form.initForm();

        this.setPageTitle("AGESIC - Backoffice de la PDI - Usuarios");
    }

    @Override
    protected BackofficeForm getCurrentForm() {
        return this.form;
    }
}
