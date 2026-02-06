<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Formulaire Etudiant</title>
</head>
<body>
    <h1>Ajouter un étudiant</h1>
    <form action="etudiant/save" method="POST">
        <label for="id">ID :</label>
        <input type="number" name="id" id="id" required><br>
        <label for="nom">Nom :</label>
        <input type="text" name="nom" id="nom" required><br>
        <button type="submit">Envoyer</button>
    </form>
</body>
</html>
