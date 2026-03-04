<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="itu.back.model.Reservation" %>
<%@ page import="itu.back.model.Client" %>
<%@ page import="itu.back.model.Hotel" %>
<%@ page import="itu.back.model.Vehicule" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="itu.back.util.SimulationService" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Simulation par Date - Tour Opérateur</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: #f5f7fa;
            min-height: 100vh;
        }

        .content-with-sidebar {
            display: flex;
            min-height: 100vh;
        }

        .main-content {
            flex: 1;
            margin-left: 280px;
            padding: 40px 60px;
            max-width: calc(100% - 280px);
            width: 100%;
        }

        .content-wrapper {
            max-width: 1400px;
            margin: 0 auto;
        }

        @media (max-width: 768px) {
            .main-content {
                margin-left: 0;
                padding: 20px;
                max-width: 100%;
            }
        }

        h1 {
            color: #2d3748;
            font-size: 2.2em;
            margin-bottom: 10px;
            display: flex;
            align-items: center;
            gap: 15px;
        }

        .subtitle {
            color: #718096;
            font-size: 1.1em;
            margin-bottom: 30px;
        }

        .search-form {
            background: white;
            padding: 25px;
            border-radius: 12px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            margin-bottom: 30px;
            display: flex;
            gap: 15px;
            align-items: end;
            flex-wrap: wrap;
        }

        .form-group {
            flex: 1;
            min-width: 200px;
        }

        .search-form label {
            display: block;
            font-weight: 600;
            color: #2d3748;
            margin-bottom: 8px;
        }

        .search-form input[type="date"] {
            width: 100%;
            padding: 12px 15px;
            border: 2px solid #e2e8f0;
            border-radius: 8px;
            font-size: 1em;
            transition: all 0.3s ease;
        }

        .search-form input[type="date"]:focus {
            outline: none;
            border-color: #667eea;
            box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
        }

        .btn-primary {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 12px 30px;
            border: none;
            border-radius: 8px;
            font-size: 1em;
            font-weight: 600;
            cursor: pointer;
            transition: transform 0.2s ease, box-shadow 0.2s ease;
        }

        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
        }

        .results-header {
            background: white;
            padding: 20px 25px;
            border-radius: 12px;
            margin-bottom: 20px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }

        .results-header h2 {
            color: #2d3748;
            font-size: 1.5em;
        }

        .badge {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 8px 18px;
            border-radius: 20px;
            font-weight: 600;
            font-size: 0.95em;
        }

        .vehicule-card {
            background: white;
            border-radius: 12px;
            padding: 25px;
            margin-bottom: 25px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
            border-left: 5px solid #667eea;
        }

        .vehicule-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
            padding-bottom: 15px;
            border-bottom: 2px solid #f7fafc;
        }

        .vehicule-info {
            flex: 1;
        }

        .vehicule-name {
            font-size: 1.4em;
            color: #2d3748;
            font-weight: 700;
            margin-bottom: 5px;
        }

        .vehicule-details {
            color: #718096;
            font-size: 0.95em;
        }

        .vehicule-details span {
            display: inline-block;
            margin-right: 15px;
        }

        .vehicule-timing {
            display: flex;
            gap: 20px;
            margin-top: 15px;
            padding: 15px;
            background: #f7fafc;
            border-radius: 8px;
        }

        .timing-item {
            display: flex;
            flex-direction: column;
            align-items: center;
        }

        .timing-label {
            font-size: 0.85em;
            color: #718096;
            text-transform: uppercase;
            letter-spacing: 0.5px;
            margin-bottom: 5px;
        }

        .timing-value {
            font-size: 1.3em;
            font-weight: 700;
            color: #2d3748;
        }

        .timing-depart {
            color: #667eea;
        }

        .timing-retour {
            color: #48bb78;
        }

        .timing-duree {
            color: #ed8936;
        }

        .reservation-count {
            background: #f7fafc;
            padding: 10px 20px;
            border-radius: 8px;
            font-weight: 600;
            color: #667eea;
        }

        .reservations-table {
            width: 100%;
            border-collapse: collapse;
        }

        .reservations-table thead {
            background: #f7fafc;
        }

        .reservations-table th {
            padding: 12px 15px;
            text-align: left;
            font-weight: 600;
            color: #4a5568;
            font-size: 0.9em;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        .reservations-table td {
            padding: 15px;
            border-bottom: 1px solid #f7fafc;
        }

        .reservations-table tbody tr:last-child td {
            border-bottom: none;
        }

        .reservations-table tbody tr:hover {
            background: #f7fafc;
        }

        .client-name {
            font-weight: 600;
            color: #2d3748;
            margin-bottom: 3px;
        }

        .client-email {
            color: #718096;
            font-size: 0.85em;
        }

        .hotel-name {
            font-weight: 600;
            color: #2d3748;
            margin-bottom: 3px;
        }

        .hotel-ville {
            color: #718096;
            font-size: 0.85em;
        }

        .passengers {
            display: inline-block;
            background: #667eea;
            color: white;
            padding: 5px 12px;
            border-radius: 15px;
            font-weight: 600;
            font-size: 0.9em;
        }

        .time-info {
            display: inline-block;
            padding: 5px 12px;
            background: #f7fafc;
            border-radius: 8px;
            color: #2d3748;
            font-weight: 600;
        }

        .no-results {
            background: white;
            padding: 60px 30px;
            border-radius: 12px;
            text-align: center;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }

        .no-results svg {
            width: 80px;
            height: 80px;
            color: #cbd5e0;
            margin-bottom: 20px;
        }

        .no-results h3 {
            color: #4a5568;
            font-size: 1.5em;
            margin-bottom: 10px;
        }

        .no-results p {
            color: #718096;
            font-size: 1.1em;
        }

        .back-link {
            display: inline-block;
            margin-top: 25px;
            color: #667eea;
            text-decoration: none;
            font-weight: 600;
            font-size: 1em;
        }

        .back-link:hover {
            text-decoration: underline;
        }

        .empty-vehicle {
            text-align: center;
            padding: 30px;
            color: #718096;
            font-style: italic;
        }

        .alert-warning {
            background: #fff3cd;
            border-left: 5px solid #ffc107;
            padding: 20px 25px;
            border-radius: 12px;
            margin-bottom: 25px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }

        .alert-warning h3 {
            color: #856404;
            margin-bottom: 15px;
            font-size: 1.3em;
        }

        .alert-warning p {
            color: #856404;
            margin-bottom: 10px;
        }

        .unassigned-list {
            list-style: none;
            margin-top: 15px;
        }

        .unassigned-item {
            background: white;
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 10px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            border-left: 3px solid #ffc107;
        }

        .unassigned-item-info {
            flex: 1;
        }
    </style>
</head>
<body>
    <%@ include file="../includes/sidebar.html" %>
    
    <div class="content-with-sidebar">
        <div class="main-content">
            <div class="content-wrapper">
            <h1>🚗 Simulation par Date</h1>
            <p class="subtitle">Visualisez l'assignation des véhicules aux réservations pour une date donnée</p>

            <form class="search-form" method="get" action="<%= request.getContextPath() %>/reservation/par-date">
                <div class="form-group">
                    <label for="date">Sélectionnez une date :</label>
                    <input type="date" id="date" name="date" 
                           value="<%= request.getAttribute("dateRecherche") != null ? request.getAttribute("dateRecherche") : "" %>" 
                           required>
                </div>
                <button type="submit" class="btn-primary">🔍 Lancer la simulation</button>
            </form>

            <% 
            String dateRecherche = (String) request.getAttribute("dateRecherche");
            Map<Vehicule, List<Reservation>> vehiculesAvecReservations = (Map<Vehicule, List<Reservation>>) request.getAttribute("vehiculesAvecReservations");
            Map<Vehicule, SimulationService.InfosTrajet> infosTrajetParVehicule = (Map<Vehicule, SimulationService.InfosTrajet>) request.getAttribute("infosTrajetParVehicule");
            List<Reservation> reservationsNonAssignees = (List<Reservation>) request.getAttribute("reservationsNonAssignees");
            Integer nombreVehicules = (Integer) request.getAttribute("nombreVehicules");
            Integer nombreReservationsTotal = (Integer) request.getAttribute("nombreReservationsTotal");
            Integer nombreReservationsAssignees = (Integer) request.getAttribute("nombreReservationsAssignees");
            Integer nombreReservationsNonAssignees = (Integer) request.getAttribute("nombreReservationsNonAssignees");
            
            if (dateRecherche != null && !dateRecherche.isEmpty()) {
            %>
                <div class="results-header">
                    <h2>Simulation du <%= dateRecherche %></h2>
                    <div>
                        <span class="badge"><%= nombreVehicules != null ? nombreVehicules : 0 %> véhicule(s) utilisé(s)</span>
                        <span class="badge" style="margin-left: 10px; background: #48bb78;"><%= nombreReservationsAssignees != null ? nombreReservationsAssignees : 0 %> / <%= nombreReservationsTotal != null ? nombreReservationsTotal : 0 %> assignée(s)</span>
                        <% if (nombreReservationsNonAssignees != null && nombreReservationsNonAssignees > 0) { %>
                            <span class="badge" style="margin-left: 10px; background: #ffc107; color: #333;"><%= nombreReservationsNonAssignees %> non assignée(s)</span>
                        <% } %>
                    </div>
                </div>

                <!-- Alerte pour les réservations non assignées -->
                <% if (reservationsNonAssignees != null && !reservationsNonAssignees.isEmpty()) { %>
                    <div class="alert-warning">
                        <h3>⚠️ Réservations non assignées</h3>
                        <p><strong><%= reservationsNonAssignees.size() %> réservation(s)</strong> n'ont pas pu être assignées à un véhicule (capacité insuffisante).</p>
                        <ul class="unassigned-list">
                            <% 
                            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                            for (Reservation r : reservationsNonAssignees) { 
                                Client client = r.getClient();
                                Hotel hotel = r.getHotel();
                            %>
                                <li class="unassigned-item">
                                    <div class="unassigned-item-info">
                                        <strong><%= client != null ? client.getPrenom() + " " + client.getNom() : "Client inconnu" %></strong>
                                        <br>
                                        <span style="font-size: 0.9em; color: #666;">
                                            <%= hotel != null ? hotel.getNom() + " (" + hotel.getVille() + ")" : "Hôtel inconnu" %>
                                            - Arrivée: <%= r.getDateHeureArrive() != null ? timeFormat.format(r.getDateHeureArrive()) : "?" %>
                                        </span>
                                    </div>
                                    <span class="passengers" style="background: #ffc107; color: #333;"><%= r.getNombrePassage() %> pers.</span>
                                </li>
                            <% } %>
                        </ul>
                    </div>
                <% } %>

                <% 
                if (vehiculesAvecReservations != null && !vehiculesAvecReservations.isEmpty()) {
                    for (Map.Entry<Vehicule, List<Reservation>> entry : vehiculesAvecReservations.entrySet()) {
                        Vehicule vehicule = entry.getKey();
                        List<Reservation> reservations = entry.getValue();
                %>
                    <div class="vehicule-card">
                        <div class="vehicule-header">
                            <div class="vehicule-info">
                                <div class="vehicule-name">
                                    <%= vehicule.getMarque() %> <%= vehicule.getModele() %> 
                                    <span style="color: #718096; font-weight: 400; font-size: 0.8em;">(Référence: <%= vehicule.getReference() %>)</span>
                                </div>
                                <div class="vehicule-details">
                                    <span>🪑 <%= vehicule.getNombrePlaces() %> places</span>
                                    <% if (vehicule.getTypeCarburant() != null) { %>
                                        <span>⛽ <%= vehicule.getTypeCarburant().getNom() %></span>
                                    <% } %>
                                    <span>🚗 <%= vehicule.getVitesseMoyenne() %> km/h</span>
                                </div>
                            </div>
                            <div class="reservation-count">
                                <%= reservations.size() %> réservation(s) assignée(s)
                            </div>
                        </div>

                        <!-- Affichage des horaires de trajet du véhicule -->
                        <% 
                        SimulationService.InfosTrajet infosTrajet = (infosTrajetParVehicule != null) ? infosTrajetParVehicule.get(vehicule) : null;
                        if (infosTrajet != null && !reservations.isEmpty()) { 
                            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                        %>
                            <div class="vehicule-timing">
                                <div class="timing-item">
                                    <div class="timing-label">Départ Aéroport</div>
                                    <div class="timing-value timing-depart">🛫 <%= timeFormat.format(infosTrajet.getHeureDepart()) %></div>
                                </div>
                                <div class="timing-item">
                                    <div class="timing-label">Retour Aéroport</div>
                                    <div class="timing-value timing-retour">🛬 <%= timeFormat.format(infosTrajet.getHeureRetour()) %></div>
                                </div>
                                <div class="timing-item">
                                    <div class="timing-label">Durée Totale</div>
                                    <div class="timing-value timing-duree">⏱️ <%= infosTrajet.getDureeTrajetMinutes() / 60 %>h<%= infosTrajet.getDureeTrajetMinutes() % 60 > 0 ? String.format("%02d", infosTrajet.getDureeTrajetMinutes() % 60) : "00" %></div>
                                </div>
                            </div>
                        <% } %>

                        <% if (!reservations.isEmpty()) { %>
                            <table class="reservations-table">
                                <thead>
                                    <tr>
                                        <th>Client</th>
                                        <th>Hôtel</th>
                                        <th>Ville</th>
                                        <th>Passagers</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <% 
                                    for (Reservation r : reservations) { 
                                        Client client = r.getClient();
                                        Hotel hotel = r.getHotel();
                                    %>
                                    <tr>
                                        <td>
                                            <div class="client-name"><%= client != null ? client.getPrenom() + " " + client.getNom() : "-" %></div>
                                            <div class="client-email"><%= client != null ? client.getEmail() : "" %></div>
                                        </td>
                                        <td>
                                            <div class="hotel-name"><%= hotel != null ? hotel.getNom() : "-" %></div>
                                        </td>
                                        <td>
                                            <div class="hotel-ville"><%= hotel != null ? hotel.getVille() : "-" %></div>
                                        </td>
                                        <td>
                                            <span class="passengers"><%= r.getNombrePassage() %> pers.</span>
                                        </td>
                                    </tr>
                                    <% } %>
                                </tbody>
                            </table>
                        <% } else { %>
                            <div class="empty-vehicle">
                                Aucune réservation assignée à ce véhicule
                            </div>
                        <% } %>
                    </div>
                <% 
                    } 
                } else { 
                %>
                    <div class="no-results">
                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                        </svg>
                        <h3>Aucun véhicule assigné</h3>
                        <p>Aucun véhicule n'a été assigné pour le <%= dateRecherche %></p>
                    </div>
                <% } %>
            <% } else { %>
                <div class="no-results">
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                    <h3>Sélectionnez une date</h3>
                    <p>Choisissez une date ci-dessus pour lancer la simulation d'assignation des véhicules</p>
                </div>
            <% } %>

            <a href="<%= request.getContextPath() %>/reservation/form" class="back-link">← Retour au formulaire de réservation</a>
            </div>
        </div>
    </div>
</body>
</html>
