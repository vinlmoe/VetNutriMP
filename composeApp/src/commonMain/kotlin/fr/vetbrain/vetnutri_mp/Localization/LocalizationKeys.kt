package fr.vetbrain.vetnutri_mp.Localization

object LocalizationKeys {
    // Clés générales de l'application
    object General {
        const val WELCOME = "welcome"
        const val APP_NAME = "general.appName"
        const val LOADING = "loading"
        const val SAVE = "save"
        const val CANCEL = "general.cancel"
        const val DELETE = "general.delete"
        const val EDIT = "general.edit"
        const val UPDATE = "update"
        const val ADD = "general.add"
        const val REMOVE = "remove"
        const val SEARCH = "general.search"
        const val CALCULATE = "calculate"
        const val VALIDATE = "general.validate"
        const val EXPORT = "export"
        const val IMPORT = "import"
        const val IMPORTING = "general.importing"
        const val IMPORT_SUCCESS = "general.importSuccess"
        const val ERROR = "general.error"
        const val OR = "general.or"
        const val CREATE = "general.create"
        const val SUCCESS = "general.success"
        const val TOTAL_ELEMENTS = "general.totalElements"
        const val CONFIRM = "general.confirm"
        const val CONFIRM_DELETE = "general.confirmDelete"
        const val DATE_PICKER = "general.datePicker"
        const val YEAR = "general.year"
        const val MONTH = "general.month"
        const val DAY = "general.day"
        const val PREVIOUS_YEAR = "general.previousYear"
        const val NEXT_YEAR = "general.nextYear"
        const val PREVIOUS_MONTH = "general.previousMonth"
        const val NEXT_MONTH = "general.nextMonth"
        const val PREVIOUS_DAY = "general.previousDay"
        const val NEXT_DAY = "general.nextDay"
        const val ANALYSE = "general.analyse"
        const val COMPARE = "general.compare"
        const val NONE = "general.none"
        
        const val UNIT_YEAR = "general.unitYear"
        const val UNIT_YEARS = "general.unitYears"
        const val UNIT_MONTH = "general.unitMonth"
        const val MEASURE_DATE = "general.measureDate"
        const val SELECT_PLACEHOLDER = "general.selectPlaceholder"
        const val EXPAND = "general.expand"
    }

    // Clés pour les aliments
    object Food {
        const val LIST_TITLE = "food.list.title"
    }

    // Clés pour les animaux
    object Animal {
        const val NAME = "animal.name"
        const val SPECIES = "animal.species"
        const val SEX = "animal.sex"
        const val BREED = "animal.breed"
        const val ID = "animal.id"
        const val OWNER = "animal.owner"
        const val DESCRIPTION = "animal.description"
        const val WEIGHT = "animal.weight"
        const val AGE = "animal.age"
        const val BIRTH_DATE = "animal.birthDate"
        const val SUMMARY = "animal.summary"
        const val STERILIZED = "animal.sterilized"
        const val DEAD = "animal.dead"
        const val EDIT_ANIMAL = "animal.edit"
        const val NEW_ANIMAL = "animal.new"
        const val DELETE_ANIMAL = "animal.delete"
        const val DELETE_ANIMAL_CONFIRM = "animal.deleteConfirm"
    }

    // Clés pour les espèces
    object Species {
        const val DOG = "species.dog"
        const val CAT = "species.cat"
        const val HORSE = "species.horse"
        const val FERRET = "species.ferret"
        const val WILD_CANINE = "species.wildCanine"
        const val WILD_FELINE = "species.wildFeline"
        const val FOLIVORE = "species.folivore"
    }

    // Clés pour les analyses nutritionnelles
    object Nutrition {
        const val PROTEINS = "nutrition.proteins"
        const val LIPIDS = "nutrition.lipids"
        const val CARBOHYDRATES = "nutrition.carbohydrates"
        const val FIBER = "nutrition.fiber"
        const val MOISTURE = "nutrition.moisture"
        const val ASH = "nutrition.ash"
        const val ENERGY = "nutrition.energy"
    }

