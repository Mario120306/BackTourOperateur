<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="itu.back.model.Client" %>
<%@ page import="itu.back.model.Hotel" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Nouvelle Reservation - Tour Operateur</title>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        :root {
            --primary: #0f172a;
            --primary-light: #1e293b;
            --accent: #3b82f6;
            --accent-hover: #2563eb;
            --success: #10b981;
            --success-light: #d1fae5;
            --danger: #ef4444;
            --danger-light: #fee2e2;
            --text-primary: #0f172a;
            --text-secondary: #64748b;
            --text-light: #ffffff;
            --bg-primary: #f8fafc;
            --bg-card: #ffffff;
            --border: #e2e8f0;
            --shadow-sm: 0 1px 2px 0 rgb(0 0 0 / 0.05);
            --shadow: 0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1);
            --radius: 8px;
            --radius-lg: 12px;
        }

        body {
            font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            background: var(--bg-primary);
            color: var(--text-primary);
            min-height: 100vh;
            line-height: 1.6;
        }

        .main-content {
            padding: 32px 40px;
        }

        .content-wrapper {
            max-width: 700px;
            margin: 0 auto;
        }

        @media (max-width: 1024px) {
            .main-content {
                padding: 24px;
            }
        }

        .page-header {
            margin-bottom: 32px;
            text-align: center;
        }

        .page-title {
            font-size: 1.875rem;
            font-weight: 700;
            color: var(--text-primary);
            margin-bottom: 8px;
        }

        .page-subtitle {
            font-size: 0.975rem;
            color: var(--text-secondary);
        }

        .card {
            background: var(--bg-card);
            border: 1px solid var(--border);
            border-radius: var(--radius-lg);
            padding: 32px;
            box-shadow: var(--shadow);
        }

        .alert {
            padding: 14px 18px;
            border-radius: var(--radius);
            margin-bottom: 24px;
            font-size: 0.875rem;
            font-weight: 500;
        }

        .alert-success {
            background: var(--success-light);
            color: var(--success);
            border: 1px solid #a7f3d0;
        }

        .alert-error {
            background: var(--danger-light);
            color: var(--danger);
            border: 1px solid #fecaca;
        }

        .form-group {
            margin-bottom: 24px;
        }

        label {
            display: block;
            font-size: 0.875rem;
            font-weight: 600;
            color: var(--text-primary);
            margin-bottom: 8px;
        }

        .required {
            color: var(--danger);
        }

        select, input {
            width: 100%;
            padding: 12px 14px;
            border: 1px solid var(--border);
            border-radius: var(--radius);
            font-size: 0.938rem;
            font-family: inherit;
            background: var(--bg-card);
            color: var(--text-primary);
            transition: border-color 0.2s, box-shadow 0.2s;
        }

        select:focus, input:focus {
            outline: none;
            border-color: var(--accent);
            box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
        }

        select {
            cursor: pointer;
            appearance: none;
            background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 24 24' stroke='%2364748b'%3E%3Cpath stroke-linecap='round' stroke-linejoin='round' stroke-width='2' d='M19 9l-7 7-7-7'%3E%3C/path%3E%3C/svg%3E");
            background-repeat: no-repeat;
            background-position: right 12px center;
            background-size: 16px;
            padding-right: 40px;
        }

        .help-text {
            font-size: 0.813rem;
            color: var(--text-secondary);
            margin-top: 6px;
        }

        .btn-container {
            margin-top: 32px;
            text-align: center;
        }

        .btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            padding: 12px 32px;
            font-size: 0.938rem;
            font-weight: 600;
            font-family: inherit;
            border: none;
            border-radius: var(--radius);
            cursor: pointer;
            transition: all 0.2s;
            text-decoration: none;
        }

        .btn-primary {
            background: var(--accent);
            color: white;
        }

        .btn-primary:hover {
            background: var(--accent-hover);
            transform: translateY(-1px);
            box-shadow: 0 4px 12px rgba(59, 130, 246, 0.3);
        }

        @media (max-width: 768px) {
            .card {
                padding: 24px;
            }

            .page-title {
                font-size: 1.5rem;
            }
        }
    </style>
</head>
<body>
    <%@ include file="../includes/sidebar.jsp" %>

    <div class="content-with-sidebar">
        <div class="main-content">
            <div class="content-wrapper">
                <div class="page-header">
                    <h1 class="page-title">Nouvelle Reservation</h1>
                    <p class="page-subtitle">Creer une nouvelle reservation de transport</p>
                </div>

                <div class="card">
                    <% 
                        String success = (String) request.getAttribute("success");
                        String error = (String) request.getAttribute("error");
                        
                        if (success != null) { 
                    %>
                        <div class="alert alert-success"><%= success %></div>
                    <% } %>
                    
                    <% if (error != null) { %>
                        <div class="alert alert-error"><%= error %></div>
                    <% } %>

                    <form action="<%= request.getContextPath() %>/reservation/save" method="POST">
                        <div class="form-group">
                            <label for="idClient">Client <span class="required">*</span></label>
                            <select name="idClient" id="idClient" required>
                                <option value="">-- Selectionner un client --</option>
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
                            <label for="idHotel">Hotel de destination <span class="required">*</span></label>
                            <select name="idHotel" id="idHotel" required>
                                <option value="">-- Selectionner un hotel --</option>
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
                            <p class="help-text">Nombre de personnes a transporter</p>
                        </div>

                        <div class="form-group">
                            <label for="dateHeureArrive">Date et heure d'arrivee <span class="required">*</span></label>
                            <input type="datetime-local" name="dateHeureArrive" id="dateHeureArrive" required>
                            <p class="help-text">Date et heure prevue d'arrivee a destination</p>
                        </div>

                        <div class="btn-container">
                            <button type="submit" class="btn btn-primary">Enregistrer la reservation</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <script>
        document.getElementById('dateHeureArrive').min = new Date().toISOString().slice(0, 16);
    </script>
</body>
</html>
