package uy.gub.agesic.pdi.backoffice.views.forms;

import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.feedback.ComponentFeedbackMessageFilter;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.validation.validator.StringValidator;
import uy.gub.agesic.pdi.backoffice.services.UsuarioService;
import uy.gub.agesic.pdi.backoffice.utiles.exceptions.BackofficeException;
import uy.gub.agesic.pdi.backoffice.utiles.spring.ApplicationContextProvider;
import uy.gub.agesic.pdi.backoffice.utiles.ui.BackofficeForm;
import uy.gub.agesic.pdi.backoffice.utiles.ui.components.ComponentCustomFeedbackPanel;
import uy.gub.agesic.pdi.backoffice.utiles.ui.sesion.BackofficeAuthenticationSession;


public class PasswordForm extends BackofficeForm {

    private static final long serialVersionUID = 1L;

    private String login;
    private String password;
    private String newPassword;

    public PasswordForm() {
        super("passwordForm");
    }

    @Override
    public void initForm() {
        this.setDefaultModel(new CompoundPropertyModel<PasswordForm>(this));

        final FormComponent<String> login = new TextField<String>("login");
        login.add(StringValidator.maximumLength(200));
        login.setEnabled(false);
        this.add(new ComponentCustomFeedbackPanel("loginFeedback", new ComponentFeedbackMessageFilter(login)));
        login.setLabel(new Model("Usuario"));
        this.add(login);

        FormComponent<String> password = new PasswordTextField("password").setRequired(true);
        this.add(new ComponentCustomFeedbackPanel("passwordFeedback", new ComponentFeedbackMessageFilter(password)));
        password.setLabel(new Model("Contraseña"));
        this.add(password);

        FormComponent<String> newPassword = new PasswordTextField("newPassword").setRequired(true);
        this.add(new ComponentCustomFeedbackPanel("newPasswordFeedback", new ComponentFeedbackMessageFilter(newPassword)));
        newPassword.add(StringValidator.maximumLength(15));
        newPassword.setLabel(new Model("Nueva Contraseña"));
        this.add(newPassword);

        this.add(new Button("confirmar") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onSubmit(){
                cambiarContraseña();
            }
        });

        this.login = ((BackofficeAuthenticationSession)AuthenticatedWebSession.get()).getUsername();
    }

    public void cambiarContraseña() {
        try {
            boolean autorizado = this.obtenerUsuarioService().authenticate(this.login, this.password);

            if(!autorizado){
                this.showError("Contraseña anterior inv\u00E1lida.");
            } else if (this.password.equals(this.newPassword)) {
                    getSession().success("Debe ingresar una contraseña distinta a la anterior.");
                    return;

                } else {
                    boolean success = this.obtenerUsuarioService().cambiarContrasena(this.login, this.password, this.newPassword);

                    if (success) {
                        this.showSuccess("Operacion.exitosa");
                        ((BackofficeAuthenticationSession) AuthenticatedWebSession.get()).invalidateNow();
                        setResponsePage(getApplication().getHomePage());

                    }
                }

        } catch (BackofficeException e){
            this.showError(e);
        }
    }

    @Override
    public void setParametersInner(PageParameters parameters) {
    }

    private UsuarioService obtenerUsuarioService() {
        UsuarioService usuarioService = ApplicationContextProvider.getBean("usuarioServiceImpl", UsuarioService.class);

        return usuarioService;
    }
}