    // Clés pour les vitamines
    object Vitamins {
        const val VITAMIN_A = "vitamins.a"
        const val VITAMIN_B1 = "vitamins.b1"
        const val VITAMIN_B2 = "vitamins.b2"
        const val VITAMIN_B3 = "vitamins.b3"
        const val VITAMIN_B5 = "vitamins.b5"
        const val VITAMIN_B6 = "vitamins.b6"
        const val VITAMIN_B8 = "vitamins.b8"
        const val VITAMIN_B9 = "vitamins.b9"
        const val VITAMIN_B12 = "vitamins.b12"
        const val VITAMIN_C = "vitamins.c"
        const val VITAMIN_D = "vitamins.d"
        const val VITAMIN_E = "vitamins.e"
        const val VITAMIN_K = "vitamins.k"
    }

    // Clés pour les minéraux
    object Minerals {
        const val CALCIUM = "minerals.calcium"
        const val PHOSPHORUS = "minerals.phosphorus"
        const val MAGNESIUM = "minerals.magnesium"
        const val SODIUM = "minerals.sodium"
        const val POTASSIUM = "minerals.potassium"
        const val CHLORINE = "minerals.chlorine"
        const val IRON = "minerals.iron"
        const val ZINC = "minerals.zinc"
        const val COPPER = "minerals.copper"
        const val MANGANESE = "minerals.manganese"
        const val IODINE = "minerals.iodine"
        const val SELENIUM = "minerals.selenium"
    }

    // Clés pour les unités
    object Units {
        const val KILOGRAM = "units.kilogram"
        const val GRAM = "units.gram"
        const val MILLIGRAM = "units.milligram"
        const val MICROGRAM = "units.microgram"
        const val LITER = "units.liter"
        const val MILLILITER = "units.milliliter"
        const val PERCENTAGE = "units.percentage"
        const val INTERNATIONAL_UNIT = "units.internationalUnit"
        const val MCAL = "units.mcal"
        const val KCAL = "units.kcal"
    }

    // Clés pour les consultations
    object Consultation {
        const val DATE = "consultation.date"
        const val OBJECTIVE = "consultation.objective"
        const val OBSERVATION = "consultation.observation"
        const val REPORT = "consultation.report"
        const val IDEAL_WEIGHT = "consultation.idealWeight"
        const val BODY_FAT = "consultation.bodyFat"
        const val BCS = "consultation.bcs"
        const val MCS = "consultation.mcs"
        const val WATER = "consultation.water"
        const val DELETE_CONSULTATION = "consultation.delete"
        const val DELETE_CONSULTATION_CONFIRM = "consultation.deleteConfirm"
        const val TITLE = "consultation.title"
        const val NONE = "consultation.none"
        const val SELECT_HINT = "consultation.selectHint"
        const val SELECT_DETAIL_HINT = "consultation.selectDetailHint"
        const val OF_DATE = "consultation.ofDate"
        const val ADD = "consultation.add"
        const val MISSING_REF_TITLE = "consultation.missingRefTitle"
        const val MISSING_REF_MESSAGE = "consultation.missingRefMessage"
    }

    object AnimalList {
        const val FOOD_LIST = "animalList.foodList"
        const val CALCULATION_DATA = "animalList.calculationData"
        const val QUICK_IMPORT = "animalList.quickImport"
        const val CROSS_ANALYSIS = "animalList.crossAnalysis"
        const val QUICK_IMPORT_TITLE = "animalList.quickImportTitle"
        const val ENTER_CODE = "animalList.enterCode"
        const val CODE_OR_URL = "animalList.codeOrUrl"
        const val SCAN_QR = "animalList.scanQr"
        const val NO_ANIMAL_FOUND = "animalList.noAnimalFound"
        const val NO_FILTER_RESULTS = "animalList.noFilterResults"
    }

    // Clés pour les rations
    object Ration {
        const val NAME = "ration.name"
        const val COEFFICIENT = "ration.coefficient"
        const val ACTUAL = "ration.actual"
        const val NUMBER = "ration.number"
        const val CONSULTATION_RATIONS = "ration.consultationRations"
        const val NO_RATION_AVAILABLE = "ration.noRationAvailable"
        const val SELECT_RATION_HINT = "ration.selectRationHint"
        const val CREATE_RECIPE = "ration.createRecipe"
        const val RECIPE_NAME = "ration.recipeName"
        const val RECIPE_CREATED = "ration.recipeCreated"
        const val ADD_FOOD_TO_RATION = "ration.addFoodToRation"
        const val DUPLICATED = "ration.duplicated"
    }

