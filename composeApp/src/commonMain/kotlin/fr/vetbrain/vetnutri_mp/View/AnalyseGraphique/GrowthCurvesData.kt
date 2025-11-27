package fr.vetbrain.vetnutri_mp.View.AnalyseGraphique

// Données des courbes de croissance pour chiens
val courbesCroissanceChien =
        listOf(
                CurveP(
                        "Female < 6.5kg",
                        listOf(
                                CurveParamP("0.4%", 1.109854, 12.08458, 1.810753),
                                CurveParamP("2%", 1.535427, 14.0034, 1.597385),
                                CurveParamP("9%", 2.039161, 15.27291, 1.562812),
                                CurveParamP("25%", 2.616284, 16.01972, 1.537392),
                                CurveParamP("50%", 3.211721, 15.877, 1.579648),
                                CurveParamP("75%", 3.866406, 15.38784, 1.625081),
                                CurveParamP("91%", 4.578398, 14.65672, 1.682141),
                                CurveParamP("98%", 5.349884, 13.75693, 1.760424),
                                CurveParamP("99.6%", 6.165964, 12.8673, 1.828332)
                        ),
                        "Référence bibliographique",
                        "Canis familiaris",
                        "0",
                        12
                ),
                CurveP(
                        "Male < 6.5kg",
                        listOf(
                                CurveParamP("0.4%", 1.272559, 12.78178, 2.038467),
                                CurveParamP("2%", 1.687415, 14.07996, 1.904777),
                                CurveParamP("9%", 2.255764, 15.48813, 1.754783),
                                CurveParamP("25%", 2.91251, 16.3563, 1.731087),
                                CurveParamP("50%", 3.699072, 16.83234, 1.719296),
                                CurveParamP("75%", 4.561009, 16.77394, 1.749617),
                                CurveParamP("91%", 5.447244, 16.18983, 1.775277),
                                CurveParamP("98%", 6.330286, 15.13836, 1.820627),
                                CurveParamP("99.6%", 7.250097, 14.11965, 1.82745)
                        ),
                        "Référence bibliographique",
                        "Canis familiaris",
                        "0",
                        12
                ),
                CurveP(
                        "Female [6.5-9]kg",
                        listOf(
                                CurveParamP("0.4%", 2.863651, 16.3876, 1.835619),
                                CurveParamP("2%", 3.459252, 16.58917, 1.845262),
                                CurveParamP("9%", 4.189704, 16.83913, 1.823969),
                                CurveParamP("25%", 4.971365, 16.67714, 1.835692),
                                CurveParamP("50%", 5.885028, 16.33739, 1.786134),
                                CurveParamP("75%", 6.76091, 15.82528, 1.882438),
                                CurveParamP("91%", 7.816347, 15.42912, 1.900119),
                                CurveParamP("98%", 8.969031, 15.0315, 1.936451),
                                CurveParamP("99.6%", 10.213104, 14.70955, 1.943391)
                        ),
                        "Référence bibliographique",
                        "Canis familiaris",
                        "0",
                        12
                ),
                CurveP(
                        "Female [9-15]kg",
                        listOf(
                                CurveParamP("0.4%", 3.935947, 19.21075, 1.68409),
                                CurveParamP("2%", 5.565112, 20.72705, 1.629216),
                                CurveParamP("9%", 7.121573, 20.5551, 1.66364),
                                CurveParamP("25%", 8.647994, 19.88941, 1.686283),
                                CurveParamP("50%", 10.151493, 18.98618, 1.717085),
                                CurveParamP("75%", 11.604435, 18.076, 1.743606),
                                CurveParamP("91%", 12.938343, 17.19162, 1.768996),
                                CurveParamP("98%", 14.178662, 16.33742, 1.797089),
                                CurveParamP("99.6%", 15.292909, 15.58999, 1.824913)
                        ),
                        "Référence bibliographique",
                        "Canis familiaris",
                        "0",
                        12
                ),
                CurveP(
                        "Female [15-30]kg",
                        listOf(
                                CurveParamP("0.4%", 12.48477, 20.53315, 2.285163),
                                CurveParamP("2%", 14.89311, 19.4463, 2.321938),
                                CurveParamP("9%", 17.52067, 18.76811, 2.316963),
                                CurveParamP("25%", 20.12642, 18.34083, 2.332147),
                                CurveParamP("50%", 22.95002, 18.25048, 2.308356),
                                CurveParamP("75%", 25.67431, 18.02113, 2.28165),
                                CurveParamP("91%", 28.05483, 17.54262, 2.296781),
                                CurveParamP("98%", 30.07991, 16.82682, 2.333136),
                                CurveParamP("99.6%", 31.97356, 16.1728, 2.357102)
                        ),
                        "Référence bibliographique",
                        "Canis familiaris",
                        "0",
                        12
                ),
                CurveP(
                        "Female [30-40]kg",
                        listOf(
                                CurveParamP("0.4%", 19.73027, 23.17412, 2.201269),
                                CurveParamP("2%", 22.28789, 21.42993, 2.20492),
                                CurveParamP("9%", 24.69983, 20.03905, 2.313005),
                                CurveParamP("25%", 27.27341, 19.08208, 2.332765),
                                CurveParamP("50%", 29.79266, 18.34165, 2.337946),
                                CurveParamP("75%", 32.23491, 17.68342, 2.364681),
                                CurveParamP("91%", 35.00966, 17.18375, 2.346992),
                                CurveParamP("98%", 37.94763, 16.67522, 2.318143),
                                CurveParamP("99.6%", 40.90867, 16.4006, 2.296036)
                        ),
                        "Référence bibliographique",
                        "Canis familiaris",
                        "0",
                        12
                ),
                CurveP(
                        "Male [6.5-9]kg",
                        listOf(
                                CurveParamP("0.4%", 3.347141, 17.37989, 2.040052),
                                CurveParamP("2%", 4.102578, 17.57553, 2.009356),
                                CurveParamP("9%", 4.962623, 17.49206, 1.993075),
                                CurveParamP("25%", 5.850609, 17.11389, 1.999331),
                                CurveParamP("50%", 6.763033, 16.4929, 2.018385),
                                CurveParamP("75%", 7.754581, 15.8306, 2.033104),
                                CurveParamP("91%", 8.86195, 15.26972, 2.066372),
                                CurveParamP("98%", 10.163427, 14.78888, 2.065822),
                                CurveParamP("99.6%", 11.483938, 14.29, 2.088452)
                        ),
                        "Référence bibliographique",
                        "Canis familiaris",
                        "0",
                        12
                ),
                CurveP(
                        "Male [9-15]kg",
                        listOf(
                                CurveParamP("0.4%", 5.984049, 20.14643, 1.967823),
                                CurveParamP("2%", 9.846229, 19.27645, 2.022343),
                                CurveParamP("9%", 13.263023, 17.86271, 2.06549),
                                CurveParamP("25%", 3.899886, 19.13236, 1.930418),
                                CurveParamP("50%", 8.058689, 19.93668, 1.954662),
                                CurveParamP("75%", 11.590806, 18.56514, 2.046613),
                                CurveParamP("91%", 14.746881, 16.89629, 2.110846),
                                CurveParamP("98%", 16.172128, 15.87276, 2.149589),
                                CurveParamP("99.6%", 17.471866, 14.94524, 2.197558)
                        ),
                        "Référence bibliographique",
                        "Canis familiaris",
                        "0",
                        12
                ),
                CurveP(
                        "Male [15-30]kg",
                        listOf(
                                CurveParamP("0.4%", 14.24569, 21.71184, 2.494588),
                                CurveParamP("2%", 18.17658, 20.35052, 2.479084),
                                CurveParamP("9%", 21.85644, 19.68832, 2.425187),
                                CurveParamP("25%", 25.18467, 19.15104, 2.358881),
                                CurveParamP("50%", 28.14069, 18.80447, 2.365743),
                                CurveParamP("75%", 31.03546, 18.58387, 2.298283),
                                CurveParamP("91%", 33.55348, 18.07695, 2.276793),
                                CurveParamP("98%", 35.93635, 17.49292, 2.234906),
                                CurveParamP("99.6%", 38.02187, 16.82385, 2.191478)
                        ),
                        "Référence bibliographique",
                        "Canis familiaris",
                        "0",
                        12
                ),
                CurveP(
                        "Male [30-40]kg",
                        listOf(
                                CurveParamP("0.4%", 23.20063, 24.57251, 2.338363),
                                CurveParamP("2%", 26.14607, 22.2654, 2.399394),
                                CurveParamP("9%", 29.25459, 20.79793, 2.426847),
                                CurveParamP("25%", 32.24568, 19.60027, 2.406423),
                                CurveParamP("50%", 34.91356, 18.56937, 2.444728),
                                CurveParamP("75%", 37.58418, 17.9064, 2.443216),
                                CurveParamP("91%", 40.59932, 17.33738, 2.429597),
                                CurveParamP("98%", 43.82808, 16.94919, 2.413749),
                                CurveParamP("99.6%", 47.07196, 16.65365, 2.365383)
                        ),
                        "Référence bibliographique",
                        "Canis familiaris",
                        "0",
                        12
                )
        )

