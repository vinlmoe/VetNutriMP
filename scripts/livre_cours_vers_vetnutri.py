#!/usr/bin/env python3
"""Convertit un livre de cours LaTeX en fichier JSON importable dans VetNutri MP.

Chaque \\section d'un chapitre devient un « extrait du livre de cours »
(conseil de catégorie LIVRE_COURS) : invisible dans la liste des conseils de
l'ordonnance, proposé dans l'Assistant IA du compte rendu pour être joint au
prompt.

Usage :
    python3 scripts/livre_cours_vers_vetnutri.py /chemin/Book-nutrition -o livre_cours.json

Le fichier produit s'importe comme un fichier de conseils (import JSON de
l'application). Réimporter remplace les extraits de même identifiant.

Conversion : sous-sections -> titres, itemize/enumerate -> listes,
tabular/tabularx -> tableaux, équations conservées en LaTeX, citations,
index, labels et figures retirés, \\ref{x} -> [x].
"""
import argparse
import json
import re
import sys
import time
import unicodedata
from pathlib import Path

SECTIONS_EXCLUES_DEFAUT = r"^(exercices?|mise a jour|mise à jour|modifications?|références)\b"

COMMANDES_SUPPRIMEES = [
    "supercite", "cite", "citep", "citet", "parencite", "textcite", "autocite",
    "index", "label", "includegraphics", "vspace", "hspace", "addcontentsline",
    "phantomsection", "setlength", "pagebreak",
]
COMMANDES_SANS_ARGUMENT = [
    "noindent", "centering", "hline", "toprule", "midrule", "bottomrule",
    "clearpage", "newpage", "par", "small", "footnotesize", "large", "Large",
    "normalsize", "raggedright", "medskip", "bigskip", "smallskip", "maketitle",
]
ENVIRONNEMENTS_SUPPRIMES = ["figure", "figure*", "flushright", "wrapfigure", "comment"]
ENVIRONNEMENTS_MATHS = ["equation", "equation*", "align", "align*", "eqnarray", "eqnarray*", "multline"]


def lire_argument(texte, debut):
    """Retourne (contenu, fin) de l'argument {…} commençant à texte[debut] == '{'."""
    if debut >= len(texte) or texte[debut] != "{":
        return "", debut
    profondeur = 0
    i = debut
    while i < len(texte):
        c = texte[i]
        if c == "\\":
            i += 2
            continue
        if c == "{":
            profondeur += 1
        elif c == "}":
            profondeur -= 1
            if profondeur == 0:
                return texte[debut + 1:i], i + 1
        i += 1
    return texte[debut + 1:], len(texte)


def sauter_options(texte, i):
    """Saute les espaces et un éventuel argument optionnel [...]."""
    while i < len(texte) and texte[i] in " \t":
        i += 1
    if i < len(texte) and texte[i] == "[":
        fin = texte.find("]", i)
        if fin >= 0:
            return fin + 1
    return i


def remplacer_commande(texte, nom, transformer, nb_args=1):
    """Remplace \\nom[opt]{a1}…{an} par transformer(args) (récursif sur les arguments)."""
    motif = re.compile(r"\\" + re.escape(nom) + r"(?![A-Za-z])\*?")
    sortie = []
    position = 0
    while True:
        m = motif.search(texte, position)
        if not m:
            break
        i = sauter_options(texte, m.end())
        args = []
        for _ in range(nb_args):
            while i < len(texte) and texte[i] in " \t\n":
                i += 1
            arg, i2 = lire_argument(texte, i)
            if i2 == i:
                break
            args.append(arg)
            i = i2
        if len(args) < nb_args:
            sortie.append(texte[position:m.end()])
            position = m.end()
            continue
        sortie.append(texte[position:m.start()])
        sortie.append(transformer(args))
        position = i
    sortie.append(texte[position:])
    return "".join(sortie)


def sans_commentaires(texte):
    return "\n".join(re.sub(r"(?<!\\)%.*$", "", ligne) for ligne in texte.split("\n"))


def retirer_environnement(texte, nom):
    return re.sub(r"\\begin\{" + re.escape(nom) + r"\}.*?\\end\{" + re.escape(nom) + r"\}", "", texte, flags=re.S)


