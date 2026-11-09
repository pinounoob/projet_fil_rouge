# Réveil — Ne sois pas le dernier ⏰

Application Android de réveil collectif réalisée pour le cours **8INF865 – Informatique mobile**.

## Le concept

Se lever seul, c'est facile à repousser. Se lever contre ses amis, beaucoup moins.

1. On crée un **groupe** avec ses amis et on choisit une **heure de réveil commune**.
2. Chaque membre **mise de l'argent virtuel** dans la cagnotte du groupe.
3. Le matin, l'alarme sonne en même temps chez tout le monde.
4. Pour l'arrêter, il ne suffit pas d'appuyer sur un bouton : il faut **secouer son téléphone**.
5. L'appli enregistre l'**ordre des réveils**. Les 3 premiers levés gagnent une part de la cagnotte (5 €, 3 € et 2 €). Les plus lents ne récupèrent rien, et ce qui reste dans la cagnotte est remis en jeu le lendemain.

Le but : **ne pas être le dernier**.

## Fonctionnalités

- Comptes avec pseudo unique (Firebase Authentication)
- Groupes avec heure d'alarme commune, contacts et cagnotte
- Alarme en plein écran, sonnerie ou vibreur, arrêt en secouant le téléphone
- Classement du matin et gains pour les 3 premiers
- Paramètres : thème, sonnerie, solde, compte

## Technologies

Kotlin · Jetpack Compose · MVVM · Coroutines et Flow · Firebase (Auth, Firestore) · DataStore · AlarmManager · Notifications · Accéléromètre

## Lancer le projet

1. Cloner le dépôt et l'ouvrir dans Android Studio.
2. Lancer l'application sur un téléphone ou un émulateur.
3. Accepter les notifications, qui sont nécessaires pour que l'alarme sonne.

## Équipe

- Inès
- Aliona
- Kaouthar
