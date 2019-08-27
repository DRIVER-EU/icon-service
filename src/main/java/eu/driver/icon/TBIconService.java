package eu.driver.icon;

import static springfox.documentation.builders.PathSelectors.regex;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@ComponentScan
@EnableSwagger2
@SpringBootApplication
public class TBIconService {

	private Logger log = Logger.getLogger(this.getClass());

	public TBIconService() throws Exception {
		log.info("Init. CISRestAdaptor");
	}
	
	public static void main(String[] args) throws Exception {
		SpringApplication.run(TBIconService.class, args);
    }
	
	@Bean
    public Docket newsApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .groupName("TBIconService")
                .apiInfo(apiInfo())
                .select()
                .paths(regex("/TBIconService.*"))
                .build();
    }
	
	private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("TBIconService REST Interface API Doc.")
                .description("This is the TBIconService REST Interface API Documentation made with Swagger.")
                .version("1.0")
                .build();
    }
}
