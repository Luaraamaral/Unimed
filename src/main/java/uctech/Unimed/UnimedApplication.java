package uctech.Unimed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@SpringBootApplication
@EnableSwagger2
public class UnimedApplication {

//    @Override
//    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
//        return builder.sources(UnimedApplication.class);
//    }

    public static void main(String[] args) {
        SpringApplication.run(UnimedApplication.class, args);
    }


}
