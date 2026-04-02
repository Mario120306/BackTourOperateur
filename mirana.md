1) à partir d'une réservation faite , on prend tous les réservations dans le tranche du temps_attente 
et il forme un groupe

2) pendant ce tranche on récupère tous les véhicules dispo :
-on vérifie si le véhicule est déjà dispo à cette heure selon l'heure_disponibilté
-si le véhicule est déjà revenu si elle a déjà enmener des réservattions

3) l'heure de départ de ce groupe sera le dernier heure du réservation ou celui dernier retour d'un véhicule ou si un véhicule devient dispo selon l'heure de disponibilité , on prend le dernier 
comme heure du départ de tous les véhicules dans ce tranche

4) on priorise d'abord le réservation avec le max de passager puis on cherche la voiture avec le plus
près de nombre de place (égal ou supérieur) 
    a - si on trouve une voiture egal au nombre de passager on le prend 
    b - sinon on priorise le supérieur on l'y ajoute puis on va essayer de remplir le véhicule directement en cherchant une réservation égal ou sup au reste de place pour l'y ajouter
    c - et sinon dans le cas où ils sont inférieur on découpe la réservation puis le reste on essaie de 
    le caser de suite dans un véhicule avec le plus près égal ou supérieur comme tout à l'heure 
Rem : si plusieurs voitures ont le même capacité :
    on choisi le véhicule ayant fait le moins de trajet (pas la distance parcouru mais juste s'ils ont déjà fait)
    puis si ils ont fait le même nb de trajet on choisi par priorité de type de carburant

5) et ainsi de suite pour les restes des réservations qui n'ont pas encore été assignés dans ce groupe

6) si une réservation est non assignée et qu'un véhicule revient avant la prochain groupe de réservation
    a - si la voiture est pleine elle part de suite 
    b - si la voiture n'est pas pleine, on ajoute un le temps_attente et :
        a-1 s'il n'y a aucune réservation  elle peut partir de suite même si elle n'est pas pleine
        a-2 si des réservation arrivent entre temps on 
        essaie de l'y ajouter comme avec la logique de tout à l'heure mais juste que les non assignés 
        sont en priorité et dès que leur véhicule sont remplis elles peuvent partir
        tout de suite sans attendre les autres

Rem : 
-si plusieurs passagers sont non assignées (de réservation différente) avec / ou plusieurs
véhicules qui arrivent en même temps , les règles de gestion d'avant sont appliqués (on priorise la réservation
avec le plus de passager , ....)
-si un véhicule emmène des réservations non assignées elle peut partir de suite dès qu'elle est pleine
-si un véhicule emmène des réservations non assignées et qu'il y a des réservations qui arrivent dans la tranche du temps
d'attente, on cherche la réservation qui correpond le plus au reste des places comme avec le logique de tout à l'heure (égal ou supérieur)


