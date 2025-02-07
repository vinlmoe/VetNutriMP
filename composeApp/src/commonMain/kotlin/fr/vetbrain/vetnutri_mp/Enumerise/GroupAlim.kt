package fr.vetbrain.vetnutri_mp.Enumerise

import fr.vetbrain.vetnutri_mp.Data.Labelable

enum class GroupAlim(
        val categorie: Int,
        val type: Int,
        val target: TargetAdjust,
        val id: Int,
        override val label: String?
) : Labelable {
    ALL(1, 0, TargetAdjust.NO, 999, "all"),
    ABATS(1, 0, TargetAdjust.PROT, 0, "offal"),
    AIDE(10, 0, TargetAdjust.NO, 1, "culinaryAids"),
    ALGUES(5, 0, TargetAdjust.NO, 2, "seaweed"),
    AROMATEf(5, 0, TargetAdjust.NO, 3, "freshHerbs"),
    AROMATEs(5, 0, TargetAdjust.NO, 4, "driedHerbs"),
    AutreCereal(3, 0, TargetAdjust.ENERGIE, 5, "otherCereals"),
    AutrePOISSON(1, 0, TargetAdjust.PROT, 6, "otherFishProducts"),
    ALIMenf(10, 0, TargetAdjust.ENERGIE, 7, "babyFood"),
    MGAnim(4, 0, TargetAdjust.LIP, 8, "animalFats"),
    MGLait(4, 0, TargetAdjust.LIP, 9, "dairyFats"),
    MGAutre(4, 0, TargetAdjust.LIP, 10, "otherFats"),
    BISCOTTE(3, 0, TargetAdjust.ENERGIE, 11, "rusks"),
    BISCUITSAL(3, 0, TargetAdjust.ENERGIE, 12, "savoryBiscuits"),
    BISCUITScu(3, 0, TargetAdjust.ENERGIE, 13, "sweetBiscuits"),
    BOUILLON(3, 0, TargetAdjust.ENERGIE, 14, "readyBroth"),
    CEREALPD(3, 0, TargetAdjust.ENERGIE, 15, "breakfastCereals"),
    CHARCUT(1, 0, TargetAdjust.PROT, 16, "delicatessen"),
    COMPLEMENT(5, 0, TargetAdjust.CALCIUMPHOS, 17, "supplements"),
    CREMES(1, 0, TargetAdjust.PROT, 18, "creams"),
    CRUSTACE(1, 0, TargetAdjust.PROT, 19, "shellfish"),
    DESSERTLAIT(1, 0, TargetAdjust.PROT, 20, "dairyDesserts"),
    DESSERT(3, 0, TargetAdjust.ENERGIE, 21, "otherDesserts"),
    EAUX(6, 0, TargetAdjust.NO, 22, "water"),
    EPICES(10, 0, TargetAdjust.NO, 23, "spices"),
    FRUITSIRP(2, 0, TargetAdjust.NO, 24, "cannedFruits"),
    FRUITFRAIS(2, 0, TargetAdjust.NO, 25, "freshFruits"),
    FRUITCOQUE(4, 0, TargetAdjust.NO, 26, "nuts"),
    JUS(10, 0, TargetAdjust.NO, 27, "juices"),
    AutreFRUITS(2, 0, TargetAdjust.NO, 28, "otherFruits"),
    FarinesAmidon(3, 0, TargetAdjust.ENERGIE, 29, "flourStarch"),
    PAINS(3, 0, TargetAdjust.ENERGIE, 30, "bread"),
    VIENNOISERIE(3, 0, TargetAdjust.ENERGIE, 31, "pastries"),
    GATEAUX(3, 0, TargetAdjust.ENERGIE, 32, "cakes"),
    CAKES(3, 0, TargetAdjust.ENERGIE, 33, "savoryCakes"),
    PATETARTE(3, 0, TargetAdjust.ENERGIE, 34, "dough"),
    FROMAGES(1, 0, TargetAdjust.PROT, 35, "cheese"),
    FRUITSEC(2, 0, TargetAdjust.NO, 36, "driedFruits"),
    EPICE(5, 0, TargetAdjust.NO, 37, "seasonings"),
    HUILES(4, 0, TargetAdjust.LIP, 38, "oils"),
    LAITS(1, 0, TargetAdjust.PROT, 39, "milk"),
    LEGUMES(2, 0, TargetAdjust.FIBER, 40, "vegetables"),
    DIVERS(10, 0, TargetAdjust.NO, 41, "miscellaneous"),
    LEGUMESSEC(2, 0, TargetAdjust.FIBER, 42, "driedVegetables"),
    OEUF(1, 0, TargetAdjust.PROT, 43, "eggs"),
    HUILEPOISSON(4, 0, TargetAdjust.EPA, 44, "fishOils"),
    POISSON(1, 0, TargetAdjust.PROT, 45, "fish"),
    POMMETERRE(3, 0, TargetAdjust.ENERGIE, 46, "potatoes"),
    PATES(3, 0, TargetAdjust.ENERGIE, 47, "pasta"),
    SAUCES(10, 0, TargetAdjust.NO, 48, "sauces"),
    SELS(5, 0, TargetAdjust.NA, 49, "salt"),
    POISSONPROD(1, 0, TargetAdjust.PROT, 50, "fishProducts"),
    RIZ(3, 0, TargetAdjust.ENERGIE, 51, "rice"),
    SALADE(2, 0, TargetAdjust.FIBER, 52, "salads"),
    PLAT(10, 0, TargetAdjust.NO, 53, "dishes"),
    SANDWICH(10, 0, TargetAdjust.NO, 54, "sandwiches"),
    SOUPE(10, 0, TargetAdjust.NO, 55, "soups"),
    SUCRE(5, 0, TargetAdjust.ENERGIE, 56, "sugar"),
    VIANDES(1, 0, TargetAdjust.PROT, 57, "meat"),
    VOLLAILLE(1, 0, TargetAdjust.PROT, 58, "poultry"),
    YAOURT(1, 0, TargetAdjust.PROT, 59, "yogurt"),
    AUTRES(10, 0, TargetAdjust.NO, 60, "others"),
    FLAIT(1, 1, TargetAdjust.PROT, 61, "dairyProducts"),
    FARO(5, 1, TargetAdjust.NO, 62, "herbsSpices"),
    FBEBE(10, 1, TargetAdjust.NO, 63, "babyFood"),
    FHUILE(4, 1, TargetAdjust.LIP, 64, "fatsOils"),
    FVOL(1, 1, TargetAdjust.PROT, 65, "poultryProducts"),
    FPOT(2, 1, TargetAdjust.LIP, 66, "soupsAndSauces"),
    FSAUCI(1, 1, TargetAdjust.PROT, 67, "sausages"),
    FCERE(3, 1, TargetAdjust.ENERGIE, 68, "breakfastCereals"),
    FFRUIT(2, 1, TargetAdjust.FIBER, 69, "fruitsAndJuices"),
    FPORC(1, 1, TargetAdjust.PROT, 70, "porkProducts"),
    FLEG(2, 1, TargetAdjust.FIBER, 71, "vegetableProducts"),
    FNOIX(3, 1, TargetAdjust.NO, 72, "nutsAndSeeds"),
    FBOEUF(1, 1, TargetAdjust.PROT, 73, "beefProducts"),
    FBOISS(6, 1, TargetAdjust.NO, 74, "beverages"),
    FPOISS(1, 1, TargetAdjust.PROT, 75, "seafoodProducts"),
    FLEGUMUNEUSE(2, 1, TargetAdjust.FIBER, 76, "legumes"),
    FAGNEAU(1, 1, TargetAdjust.PROT, 77, "lambAndGame"),
    FBOULAN(3, 1, TargetAdjust.ENERGIE, 78, "bakeryProducts"),
    FSUCRE(3, 1, TargetAdjust.ENERGIE, 79, "sweets"),
    FPAIN(3, 1, TargetAdjust.ENERGIE, 80, "cerealsAndPasta"),
    FPRET(10, 1, TargetAdjust.NO, 81, "readyToEat"),
    FCOMP(10, 1, TargetAdjust.ENERGIE, 82, "composedDishes"),
    FGRI(10, 1, TargetAdjust.ENERGIE, 83, "snacks");

    companion object {
        fun byId(id: Int): GroupAlim = entries.find { it.id == id } ?: AUTRES

        fun isPresent(group: GroupAlim): Boolean = entries.contains(group)

        fun byName(name: String): GroupAlim =
                entries.find { it.label?.equals(name, ignoreCase = true) == true } ?: AUTRES

        fun listByType(type: Int): List<String> =
                entries.filter { it.type == type }.mapNotNull { it.label }

        fun valuesExcept(): List<GroupAlim> = entries.filter { it != ALL }
    }

    override fun toString() = label ?: "Unknown"
}