    // Clés pour les erreurs
    object Error {
        const val REQUIRED_FIELD = "error.requiredField"
        const val INVALID_VALUE = "error.invalidValue"
        const val DATABASE_ERROR = "error.database"
        const val NETWORK_ERROR = "error.network"
        const val UNKNOWN_ERROR = "error.unknown"
    }

    enum class AnimalKeys {
        ID,
        NAME,
        OWNER,
        BREED,
        BIRTHDATE,
        SUMMARY
    }

    object AnimalDetail {
        const val IDENTIFICATION = "animalDetail.identification"
        const val CONSULTATIONS = "animalDetail.consultations"
        const val RATIONS = "animalDetail.rations"
        const val GRAPH = "animalDetail.graph"
        const val FOOD_GRAPH = "animalDetail.foodGraph"
        const val EXPORT = "animalDetail.export"
        const val EXPORT_ANIMAL = "animalDetail.exportAnimal"
        const val SHARE_ONLINE = "animalDetail.shareOnline"
        const val BACK = "animalDetail.back"
        const val EXPORT_SUCCESS = "animalDetail.exportSuccess"
        const val EXPORT_ERROR = "animalDetail.exportError"
        const val EXPORT_CANCELLED = "animalDetail.exportCancelled"
        const val SHARE_SUCCESS = "animalDetail.shareSuccess"
        const val SHARE_ERROR = "animalDetail.shareError"
        const val PRESCRIPTION_TITLE = "animalDetail.prescriptionTitle"
        const val RATION_ANALYSIS_TITLE = "animalDetail.rationAnalysisTitle"
        const val LOADING_FOODS = "animalDetail.loadingFoods"
        const val LOADING_NUTRITION = "animalDetail.loadingNutrition"
        const val NO_FOOD_AVAILABLE = "animalDetail.noFoodAvailable"
        const val NO_FOOD_GRAPH_AVAILABLE = "animalDetail.noFoodGraphAvailable"
        const val BACK_TO_EXPORT = "animalDetail.backToExport"
        const val HTML_EDITOR = "animalDetail.htmlEditor"
        const val DELETE_CONFIRM_MESSAGE = "animalDetail.deleteConfirmMessage"
    }

    object Graph {
        const val NO_CONSULTATION = "graph.noConsultation"
        const val CREATE_CONSULTATION_HINT = "graph.createConsultationHint"
        const val NO_REFERENCE = "graph.noReference"
        const val SELECT_REFERENCE_HINT = "graph.selectReferenceHint"
        const val NO_PREFERENCE = "graph.noPreference"
        const val CONFIG_PREFERENCE_HINT = "graph.configPreferenceHint"
        const val CALCULATING = "graph.calculating"
        const val NO_RATION = "graph.noRation"
        const val NO_RATION_DENSITY = "graph.noRationDensity"
        
        const val ZOOM_IN = "graph.zoomIn"
        const val ZOOM_OUT = "graph.zoomOut"
        const val RESET = "graph.reset"
        
        const val ENERGY_DISTRIBUTION_TITLE = "graph.energyDistributionTitle"
        const val ENERGY_DISTRIBUTION_SUBTITLE = "graph.energyDistributionSubtitle"
        const val PROTEIN_ENERGY = "graph.proteinEnergy"
        const val LIPID_ENERGY = "graph.lipidEnergy"
        
        const val DENSITY_TITLE = "graph.densityTitle"
        const val DENSITY_SUBTITLE_DM = "graph.densitySubtitleDm"
        const val DENSITY_SUBTITLE_CAL = "graph.densitySubtitleCal"
        const val DENSITY_AXIS_DM = "graph.densityAxisDm"
        const val DENSITY_AXIS_CAL = "graph.densityAxisCal"
        const val DENSITY_LABEL = "graph.densityLabel"
        
        const val LEGEND_RATIONS = "graph.legendRations"
        const val LEGEND_CURRENT_RATIONS = "graph.legendCurrentRations"
        const val DATE_UNKNOWN = "graph.dateUnknown"
        
