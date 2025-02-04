package fr.vetbrain.vetnutri_mp.Data

import kotlinx.serialization.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.descriptors.SerialDescriptor

@Serializer(forClass = AlimentEv::class)
object AlimentEvSerializer : KSerializer<AlimentEv> {
    override var descriptor: SerialDescriptor = AlimentEv.serializer().descriptor

    override fun serialize(encoder: Encoder, value: AlimentEv) {
        encoder.encodeSerializableValue(AlimentEv.serializer(), value)
    }

    override fun deserialize(decoder: Decoder): AlimentEv {
        return decoder.decodeSerializableValue(AlimentEv.serializer())
    }
}

@Serializer(forClass = Equation::class)
object EquationSerializer : KSerializer<Equation> {
    override var descriptor: SerialDescriptor = Equation.serializer().descriptor

    override fun serialize(encoder: Encoder, value: Equation) {
        encoder.encodeSerializableValue(Equation.serializer(), value)
    }

    override fun deserialize(decoder: Decoder): Equation {
        return decoder.decodeSerializableValue(Equation.serializer())
    }
}

@Serializer(forClass = BiblioRef::class)
object BiblioRefSerializer : KSerializer<BiblioRef> {
    override var descriptor: SerialDescriptor = BiblioRef.serializer().descriptor

    override fun serialize(encoder: Encoder, value: BiblioRef) {
        encoder.encodeSerializableValue(BiblioRef.serializer(), value)
    }

    override fun deserialize(decoder: Decoder): BiblioRef {
        return decoder.decodeSerializableValue(BiblioRef.serializer())
    }
}

@Serializer(forClass = ReferenceEv::class)
object ReferenceEvSerializer : KSerializer<ReferenceEv> {
    override var descriptor: SerialDescriptor = ReferenceEv.serializer().descriptor

    override fun serialize(encoder: Encoder, value: ReferenceEv) {
        encoder.encodeSerializableValue(ReferenceEv.serializer(), value)
    }

    override fun deserialize(decoder: Decoder): ReferenceEv {
        return decoder.decodeSerializableValue(ReferenceEv.serializer())
    }
} 