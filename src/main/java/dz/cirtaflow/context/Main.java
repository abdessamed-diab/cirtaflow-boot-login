package dz.cirtaflow.context;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.Serializable;


public class Main implements Serializable{
    private static final Logger LOG= LogManager.getLogger(Main.class);

    /**
     * main method, this method is the entry point for cirtaflow project.
     * @param args arguments to be passed to the container, see IOC or CDI.
     */
    public static void main(String [] args) {
        LOG.debug("main method.");
        ConfigurableApplicationContext configurableApplicationContext =
                SpringApplication.run(CirtaflowBootApplicationEntryPoint.class);
    }

}
