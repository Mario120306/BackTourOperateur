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