def nettoyer_inline(texte):
    """Texte LaTeX en ligne -> texte brut (les maths $…$ sont conservées)."""
    for nom in COMMANDES_SUPPRIMEES:
        texte = remplacer_commande(texte, nom, lambda a: "")
    for nom in ("newcolumntype", "renewcommand", "newcommand"):
        texte = remplacer_commande(texte, nom, lambda a: "", nb_args=2)
    texte = remplacer_commande(texte, "ref", lambda a: "[" + a[0] + "]")
    texte = remplacer_commande(texte, "eqref", lambda a: "[" + a[0] + "]")
    texte = remplacer_commande(texte, "footnote", lambda a: " (" + a[0].strip() + ")")
    texte = remplacer_commande(texte, "href", lambda a: a[1] + " (" + a[0] + ")", nb_args=2)
    texte = remplacer_commande(texte, "multicolumn", lambda a: a[2], nb_args=3)
    texte = remplacer_commande(texte, "multirow", lambda a: a[2], nb_args=3)
    texte = remplacer_commande(texte, "textsuperscript", lambda a: "^" + a[0])
    texte = remplacer_commande(texte, "textsubscript", lambda a: "_" + a[0])
    for nom in ("textbf", "textit", "emph", "underline", "texttt", "textsc", "mbox", "url", "textrm", "textsf", "uline"):
        texte = remplacer_commande(texte, nom, lambda a: a[0])
    for nom in COMMANDES_SANS_ARGUMENT:
        texte = re.sub(r"\\" + nom + r"(?![A-Za-z])\s*", "", texte)
    remplacements = [
        (r"\og", "« "), (r"\fg{}", " »"), (r"\fg", " »"), (r"\%", "%"), (r"\&", "&"),
        (r"\_", "_"), (r"\#", "#"), (r"\$", "$"), (r"\ldots", "…"), (r"\dots", "…"),
        (r"\textdegree", "°"), (r"\degree", "°"), (r"\times", "×"), ("---", "—"), ("--", "–"),
        ("``", "« "), ("''", " »"), (r"\,", " "), (r"\;", " "), (r"\ ", " "), (r"\newline", "\n"),
    ]
    for ancien, nouveau in remplacements:
        texte = texte.replace(ancien, nouveau)
    texte = re.sub(r"(?<!\\)~", " ", texte)
    texte = re.sub(r"\\\\(\[[^\]]*\])?", "\n", texte)
    # Commandes restantes hors maths : \cmd{x} -> x, \cmd -> ""
    morceaux = re.split(r"(\$[^$]*\$)", texte)
    for k in range(0, len(morceaux), 2):
        m = re.sub(r"\\(begin|end)\{[^}]*\}(\{[^}]*\}|\[[^\]]*\])*", "", morceaux[k])
        for _ in range(3):
            m = re.sub(r"\\[A-Za-z]+\*?(\[[^\]]*\])?\{([^{}]*)\}", r"\2", m)
        m = re.sub(r"\\[A-Za-z]+\*?", "", m)
        morceaux[k] = m.replace("{", "").replace("}", "").replace("\\", "")
    texte = "".join(morceaux)
    texte = re.sub(r"[ \t]+", " ", texte)
    texte = re.sub(r" +([,.;:)])", r"\1", texte)
    return "\n".join(l.strip() for l in texte.split("\n")).strip()


class Blocs:
    def __init__(self, prefixe):
        self.prefixe = prefixe
        self.liste = []

    def _id(self, lettre):
        return f"{self.prefixe}-{lettre}{len(self.liste) + 1}"

    def paragraphe(self, texte, italique=False):
        texte = nettoyer_inline(texte)
        if texte:
            self.liste.append({
                "type": "fr.vetbrain.vetnutri_mp.Data.TextBlockApi.Paragraph",
                "id": self._id("p"), "text": texte,
                "formatting": {"isBold": False, "isItalic": italique, "isUnderline": False,
                               "isStrikethrough": False, "alignment": "LEFT"},
            })

    def titre(self, niveau, texte):
        texte = nettoyer_inline(texte)
        if texte:
            self.liste.append({"type": "fr.vetbrain.vetnutri_mp.Data.TextBlockApi.Heading",
                               "id": self._id("h"), "level": niveau, "text": texte})

    def liste_items(self, items, ordonnee):
        items = [i for i in (nettoyer_inline(x) for x in items) if i]
        if items:
            self.liste.append({"type": "fr.vetbrain.vetnutri_mp.Data.TextBlockApi.ListBlock",
                               "id": self._id("l"), "items": items, "isOrdered": ordonnee})

    def tableau(self, lignes):
        lignes = [l for l in lignes if any(c for c in l)]
        if lignes:
            self.liste.append({"type": "fr.vetbrain.vetnutri_mp.Data.TextBlockApi.TableBlock",
                               "id": self._id("t"), "headers": lignes[0], "rows": lignes[1:]})