        const val LEGEND_ENA_20 = "graph.legendEna20"
        const val LEGEND_ENA_40 = "graph.legendEna40"
        const val LEGEND_ENA_60 = "graph.legendEna60"

        const val SELECT_CURVE = "graph.selectCurve"
        const val ZOOM_GROWTH_PDF = "graph.zoomGrowthPdf"
        const val ZOOM_CONE_REPORT = "graph.zoomConeReport"
        const val SHOW_REF_CURVES = "graph.showRefCurves"
        const val WEIGHT_EVOLUTION_TITLE = "graph.weightEvolutionTitle"
        const val WEIGHT_EVOLUTION_SUBTITLE_YEARS = "graph.weightEvolutionSubtitleYears"
        const val WEIGHT_EVOLUTION_SUBTITLE_MONTHS = "graph.weightEvolutionSubtitleMonths"
        const val AXIS_AGE_YEARS = "graph.axisAgeYears"
        const val AXIS_AGE_MONTHS = "graph.axisAgeMonths"
        const val AXIS_WEIGHT = "graph.axisWeight"
        
        const val HISTORY_TITLE = "graph.historyTitle"
        const val MIN_LOSS_LABEL = "graph.minLossLabel"
        const val MAX_LOSS_LABEL = "graph.maxLossLabel"
        const val TARGET_WEIGHT_LABEL = "graph.targetWeightLabel"
        const val CLEAR_CONE = "graph.clearCone"
        
        const val NO_WEIGHT_RECORDED = "graph.noWeightRecorded"
        const val HEADER_DATE = "graph.headerDate"
        const val HEADER_AGE = "graph.headerAge"
        const val HEADER_WEIGHT = "graph.headerWeight"
        const val HEADER_SOURCE = "graph.headerSource"
        const val HEADER_ACTIONS = "graph.headerActions"
        const val SOURCE_CONSULTATION = "graph.sourceConsultation"
        const val SOURCE_OTHER = "graph.sourceOther"
        
        const val TOOLTIP_LOSS_CONE = "graph.tooltipLossCone"
        const val TOOLTIP_DELETE_WEIGHT = "graph.tooltipDeleteWeight"
        const val BACK = "graph.back"
        
        const val EXPORT_PDF_FULL = "graph.exportPdfFull"
        const val EXPORT_PDF_GROWTH = "graph.exportPdfGrowth"
        const val ZOOM_CONE_TITLE = "graph.zoomConeTitle"
        const val ZOOM_CONE_SUBTITLE = "graph.zoomConeSubtitle"
        const val AXIS_WEEKS = "graph.axisWeeks"
        const val PERIOD_DATA_TITLE = "graph.periodDataTitle"
        const val WEEK_LABEL = "graph.weekLabel"
        
        const val GROWTH_ZOOM_TITLE = "graph.growthZoomTitle"
        const val GROWTH_ZOOM_SUBTITLE = "graph.growthZoomSubtitle"
        const val AXIS_WEEKS_SINCE_BIRTH = "graph.axisWeeksSinceBirth"
        
        const val EXPORT_TITLE_WEIGHT_LOSS = "graph.exportTitleWeightLoss"
        const val SECTION_GRAPH_EVOLUTION = "graph.sectionGraphEvolution"
        const val SECTION_DETAILED_DATA = "graph.sectionDetailedData"
        const val DIET_START = "graph.dietStart"
        const val INITIAL_WEIGHT = "graph.initialWeight"
        const val TARGET = "graph.target"
        
        const val EXPORT_TITLE_GROWTH = "graph.exportTitleGrowth"
        const val SECTION_GROWTH_GRAPH = "graph.sectionGrowthGraph"
        const val SECTION_WEIGHT_DATA = "graph.sectionWeightData"
        const val SEMAINES_TITLE = "graph.semainesTitle"
        
