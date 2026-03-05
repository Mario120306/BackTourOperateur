<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="itu.back.model.Client" %>
<%@ page import="itu.back.model.Hotel" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nouvelle Réservation</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        :root {
            --bg-primary: #ffffff;
            --bg-secondary: #1f2937;
            --bg-tertiary: #374151;
            --text-light: #ffffff;
            --text-muted: #6b7280;
            --text-dark: #1f2937;
            --border-light: #e5e7eb;
            --success: #059669;
            --danger: #dc2626;
        }

        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: #ffffff;
            min-height: 100vh;
        }

        .container {
            background: #ffffff;
            border-radius: 12px;
            box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
            padding: 40px;
            max-width: 800px;
            margin: 0 auto;
            border: 1px solid #e5e7eb;
        }

        h1 {
            color: var(--text-dark);
            margin-bottom: 10px;
            font-size: 2em;
            font-weight: 700;
        }

        .subtitle {
            color: var(--text-muted);
            margin-bottom: 30px;
            font-size: 0.95em;
        }

        .alert {
            padding: 15px 20px;
            border-radius: 8px;
            margin-bottom: 20px;
            font-weight: 500;
        }

        .alert-success {
            background-color: #ecfdf5;
            color: var(--success);
            border: 1px solid #a7f3d0;
        }

        .alert-error {
            background-color: #fef2f2;
            color: var(--danger);
            border: 1px solid #fecaca;
        }

        .info-box {
            background-color: #f9fafb;
            border: 1px solid var(--border-light);
            border-radius: 8px;
            padding: 15px;
            margin-bottom: 25px;
        }

        .info-box h3 {
            color: var(--text-dark);
            font-size: 1em;
            margin-bottom: 8px;
        }

        .info-box p {
            color: var(--text-muted);
            font-size: 0.9em;
            line-height: 1.5;
        }

        .form-group {
            margin-bottom: 25px;
        }

        label {
            display: block;
            margin-bottom: 8px;
            color: var(--text-dark);
            font-weight: 600;
            font-size: 0.95em;
        }

        select, input {
            width: 100%;
            padding: 12px 15px;
            border: 2px solid var(--border-light);
            border-radius: 8px;
            font-size: 1em;
            transition: all 0.2s ease;
            background-color: #f9fafb;
        }

        select:focus, input:focus {
            outline: none;
            border-color: var(--bg-secondary);
            background-color: white;
            box-shadow: 0 0 0 3px rgba(22, 27, 34, 0.1);
        }

        select {
            cursor: pointer;
        }

        .btn-container {
            display: flex;
            gap: 15px;
            margin-top: 30px;
        }

        button, .btn {
            flex: 1;
            padding: 14px 25px;
            border: none;
            border-radius: 8px;
            font-size: 1em;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.2s ease;
            text-decoration: none;
            text-align: center;
            display: inline-block;
        }

        .btn-primary {
            background: var(--bg-secondary);
            color: var(--text-light);
        }

        .btn-primary:hover {
            background: var(--bg-tertiary);
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(31, 41, 55, 0.3);
        }

        .btn-primary:active {
            transform: translateY(0);
        }

        .required {
            color: var(--danger);
        }

        .help-text {
            font-size: 0.85em;
            color: var(--text-muted);
            margin-top: 5px;
        }

        @media (max-width: 768px) {
            .content-with-sidebar {
                padding: 15px;
            }

            .container {
                padding: 25px;
            }

            h1 {
                font-size: 1.5em;
            }

            .btn-container {
                flex-direction: column;
            }
        }
    </style>
</head>
<body>
    <%@ include file="../includes/sidebar.jsp" %>

    <div class="content-with-sidebar">
        <div class="container">
            <h1>📅 Nouvelle Réservation</h1>
            <p class="subtitle">Créer une nouvelle réservation de transport</p>

        <% 
            String success = (String) request.getAttribute("success");
            String error = (String) request.getAttribute("error");
            
            if (success != null) { 
        %>
            <div class="alert alert-success"><%= success %></div>
        <% } %>
        
        <% if (error != null) { %>
            <div class="alert alert-error">✗ <%= error %></div>
        <% } %>

        <form action="<%= request.getContextPath() %>/reservation/save" method="POST">
            <div class="form-group">
                <label for="idClient">Client <span class="required">*</span></label>
                <select name="idClient" id="idClient" required>
                    <option value="">-- Sélectionner un client --</option>
                    <% 
                        List<Client> clients = (List<Client>) request.getAttribute("clients");
                        if (clients != null) {
                            for (Client client : clients) {
                    %>
                        <option value="<%= client.getId() %>">
                            <%= client.getNom() %> <%= client.getPrenom() %> (<%= client.getEmail() %>)
                        </option>
                    <% 
                            }
                        }
                    %>
                </select>
            </div>

            <div class="form-group">
                <label for="idHotel">Hôtel de destination <span class="required">*</span></label>
                <select name="idHotel" id="idHotel" required>
                    <option value="">-- Sélectionner un hôtel --</option>
                    <% 
                        List<Hotel> hotels = (List<Hotel>) request.getAttribute("hotels");
                        if (hotels != null) {
                            for (Hotel hotel : hotels) {
                    %>
                        <option value="<%= hotel.getId() %>">
                            <%= hotel.getNom() %> - <%= hotel.getVille() %>
                        </option>
                    <% 
                            }
                        }
                    %>
                </select>
            </div>

            <div class="form-group">
                <label for="nombrePassage">Nombre de passagers <span class="required">*</span></label>
                <input type="number" name="nombrePassage" id="nombrePassage" 
                       min="1" max="50" value="1" required>
                <p class="help-text">Nombre de personnes à transporter</p>
            </div>

            <div class="form-group">
                <label for="dateHeureArrive">Date et heure d'arrivée <span class="required">*</span></label>
                <input type="datetime-local" name="dateHeureArrive" id="dateHeureArrive" required>
                <p class="help-text">Date et heure prévue d'arrivée à destination</p>
            </div>

            <div class="btn-container">
                <button type="submit" class="btn-primary">💾 Enregistrer la réservation</button>
            </div>
        </form>
        </div>
    </div>

    <script>
        // Définir la date minimale à aujourd'hui
        document.getElementById('dateHeureArrive').min = new Date().toISOString().slice(0, 16);
    </script>
</body>
</html>
