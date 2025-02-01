/*class AllNutrient {
    var label: String = ""
        private set
    var unit: String = ""
        private set
    var mne: Int = 0
        private set
    var kindnut: Int = 0
        private set


    private constructor(lab: String, mne: Int, kind: Int) {
        this.label = lab
        this.mne = mne
        this.kindnut = kind
    }

    constructor(n: Nutrient) {
        this.label = n.getLabel()
        this.unit = n.getUnite()
        this.mne = n.getMNE().getCoef()
        this.kindnut = n.getCoef()
    }


    val iD: Int
        get() {
            println("ALLnut ID " + label + " " + (mne * 1000 + kindnut))
            return (mne * 1000 + kindnut)
        }

    companion object {
        private const val serialVersionUID = 1L
        fun values(): Map<*, *> {
            val v: MutableMap<*, *> = java.util.HashMap<Any, Any>()

            for (n in NutrientBase.values()) {
                v[n.getMNE().getCoef() * 1000 + n.getCoef()] = AllNutrient(n)
            }
            for (n in NutrientMacro.values()) {
                v[n.getMNE().getCoef() * 1000 + n.getCoef()] = AllNutrient(n)
            }
            for (n in NutrientMin.values()) {
                v[n.getMNE().getCoef() * 1000 + n.getCoef()] = AllNutrient(n)
            }
            for (n in NutrientAnalysis.values()) {
                v[n.getMNE().getCoef() * 1000 + n.getCoef()] = AllNutrient(n)
            }
            for (n in NutrientLipid.values()) {
                v[n.getMNE().getCoef() * 1000 + n.getCoef()] = AllNutrient(n)
            }
            for (n in NutrientVitam.values()) {
                v[n.getMNE().getCoef() * 1000 + n.getCoef()] = AllNutrient(n)
            }
            return v
        }
    }
}
*/