        const val NUTRIMENTS_ANALYSIS_TITLE = "graph.nutrimentsAnalysisTitle"
        const val NUTRIMENTS_ANALYSIS_SUBTITLE = "graph.nutrimentsAnalysisSubtitle"
        const val DISTRIBUTION_OF = "graph.distributionOf"
        const val AXIS_X_LABEL = "graph.axisXLabel"
        const val AXIS_Y_LABEL = "graph.axisYLabel"
        const val SELECT_NUTRIMENTS_X_HINT = "graph.selectNutrimentsXHint"
        const val INSUFFICIENT_DATA_HISTOGRAM = "graph.insufficientDataHistogram"
        const val INSUFFICIENT_DATA_SCATTER = "graph.insufficientDataScatter"
        const val ZOOM_IN_TOOLTIP = "graph.zoomInTooltip"
        const val ZOOM_OUT_TOOLTIP = "graph.zoomOutTooltip"
        const val RESET_ZOOM = "graph.resetZoom"
        
        const val REF_BIBLIO = "graph.refBiblio"
        const val CURVE_DOG_FEMALE_LT_6_5 = "graph.curveDogFemaleLt6.5"
        const val CURVE_DOG_MALE_LT_6_5 = "graph.curveDogMaleLt6.5"
        const val CURVE_DOG_FEMALE_6_5_9 = "graph.curveDogFemale6.5-9"
        const val CURVE_DOG_FEMALE_9_15 = "graph.curveDogFemale9-15"
        const val CURVE_DOG_FEMALE_15_30 = "graph.curveDogFemale15-30"
        const val CURVE_DOG_FEMALE_30_40 = "graph.curveDogFemale30-40"
        const val CURVE_DOG_MALE_6_5_9 = "graph.curveDogMale6.5-9"
        const val CURVE_DOG_MALE_9_15 = "graph.curveDogMale9-15"
        const val CURVE_DOG_MALE_15_30 = "graph.curveDogMale15-30"
        const val CURVE_DOG_MALE_30_40 = "graph.curveDogMale30-40"
        const val CURVE_CAT_MALE = "graph.curveCatMale"
        const val CURVE_CAT_FEMALE = "graph.curveCatFemale"
        
        const val GRAPHIC_ANALYSIS_TITLE = "graph.graphicAnalysisTitle"
        const val LEGEND_INFO_TITLE = "graph.legendInfoTitle"
        const val LEGEND_INFO_EVOLUTION = "graph.legendInfoEvolution"
        const val LEGEND_INFO_RATIONS = "graph.legendInfoRations"
        const val LEGEND_INFO_DENSITY = "graph.legendInfoDensity"
        const val LEGEND_INFO_NUTRIMENTS = "graph.legendInfoNutriments"
        const val ADD_WEIGHT_TITLE = "graph.addWeightTitle"
        const val ADD_WEIGHT_ACTION = "graph.addWeightAction"
        const val DATE_PREFIX = "graph.datePrefix"
        const val PICK_DATE_BUTTON = "graph.pickDateButton"
        const val WEIGHT_KG_LABEL = "graph.weightKgLabel"
    }

    object AnalNut {
        const val DETAILS_TITLE = "analnut.details.title"
        const val NUTRITIONAL_REFERENCES = "analnut.references.nutritional"
        const val DISEASE_REFERENCES = "analnut.references.disease"
        const val CLICK_DETAILS = "analnut.click.details"
        
        const val RATION_ALIMENTS_TITLE = "analnut.ration.aliments.title"
        const val SAVE_AS_RECIPE = "analnut.action.save_recipe"
        const val MULTI_NUTRIENT_ADJUSTMENT = "analnut.action.multi_adjustment"
        const val QUICK_MULTI_NUTRIENT_ADJUSTMENT = "analnut.action.quick_adjustment"
        const val OPEN_RECIPES = "analnut.action.open_recipes"
        const val ADD_ALIMENT = "analnut.action.add_aliment"
        const val NO_RATION_SELECTED = "analnut.error.no_ration_selected"
        const val NO_ALIMENT_IN_RATION = "analnut.error.no_aliment_in_ration"
        const val QUICK_ADJUST_SUCCESS = "analnut.success.quick_adjust"
        const val ADJUST_ERROR = "analnut.error.adjust"
        
