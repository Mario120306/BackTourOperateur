package itu.back.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour marquer les méthodes qui nécessitent un token JWT valide.
 * 
 * Utilisation:
 * 
 * <pre>
 * &#64;GetMapping("/api/data")
 * &#64;Json
 * @TokenRequired
 * public JsonResponse getData() {
 *     // Cette méthode nécessite un token valide
 * }
 * </pre>
 * 
 * Le token doit être envoyé dans le header HTTP "Authorization" avec le préfixe
 * "Bearer ".
 * Exemple: Authorization: Bearer votre_token_ici
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TokenRequired {
}
