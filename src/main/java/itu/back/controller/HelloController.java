package itu.back.controller;

import itu.sprint.annotation.AnnotationController;
import itu.sprint.annotation.MapURL;
import itu.sprint.annotation.PathVariable;
import itu.sprint.annotation.PathVariable;
import itu.sprint.mvc.ModelView;

@AnnotationController
public class HelloController {

    @MapURL(url = "/hello")
    public void sayHello() {
        System.out.println("Hello !");
    }

    @MapURL(url = "/bye")
    public void sayBye() {
        System.out.println("Bye !");
    }

    @MapURL(url = "/test",method = "POST")
    public ModelView showJsp() {
        ModelView mv = new ModelView();
        mv.addAttribute("nom", "NOOMMMMMMMM");
        mv.setView("/test.jsp"); // vue cible
        
        return mv;
    }
    @MapURL(url = "/welcome")
    public ModelView welcomeUser() {
        ModelView mv = new ModelView();
        mv.addAttribute("nom", "NOOMMMMMMMM");
        mv.setView("/test.jsp"); // vue cible
        
        return mv;
    }
    @MapURL(url = "/etudiants/{nom}/{id}")
    public String getEtudiant(@PathVariable("nom") String nom, @PathVariable("id") int id) {
        return "Étudiant " + nom + " ID: " + id;
    }

    /*@MapURL(url = "/eleves/{nom}/{id}/{test}")
    public String getEleve(@PathVariable("id") int studentId, String nom) {
        return "Élève " + nom + " ID: " + studentId;
    }*/
}
