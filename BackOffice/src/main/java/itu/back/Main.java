package itu.back;

import itu.sprint.annotation.WebRoute;
import itu.sprint.util.Scanner;

public class Main {

    @WebRoute(url ="/main")
    public void def(){
        System.out.println("coucou les loulous");
    }
    public static void main(String[] args) {
        //  Utilise le Scanner pour afficher toutes les routes trouvées dans le package
        String requestString="/main";
        Scanner.printWebRouteAnnotations("itu.sprintest");
        Scanner.executeWebRoute("itu.sprintest",requestString);
        System.out.println("Hello World!");
    }
}