        const val METABOLIC_VALUES_TITLE = "analnut.metabolic.title"
        const val EXPAND_METABOLIC = "analnut.metabolic.expand"
        const val WEIGHT_CURRENT = "analnut.weight.current"
        const val WEIGHT_IDEAL = "analnut.weight.ideal"
        const val WEIGHT_METABOLIC = "analnut.weight.metabolic"
        const val P_METABOLIC = "analnut.weight.p_metabolic"
        const val BEE_STANDARD = "analnut.bee.standard"
        const val COMPLEMENTARY_REQ = "analnut.req.complementary"
        const val TOTAL_REQ = "analnut.req.total"
        const val BE_SHORT = "analnut.req.be_short"
        
        const val COEFFICIENTS_TITLE = "analnut.coefficients.title"
        const val EXPAND_COEFFICIENTS = "analnut.coefficients.expand"
        const val COEFF_ADJUST = "analnut.coefficients.adjust"
        
        const val ENERGY_BALANCE_TITLE = "analnut.energy.balance.title"
        const val ENERGY_PROVIDED = "analnut.energy.provided"
        const val COVERAGE = "analnut.energy.coverage"
        const val K_OBSERVED = "analnut.k.observed"
        const val K_CALCULATED = "analnut.k.calculated"
        
        const val NUTR_ANALYSIS_TITLE = "analnut.analysis.title"
        const val DISPLAY_MODE = "analnut.display.mode"
        const val ALL = "analnut.filter.all"
        const val SELECTED = "analnut.filter.selected"
        const val SHOW_SELECTED_ONLY = "analnut.tooltip.show_selected"
        const val SHOW_ALL = "analnut.tooltip.show_all"
        const val BULLET_VIEW = "analnut.view.bullet"
        const val CARDS_VIEW = "analnut.view.cards"
        const val SHOW_CARDS = "analnut.tooltip.show_cards"
        const val SHOW_BULLETS = "analnut.tooltip.show_bullets"
        const val COMPOSITION = "analnut.chart.composition"
        const val ENERGY_ORIGIN = "analnut.chart.energy_origin"
        
        const val ADD_RATION = "analnut.ration.add"
        
        const val ADJUSTMENT_TITLE = "analnut.adjustment.title"
        const val CLOSE = "analnut.close"
        const val QUICK_ACTIONS = "analnut.actions.quick"
        const val LOCK_ALL = "analnut.actions.lock_all"
        const val UNLOCK_ALL = "analnut.actions.unlock_all"
        const val AUTO_SELECT = "analnut.actions.auto_select"
        const val RESET = "analnut.actions.reset"
        
        const val FOOD_CONFIG = "analnut.config.food"
        const val REF_LEVELS_BY_NUTRIENT = "analnut.config.ref_levels"
        const val ACTIONS = "analnut.actions.title"
        const val PREVIEW = "analnut.action.preview"
        const val ADJUST = "analnut.action.adjust"
        const val ADJUST_ENERGY_EQUAL = "analnut.action.adjust_energy_equal"
        
        const val ADJUSTING_ENERGY_MSG = "analnut.msg.adjusting_energy"
        const val ENERGY_NULL_ERROR = "analnut.error.energy_null"
        const val ADJUST_ENERGY_SUCCESS = "analnut.success.adjust_energy"
        const val CALCULATING = "analnut.msg.calculating"
        const val ADJUSTING_MSG = "analnut.msg.adjusting"
        const val ADJUST_SUCCESS = "analnut.success.adjust"
        const val ADJUST_FAIL = "analnut.error.adjust_fail"
        
        const val LOCKED_NO_ADJUSTMENT = "analnut.status.locked_no_adjustment"
        const val UNLOCK = "analnut.action.unlock"
        const val LOCK = "analnut.action.lock"
        const val CURRENT_QUANTITY = "analnut.info.current_quantity"
        const val TARGET_NUTRIENT = "analnut.label.target_nutrient"
        
        const val NO_INTAKE_DETECTED = "analnut.error.no_intake"
        const val ADJUST_NUTRIENT_SUCCESS = "analnut.success.adjust_nutrient"
        const val NO_FOOD_FOR_NUTRIENT = "analnut.error.no_food_nutrient"
        const val NO_CONTRIBUTION_POSSIBLE = "analnut.error.no_contribution"
        const val IMPOSSIBLE_COVERAGE = "analnut.error.impossible_coverage"
        const val ADJUST_NUTRIENT_ADDED = "analnut.success.adjust_added"
    }
    
