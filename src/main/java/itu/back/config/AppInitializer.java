package itu.back.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.itu.framework.mapping.Mapping;
import com.itu.framework.util.AnnotationScanner;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppInitializer implements ServletContextListener {

    private static List<Mapping> dynamicMappings = new ArrayList<>();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("==============================================");
        System.out.println("INITIALISATION DE L'APPLICATION TOUR OPERATEUR");
        System.out.println("==============================================");

        // Scanner les contrôleurs avec @Controller
        Map<String, Mapping> mappings = AnnotationScanner.scanForMappings("itu.back");
        dynamicMappings.addAll(mappings.values());

        System.out.println("\n==============================================");
        System.out.println("RÉSUMÉ DES MAPPINGS");
        System.out.println("==============================================");
        System.out.println("Total mappings : " + dynamicMappings.size());

        if (!dynamicMappings.isEmpty()) {
            System.out.println("\nToutes les routes :");
            dynamicMappings.forEach(mapping -> {
                com.itu.framework.util.RouteHandler rh = mapping.getRouteHandler();
                System.out.println("  - [" + rh.getHttpMethod() + "] " + mapping.getUrl() + " -> " +
                        rh.getControllerClass().getSimpleName() + "." +
                        rh.getMethod().getName());
            });
        }

        System.out.println("==============================================\n");

        // Stocker les mappings dans le contexte
        sce.getServletContext().setAttribute("dynamicMappings", dynamicMappings);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Application Tour Operateur arrêtée");
    }

    public static Mapping findMapping(String requestUrl, String httpMethod) {
        for (Mapping mapping : dynamicMappings) {
            com.itu.framework.util.RouteHandler rh = mapping.getRouteHandler();

            boolean urlMatches = mapping.getPattern().matcher(requestUrl).matches();
            boolean methodMatches = rh.getHttpMethod().equalsIgnoreCase("ALL") ||
                    rh.getHttpMethod().equalsIgnoreCase(httpMethod);

            if (urlMatches && methodMatches) {
                return mapping;
            }
        }
        return null;
    }

    public static List<Mapping> getAllMappings() {
        return new ArrayList<>(dynamicMappings);
    }
}
