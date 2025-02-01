package fr.vetbrain.vetnutri_mp.Data



import fr.vetbrain.vetnutri_mp.Enumerise.NutrientAnalysis
import fr.vetbrain.vetnutri_mp.Enumerise.NutrientBase
import fr.vetbrain.vetnutri_mp.Enumerise.NutrientLipid
import fr.vetbrain.vetnutri_mp.Enumerise.NutrientMacro
import fr.vetbrain.vetnutri_mp.Enumerise.NutrientMin
import fr.vetbrain.vetnutri_mp.Enumerise.NutrientOther
import fr.vetbrain.vetnutri_mp.Enumerise.TextConstant
import kotlinx.serialization.Serializable


@Serializable
class Ration(
    val UUID: String = UUID.randomUUID().toString(),
    var number: Int = 0,
    var nom: String = "",
    var coef: Float = 0.0F,
    var fibrep: Float = 4F,
    var transCoef: Float = 0F,
    var rpcp: Float = 60F,
    var huilep: Float = 5F,
    var indus: Float = 100F,
    var protAnim: Float = 80F,
    var objectComp: Float = 0F,
    var version: String = TextConstant.VERSION.nameToString(),
    var objectType: Int = 0,
    var actual: Boolean = false,
    var alimentList: MutableList<AlimentRation> = mutableListOf(),
    var enerTot: Float = 0F
) : Serializable, Cloneable {

    companion object {
        private const val serialVersionUID = 1120L
    }

    constructor(UUID: String) : this(UUID = UUID)

    fun setVersion(version: String) {
        this.version = version
    }

    fun setIndus(indus: Float) {
        this.indus = indus
    }

    fun getIndus(): Float = indus
    fun getVersion(): String = version
    fun setNumber(number: Int) {
        this.number = number
    }

    fun getNumber(): Int = number
    fun size(): Int = alimentList.size
    fun isActual(): Boolean = actual
    fun setActual(actual: Boolean) {
        this.actual = actual
    }

    fun setFibrep(fibrep: Float) {
        this.fibrep = fibrep
    }

    fun setHuilep(huilep: Float) {
        this.huilep = huilep
    }

    fun setObjectComp(objectComp: Float) {
        this.objectComp = objectComp
    }

    fun setObjectType(objectType: Int) {
        this.objectType = objectType
    }

    fun setProtAnim(protAnim: Float) {
        this.protAnim = protAnim
    }

    fun setRpcp(rpcp: Float) {
        this.rpcp = rpcp
    }

    fun getFibrep(): Float = fibrep
    fun getHuilep(): Float = huilep
    fun getObjectComp(): Float = objectComp
    fun getObjectType(): Int = objectType
    fun getProtAnim(): Float = protAnim
    fun getRpcp(): Float = rpcp

    fun addAliment(alimentEv: AlimentEv) {
        alimentList.add(AlimentRation(alimentEv))
    }

    fun getCoef(): Float = coef
    fun setCoef(coef: Float) {
        this.coef = coef
    }

    fun addAliment(alim: AlimentRation) {
        alimentList.add(alim.clone())
    }

    fun addRecette(recette: Recette) {
        recette.alimentList.forEach { alr ->
            addAliment(alr)
        }
    }

    fun addAlimentUnif(alim: AlimentRation) {
        if (alim.UUIDunif != "nonINDEX") {
            val existingAlim = alimentList.find { it.UUIDunif == alim.UUIDunif }
            if (existingAlim != null) {
                existingAlim.quantite += alim.quantite
            } else {
                alimentList.add(alim.clone())
            }
        } else {
            alimentList.add(alim.clone())
        }
    }

    fun removeAliment(UUIDVal: String) {
        alimentList.removeIf { it.UUID == UUIDVal }
    }

    fun removeAliment(targ: targetAdjust) {
        alimentList.removeIf { it.target == targ }
    }

    fun setQuantiteOfAlim(UUIDVal: String, quant: Float) {
        alimentList.find { it.UUID == UUIDVal }?.quantite = quant
    }

    fun setWeightOfAlim(UUIDVal: String, quant: Float) {
        alimentList.find { it.UUID == UUIDVal }?.weight = quant
    }

    fun setTargetOfAlim(UUIDVal: String, targ: targetAdjust) {
        alimentList.find { it.UUID == UUIDVal }?.target = targ
    }

    fun removeAllAlim() {
        alimentList.clear()
    }

    fun transfertRation(rat: Ration) {
        rat.alimentList.forEach { alim ->
            addAliment(alim.clone())
        }
    }

    fun getDensity(): Float {
        var ener = 0F
        var qtot = 0F
        alimentList.forEach { alim ->
            ener += alim.DE * alim.quantite
            qtot += alim.quantite
        }
        return if (qtot > 0) ener / qtot else 0F
    }

    fun getEnerT(): Float {
        var ener = 0F
        alimentList.forEach { alim ->
            ener += alim.DE * alim.quantite / 100F
        }
        return ener
    }

    fun getMasse(): Float {
        var qtot = 0F
        alimentList.forEach { alim ->
            qtot += alim.quantite
        }
        return qtot
    }

    fun add(alimList: List<AlimentRation>) {
        alimList.forEach { alimrat ->
            addAliment(alimrat)
        }
    }

    fun addUnif(alimList: List<AlimentRation>) {
        alimList.forEach { alimrat ->
            addAlimentUnif(alimrat)
        }
    }

    fun getNutrient(nut: Nutrient): Float {
        if (nut is NutrientAnalysis && nut != NutrientAnalysis.MethCys && nut != NutrientAnalysis.PhenTyr) {
            return getNutrientAna(nut)
        }
        var quantite = 0F
        alimentList.forEach { alim ->
            quantite += alim.getNutrient(nut) * alim.quantite / 100
        }
        return quantite
    }

    private fun getNutrientAna(en: NutrientAnalysis): Float = when (en) {
        NutrientAnalysis.o6o3 -> getNutrient(NutrientLipid.O6) / getNutrient(NutrientLipid.O3)
        NutrientAnalysis.ZnCu -> getNutrient(NutrientMin.ZN) / getNutrient(NutrientMin.CU)
        NutrientAnalysis.PCa -> getNutrient(NutrientMacro.CAL) / getNutrient(NutrientMacro.PHOS)
        NutrientAnalysis.PhosphProt -> getNutrient(NutrientBase.PROTEINE) / getNutrient(
            NutrientMacro.PHOS)
        NutrientAnalysis.NaK -> getNutrient(NutrientMacro.K) / getNutrient(NutrientMacro.NA)
        NutrientAnalysis.nonOsPhos -> 100 * (getNutrient(NutrientMacro.PHOS) - getNutrient(
            NutrientMacro.CAL) / 2) / getNutrient(NutrientMacro.PHOS)
        NutrientAnalysis.nonOsProt -> 100 * (getNutrient(NutrientBase.PROTEINE) - 3 * getNutrient(
            NutrientMacro.CAL)) / getNutrient(NutrientBase.PROTEINE)
        NutrientAnalysis.nonOsPP -> getNutrient(NutrientBase.PROTEINE) * getNutrient(
            NutrientAnalysis.nonOsProt) / (getNutrient(NutrientAnalysis.nonOsPhos) * getNutrient(
            NutrientMacro.PHOS))
        else -> 0.0F
    }

    fun getNutrientPart(nut: Nutrient): Array<pourcentPart> {
        val quantite = getNutrient(nut)
        return if (alimentList.isEmpty()) {
            arrayOf(pourcentPart("", 0F))
        } else {
            alimentList.map { alim ->
                pourcentPart(alim.nom, alim.getNutrient(nut) * alim.quantite / quantite)
            }.toTypedArray().tri()
        }
    }

    fun getNutrientPart(en: NutrientLipid): Array<pourcentPart> {
        val quantite = getNutrient(en)
        return if (alimentList.isEmpty()) {
            arrayOf(pourcentPart("", 0F))
        } else {
            alimentList.map { alim ->
                pourcentPart(alim.nom, alim.quantite * alim.getNutrient(en) / quantite)
            }.toTypedArray().tri()
        }
    }

    fun getNutrientPart(en: NutrientMacro): Array<pourcentPart> {
        val quantite = getNutrient(en)
        return if (alimentList.isEmpty()) {
            arrayOf(pourcentPart("", 0F))
        } else {
            alimentList.map { alim ->
                pourcentPart(alim.nom, alim.quantite * alim.getNutrient(en) / quantite)
            }.toTypedArray().tri()
        }
    }

    fun getNutrientPartEner(): Array<pourcentPart> {
        val quantite = getEnerT()
        return if (alimentList.isEmpty()) {
            arrayOf(pourcentPart("", 0F))
        } else {
            alimentList.map { alim ->
                pourcentPart(alim.nom, alim.quantite * alim.DE / quantite)
            }.toTypedArray().tri()
        }
    }

    fun getNutrientPart(en: NutrientMin): Array<pourcentPart> {
        val quantite = getNutrient(en)
        return if (alimentList.isEmpty()) {
            arrayOf(pourcentPart("", 0F))
        } else {
            alimentList.map { alim ->
                pourcentPart(alim.nom, alim.quantite * alim.getNutrient(en) / quantite)
            }.toTypedArray().tri()
        }
    }

    fun getNutrientPart(en: NutrientVitam): Array<pourcentPart> {
        val quantite = getNutrient(en)
        return if (alimentList.isEmpty()) {
            arrayOf(pourcentPart("", 0F))
        } else {
            alimentList.map { alim ->
                pourcentPart(alim.nom, alim.quantite * alim.getNutrient(en) / quantite)
            }.toTypedArray().tri()
        }
    }

    fun getNutrientPart(en: NutrientOther): Array<pourcentPart> {
        val quantite = getNutrient(en)
        return if (alimentList.isEmpty()) {
            arrayOf(pourcentPart("", 0F))
        } else {
            alimentList.map { alim ->
                pourcentPart(alim.nom, alim.quantite * alim.getNutrient(en) / quantite)
            }.toTypedArray().tri()
        }
    }

    fun getPoids(): Float {
        var quantite = 0F
        alimentList.forEach { alim ->
            quantite += alim.quantite
        }
        return quantite
    }

    fun getNutrient(en: AAEnum): Float {
        var quantite = 0F
        alimentList.forEach { alim ->
            if (alim.getNutrient(NutrientBase.PROTEINE) != 0F) {
                quantite += alim.getNutrient(en) * alim.quantite / 100
            }
        }
        return quantite
    }

    fun isNutrient(en: Nutrient): Boolean = alimentList.all { it.isNutrient(en) }

    fun getAlimentByUUID(UUIDalim: String): AlimentRation? = alimentList.find { it.UUID == UUIDalim }

    fun reInitialise() {
        alimentList.filter { it.target != targetAdjust.NO }.forEach { it.quantite = 0F }
    }

    fun adjust(target: Float, tar: targetAdjust, pas: Float) {
        when (tar) {
            targetAdjust.CALCIUM, targetAdjust.CALCIUMPHOS -> adjust(target, tar, pas, NutrientMacro.CAL)
            targetAdjust.EPA -> adjust(target, tar, pas, NutrientLipid.EPADHA)
            targetAdjust.FIBER -> adjust(target, tar, pas, NutrientBase.FIBRETOT)
            targetAdjust.LIP -> adjust(target, tar, pas, NutrientBase.LIPIDE)
            targetAdjust.MG -> adjust(target, tar, pas, NutrientMacro.MG)
            targetAdjust.NA -> adjust(target, tar, pas, NutrientMacro.NA)
            targetAdjust.O3 -> adjust(target, tar, pas, NutrientLipid.O3)
            targetAdjust.O6 -> adjust(target, tar, pas, NutrientLipid.O6)
            targetAdjust.PROT -> adjust(target, tar, pas, NutrientBase.PROTEINE)
            targetAdjust.VITA -> adjust(target, tar, pas, NutrientVitam.VITA)
            targetAdjust.VITD -> adjust(target, tar, pas, NutrientVitam.VITD)
            targetAdjust.VITE -> adjust(target, tar, pas, NutrientVitam.VITE)
            else -> {}
        }
    }

    private fun getSumWeight(tar: targetAdjust): Float {
        var transCoef = 0F
        alimentList.forEach { al ->
            if (al.target == tar) {
                transCoef += if (tar == targetAdjust.CALCIUMPHOS) {
                    if (al.getNutrient(NutrientMacro.CAL) / al.getNutrient(NutrientMacro.PHOS) > 1) al.weight else 0F
                } else {
                    al.weight
                }
            }
        }
        return transCoef
    }

    fun setEnerTot(enerTot: Float) {
        this.enerTot = enerTot
    }

    fun getEnerTot(): Float = enerTot

    fun updateQuantite(li: List<AlimentRation>) {
        li.forEach { ai ->
            alimentList.find { a -> a.UUID == ai.UUID }?.quantite = ai.quantite
        }
    }

    private fun adjust(target: Float, tar: targetAdjust, pas: Float, enu: NutrientMacro) {
        val initial = getNutrient(enu)
        val dif = target - initial
        if (dif > 0) {
            val r = getSumWeight(tar)
            if (r > 0) {
                alimentList.filter { it.weight > 0 && it.getNutrient(enu) > 0 && it.target == tar }.forEach { al ->
                    val q = 100 * dif / ((r / al.weight) * al.getNutrient(enu))
                    al.quantite = pas * (1 + Math.floor(q / pas))
                }
            }
        }
    }

    fun adjustCal(target: Float, pas: Float, objectifPCa: Float) {
        var initial = getNutrient(NutrientMacro.CAL)
        var initialP = getNutrient(NutrientMacro.PHOS)
        val dif = target - initial
        if (dif > 0) {
            val r = getSumWeight(targetAdjust.CALCIUM) + getSumWeight(targetAdjust.CALCIUMPHOS)
            if (r > 0) {
                alimentList.filter { it.weight > 0 && it.getNutrient(NutrientMacro.CAL) > 0 && (it.target == targetAdjust.CALCIUM || it.target == targetAdjust.CALCIUMPHOS) }.forEach { al ->
                    val q = 100 * dif / ((r / al.weight) * al.getNutrient(NutrientMacro.CAL))
                    al.quantite = pas * (1 + Math.floor(q / pas))
                }
                initial = getNutrient(NutrientMacro.CAL)
                initialP = getNutrient(NutrientMacro.PHOS)
                val r2 = getSumWeight(targetAdjust.CALCIUMPHOS)
                if (r2 > 0) {
                    var outer = 0
                    while ((objectifPCa * 1.1) > (initial / initialP) && outer < 1000) {
                        alimentList.filter { it.weight > 0 && (it.getNutrient(NutrientMacro.CAL) / it.getNutrient(
                            NutrientMacro.PHOS) > 1 && it.target == targetAdjust.CALCIUMPHOS) }.forEach { al ->
                            val q = 100 * dif / ((r2 / al.weight) * al.getNutrient(NutrientMacro.CAL))
                            al.quantite = pas + al.quantite
                        }
                        initial = getNutrient(NutrientMacro.CAL)
                        initialP = getNutrient(NutrientMacro.PHOS)
                        outer++
                    }
                }
            }
        }
    }

    private fun adjust(target: Float, tar: targetAdjust, pas: Float, enu: NutrientBase) {
        val initial = getNutrient(enu)
        val dif = target - initial
        if (dif > 0) {
            val r = getSumWeight(tar)
            if (r > 0) {
                alimentList.filter { it.weight > 0 && it.getNutrient(enu) > 0 && it.target == tar }.forEach { al ->
                    val q = 100 * dif / ((r / al.weight) * al.getNutrient(enu))
                    al.quantite = pas * (1 + Math.floor(q / pas))
                }
            }
        }
    }

    private fun adjust(target: Float, tar: targetAdjust, pas: Float, enu: NutrientLipid) {
        val initial = getNutrient(enu)
        val dif = target - initial
        if (dif > 0) {
            val r = getSumWeight(tar)
            if (r > 0) {
                alimentList.filter { it.weight > 0 && it.getNutrient(enu) > 0 && it.target == tar }.forEach { al ->
                    val q = 100 * dif / ((r / al.weight) * al.getNutrient(enu))
                    al.quantite = pas * (1 + Math.floor(q / pas))
                }
            }
        }
    }

    private fun adjust(target: Float, tar: targetAdjust, pas: Float, enu: NutrientVitam) {
        val initial = getNutrient(enu)
        val dif = target - initial
        if (dif > 0) {
            val r = getSumWeight(tar)
            if (r > 0) {
                alimentList.filter { it.weight > 0 && it.getNutrient(enu) > 0 && it.target == tar }.forEach { al ->
                    val q = 100 * dif / ((r / al.weight) * al.getNutrient(enu))
                    al.quantite = pas * (1 + Math.floor(q / pas))
                }
            }
        }
    }

    fun adjustEner(target: Float, tar: targetAdjust, pas: Float) {
        val initial = getEnerT()
        val dif = target - initial
        if (dif > 0) {
            val r = getSumWeight(tar)
            if (r > 0) {
                alimentList.filter { it.weight > 0 && it.DE > 0 && it.target == tar }.forEach { al ->
                    val q = 100 * dif / ((r / al.weight) * al.DE)
                    al.quantite = pas * Math.round(q / pas)
                    al.quantite += al.quantite
                }
            }
        }
    }

    override fun clone(): Ration {
        return (super.clone() as Ration).apply {
            UUID = UUID.randomUUID().toString()
            alimentList = alimentList.map { it.clone() }.toMutableList()
        }
    }

    override fun toString(): String = nom
}