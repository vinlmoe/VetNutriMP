#!/usr/bin/env python3
"""Recrée captures/ à partir de guide_utilisateur.pdf (les captures ne sont pas versionnées).

pdflatex nomme les images /Im1, /Im2… dans l'ordre d'inclusion : Im1 est le logo, les suivantes
suivent l'ordre des \\capture et \\captureannotee du .tex. Sans dépendance.
Usage : python3 extraire_captures.py   (depuis docs/guide_utilisateur)
"""
import os, re, struct, zlib

data = open('guide_utilisateur.pdf', 'rb').read()
noms = re.findall(r'\\capture(?:annotee)?\{([^}]+\.png)\}', open('guide_utilisateur.tex', encoding='utf-8').read())

objets = {int(m.group(1)): (m.group(2), m.end())
          for m in re.finditer(rb'(\d+) 0 obj\s*<<(.*?)>>\s*stream\r?\n', data, re.S)}

def flux(num):
    d, debut = objets[num]
    L = re.search(rb'/Length (\d+)( 0 R)?', d)
    n = int(re.search(rb'\n%s 0 obj\s*(\d+)' % L.group(1), data).group(1)) if L.group(2) else int(L.group(1))
    return zlib.decompress(data[debut:debut + n])

def png(w, h, gris, brut):
    def bloc(t, d): return struct.pack('>I', len(d)) + t + d + struct.pack('>I', zlib.crc32(t + d) & 0xffffffff)
    pas = w * (1 if gris else 3)
    lignes = b''.join(b'\x00' + brut[i * pas:(i + 1) * pas] for i in range(h))
    return (b'\x89PNG\r\n\x1a\n' + bloc(b'IHDR', struct.pack('>IIBBBBB', w, h, 8, 0 if gris else 2, 0, 0, 0))
            + bloc(b'IDAT', zlib.compress(lignes, 6)) + bloc(b'IEND', b''))

texte = data + b''.join(flux(n) for n, (d, _) in objets.items() if b'/ObjStm' in d)
images = {int(k): int(n) for k, n in re.findall(rb'/Im(\d+) (\d+) 0 R', texte)}
os.makedirs('captures', exist_ok=True)
for rang, nom in enumerate(noms, start=2):
    d, _ = objets[images[rang]]
    w = int(re.search(rb'/Width (\d+)', d).group(1)); h = int(re.search(rb'/Height (\d+)', d).group(1))
    open(os.path.join('captures', nom), 'wb').write(png(w, h, b'DeviceGray' in d, flux(images[rang])))
print(len(noms), 'captures écrites dans captures/')
