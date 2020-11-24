package uy.gub.agesic.pdi.services.timestamp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication(
		scanBasePackages = {"uy.gub.agesic.pdi.services", "uy.gub.agesic.pdi.common"}
)
@EnableEurekaClient
@EnableFeignClients
@EnableAspectJAutoProxy
public class TimestampServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(TimestampServiceApplication.class, args);
	}
}