def lire_tabular(corps):
    """Corps d'un environnement tabular/tabularx (après la spécification de colonnes) -> lignes de cellules."""
    lignes = []
    for brute in re.split(r"\\\\(?:\[[^\]]*\])?", corps):
        brute = re.sub(r"\\(hline|toprule|midrule|bottomrule)\b", "", brute)
        brute = re.sub(r"\\cline\{[^}]*\}", "", brute)
        if not brute.strip():
            continue
        cellules = [nettoyer_inline(c).replace("\n", " ") for c in re.split(r"(?<!\\)&", brute)]
        lignes.append(cellules)
    return lignes


def extraire_tabular(texte):
    """Trouve le premier tabular/tabularx/longtable du texte : (lignes, début, fin) ou None."""
    m = re.search(r"\\begin\{(tabularx|tabular\*?|longtable)\}", texte)
    if not m:
        return None
    nom = m.group(1)
    i = m.end()
    if nom in ("tabularx", "tabular*"):
        _, i = lire_argument(texte, i)
    i = sauter_options(texte, i)
    _, i = lire_argument(texte, i)
    fin = texte.find("\\end{" + nom + "}", i)
    if fin < 0:
        return None
    return lire_tabular(texte[i:fin]), m.start(), fin + len("\\end{" + nom + "}")


def convertir_corps(corps, blocs):
    """Découpe un corps de section en blocs (titres, paragraphes, listes, tableaux, équations)."""
    jetons = re.compile(
        r"\\(sub)?subsection\*?\{|\\paragraph\*?\{|\\begin\{(itemize|enumerate|table\*?|tabularx|tabular\*?|longtable|"
        + "|".join(re.escape(e) for e in ENVIRONNEMENTS_MATHS)
        + r"|theorem|exercise|exercices)\}"
    )
    position = 0
    while True:
        m = jetons.search(corps, position)
        texte_avant = corps[position:m.start() if m else len(corps)]
        for para in re.split(r"\n\s*\n", texte_avant):
            blocs.paragraphe(para)
        if not m:
            return
        jeton = m.group(0)
        if jeton.startswith("\\subsubsection") or jeton.startswith("\\subsection") or jeton.startswith("\\paragraph"):
            niveau = 3 if jeton.startswith("\\subsubsection") else (4 if jeton.startswith("\\paragraph") else 2)
            titre, position = lire_argument(corps, m.end() - 1)
            blocs.titre(niveau, titre)
            continue
        env = m.group(2)
        fin_env = trouver_fin_environnement(corps, env, m.end())
        contenu = corps[m.end():fin_env]
        position = fin_env + len("\\end{" + env + "}")
        if env in ("itemize", "enumerate"):
            items = re.split(r"\\item(?:\[[^\]]*\])?", contenu)[1:]
            items = [re.sub(r"\\(begin|end)\{(itemize|enumerate)\}", "", it) for it in items]
            blocs.liste_items(items, env == "enumerate")
        elif env.startswith("table") and not env.startswith("tabular"):
            legende = ""
            mc = re.search(r"\\caption", contenu)
            if mc:
                legende, _ = lire_argument(contenu, sauter_options(contenu, mc.end()))
            label = re.search(r"\\label\{([^}]*)\}", contenu)
            tab = extraire_tabular(contenu)
            if legende or label:
                blocs.paragraphe(("Tableau [" + label.group(1) + "] : " if label else "Tableau : ") + legende, italique=True)
            if tab:
                blocs.tableau(tab[0])
        elif env in ("tabularx", "tabular", "tabular*", "longtable"):
            tab = extraire_tabular(m.group(0) + contenu + "\\end{" + env + "}")
            if tab:
                blocs.tableau(tab[0])
        elif env in ENVIRONNEMENTS_MATHS:
            label = re.search(r"\\label\{([^}]*)\}", contenu)
            formule = re.sub(r"\\label\{[^}]*\}", "", contenu).strip()
            if formule:
                prefixe = "Équation [" + label.group(1) + "] : " if label else "Équation : "
                blocs.liste.append({
                    "type": "fr.vetbrain.vetnutri_mp.Data.TextBlockApi.Paragraph",
                    "id": blocs._id("e"), "text": prefixe + "$" + " ".join(formule.split()) + "$",
                    "formatting": {"isBold": False, "isItalic": False, "isUnderline": False,
                                   "isStrikethrough": False, "alignment": "LEFT"},
                })
        else:  # theorem (Exemple), exercise
            libelle = "Exemple" if env == "theorem" else "Exercice"
            blocs.paragraphe(libelle + " : " + contenu)


def trouver_fin_environnement(texte, env, debut):
    """Position de \\end{env} correspondant (environnements imbriqués de même nom gérés)."""
    ouvrant = "\\begin{" + env + "}"
    fermant = "\\end{" + env + "}"
    profondeur = 1
    i = debut
    while profondeur > 0:
        o = texte.find(ouvrant, i)
        f = texte.find(fermant, i)
        if f < 0:
            return len(texte)
        if 0 <= o < f:
            profondeur += 1
            i = o + len(ouvrant)
        else:
            profondeur -= 1
            i = f + len(fermant) if profondeur > 0 else f
    return i


