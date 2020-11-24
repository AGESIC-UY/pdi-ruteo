package uy.gub.agesic.pdi.backoffice.utiles.ui.components;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;

public class ComponentCustomFeedbackPanel extends FeedbackPanel {

	private static final long serialVersionUID = 1L;

	public ComponentCustomFeedbackPanel(String id) {
        super(id);
        this.setEscapeModelStrings(false);
    }

    public ComponentCustomFeedbackPanel(String id, IFeedbackMessageFilter filter) {
        super(id, filter);
        this.setEscapeModelStrings(false);
    }

    @Override
    protected String getCSSClass(FeedbackMessage message) {
        String css;
        switch (message.getLevel()){
            case FeedbackMessage.SUCCESS:
                css = "successMessage";
                break;
            case FeedbackMessage.WARNING:
                css = "warningMessage";
                break;
            case FeedbackMessage.ERROR:
                css = "errorMessage";
                break;
            default:
                css = "alert";
        }

        return css;
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();

        // Eliminamos el bullet del UL
        Component feedbackul = this.get("feedbackul");
        feedbackul.add(AttributeModifier.append("style", Model.of("list-style-type: none;" +
                "margin:0px;" +
                "padding: 0px;" +
                "list-style:none !important;" +
                "-webkit-margin-before: 0em;" +
                "-webkit-margin-after: 0em;" +
                "-webkit-margin-start: 0px;" +
                "-webkit-margin-end: 0px;" +
                "-webkit-padding-start: 0px;" +
                "-moz-margin-before: 0em;" +
                "-moz-margin-after: 0em;" +
                "-moz-margin-start: 0px;" +
                "-moz-margin-end: 0px;" +
                "-moz-padding-start: 0px;")));

        // Eliminamos el estilo (class) de los spans que estan dentro de los LIs
        ListView<?> lvMessages = (ListView<?>)this.get("feedbackul:messages");
        for (Component messages : lvMessages) {
            Component message = messages.get("message");
            if (message != null) {
                message.add(AttributeModifier.remove("class"));
            }
        }
    }
}