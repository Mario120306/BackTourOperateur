<%@ page import="java.util.List" %>
<%@ page import="itu.sprintest.models.Etudiant" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Liste des Etudiants</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f4f4f4;
            padding: 20px;
        }
        .container {
            max-width: 900px;
            margin: 0 auto;
            background: white;
            padding: 30px;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }
        h2 {
            color: #2c3e50;
            text-align: center;
            margin-bottom: 30px;
        }
        .success-message {
            background: #d4edda;
            color: #155724;
            padding: 15px;
            border-radius: 5px;
            margin-bottom: 20px;
            border: 1px solid #c3e6cb;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }
        th {
            background: #3498db;
            color: white;
            padding: 12px;
            text-align: left;
            font-weight: bold;
        }
        td {
            padding: 12px;
            border-bottom: 1px solid #ddd;
        }
        tr:hover {
            background: #f5f5f5;
        }
        tr:nth-child(even) {
            background: #f9f9f9;
        }
        .back-link {
            display: inline-block;
            margin-top: 20px;
            color: #3498db;
            text-decoration: none;
            font-weight: bold;
        }
        .back-link:hover {
            text-decoration: underline;
        }
        .count {
            color: #666;
            font-style: italic;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>3229</h1>
        <h2>Liste des Etudiants Enregistres</h2>
        
        <%
            List<Etudiant> etudiants = (List<Etudiant>) request.getAttribute("etudiants");
            if (etudiants != null && !etudiants.isEmpty()) {
        %>
            <div class="success-message">
                <%= etudiants.size() %> etudiant(s) enregistre(s) avec succes !
            </div>
            
            <table>
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Nom</th>
                        <th>Prenom</th>
                        <th>Age</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        for (Etudiant e : etudiants) {
                    %>
                        <tr>
                            <td><%= e.getId() %></td>
                            <td><%= e.getNom() %></td>
                            <td><%= e.getPrenom() %></td>
                            <td><%= e.getAge() %> ans</td>
                        </tr>
                    <%
                        }
                    %>
                </tbody>
            </table>
            
            <p class="count">Total: <%= etudiants.size() %> etudiant(s)</p>
        <%
            } else {
        %>
            <div class="success-message">
                Aucun etudiant à afficher.
            </div>
        <%
            }
        %>
        
        <a href="/etudiant/formMultiple" class="back-link"> Retour au formulaire</a>
    </div>
</body>
</html>