def ascii_slug(texte):
    texte = unicodedata.normalize("NFKD", texte).encode("ascii", "ignore").decode()
    return re.sub(r"[^a-z0-9]+", "-", texte.lower()).strip("-")[:60]


def fichiers_inclus(livre):
    main = sans_commentaires((livre / "main.tex").read_text(encoding="utf-8"))
    for nom in re.findall(r"\\(?:include|input)\{([^}]+)\}", main):
        chemin = livre / (nom if nom.endswith(".tex") else nom + ".tex")
        if chemin.name != "preambule.tex" and chemin.exists():
            yield chemin


def convertir_chapitre(chemin, exclues, horodatage):
    texte = sans_commentaires(chemin.read_text(encoding="utf-8"))
    for env in ENVIRONNEMENTS_SUPPRIMES:
        texte = retirer_environnement(texte, env)
    m = re.search(r"\\chapter\*?\{", texte)
    if not m:
        return []
    titre_chapitre, fin = lire_argument(texte, m.end() - 1)
    titre_chapitre = nettoyer_inline(titre_chapitre)
    label = re.match(r"\s*\\label\{([^}]*)\}", texte[fin:])
    slug_chapitre = ascii_slug(label.group(1) if label else titre_chapitre)
    corps = texte[fin:]

    sections = []
    debuts = [s for s in re.finditer(r"\\section\*?\{", corps)]
    preambule = corps[:debuts[0].start()] if debuts else corps
    if nettoyer_inline(preambule).strip() and len(nettoyer_inline(preambule)) > 200:
        sections.append(("Présentation du chapitre", preambule))
    for k, s in enumerate(debuts):
        titre, fin_titre = lire_argument(corps, s.end() - 1)
        fin_section = debuts[k + 1].start() if k + 1 < len(debuts) else len(corps)
        sections.append((nettoyer_inline(titre).strip('"'), corps[fin_titre:fin_section]))

    extraits = []
    deja = set()
    for titre, contenu in sections:
        if re.search(exclues, titre, re.I):
            continue
        slug = ascii_slug(titre) or "section"
        identifiant = f"livre-{slug_chapitre}-{slug}"
        n = 2
        while identifiant in deja:
            identifiant = f"livre-{slug_chapitre}-{slug}-{n}"
            n += 1
        deja.add(identifiant)
        blocs = Blocs(identifiant)
        convertir_corps(contenu, blocs)
        if not blocs.liste:
            continue
        termes = sorted({nettoyer_inline(t) for t in re.findall(r"\\index\{([^}]*)\}", contenu)} - {""})
        extraits.append({
            "id": identifiant,
            "title": f"{titre_chapitre} — {titre}",
            "content": {"blocks": blocs.liste},
            "category": "LIVRE_COURS",
            "tags": list(dict.fromkeys(["livre-cours", titre_chapitre, titre] + termes)),
            "priority": 0,
            "isActive": True,
            "targetSpecies": [],
            "targetAgeGroups": [],
            "usageCount": 0,
            "isTemplate": False,
            "createdAt": horodatage,
            "updatedAt": horodatage,
        })
    return extraits


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("livre", type=Path, help="dossier du livre (contenant main.tex)")
    parser.add_argument("-o", "--sortie", type=Path, default=Path("livre_cours_vetnutri.json"))
    parser.add_argument("--exclure", default=SECTIONS_EXCLUES_DEFAUT,
                        help="expression régulière des titres de section ignorés")
    args = parser.parse_args()

    if not (args.livre / "main.tex").exists():
        sys.exit(f"main.tex introuvable dans {args.livre}")
    horodatage = int(time.time() * 1000)
    conseils = []
    for chemin in fichiers_inclus(args.livre):
        extraits = convertir_chapitre(chemin, args.exclure, horodatage)
        print(f"{chemin.relative_to(args.livre)} : {len(extraits)} extrait(s)", file=sys.stderr)
        conseils.extend(extraits)

    enveloppe = {
        "version": "1.0.0", "generatedAtEpochMs": horodatage,
        "animals": [], "foods": [], "rations": [], "recipes": [], "equations": [],
        "biblioRefs": [], "references": [], "conseils": conseils, "consultationKeywords": [],
    }
    args.sortie.write_text(json.dumps(enveloppe, ensure_ascii=False, indent=1), encoding="utf-8")
    print(f"{len(conseils)} extraits écrits dans {args.sortie}", file=sys.stderr)


if __name__ == "__main__":
    main()