    object Recipe {
        const val TITLE = "recipe.title"
        const val CREATE_TITLE = "recipe.create_title"
        const val NAME_LABEL = "recipe.name_label"
        const val APPLY = "recipe.action.apply"
        const val CLONE = "recipe.action.clone"
        // Delete, Close, Cancel can come from General or be added here if specific
        const val CONTENT_APPLY = "recipe.content.apply"
        const val CONTENT_CLONE = "recipe.content.clone"
        const val CONTENT_DELETE = "recipe.content.delete"
        const val CONTENT_CREATE = "recipe.content.create"
        // Additions for RecipeAddAlimentView
        const val ADD_FOOD_TITLE = "recipe.add_food.title"
        const val ADD_SELECTED = "recipe.add_food.add_selected"
        const val SELECT_FOOD_HINT = "recipe.add_food.select_hint"
        const val FOOD_DETAILS = "recipe.add_food.details"
        const val QUANTITY_ADD = "recipe.add_food.quantity_add"
        const val QUANTITY_PLACEHOLDER = "recipe.add_food.quantity_placeholder"
        const val QUANTITY_ERROR = "recipe.add_food.quantity_error"
        const val NUTRITIONAL_COMPOSITION = "recipe.add_food.nutritional_composition"
        const val BRAND = "recipe.detail.brand"
        const val RANGE = "recipe.detail.range"
        const val TYPE = "recipe.detail.type"
        const val GROUP = "recipe.detail.group"
        const val SPECIES = "recipe.detail.species"
        const val INDICATIONS = "recipe.detail.indications"
        const val INGREDIENTS = "recipe.detail.ingredients"
    }
    
    object Reference {
        const val SELECT_TITLE = "reference.select_title"
        const val NONE_AVAILABLE = "reference.none_available"
        const val NONE = "reference.none"
        const val SPECIES_PREFIX = "reference.species_prefix"
        const val DISEASE_PREFIX = "reference.disease_prefix"
        const val SELECTED = "reference.status.selected"
        const val SELECT = "reference.action.select"
        const val ACTIVE_REFS_TITLE = "reference.active_refs_title"
        const val GENERAL_REF_LABEL = "reference.general_ref_label"
        const val DISEASE_REFS_LABEL = "reference.disease_refs_label"
        const val UNKNOWN = "reference.unknown"
    }

    object Chart {
        const val NO_DATA = "chart.no_data"
        const val DISTRIBUTION_TITLE = "chart.distribution_title"
        const val MOISTURE = "chart.label.moisture"
        const val PROTEIN = "chart.label.protein"
        const val FAT = "chart.label.fat"
        const val ENA = "chart.label.ena"
        const val ASH = "chart.label.ash"
        const val FIBER = "chart.label.fiber"
    }
    
    object Database {
        const val INCOMPLETE_TITLE = "database.status.incomplete"
        const val UPDATE_REQUIRED = "database.status.update_required"
        const val COMPLETE_TITLE = "database.status.complete"
        const val CHECKING = "database.status.checking"
        const val LOADING = "database.status.loading"
        const val CHECKING_DB = "database.action.checking_db"
        const val UPDATING = "database.action.updating"
        const val UPDATE_TITLE = "database.dialog.update_title"
        const val UPDATE_MSG = "database.dialog.update_msg"
        const val COMPLETE_DESC = "database.status.complete_desc"
    }
    
    object Terms {
        const val TITLE = "terms.title"
        const val WARNING = "terms.warning"
        const val ACCEPT_HEADER = "terms.accept_header"
        const val CONDITION_PRO = "terms.condition_pro"
        const val CONDITION_INFO = "terms.condition_info"
        const val CONDITION_RESPONSIBILITY = "terms.condition_responsibility"
        const val CONDITION_PRIVACY = "terms.condition_privacy"
        const val CREDITS = "terms.credits"
        const val ACCEPT_BUTTON = "terms.accept_button"
    }
    
    object Update {
        const val JSON_TITLE = "update.json.title"
        const val JSON_MSG = "update.json.msg"
        const val JSON_CURRENT = "update.json.current"
        const val JSON_NEW = "update.json.new"
        const val UPDATE_BUTTON = "update.action.update"
        const val LATER_BUTTON = "update.action.later"
    }

