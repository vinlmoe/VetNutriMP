package fr.vetbrain.vetnutri_mp.Data

/**
 * Mots-clés de consultation livrés en double (casse ou faute de frappe) jusqu'aux données 3.3.18.
 * Chaque doublon est remplacé par le mot-clé conservé ; deux libellés sont corrigés.
 *
 * Appliqué au démarrage et après chaque import (une ancienne sauvegarde les ramènerait), sans
 * effet quand la base est déjà propre.
 */
object FusionMotsClesConsultation {

    /** Identifiant du doublon supprimé → mot-clé conservé. */
    val remplacements: Map<String, ConsultationKeyword> =
            listOf(
                            "d25cccb6-a493-4184-b717-f1ab78793e18" to
                                    ConsultationKeyword("d246fef7-d50a-4507-b949-455af945b480", "Cardiomyopathie dilatée"),
                            "0e83dbdd-007b-4c1c-a702-141e2f130cf6" to
                                    ConsultationKeyword("85b8a2d1-4c3b-419b-abd9-1c472f3bf324", "Insuffisance hépatique"),
                            "24e9c1ee-485a-4818-b71c-5fda60fee7ff" to
                                    ConsultationKeyword("dabe8609-8dfd-4d3a-ab02-5f6d66d10d5f", "Cardiomyopathie hypertrophique"),
                            "082071ee-b748-47c7-a8d7-6737b36e45fd" to
                                    ConsultationKeyword("76797599-c32e-44fb-bf82-05073e17dbef", "Diabète insipide"),
                            "340bd6f5-b5d7-4635-8696-c0c7103df4d0" to
                                    ConsultationKeyword("67bfcf5a-d787-4bb1-b7bd-80cc9d4e681e", "Diabète sucré"),
                            "6af3a494-32c9-42cc-9e14-b49065e84708" to
                                    ConsultationKeyword("5353ca07-557b-4b46-afef-8e32d0acdac4", "Hyperthyroïdie"),
                            "d71402be-600d-4bcd-bde0-9eeeecd2a2fb" to
                                    ConsultationKeyword("852fea4f-8b5f-4b6d-a745-9022f25b07f3", "Hypothyroïdie"),
                            "9d715287-df1f-468a-a3c4-0cbd4f1544d9" to
                                    ConsultationKeyword("d8241881-83f4-4749-bcc3-f636903af458", "Maladie valvulaire dégénérative"),
                            "6e9ea780-709f-493c-8eab-93dd91589aae" to
                                    ConsultationKeyword("9da7c5dd-8d6e-4110-b645-9f4aa295be37", "Shunt portosystémique")
                    )
                    .toMap()

    /** Libellés corrigés (sans doublon). */
    val libellesCorriges: Map<String, String> =
            mapOf(
                    "053e3e77-361d-48ae-9aaa-49f9e2e283d1" to "Maladie cœliaque",
                    "d6403d98-2746-4fcb-817b-5300c54f1a2d" to "Mucocèle biliaire"
            )
}
