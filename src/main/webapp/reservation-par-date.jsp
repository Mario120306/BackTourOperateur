<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="itu.back.model.Reservation" %>
<%@ page import="itu.back.model.Client" %>
<%@ page import="itu.back.model.Hotel" %>
<%@ page import="itu.back.model.Vehicule" %>
<%@ page import="itu.back.model.Aeroport" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Réservations par date</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }

        .container {
            background: white;
            border-radius: 15px;
            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
            padding: 40px;
            max-width: 1200px;
            margin: 0 auto;
        }

        h1 {
            color: #333;
            margin-bottom: 10px;
            text-align: center;
            font-size: 2em;
        }

        .subtitle {
            color: #666;
            text-align: center;
            margin-bottom: 25px;
            font-size: 0.9em;
        }

        .search-form {
            background: #f8f9fa;
            padding: 25px;
            border-radius: 10px;
            margin-bottom: 30px;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 20px;
            flex-wrap: wrap;
        }

        .search-form label {
            font-weight: 600;
            color: #555;
        }

        .search-form input[type="date"] {
            padding: 12px 15px;
            border: 2px solid #ddd;
            border-radius: 8px;
            font-size: 1em;
            transition: border-color 0.3s;
        }

        .search-form input[type="date"]:focus {
            outline: none;
            border-color: #667eea;
        }

        .btn {
            padding: 12px 30px;
            border: none;
            border-radius: 8px;
            font-size: 1em;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
        }

        .btn-primary {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }

        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 20px rgba(102, 126, 234, 0.4);
        }

        .results-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 15px 25px;
            border-radius: 10px;
            margin-bottom: 20px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .results-header h2 {
            font-size: 1.3em;
        }

        .badge {
            background: white;
            color: #667eea;
            padding: 8px 16px;
            border-radius: 20px;
            font-weight: 700;
        }

        .no-results {
            text-align: center;
            padding: 50px;
            color: #666;
        }

        .no-results svg {
            width: 80px;
            height: 80px;
            margin-bottom: 20px;
            opacity: 0.5;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 10px;
        }

        th, td {
            padding: 15px;
            text-align: left;
            border-bottom: 1px solid #eee;
        }

        th {
            background: #f8f9fa;
            color: #555;
            font-weight: 600;
            font-size: 0.85em;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }

        tr:hover {
            background-color: #f8f9fa;
        }

        .client-name {
            font-weight: 600;
            color: #333;
        }

        .client-email {
            font-size: 0.85em;
            color: #888;
        }

        .hotel-name {
            font-weight: 600;
            color: #667eea;
        }

        .hotel-ville {
            font-size: 0.85em;
            color: #888;
        }

        .vehicule-info {
            font-weight: 500;
        }

        .vehicule-details {
            font-size: 0.85em;
            color: #888;
        }

        .time-info {
            font-weight: 600;
            color: #28a745;
        }

        .time-depart {
            color: #dc3545;
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

        .distance-badge {
            display: inline-block;
            background: #ffc107;
            color: #333;
            padding: 4px 10px;
            border-radius: 10px;
            font-size: 0.85em;
            font-weight: 500;
        }

        .back-link {
            display: inline-block;
            margin-top: 25px;
            color: #667eea;
            text-decoration: none;
            font-weight: 600;
        }

        .back-link:hover {
            text-decoration: underline;
        }

        @media (max-width: 768px) {
            .container {
                padding: 20px;
            }
            
            table {
                display: block;
                overflow-x: auto;
            }
            
            .search-form {
                flex-direction: column;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>📅 Réservations par Date</h1>
        <p class="subtitle">Consultez toutes les réservations prévues pour une date spécifique</p>

        <form class="search-form" method="get" action="<%= request.getContextPath() %>/reservation/par-date">
            <label for="date">Sélectionnez une date :</label>
            <input type="date" id="date" name="date" 
                   value="<%= request.getAttribute("dateRecherche") != null ? request.getAttribute("dateRecherche") : "" %>" 
                   required>
            <button type="submit" class="btn btn-primary">🔍 Rechercher</button>
        </form>

        <% 
        String dateRecherche = (String) request.getAttribute("dateRecherche");
        List<Reservation> reservations = (List<Reservation>) request.getAttribute("reservations");
        Integer nombreReservations = (Integer) request.getAttribute("nombreReservations");
        
        if (dateRecherche != null && !dateRecherche.isEmpty()) {
        %>
            <div class="results-header">
                <h2>Réservations du <%= dateRecherche %></h2>
                <span class="badge"><%= nombreReservations %> réservation(s)</span>
            </div>

            <% if (reservations != null && !reservations.isEmpty()) { %>
                <table>
                    <thead>
                        <tr>
                            <th>Client</th>
                            <th>Hôtel</th>
                            <th>Passagers</th>
                            <th>Véhicule</th>
                            <th>Aéroport</th>
                            <th>Distance</th>
                            <th>Heure Départ</th>
                            <th>Heure Arrivée</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% 
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                        for (Reservation r : reservations) { 
                            Client client = r.getClient();
                            Hotel hotel = r.getHotel();
                            Vehicule vehicule = r.getVehicule();
                            Aeroport aeroport = r.getAeroport();
                        %>
                        <tr>
                            <td>
                                <div class="client-name"><%= client != null ? client.getPrenom() + " " + client.getNom() : "-" %></div>
                                <div class="client-email"><%= client != null ? client.getEmail() : "" %></div>
                            </td>
                            <td>
                                <div class="hotel-name"><%= hotel != null ? hotel.getNom() : "-" %></div>
                                <div class="hotel-ville"><%= hotel != null ? hotel.getVille() : "" %></div>
                            </td>
                            <td>
                                <span class="passengers"><%= r.getNombrePassage() %> pers.</span>
                            </td>
                            <td>
                                <% if (vehicule != null) { %>
                                    <div class="vehicule-info"><%= vehicule.getMarque() %> <%= vehicule.getModele() %></div>
                                    <div class="vehicule-details">
                                        <%= vehicule.getNombrePlaces() %> places
                                        <% if (vehicule.getTypeCarburant() != null) { %>
                                            - <%= vehicule.getTypeCarburant().getNom() %>
                                        <% } %>
                                    </div>
                                <% } else { %>
                                    <span style="color: #999;">Non assigné</span>
                                <% } %>
                            </td>
                            <td>
                                <% if (aeroport != null) { %>
                                    <strong><%= aeroport.getCode() %></strong><br>
                                    <span style="font-size: 0.85em; color: #666;"><%= aeroport.getLibelle() %></span>
                                <% } else { %>
                                    <span style="color: #999;">-</span>
                                <% } %>
                            </td>
                            <td>
                                <% if (r.getDistanceKm() != null) { %>
                                    <span class="distance-badge"><%= r.getDistanceKm() %> km</span>
                                    <% if (r.getTempsEstimeMinutes() != null) { %>
                                        <br><span style="font-size: 0.85em; color: #666;"><%= r.getTempsFormate() %></span>
                                    <% } %>
                                <% } else { %>
                                    <span style="color: #999;">-</span>
                                <% } %>
                            </td>
                            <td>
                                <% if (r.getHeureDepart() != null) { %>
                                    <span class="time-info time-depart"><%= timeFormat.format(r.getHeureDepart()) %></span>
                                <% } else { %>
                                    <span style="color: #999;">-</span>
                                <% } %>
                            </td>
                            <td>
                                <% if (r.getDateHeureArrive() != null) { %>
                                    <span class="time-info"><%= timeFormat.format(r.getDateHeureArrive()) %></span>
                                <% } else { %>
                                    <span style="color: #999;">-</span>
                                <% } %>
                            </td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            <% } else { %>
                <div class="no-results">
                    <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                    <h3>Aucune réservation</h3>
                    <p>Il n'y a pas de réservation prévue pour le <%= dateRecherche %></p>
                </div>
            <% } %>
        <% } else { %>
            <div class="no-results">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
                <h3>Sélectionnez une date</h3>
                <p>Choisissez une date ci-dessus pour voir les réservations correspondantes</p>
            </div>
        <% } %>

        <a href="<%= request.getContextPath() %>/reservation/form" class="back-link">← Retour au formulaire de réservation</a>
    </div>
</body>
</html>
