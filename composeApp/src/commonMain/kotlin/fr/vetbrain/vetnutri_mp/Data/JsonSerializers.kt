package fr.vetbrain.vetnutri_mp.Data

import fr.vetbrain.vetnutri_mp.Enumer.Espece
import fr.vetbrain.vetnutri_mp.Enumer.FoodKind
import fr.vetbrain.vetnutri_mp.Enumer.GroupAlim
import fr.vetbrain.vetnutri_mp.Enumer.UnitReqEnum
import kotlinx.datetime.LocalDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/** Sérialiseur personnalisé pour LocalDate */
object LocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): LocalDate {
        val dateString = decoder.decodeString()
        return try {
            LocalDate.parse(dateString)
        } catch (e: Exception) {
            LocalDate(2023, 1, 1) // Valeur par défaut en cas d'erreur
        }
    }
}

/** Sérialiseur personnalisé pour GroupAlim */
object GroupAlimSerializer : KSerializer<GroupAlim> {
    override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("GroupAlim", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: GroupAlim) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): GroupAlim {
        val groupString = decoder.decodeString()
        return try {
            GroupAlim.valueOf(groupString)
        } catch (e: Exception) {
            // Valeur par défaut ou gestion d'erreur
            GroupAlim.values().firstOrNull()
                    ?: throw IllegalArgumentException("GroupAlim non trouvé: $groupString")
        }
    }
}

/** Sérialiseur personnalisé pour FoodKind */
object FoodKindSerializer : KSerializer<FoodKind> {
    override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("FoodKind", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: FoodKind) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): FoodKind {
        val foodKindString = decoder.decodeString()
        return try {
            FoodKind.valueOf(foodKindString)
        } catch (e: Exception) {
            // Valeur par défaut ou gestion d'erreur
            FoodKind.values().firstOrNull()
                    ?: throw IllegalArgumentException("FoodKind non trouvé: $foodKindString")
        }
    }
}

/** Sérialiseur personnalisé pour Espece */
object EspeceSerializer : KSerializer<Espece> {
    override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("Espece", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Espece) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): Espece {
        val especeString = decoder.decodeString()
        return try {
            // D'abord essayer de parser comme enum par nom
            Espece.valueOf(especeString)
        } catch (e: Exception) {
            try {
                // Ensuite essayer de convertir comme ID numérique
                val especeId = especeString.toIntOrNull()
                if (especeId != null) {
                    Espece.getEnumFromInt(especeId)
                } else {
                    // Enfin essayer de trouver par label
                    val especeByLabel = Espece.getByLabel(especeString)
                    if (especeByLabel != null) {
                        especeByLabel
                    } else {
                        Espece.CHIEN
                    }
                }
            } catch (e2: Exception) {
                Espece.CHIEN // Valeur par défaut
            }
        }
    }
}

/** Sérialiseur personnalisé pour UnitReqEnum */
object UnitReqEnumSerializer : KSerializer<UnitReqEnum> {
    override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("UnitReqEnum", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UnitReqEnum) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): UnitReqEnum {
        val unitReqString = decoder.decodeString()
        return try {
            UnitReqEnum.valueOf(unitReqString)
        } catch (e: Exception) {
            // Valeur par défaut ou gestion d'erreur
            UnitReqEnum.values().firstOrNull()
                    ?: throw IllegalArgumentException("UnitReqEnum non trouvé: $unitReqString")
        }
    }
}
