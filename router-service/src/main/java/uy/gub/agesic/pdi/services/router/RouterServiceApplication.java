package uy.gub.agesic.pdi.services.router;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import uy.gub.agesic.pdi.services.router.controller.EchoServlet;
import uy.gub.agesic.pdi.services.router.controller.RouterServlet;

@SpringBootApplication(
        scanBasePackages = {"uy.gub.agesic.pdi.services", "uy.gub.agesic.pdi.common"}
)
@EnableEurekaClient
@EnableFeignClients
@EnableCircuitBreaker
@EnableCaching
@EnableAspectJAutoProxy
public class RouterServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RouterServiceApplication.class, args);
    }

    @Bean
    public ServletRegistrationBean routerServletBean(RouterServlet routerServlet) {
        ServletRegistrationBean bean = new ServletRegistrationBean(routerServlet, "/routerService/*");
        bean.setLoadOnStartup(1);
        return bean;
    }

    @Bean
    public ServletRegistrationBean echoServletBean(EchoServlet echoServlet) {
        ServletRegistrationBean bean = new ServletRegistrationBean(echoServlet, "/echo/*");
        bean.setLoadOnStartup(1);
        return bean;
    }
}
