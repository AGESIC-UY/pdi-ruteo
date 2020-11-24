package uy.gub.agesic.pdi.backoffice.utiles.ui;

import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.feedback.IFeedbackMessageFilter;

public class AllExceptFeedbackFilter implements IFeedbackMessageFilter {

    private IFeedbackMessageFilter[] filters = null;

    public AllExceptFeedbackFilter() {
        this(null);
    }

    public AllExceptFeedbackFilter(IFeedbackMessageFilter[] filters) {
        this.filters = filters;
    }

    @Override
    public boolean accept(FeedbackMessage message) {
        IFeedbackMessageFilter[] localFilters = (IFeedbackMessageFilter[]) getFilters();
        for (IFeedbackMessageFilter filter : localFilters) {
            if (filter.accept(message)) {
                return false;
            }
        }
        return true;
    }

    protected IFeedbackMessageFilter[] getFilters() {
        return filters;
    }
}
