package fr.vetbrain.vetnutri_mp.Enumer

enum class NutrientVitam(
        val displayName: String,
        override val coef: Int,
        override val unite: String,
        override val ue: UnitEnum,
        val abr: String,
        override val label: String,
        /** Liste de labels alternatifs pour ce nutriment */
        val altLabels: List<String> = emptyList()
) : Nutrient {
        VITA("Vitamine A", 0, "UI", UnitEnum.AUui, "Vit A", "VITA", listOf("VIT_A", "VITAMINE_A")),
        VITC("Vitamine C", 1, "mg", UnitEnum.BUmg, "Vit C", "VITC", listOf("VIT_C", "VITAMINE_C")),
        VITD("Vitamine D", 2, "UI", UnitEnum.DUui, "Vit D", "VITD", listOf("VIT_D", "VITAMINE_D")),
        VITE("Vitamine E", 3, "UI", UnitEnum.EUui, "Vit E", "VITE", listOf("VIT_E", "VITAMINE_E")),
        VITK("Vitamine K", 4, "mg", UnitEnum.BUmg, "Vit K", "VITK", listOf("VIT_K", "VITAMINE_K")),
        VITB1(
                "Thiamine (B1)",
                5,
                "mg",
                UnitEnum.BUmg,
                "Vit B1",
                "VITB1",
                listOf("VIT_B1", "VITAMINE_B1")
        ),
        VITB2(
                "Riboflavine (B2)",
                6,
                "mg",
                UnitEnum.BUmg,
                "Vit B2",
                "VITB2",
                listOf("VIT_B2", "VITAMINE_B2")
        ),
        VITB3(
                "Nicotinamide, Niacine (B3/PP)",
                7,
                "mg",
                UnitEnum.BUmg,
                "Vit B3",
                "VITB3",
                listOf("VIT_B3", "VITAMINE_B3")
        ),
        VITB5(
                "Acide pantothénique (B5)",
                8,
                "mg",
                UnitEnum.BUmg,
                "Vit B5",
                "VITB5",
                listOf("VIT_B5", "VITAMINE_B5")
        ),
        VITB6(
                "Pyridoxine (B6)",
                9,
                "mg",
                UnitEnum.BUmg,
                "Vit B6",
                "VITB6",
                listOf("VIT_B6", "VITAMINE_B6")
        ),
        VITB8(
                "Biotine (B8)",
                10,
                "µg",
                UnitEnum.BUmu,
                "Vit B8",
                "VITB8",
                listOf("VIT_B8", "VITAMINE_B8")
        ),
        VITB9(
                "Acide folique (B9)",
                11,
                "µg",
                UnitEnum.BUmu,
                "Vit B9",
                "VITB9",
                listOf("VIT_B9", "VITAMINE_B9")
        ),
        VITB12(
                "Cyanocobalamine (B12)",
                12,
                "µg",
                UnitEnum.BUmu,
                "Vit B12",
                "VITB12",
                listOf("VIT_B12", "VITAMINE_B12")
        ),
        CHOLINE("Choline", 13, "mg", UnitEnum.BUmg, "CHL", "CHOLINE"),
        RETINOL("Retinol", 14, "µg", UnitEnum.BUmu, "Ret", "RETINOL"),
        BETACAR("Bêta-carotène", 15, "µg", UnitEnum.BUmu, "Bet", "BETACAR");

        override fun getMNE() = MainNutrientEnum.VITAM

        companion object {
                private val coefMap = values().associateBy { it.coef }
                private val labelMap = values().associateBy { it.label }

                /** Map des labels alternatifs vers leur nutriment correspondant */
                private val altLabelMap by lazy {
                        val map = mutableMapOf<String, NutrientVitam>()
                        values().forEach { nutrient ->
                                nutrient.altLabels.forEach { altLabel -> map[altLabel] = nutrient }
                        }
                        map
                }

                fun isByLabel(label: String): Boolean {
                        return label in labelMap || label in altLabelMap
                }

                fun getByCoef(coef: Int) = coefMap[coef]

                fun getByLabel(label: String): NutrientVitam? {
                        // Priorité aux labels originaux
                        return labelMap[label] ?: altLabelMap[label]
                }

                fun size() = values().size
        }
}
