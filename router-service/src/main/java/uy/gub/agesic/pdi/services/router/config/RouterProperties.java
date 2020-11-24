package uy.gub.agesic.pdi.services.router.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@RefreshScope
public class RouterProperties implements Serializable {

    @Value("${application.accessManager.enabled:false}")
    private Boolean accessManagerEnabled;

    @Value("${application.copyBody.enabled:false}")
    private Boolean copyBodyEnabled;

    @Value("${application.router.timeout:60}")
    private int routerTimeout;

    public Boolean getAccessManagerEnabled() {
        return accessManagerEnabled;
    }

    public Boolean getCopyBodyEnabled() { return copyBodyEnabled; }

    public int getRouterTimeout() { return routerTimeout; }

    public void setAccessManagerEnabled(Boolean accessManagerEnabled) { this.accessManagerEnabled = accessManagerEnabled; }

    public void setCopyBodyEnabled(Boolean copyBodyEnabled) {
        this.copyBodyEnabled = copyBodyEnabled;
    }

    public void setRouterTimeout(int routerTimeout) { this.routerTimeout = routerTimeout; }
}
