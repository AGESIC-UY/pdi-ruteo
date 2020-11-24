package uy.gub.agesic.pdi.backoffice.views;

import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.StatelessLink;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import uy.gub.agesic.pdi.backoffice.utiles.exceptions.BackofficeException;
import uy.gub.agesic.pdi.backoffice.utiles.soporte.DateUtil;
import uy.gub.agesic.pdi.backoffice.utiles.ui.AllExceptFeedbackFilter;
import uy.gub.agesic.pdi.backoffice.utiles.ui.BackofficePage;
import uy.gub.agesic.pdi.backoffice.utiles.ui.components.GlobalCustomFeedbackPanel;

import java.util.ArrayList;
import java.util.List;

public abstract class PaginaBase extends BackofficePage {

    public PaginaBase() {
    	this.initPage();
    }

    public PaginaBase(final PageParameters parameters) {
		super(parameters);
		
    	this.initPage();
	}

    @SuppressWarnings("rawtypes")
	private void initPage() {

        this.setVersioned(false);
    	this.setStatelessHint(true);
    	
    	// Etiquetas generales
    	this.add(new Label("title", Model.of("AGESIC - Backoffice de la PDI")).setEscapeModelStrings(false));
    	this.add(new Label("labelFechaVersion", Model.of(String.format("&copy; %s AGESIC - v%s", DateUtil.currentYear().toString(), this.getProperties().getAppVersion()))).setEscapeModelStrings(false));

    	// Items de menu para manipular luego
    	this.add(new WebMarkupContainer("mnuItemRutas"));
        this.add(new WebMarkupContainer("mnuItemUsuarios"));
        this.add(new WebMarkupContainer("mnuItemPassword"));

        // Link de salida
        WebMarkupContainer logoutMenuItem = new WebMarkupContainer("logoutMenuItem");
        logoutMenuItem.add(new StatelessLink("logoutLink") {
        	private static final long serialVersionUID = 1L;

			@Override
            public void onClick() {
                try {
                    PaginaBase.this.logout();
                } catch (BackofficeException e) {
                    PaginaBase.this.getCurrentForm().showError(e);
                }
            }
        });
        this.add(logoutMenuItem);

        // Area general de feedback
        final GlobalCustomFeedbackPanel pageFeedback = new GlobalCustomFeedbackPanel("pageFeedback");
        pageFeedback.setFilter(new AllExceptFeedbackFilter() {
			private static final long serialVersionUID = 1L;
			@SuppressWarnings("unchecked")
			@Override
            protected IFeedbackMessageFilter[] getFilters() {
                final List filters = new ArrayList();
                getPage().visitChildren(FeedbackPanel.class, new IVisitor() {
                    public void component(Object object, IVisit visit) {
                        if (pageFeedback.equals(object)) {
                            visit.dontGoDeeper();
                        } else {
                            filters.add(((FeedbackPanel) object).getFilter());
                        }
                    }
                });
                return (IFeedbackMessageFilter[]) filters.toArray(new IFeedbackMessageFilter[filters.size()]);
            }
        });
        this.add(pageFeedback);    	

        // Menu de funcionalidades
        this.buildHeaderElements();
    }

	protected void setPageTitle(String title) {  
    	this.get("title").setDefaultModelObject(title);
    }  
    
    private void buildHeaderElements() {
        // Agregamos un ID para el link de logout
        this.get("logoutMenuItem").get("logoutLink").setMarkupId("logoutLink");
    }
    
}