    object Administration {
        const val TITLE = "admin.title"
        const val SUBTITLE = "admin.subtitle"
        const val AUTO_IMPORT_SUCCESS = "admin.import.success"
        const val AUTO_IMPORT_ERROR = "admin.import.error"
        const val AUTO_IMPORT_RUNNING = "admin.import.running"
        const val AUTO_IMPORT_ACTION = "admin.import.action"
        const val MANAGE_BACKUPS = "admin.backup.manage"
        const val IRREVERSIBLE_TITLE = "admin.irreversible.title"
        const val IRREVERSIBLE_MSG = "admin.irreversible.msg"
        const val CLEAR_FOODS = "admin.clear.foods"
        const val CLEAR_ANIMALS = "admin.clear.animals"
        const val CLEAR_REFS = "admin.clear.refs"
        const val CLEAR_EQUATIONS = "admin.clear.equations"
        const val CLEAR_BIBLIO = "admin.clear.biblio"
        const val CLEAR_SUCCESS = "admin.clear.success"
        const val UNEXPECTED_ERROR = "admin.error.unexpected"
    }

    object Settings {
        const val TITLE = "settings.title"
        const val DISPLAY_SETTINGS = "settings.displaySettings"
        const val UI_SCALE = "settings.uiScale"
        // Additions for InterfaceSettings
        const val SCALE_TITLE = "settings.scale.title"
        const val SCALE_SUBTITLE = "settings.scale.subtitle"
        const val SCALE_CURRENT = "settings.scale.current"
        const val SCALE_INFO_TITLE = "settings.scale.info_title"
        const val SCALE_INFO_MSG = "settings.scale.info_msg"
        
        const val DATABASE_MANAGEMENT = "settings.databaseManagement"
        const val FOOD_DB = "settings.foodDb"
        const val REFERENCE_DB = "settings.referenceDb"
        const val USER_DATA = "settings.userData"
        const val GENERAL_SETTINGS = "settings.generalSettings"
        const val DEBUG_MODE = "settings.debugMode"
        const val CLOSE = "settings.close"
        const val TAB_GENERAL = "settings.tab.general"
        const val TAB_PROFIL = "settings.tab.profil"
        const val TAB_SUBSCRIPTION = "settings.tab.subscription"
        const val TAB_ABOUT = "settings.tab.about"
        const val TAB_DEBUG = "settings.tab.debug"
        const val TAB_BACKUP = "settings.tab.backup"
        const val TAB_INTERFACE = "settings.tab.interface"
        const val TAB_PREFERENCES = "settings.tab.preferences"
        const val TAB_ADMINISTRATION = "settings.tabAdministration"
        const val TAB_IMPORT = "settings.tab.import"
        const val TAB_EXCEL = "settings.tab.excel"
        const val TAB_RECIPES = "settings.tab.recipes"
        const val CONSEILS_COUNT = "settings.conseilsCount"
        const val SELECT_ANIMALS = "settings.selectAnimals"
        const val SELECT_FOODS = "settings.selectFoods"
        const val INCLUDE_ANIMALS = "settings.includeAnimals"
        const val INCLUDE_FOODS = "settings.includeFoods"
        const val INCLUDE_EQUATIONS = "settings.includeEquations"
        const val INCLUDE_RATIONS = "settings.includeRations"
        const val INCLUDE_RECIPES = "settings.includeRecipes"
        const val INCLUDE_CONSEILS = "settings.includeConseils"
        const val IMPORT_ANIMALS = "settings.importAnimals"
        const val EXPORT_API = "settings.exportApi"
        const val IMPORT_API = "settings.importApi"
        const val IMPORT_JSONBIN = "settings.importJsonbin"
        const val JSONBIN_TITLE = "settings.jsonbinTitle"
        const val JSONBIN_MESSAGE = "settings.jsonbinMessage"
        const val JSONBIN_LABEL = "settings.jsonbinLabel"
        const val JSONBIN_PLACEHOLDER = "settings.jsonbinPlaceholder"
        const val IMPORT_RUNNING = "settings.importRunning"
    }
}
