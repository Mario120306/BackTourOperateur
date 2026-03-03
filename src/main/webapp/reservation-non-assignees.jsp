<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="itu.back.model.Reservation" %>
<%@ page import="itu.back.model.Client" %>
<%@ page import="itu.back.model.Hotel" %>
<%@ page import="itu.back.model.Aeroport" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Réservations non assignées</title>
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

        .alert {
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-weight: 500;
        }

        .alert-success {
            background-color: #d4edda;
            color: #155724;
            border: 1px solid #c3e6cb;
        }

        .alert-error {
            background-color: #f8d7da;
            color: #721c24;
            border: 1px solid #f5c6cb;
        }

        .results-header {
            background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);
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
            color: #ff6b6b;
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

        .passengers {
            display: inline-block;
            background: #667eea;
            color: white;
            padding: 5px 12px;
            border-radius: 15px;
            font-weight: 600;
            font-size: 0.9em;
        }

        .status-badge {
            display: inline-block;
            background: #ff6b6b;
            color: white;
            padding: 5px 12px;
            border-radius: 15px;
            font-weight: 600;
            font-size: 0.85em;
        }

        .btn {
            padding: 10px 20px;
            border: none;
            border-radius: 8px;
            font-size: 0.9em;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
            text-decoration: none;
            display: inline-block;
        }

        .btn-assign {
            background: linear-gradient(135deg, #28a745 0%, #20c997 100%);
            color: white;
        }

        .btn-assign:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 20px rgba(40, 167, 69, 0.4);
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

        .time-info {
            font-weight: 600;
            color: #28a745;
        }

        @media (max-width: 768px) {
            .container {
                padding: 20px;
            }
            
            table {
                display: block;
                overflow-x: auto;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🚗 Réservations à assigner</h1>
        <p class="subtitle">Liste des réservations en attente d'assignation de véhicule</p>

        <% 
        String success = (String) request.getAttribute("success");
        String error = (String) request.getAttribute("error");
        
        if (success != null && !success.isEmpty()) {
        %>
            <div class="alert alert-success"><%= success %></div>
        <% } %>
        
        <% if (error != null && !error.isEmpty()) { %>
            <div class="alert alert-error"><%= error %></div>
        <% } %>

        <% 
        List<Reservation> reservations = (List<Reservation>) request.getAttribute("reservations");
        Integer nombreReservations = (Integer) request.getAttribute("nombreReservations");
        %>
        
        <div class="results-header">
            <h2>Réservations non assignées</h2>
            <span class="badge"><%= nombreReservations != null ? nombreReservations : 0 %> en attente</span>
        </div>

        <% if (reservations != null && !reservations.isEmpty()) { %>
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Client</th>
                        <th>Hôtel</th>
                        <th>Passagers</th>
                        <th>Aéroport</th>
                        <th>Date/Heure Arrivée</th>
                        <th>Statut</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    <% 
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
                    for (Reservation r : reservations) { 
                        Client client = r.getClient();
                        Hotel hotel = r.getHotel();
                        Aeroport aeroport = r.getAeroport();
                    %>
                    <tr>
                        <td><strong>#<%= r.getId() %></strong></td>
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
                            <% if (aeroport != null) { %>
                                <strong><%= aeroport.getCode() %></strong><br>
                                <span style="font-size: 0.85em; color: #666;"><%= aeroport.getLibelle() %></span>
                            <% } else { %>
                                <span style="color: #999;">-</span>
                            <% } %>
                        </td>
                        <td>
                            <% if (r.getDateHeureArrive() != null) { %>
                                <strong><%= dateFormat.format(r.getDateHeureArrive()) %></strong><br>
                                <span class="time-info"><%= timeFormat.format(r.getDateHeureArrive()) %></span>
                            <% } else { %>
                                <span style="color: #999;">-</span>
                            <% } %>
                        </td>
                        <td>
                            <span class="status-badge">Non assigné</span>
                        </td>
                        <td>
                            <form method="post" action="<%= request.getContextPath() %>/reservation/assigner" style="display: inline;">
                                <input type="hidden" name="idReservation" value="<%= r.getId() %>">
                                <button type="submit" class="btn btn-assign">✓ Assigner</button>
                            </form>
                        </td>
                    </tr>
                    <% } %>
                </tbody>
            </table>
        <% } else { %>
            <div class="no-results">
                <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <h3>Aucune réservation en attente</h3>
                <p>Toutes les réservations ont été assignées à un véhicule.</p>
            </div>
        <% } %>

        <a href="<%= request.getContextPath() %>/reservation/form" class="back-link">← Nouvelle réservation</a>
        &nbsp;&nbsp;|&nbsp;&nbsp;
        <a href="<%= request.getContextPath() %>/reservation/par-date/form" class="back-link">📅 Réservations par date</a>
    </div>
</body>
</html>
