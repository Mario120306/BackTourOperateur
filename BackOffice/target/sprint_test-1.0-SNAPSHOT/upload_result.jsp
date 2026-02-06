<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Résultat Upload</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 600px;
            margin: 50px auto;
            padding: 20px;
            background: #f5f5f5;
        }
        .result-container {
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        h1 {
            text-align: center;
        }
        .success {
            color: #4CAF50;
        }
        .error {
            color: #f44336;
        }
        .info-box {
            background: #f9f9f9;
            padding: 15px;
            border-radius: 5px;
            margin: 20px 0;
            border-left: 4px solid #4CAF50;
        }
        .info-item {
            margin: 10px 0;
        }
        .info-label {
            font-weight: bold;
            color: #555;
        }
        .info-value {
            color: #333;
        }
        .back-link {
            display: block;
            text-align: center;
            margin-top: 20px;
            padding: 10px;
            background: #2196F3;
            color: white;
            text-decoration: none;
            border-radius: 5px;
            transition: background 0.3s;
        }
        .back-link:hover {
            background: #0b7dda;
        }
    </style>
</head>
<body>
    <div class="result-container">
        <% Boolean success = (Boolean) request.getAttribute("success"); %>
        <% if (success != null && success) { %>
            <h1 class="success">Succès !</h1>
            <p class="success"><%= request.getAttribute("message") %></p>
            
            <div class="info-box">
                <div class="info-item">
                    <span class="info-label"> Nom du fichier :</span>
                    <span class="info-value"><%= request.getAttribute("fileName") %></span>
                </div>
                <div class="info-item">
                    <span class="info-label"> Taille :</span>
                    <span class="info-value"><%= request.getAttribute("fileSize") %> octets</span>
                </div>
                <div class="info-item">
                    <span class="info-label"> Type :</span>
                    <span class="info-value"><%= request.getAttribute("contentType") %></span>
                </div>
                <div class="info-item">
                    <span class="info-label"> Chemin :</span>
                    <span class="info-value"><%= request.getAttribute("savedPath") %></span>
                </div>
                <% if (request.getAttribute("description") != null && !request.getAttribute("description").toString().isEmpty()) { %>
                <div class="info-item">
                    <span class="info-label">Description :</span>
                    <span class="info-value"><%= request.getAttribute("description") %></span>
                </div>
                <% } %>
            </div>
        <% } else { %>
            <h1 class="error">❌ Erreur</h1>
            <p class="error"><%= request.getAttribute("message") %></p>
        <% } %>
        
        <a href="upload" class="back-link"> Retour au formulaire</a>
    </div>
</body>
</html>
