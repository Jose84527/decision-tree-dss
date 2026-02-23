import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"config", "controller", "service"})
public class DecisionTreeDssApplication {

    public static void main(String[] args) {
        SpringApplication.run(DecisionTreeDssApplication.class, args);
    }
}