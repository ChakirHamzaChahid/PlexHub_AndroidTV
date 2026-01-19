package com.chakir.aggregatorhubplex.util

/**
 * Une classe scellée (sealed class) générique qui représente l'état d'une demande de données
 * (réseau ou base de données). Elle peut se trouver dans l'un des trois états suivants : Succès
 * (Success), Erreur (Error) ou Chargement (Loading).
 *
 * @param T Le type de données transporté par la ressource.
 * @param data Les données réelles. Peuvent être présentes dans n'importe quel état (ex: afficher
 * des données obsolètes pendant le chargement).
 * @param message Un message d'erreur optionnel, généralement utilisé dans l'état Error.
 */
sealed class Resource<out T>(val data: T? = null, val message: String? = null) {

    /**
     * Représente une demande de données réussie.
     * @param data Les données récupérées avec succès. Ne peut pas être nul.
     */
    class Success<out T>(data: T) : Resource<T>(data)

    /**
     * Représente une demande de données échouée.
     * @param message Le message d'erreur décrivant ce qui s'est mal passé.
     * @param data Données optionnelles qui peuvent encore être pertinentes (par exemple, données
     * mises en cache).
     */
    class Error<out T>(message: String, data: T? = null) : Resource<T>(data, message)

    /**
     * Représente une demande de données en cours de traitement.
     * @param data Données obsolètes optionnelles qui peuvent être affichées pendant le chargement
     * des nouvelles données.
     */
    class Loading<out T>(data: T? = null) : Resource<T>(data)
}
