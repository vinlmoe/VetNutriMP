package fr.vetbrain.vetnutri_mp.Data

/*
 * Catalogue des races de chiens et de chats proposées à la saisie (hors ligne).
 *
 * Une ligne par race : `français|anglais|chinois|synonymes` (synonymes séparés par `;`, facultatifs ;
 * ils servent seulement à la recherche, quelle que soit la langue de l'interface).
 *
 * Chiens : nomenclature de la FCI (Fédération cynologique internationale, à laquelle se réfère la
 * Société centrale canine), d'après le jeu de données paiv/fci-breeds (licence MIT), noms français
 * et anglais réécrits dans leur forme usuelle, variétés courantes séparées (caniche, teckel, berger
 * belge, spitz) et quelques races ou croisements fréquents hors FCI. Chats : races du LOOF (Livre
 * officiel des origines félines) et races courantes d'autres registres, plus les chats domestiques.
 */

internal const val DONNEES_RACES_CHIENS = """
Pointer anglais|English Pointer|英国波音达犬|Pointer
Setter anglais|English Setter|英国塞特犬
Kerry blue terrier|Kerry Blue Terrier|凯利蓝梗|Terrier Kerry blue
Cairn terrier|Cairn Terrier|凯恩梗
Cocker anglais|English Cocker Spaniel|英国可卡犬|Cocker spaniel anglais;Cocker;可卡
Setter Gordon|Gordon Setter|戈登塞特犬
Airedale terrier|Airedale Terrier|万能梗
Terrier australien|Australian Terrier|澳大利亚梗
Bedlington terrier|Bedlington Terrier|贝林登梗|Terrier de Bedlington
Border terrier|Border Terrier|边境梗
Bull terrier|Bull Terrier|牛头梗
Fox-terrier à poil lisse|Smooth Fox Terrier|短毛猎狐梗|Fox terrier;猎狐梗
Terrier anglais d'agrément noir et feu|English Toy Terrier (Black & Tan)|英国玩具梗|Toy terrier
Vallhund suédois|Swedish Vallhund|瑞典柯基犬|Spitz des Wisigoths;Spitz des Visigoths
Berger belge|Belgian Shepherd Dog|比利时牧羊犬|Chien de berger belge
Berger belge malinois|Belgian Malinois|马利诺斯犬（比利时马犬）|Malinois;马犬
Berger belge groenendael|Belgian Groenendael|格罗安达犬（比利时黑色牧羊犬）|Groenendael
Berger belge tervueren|Belgian Tervuren|特伏丹犬（比利时特武伦牧羊犬）|Tervueren;Tervuren
Berger belge laekenois|Belgian Laekenois|拉坎诺斯犬（比利时拉肯牧羊犬）|Laekenois
Bobtail|Old English Sheepdog|古代英国牧羊犬|Chien de berger anglais ancestral;古牧;Berger anglais ancestral
Griffon nivernais|Griffon Nivernais|尼韦奈格里芬犬
Briquet griffon vendéen|Briquet Griffon Vendéen|旺代布里凯格里芬犬
Ariégeois|Ariegeois|阿列日猎犬
Gascon saintongeois|Gascon Saintongeois|加斯科-圣通日猎犬
Grand bleu de Gascogne|Great Gascony Blue|大加斯科涅蓝色猎犬
Poitevin|Poitevin|普瓦图猎犬
Billy|Billy|比利犬
Chien d'Artois|Artois Hound|阿图瓦猎犬
Porcelaine|Porcelaine|瓷器犬
Petit bleu de Gascogne|Small Blue Gascony|小加斯科涅蓝色猎犬
Griffon bleu de Gascogne|Blue Gascony Griffon|加斯科涅蓝色格里芬犬
Grand basset griffon vendéen|Grand Basset Griffon Vendéen|大型旺代巴吉度格里芬犬|GBGV
Basset artésien normand|Norman Artesian Basset|阿图瓦诺曼底短腿猎犬
Basset bleu de Gascogne|Blue Gascony Basset|加斯科涅蓝色短腿猎犬
Basset fauve de Bretagne|Fawn Brittany Basset|布列塔尼淡黄短腿猎犬
Chien d'eau portugais|Portuguese Water Dog|葡萄牙水犬
Welsh corgi cardigan|Welsh Corgi Cardigan|卡迪根威尔士柯基犬|Corgi;柯基
Welsh corgi pembroke|Welsh Corgi Pembroke|彭布罗克威尔士柯基犬|Corgi;柯基
Terrier irlandais à poil doux|Irish Soft Coated Wheaten Terrier|爱尔兰软毛麦色梗|Wheaten terrier
Berger yougoslave de Charplanina|Sharplanina|沙普兰尼那犬|Sarplaninac;Yugoslavian Shepherd Dog
Chien d'élan suédois|Jämthund|瑞典猎鹿犬|Jämthund;Swedish Elkhound
Basenji|Basenji|巴仙吉犬
Beauceron|Beauce Sheepdog|博斯牧羊犬|Berger de Beauce;Bas-rouge;Beauceron
Bouvier bernois|Bernese Mountain Dog|伯恩山犬|Berner sennenhund
Bouvier appenzellois|Appenzell Cattle Dog|阿彭策尔山犬|Bouvier de l'Appenzell
Bouvier de l'Entlebuch|Entlebuch Cattle Dog|恩特布山犬
Chien d'ours de Carélie|Karelian Bear Dog|卡累利亚熊犬
Spitz finlandais|Finnish Spitz|芬兰狐狸犬
Terre-neuve|Newfoundland|纽芬兰犬|Chien de Terre-Neuve
Chien courant finlandais|Finnish Hound|芬兰猎犬
Brachet polonais|Polish Hound|波兰猎犬
Komondor|Komondor|可蒙犬
Kuvasz|Kuvasz|库瓦兹犬
Puli|Puli|波利犬
Pumi|Pumi|波密犬
Braque hongrois à poil court (Vizsla)|Hungarian Short-haired Pointer (Vizsla)|维兹拉犬（短毛）|Vizsla;维兹拉;Braque hongrois à poil court
Grand bouvier suisse|Great Swiss Mountain Dog|大瑞士山地犬
Chien courant suisse|Swiss Hound|瑞士猎犬
Petit chien courant suisse|Small Swiss Hound|小型瑞士猎犬
Saint-bernard|St. Bernard|圣伯纳犬|Chien du Mont Saint-Bernard;Saint Bernard
Brachet de Styrie à poil dur|Coarse-haired Styrian Hound|施蒂里亚粗毛猎犬
Brachet autrichien noir et feu|Austrian Black and Tan Hound|奥地利黑褐猎犬|Quatre-œillé
Pinscher autrichien|Austrian Pinscher|奥地利宾莎犬
Bichon maltais|Maltese|马尔济斯犬|Maltais;马尔泰
Griffon fauve de Bretagne|Fawn Brittany Griffon|布列塔尼淡黄格里芬犬
Petit basset griffon vendéen|Petit Basset Griffon Vendéen|小型旺代巴吉度格里芬犬|PBGV
Brachet tyrolien|Tyrolean Hound|蒂罗尔猎犬
Lakeland terrier|Lakeland Terrier|湖畔梗
Terrier de Manchester|Manchester Terrier|曼彻斯特梗
Norwich terrier|Norwich Terrier|罗威士梗
Scottish terrier|Scottish Terrier|苏格兰梗|Terrier écossais;Scottie
Sealyham terrier|Sealyham Terrier|西里汉梗
Skye terrier|Skye Terrier|斯凯梗
Staffordshire bull terrier|Staffordshire Bull Terrier|斯塔福郡斗牛梗|Staffie;Staffy;Staffordshire bull terrier ou Staffie
Épagneul nain continental|Continental Toy Spaniel|蝴蝶犬|Papillon;Phalène
Welsh terrier|Welsh Terrier|威尔士梗|Terrier gallois
Griffon bruxellois|Griffon Bruxellois|布鲁塞尔格里芬犬
Griffon belge|Griffon Belge|比利时格里芬犬
Petit brabançon|Petit Brabançon|小布拉班松犬
Schipperke|Schipperke|史奇派克犬
Chien de Saint-Hubert|Bloodhound|寻血猎犬|Saint-Hubert
West highland white terrier|West Highland White Terrier|西高地白梗|Westie;西高地;Terrier blanc des Highlands de l'Ouest
Yorkshire terrier|Yorkshire Terrier|约克夏梗|Yorkshire;Terrier du Yorkshire;Yorkie;约克夏
Berger catalan|Catalan Sheepdog|加泰罗尼亚牧羊犬|Chien de berger catalan;Gos d'atura
Berger des Shetland|Shetland Sheepdog|喜乐蒂牧羊犬|Sheltie;Chien de berger des Shetland;喜乐蒂
Podenco d'Ibiza|Ibizan Podenco|伊比萨猎犬|Ibizan Hound
Perdiguero de Burgos|Burgos Pointing Dog|布尔戈斯指示犬|Braque de Burgos
Mâtin espagnol|Spanish Mastiff|西班牙獒犬
Mâtin des Pyrénées|Pyrenean Mastiff|比利牛斯獒犬
Berger de la Serra de Aires|Portuguese Sheepdog|葡萄牙牧羊犬
Podengo portugais|Portuguese Podengo|葡萄牙波登可犬|Chien de garenne portugais;Portuguese Warren Hound
Épagneul breton|Brittany Spaniel|布列塔尼猎犬|Brittany
Rafeiro de l'Alentejo|Rafeiro of Alentejo|阿兰特茹獒犬
Spitz allemand|German Spitz|德国狐狸犬|Spitz
Loulou de Poméranie|Pomeranian|博美犬|Spitz nain;Zwergspitz;Spitz allemand nain;博美
Keeshond|Keeshond|荷兰毛狮犬|Spitz loup;Wolfspitz
Drahthaar|German Wire-haired Pointing Dog|德国刚毛指示犬|Braque allemand à poil dur;Chien d'arrêt allemand à poil dur;German Wirehaired Pointer
Braque de Weimar|Weimaraner|魏玛猎犬|Weimaraner
Basset de Westphalie|Westphalian Dachsbracke|威斯特伐利亚达克斯布若卡犬
Bouledogue français|French Bulldog|法国斗牛犬|Bulldog français;Frenchie;法斗
Petit épagneul de Münster|Small Münsterländer|小明斯特兰犬|Kleiner Münsterländer;Petit Münsterländer
Terrier de chasse allemand|German Hunting Terrier|德国猎梗|Jagdterrier
Épagneul allemand|German Spaniel|德国猎鹬犬|Chien d'oysel allemand;Wachtelhund
Barbet|French Water Dog|法国水犬|Barbet
Épagneul bleu de Picardie|Blue Picardy Spaniel|蓝色皮卡第猎犬
Griffon Korthals|Wire-haired Pointing Griffon Korthals|科萨尔格里芬犬|Griffon à poil dur Korthals;Griffon d'arrêt à poil dur Korthals
Épagneul picard|Picardy Spaniel|皮卡第猎犬
Clumber spaniel|Clumber Spaniel|克伦伯猎犬
Retriever à poil bouclé|Curly Coated Retriever|卷毛寻回犬|Curly
Golden retriever|Golden Retriever|金毛寻回犬|Golden;金毛
Briard|Briard|伯瑞犬|Berger de Brie
Épagneul de Pont-Audemer|Pont-Audemer Spaniel|蓬托德梅尔猎犬
Braque Saint-Germain|Saint Germain Pointer|圣日耳曼指示犬
Dogue de Bordeaux|Dogue de Bordeaux|波尔多犬|French Mastiff
Braque allemand à poil long|Deutsch Langhaar|德国长毛指示犬|Langhaar;German Longhaired Pointer;Chien d'arrêt allemand à poil long
Grand épagneul de Münster|Large Münsterländer|大明斯特兰犬
Braque allemand à poil court|German Short-haired Pointing Dog|德国短毛指示犬|Kurzhaar;German Shorthaired Pointer
Setter irlandais rouge|Irish Red Setter|爱尔兰红色塞特犬|Irish Setter;Setter irlandais
Retriever à poil plat|Flat Coated Retriever|平毛寻回犬|Flat-coated retriever
Labrador retriever|Labrador Retriever|拉布拉多寻回犬|Labrador;Retriever du Labrador;拉布拉多
Field spaniel|Field Spaniel|田野猎犬
Chien d'eau irlandais|Irish Water Spaniel|爱尔兰水猎犬
English springer spaniel|English Springer Spaniel|英国史宾格犬|Springer;史宾格
Welsh springer spaniel|Welsh Springer Spaniel|威尔士史宾格犬|Springer;Épagneul springer gallois
Sussex spaniel|Sussex Spaniel|苏塞克斯猎犬
Épagneul King Charles|King Charles Spaniel|查理王猎犬|King Charles
Chien courant du Småland|Smålandsstövare|斯莫兰猎犬
Drever|Drever|瑞典达克斯布若卡犬|Basset suédois
Chien courant de Schiller|Schillerstövare|席勒猎犬
Chien courant de Hamilton|Hamiltonstövare|汉密尔顿猎犬
Braque français type Gascogne|French Pointing Dog, Gascogne type|法国加斯科涅指示犬
Braque français type Pyrénées|French Pointing Dog, Pyrenean type|法国比利牛斯指示犬
Lapphund suédois|Swedish Lapphund|瑞典拉普猎犬
Cavalier King Charles|Cavalier King Charles Spaniel|骑士查理王猎犬|Cavalier;Cavalier King Charles spaniel;查理王;Cavalier roi Charles spaniel
Chien de montagne des Pyrénées|Pyrenean Mountain Dog|大白熊犬|Montagne des Pyrénées;Patou;Great Pyrenees
Berger des Pyrénées à face rase|Pyrenean Sheepdog, Smooth-faced|比利牛斯牧羊犬（短脸）|Labrit
Terrier irlandais|Irish Terrier|爱尔兰梗
Boston terrier|Boston Terrier|波士顿梗|Terrier de Boston
Berger des Pyrénées à poil long|Long-haired Pyrenean Sheepdog|比利牛斯牧羊犬（长毛）|Labrit
Tchouvatch slovaque|Slovakian Chuvach|斯洛伐克楚瓦卡犬
Dobermann|Dobermann|杜宾犬|Doberman;杜宾
Boxer|Boxer|拳师犬
Leonberg|Leonberger|兰伯格犬|Chien de Leonberg;Leonberger
Rhodesian ridgeback|Rhodesian Ridgeback|罗得西亚脊背犬|Chien de Rhodésie à crête dorsale
Rottweiler|Rottweiler|罗威纳犬|罗威纳
Teckel|Dachshund|腊肠犬|Dachshund;Teckel standard;Basset allemand;腊肠
Teckel nain|Miniature Dachshund|迷你腊肠犬|Dachshund
Teckel kaninchen|Rabbit Dachshund|兔型腊肠犬|Dachshund
Bulldog anglais|English Bulldog|英国斗牛犬|Bulldog;英斗
Chien courant serbe|Serbian Hound|塞尔维亚猎犬
Chien courant d'Istrie à poil ras|Istrian Short-haired Hound|伊斯特拉短毛猎犬
Chien courant d'Istrie à poil dur|Istrian Wire-haired Hound|伊斯特拉粗毛猎犬
Dalmatien|Dalmatian|大麦町犬|斑点狗
Chien courant de la vallée de la Save|Posavatz Hound|波萨维茨猎犬|Chien courant de Posavatz
Barak|Bosnian Broken-haired Hound (Barak)|波斯尼亚粗毛猎犬|Chien courant de Bosnie à poil raide;Chien courant de Bosnie à poil dur dit Barak
Colley à poil long|Rough Collie|苏格兰牧羊犬（粗毛柯利犬）|Colley;Collie;Lassie;苏牧
Bullmastiff|Bullmastiff|斗牛獒犬
Greyhound|Greyhound|灵缇犬|Lévrier anglais;格力犬
Foxhound anglais|English Foxhound|英国猎狐犬|Foxhound
Lévrier irlandais|Irish Wolfhound|爱尔兰猎狼犬|Irish wolfhound
Beagle|Beagle|比格犬|小猎兔犬
Whippet|Whippet|惠比特犬|Lévrier whippet
Basset hound|Basset Hound|巴吉度犬|Basset
Deerhound|Deerhound|苏格兰猎鹿犬|Lévrier écossais;Scottish Deerhound
Spinone italiano|Italian Spinone|意大利史毕诺犬|Spinone
Berger allemand|German Shepherd Dog|德国牧羊犬|German Shepherd;Berger allemand à poil long;德牧
Cocker américain|American Cocker Spaniel|美国可卡犬|Cocker spaniel américain;Cocker;可卡
Dandie Dinmont terrier|Dandie Dinmont Terrier|丹迪丁蒙梗
Fox-terrier à poil dur|Wire Fox Terrier|刚毛猎狐梗|Fox terrier;猎狐梗
Chien de Castro Laboreiro|Castro Laboreiro Dog|卡斯特罗拉博雷罗犬
Bouvier des Ardennes|Bouvier des Ardennes|阿登牧牛犬
Caniche|Poodle|贵宾犬|贵宾;泰迪
Caniche toy|Toy Poodle|玩具贵宾犬|Poodle;泰迪;贵宾
Caniche nain|Miniature Poodle|迷你贵宾犬|Poodle;贵宾
Caniche moyen|Medium Poodle|中型贵宾犬|Poodle;贵宾
Grand caniche|Standard Poodle|标准贵宾犬|Caniche royal;Caniche géant;Poodle;贵宾
Chien de la Serra da Estrela|Estrela Mountain Dog|埃什特雷拉山犬
Épagneul français|French Spaniel|法国猎鹬犬
Berger picard|Picardy Sheepdog|皮卡第牧羊犬|Berger de Picardie;Picard
Braque de l'Ariège|Ariege Pointing Dog|阿列日指示犬
Braque du Bourbonnais|Bourbonnais Pointing Dog|波旁指示犬
Braque d'Auvergne|Auvergne Pointer|奥弗涅指示犬
Schnauzer géant|Giant Schnauzer|巨型雪纳瑞|雪纳瑞
Schnauzer moyen|Schnauzer|标准雪纳瑞|Schnauzer;Standard Schnauzer;雪纳瑞
Schnauzer nain|Miniature Schnauzer|迷你雪纳瑞|Schnauzer;雪纳瑞
Pinscher allemand|German Pinscher|德国宾莎犬
Pinscher nain|Miniature Pinscher|迷你宾莎犬|Zwergpinscher;小鹿犬;迷你杜宾
Affenpinscher|Affenpinscher|猴面梗
Chien d'arrêt portugais|Portuguese Pointing Dog|葡萄牙指示犬|Perdigueiro português;Braque portugais
Sloughi|Sloughi|北非猎犬|Lévrier arabe
Chien finnois de Laponie|Finnish Lapponian Dog|芬兰拉普猎犬|Lapinkoira
Hovawart|Hovawart|霍夫瓦尔特犬
Bouvier des Flandres|Bouvier des Flandres|法兰德斯畜牧犬
Kromfohrländer|Kromfohrländer|克罗姆费兰德犬
Barzoï|Borzoi|俄罗斯猎狼犬|Lévrier russe;苏俄猎狼犬
Berger bergamasque|Bergamasco Shepherd Dog|贝加马斯卡牧羊犬|Berger de Bergame
Volpino italien|Italian Volpino|意大利狐狸犬
Bichon bolonais|Bolognese|博洛尼亚犬
Mâtin napolitain|Neapolitan Mastiff|那不勒斯獒犬|Mastino napoletano
Chien courant italien à poil dur|Italian Rough-haired Segugio|意大利粗毛猎犬
Cirneco de l'Etna|Cirneco dell'Etna|西西里猎犬
Petit lévrier italien|Italian Sighthound|意大利灵缇犬|Levrette d'Italie;Italian Greyhound;Levrette italienne
Berger de la Maremme et des Abruzzes|Maremma and the Abruzzes Sheepdog|马雷马牧羊犬|Berger des Abruzzes;Maremma
Braque italien|Italian Pointing Dog|意大利指示犬|Bracco italiano
Chien courant norvégien|Norwegian Hound|挪威猎犬|Dunker
Chien courant espagnol|Spanish Hound|西班牙猎犬|Sabueso español
Chow-chow|Chow Chow|松狮犬|松狮
Épagneul japonais|Japanese Chin|日本狆|Chin
Pékinois|Pekingese|北京犬|京巴
Shih tzu|Shih Tzu|西施犬
Terrier tibétain|Tibetan Terrier|西藏梗
Chien esquimau canadien|Canadian Eskimo Dog|加拿大爱斯基摩犬|Esquimau canadien
Samoyède|Samoyed|萨摩耶犬|萨摩耶
Chien de rouge de Hanovre|Hanoverian Scent Hound|汉诺威嗅猎犬|Chien de recherche au sang de Hanovre;Chien de recherche au sang du Hanovre
Chien courant grec|Hellenic Hound|希腊猎犬
Bichon frisé|Bichon Frise|卷毛比熊犬|Bichon à poil frisé;比熊
Pudelpointer|Pudelpointer|普德尔指示犬
Chien de rouge de Bavière|Bavarian Mountain Scent Hound|巴伐利亚山地嗅猎犬|Chien de recherche au sang de la montagne bavaroise
Chihuahua|Chihuahua|吉娃娃
Français tricolore|French Tricolour Hound|法国三色猎犬
Français blanc et noir|French White & Black Hound|法国黑白猎犬
Chien d'eau frison|Wetterhoun|弗里斯兰水犬|Wetterhoun
Chien d'arrêt frison|Stabijhoun|弗里斯兰指示犬|Stabyhoun
Berger hollandais|Dutch Shepherd Dog|荷兰牧羊犬
Chien de perdrix de Drente|Drentsche Partridge Dog|德伦特猎鹧鸪犬
Fila brasileiro|Fila Brasileiro|巴西獒犬
Landseer|Landseer (European Continental type)|兰西尔犬
Lhassa apso|Lhasa Apso|拉萨犬
Lévrier afghan|Afghan Hound|阿富汗猎犬|Chien afghan
Chien courant tricolore serbe|Serbian Tricolour Hound|塞尔维亚三色猎犬
Dogue du Tibet|Tibetan Mastiff|藏獒
Épagneul tibétain|Tibetan Spaniel|西藏猎犬
Braque allemand à poil raide|Deutsch Stichelhaar|德国硬毛指示犬|Stichelhaar;Chien d'arrêt allemand à poil raide
Petit chien lion|Little Lion Dog|罗秦犬|Löwchen
Xoloitzcuintle|Xoloitzcuintle|墨西哥无毛犬|Chien nu mexicain;Xolo
Dogue allemand|Great Dane|大丹犬|Danois
Silky terrier|Australian Silky Terrier|澳大利亚丝毛梗|Terrier australien à poil soyeux
Buhund norvégien|Norwegian Buhund|挪威布哈德犬
Mudi|Mudi|穆迪犬
Braque hongrois à poil dur|Hungarian Wire-haired Pointer|刚毛维兹拉犬|Vizsla à poil dur
Lévrier hongrois|Hungarian Greyhound|匈牙利灵缇犬|Magyar agár
Chien courant de Transylvanie|Transylvanian Hound|特兰西瓦尼亚猎犬
Chien d'élan norvégien gris|Norwegian Elkhound Grey|挪威猎鹿犬（灰色）|Elkhound
Malamute de l'Alaska|Alaskan Malamute|阿拉斯加雪橇犬|Malamute;阿拉斯加;Malamute d'Alaska
Chien courant slovaque|Slovakian Hound|斯洛伐克猎犬
Barbu tchèque|Bohemian Wire-haired Pointing Griffon|捷克刚毛指示格里芬犬|Cesky fousek;Griffon d'arrêt tchèque
Terrier tchèque|Cesky Terrier|捷克梗|Cesky terrier
Aïdi|Atlas Mountain Dog (Aidi)|阿特拉斯山犬|Chien de montagne de l'Atlas
Chien du pharaon|Pharaoh Hound|法老王猎犬|Lévrier de Pharaon
Dogue de Majorque|Majorca Mastiff|马略卡獒犬|Ca de bou
Bichon havanais|Havanese|哈瓦那犬
Berger polonais de plaine|Polish Lowland Sheepdog|波兰低地牧羊犬|PON
Berger polonais de Podhale|Polish Tatra Shepherd|波兰塔特拉牧羊犬|Chien de berger polonais des Tatras;Berger des Tatras
Carlin|Pug|巴哥犬|Pug;八哥
Basset des Alpes|Alpine Dachsbracke|阿尔卑斯达克斯布若卡犬
Akita|Akita|秋田犬|Akita inu
Shiba|Shiba|柴犬|Shiba inu
Terrier japonais|Japanese Terrier|日本梗
Tosa|Tosa|土佐犬|Tosa inu
Hokkaido|Hokkaido|北海道犬
Spitz japonais|Japanese Spitz|日本狐狸犬|银狐犬
Retriever de la baie de Chesapeake|Chesapeake Bay Retriever|乞沙比克猎犬
Mastiff|Mastiff|英国獒犬
Chien norvégien de macareux|Norwegian Lundehund|挪威伦德猎犬|Lundehund
Chien courant de Hygen|Hygen Hound|海根猎犬
Chien courant de Halden|Halden Hound|哈尔登猎犬
Chien d'élan norvégien noir|Norwegian Elkhound Black|挪威猎鹿犬（黑色）|Elkhound
Saluki|Saluki|萨路基猎犬|Lévrier persan
Husky sibérien|Siberian Husky|西伯利亚哈士奇|Husky;哈士奇
Colley barbu|Bearded Collie|长须牧羊犬|Bearded collie
Norfolk terrier|Norfolk Terrier|诺福克梗
Chien de Canaan|Canaan Dog|迦南犬
Chien du Groenland|Greenland Dog|格陵兰犬
Pisteur brésilien|Brazilian Tracker|巴西追踪犬
Spitz de Norrbotten|Norrbottenspitz|诺波丹狐狸犬
Berger croate|Croatian Shepherd Dog|克罗地亚牧羊犬
Berger du Karst|Karst Shepherd Dog|喀斯特牧羊犬
Chien courant du Monténégro|Montenegrin Mountain Hound|黑山山地猎犬|Chien courant de montagne du Monténégro
Braque danois ancestral|Old Danish Pointing Dog|古丹麦指示犬|Chien d'arrêt danois ancestral
Grand griffon vendéen|Grand Griffon Vendéen|大型旺代格里芬犬
Coton de Tuléar|Coton de Tuléar|棉花面纱犬
Berger finnois de Laponie|Lapponian Herder|拉普兰驯鹿犬
Lévrier espagnol|Spanish Greyhound|西班牙灵缇犬|Galgo
American staffordshire terrier|American Staffordshire Terrier|美国斯塔福郡梗|Amstaff;Staffordshire terrier américain
Bouvier australien|Australian Cattle Dog|澳洲牧牛犬|Blue heeler;Red heeler
Chien chinois à crête|Chinese Crested Dog|中国冠毛犬
Berger islandais|Icelandic Sheepdog|冰岛牧羊犬
Beagle-harrier|Beagle Harrier|比格-哈利犬
Eurasier|Eurasier|欧亚大陆犬|Eurasian
Dogue argentin|Dogo Argentino|杜高犬|Dogo argentino;阿根廷杜高
Kelpie australien|Australian Kelpie|澳洲卡尔比犬|Kelpie
Otterhound|Otterhound|猎獭犬|Chien à loutre
Harrier|Harrier|哈利犬
Colley à poil court|Smooth Collie|短毛柯利犬|Colley;Collie
Border collie|Border Collie|边境牧羊犬|边牧
Lagotto romagnolo|Romagna Water Dog|拉戈托罗马阁挪露犬|Chien d'eau romagnol;Lagotto
Brachet allemand|German Hound|德国猎犬|Deutsche Bracke
Coonhound noir et feu|Black and Tan Coonhound|黑褐猎浣熊犬|Chien noir et feu pour la chasse au raton laveur
Chien d'eau américain|American Water Spaniel|美国水猎犬
Glen of Imaal terrier|Irish Glen of Imaal Terrier|爱尔兰峡谷梗|Terrier Glen of Imaal
Foxhound américain|American Foxhound|美国猎狐犬
Laïka russo-européen|Russian-European Laika|俄欧莱卡犬|Laika
Laïka de Sibérie orientale|East Siberian Laika|东西伯利亚莱卡犬|Laika
Laïka de Sibérie occidentale|West Siberian Laika|西西伯利亚莱卡犬|Laika
Azawakh|Azawakh|阿札瓦克犬|Lévrier azawakh
Smous des Pays-Bas|Dutch Smoushond|荷兰斯牟雄德犬
Shar-pei|Shar Pei|沙皮犬
Chien nu du Pérou|Peruvian Hairless Dog|秘鲁无毛犬
Chien-loup de Saarloos|Saarloos Wolfdog|萨卢斯猎狼犬
Retriever de la Nouvelle-Écosse|Nova Scotia Duck Tolling Retriever|新斯科舍诱鸭寻回犬|Toller
Schapendoes néerlandais|Dutch Schapendoes|荷兰斯恰潘道斯犬
Kooikerhondje|Nederlandse Kooikerhondje|荷兰诱鸭犬|Kooiker
Broholmer|Broholmer|布罗荷马獒
Français blanc et orange|French White and Orange Hound|法国橙白猎犬
Kai|Kai|甲斐犬
Kishu|Kishu|纪州犬
Shikoku|Shikoku|四国犬
Braque slovaque à poil dur|Wirehaired Slovakian Pointer|斯洛伐克刚毛指示犬
Berger de Majorque|Majorca Shepherd Dog|马略卡牧羊犬|Ca de bestiar
Grand anglo-français tricolore|Great Anglo-French Tricolour Hound|大型英法三色猎犬
Grand anglo-français blanc et noir|Great Anglo-French White and Black Hound|大型英法黑白猎犬
Grand anglo-français blanc et orange|Great Anglo-French White and Orange Hound|大型英法橙白猎犬
Anglo-français de petite vénerie|Medium-sized Anglo-French Hound|中型英法猎犬
Berger de Russie méridionale|South Russian Shepherd Dog|南俄罗斯牧羊犬|Berger de Russie méridionale ou berger d'Ukraine
Terrier noir russe|Russian Black Terrier|俄罗斯黑梗
Berger du Caucase|Caucasian Shepherd Dog|高加索牧羊犬|高加索
Podenco canario|Canarian Warren Hound|加那利猎犬|Chien de garenne des Canaries
Setter irlandais rouge et blanc|Irish Red and White Setter|爱尔兰红白塞特犬
Berger kangal|Kangal Shepherd Dog|坎高犬|Kangal;Kangal Dog
Chien-loup tchécoslovaque|Czechoslovakian Wolfdog|捷克狼犬
Lévrier polonais|Polish Greyhound|波兰灵缇犬|Chart polski
Jindo coréen|Korea Jindo Dog|韩国珍岛犬|Jindo
Berger d'Asie centrale|Central Asia Shepherd Dog|中亚牧羊犬|Alabaï
Chien d'eau espagnol|Spanish Water Dog|西班牙水犬
Chien courant italien à poil ras|Italian Short-haired Segugio|意大利短毛猎犬
Thai ridgeback|Thai Ridgeback Dog|泰国脊背犬|Chien thaïlandais à crête dorsale
Parson Russell terrier|Parson Russell Terrier|帕森罗素梗|Terrier du révérend Russell;Terrier du révérend Russel
Fila de Saint-Miguel|Saint Miguel Cattle Dog|圣米格尔牧牛犬
Terrier brésilien|Brazilian Terrier|巴西梗
Berger australien|Australian Shepherd|澳大利亚牧羊犬|Aussie;澳牧
Cane corso|Cane Corso|卡斯罗犬|Chien de cour italien;Italian Cane Corso
Akita américain|American Akita|美国秋田犬|Akita
Jack Russell terrier|Jack Russell Terrier|杰克罗素梗|Jack Russell
Dogue canari|Presa Canario|加纳利犬|Presa canario;Dogue des Canaries
Berger blanc suisse|White Swiss Shepherd Dog|瑞士白色牧羊犬|Berger blanc
Chien de Taïwan|Taiwan Dog|台湾犬
Berger roumain de Mioritza|Romanian Mioritic Shepherd Dog|罗马尼亚米奥里察牧羊犬
Berger roumain des Carpates|Romanian Carpathian Shepherd Dog|罗马尼亚喀尔巴阡牧羊犬
Bouvier australien à queue courte|Australian Stumpy Tail Cattle Dog|澳洲短尾牧牛犬|Bouvier australien à courte queue
Petit chien russe|Russian Toy|俄罗斯玩具犬|Russkiy toy
Cimarrón uruguayen|Cimarrón Uruguayo|乌拉圭西马伦犬
Chien courant polonais|Polish Hunting Dog|波兰狩猎犬
Berger de Bosnie-Herzégovine et de Croatie|Bosnian and Herzegovinian-Croatian Shepherd Dog|波黑-克罗地亚牧羊犬|Tornjak
Chien de ferme dano-suédois|Danish-Swedish Farmdog|丹麦-瑞典农场犬
Berger roumain de Bucovine|Romanian Bucovina Shepherd|罗马尼亚布科维纳牧羊犬
Bangkaew de Thaïlande|Thai Bangkaew Dog|泰国班卡乌犬
Bull terrier miniature|Miniature Bull Terrier|迷你牛头梗
Lancashire heeler|Lancashire Heeler|兰开夏赫勒犬
Segugio maremmano|Segugio Maremmano|马雷马猎犬
Chien de Bali Kintamani|Kintamani-Bali Dog|巴厘岛金塔马尼犬|Chien de Bali
Ratier de Prague|Prague Ratter|布拉格捕鼠犬
Berger de Bohême|Bohemian Shepherd Dog|波西米亚牧羊犬|Chodsky pes
Laïka de Iakoutie|Yakutian Laika|雅库特莱卡犬|Laika
Chien courant d'Estonie|Estonian Hound|爱沙尼亚猎犬
Berger américain miniature|Miniature American Shepherd|迷你美国牧羊犬|Mini aussie;Berger australien miniature
Mâtin transmontano|Transmontano Mastiff|特拉斯蒙塔诺獒犬|Cão de gado transmontano
Bulldog continental|Continental Bulldog|大陆斗牛犬
Ratier valencien|Valencian Terrier|瓦伦西亚捕鼠梗|Ratonero valenciano
Terrier andalou|Andalusian Terrier|安达卢西亚梗|Sherry terrier
Tazy kazakh|Kazakh Tazy|哈萨克塔兹犬
Berger roumain corb|Romanian Raven Shepherd Dog|罗马尼亚乌鸦牧羊犬
Bulldog campeiro brésilien|Brazilian Campeiro Bulldog|巴西坎佩罗斗牛犬
Croisé|Mixed breed|混血犬|Bâtard;Corniaud;Mongrel;Crossbreed;串串
Chien rural chinois|Chinese Rural Dog|中华田园犬|土狗
American bully|American Bully|美国恶霸犬|Bully
American pit bull terrier|American Pit Bull Terrier|美国比特犬|Pit bull;Pitbull;比特
Bouledogue américain|American Bulldog|美国斗牛犬|Bulldog américain
Berger d'Anatolie|Anatolian Shepherd Dog|安纳托利亚牧羊犬
Boerboel|Boerboel|南非獒犬
Biewer terrier|Biewer Terrier|比沃梗|Biewer yorkshire
Labradoodle|Labradoodle|拉布拉多贵宾犬|Doodle
Goldendoodle|Goldendoodle|金毛贵宾犬|Golden doodle;Doodle
Cockapoo|Cockapoo|可卡贵宾犬|Doodle
Maltipoo|Maltipoo|马尔济斯贵宾犬
"""

internal const val DONNEES_RACES_CHATS = """
Abyssin|Abyssinian|阿比西尼亚猫
American bobtail|American Bobtail|美国短尾猫|Bobtail américain
American curl|American Curl|美国卷耳猫
American shorthair|American Shorthair|美国短毛猫|美短
American wirehair|American Wirehair|美国刚毛猫
Anatoli|Anatolian|安纳托利亚猫
Angora turc|Turkish Angora|土耳其安哥拉猫|Angora
Asian|Asian|亚洲猫
Balinais|Balinese|巴厘猫
Bengal|Bengal|孟加拉豹猫|豹猫
Bleu russe|Russian Blue|俄罗斯蓝猫
Bobtail des Kouriles|Kurilian Bobtail|千岛短尾猫
Bobtail japonais|Japanese Bobtail|日本短尾猫
Bombay|Bombay|孟买猫
Brazilian shorthair|Brazilian Shorthair|巴西短毛猫
British longhair|British Longhair|英国长毛猫|英长
British shorthair|British Shorthair|英国短毛猫|英短
Brume australienne|Australian Mist|澳大利亚雾猫
Burmese|Burmese|缅甸猫
Burmilla|Burmilla|波米拉猫
Californian spangled|California Spangled|加州闪亮猫
Chantilly|Chantilly-Tiffany|尚蒂伊猫
Chartreux|Chartreux|沙特尔猫|Chartreuse
Chausie|Chausie|肖西猫
Colorpoint shorthair|Colorpoint Shorthair|重点色短毛猫
Cornish rex|Cornish Rex|柯尼斯卷毛猫
Cymric|Cymric|威尔士猫
Devon rex|Devon Rex|德文卷毛猫
Donskoy|Donskoy|顿斯科伊猫|Don sphynx
Européen|European Shorthair|欧洲短毛猫|European
Exotic shorthair|Exotic Shorthair|异国短毛猫|Exotic;加菲猫
German rex|German Rex|德国卷毛猫
Havana brown|Havana Brown|哈瓦那棕猫|Havana
Highland fold|Highland Fold|苏格兰折耳长毛猫|Scottish fold longhair;折耳;Highland fold[11]
Highlander|Highlander|高地猞猁猫
Himalayen|Himalayan|喜马拉雅猫|Colourpoint persan
Khao Manee|Khao Manee|泰国钻石眼猫|考曼尼
Korat|Korat|呵叻猫
LaPerm|LaPerm|拉邦猫
Li hua|Dragon Li|中国狸花猫|狸花猫;Chinese Li Hua
Lykoi|Lykoi|狼猫
Maine coon|Maine Coon|缅因猫
Mandarin|Oriental Longhair|东方长毛猫|Oriental longhair;Javanais
Manx|Manx|马恩岛猫
Mau arabe|Arabian Mau|阿拉伯猫
Mau égyptien|Egyptian Mau|埃及猫
Minskin|Minskin|明斯欣猫
Minuet|Minuet|拿破仑猫|Napoléon
Munchkin|Munchkin|曼基康猫|矮脚猫
Nebelung|Nebelung|尼比龙猫
Neva masquerade|Neva Masquerade|涅瓦假面猫|Sibérien
Norvégien|Norwegian Forest Cat|挪威森林猫|Skogkatt;Chat des forêts norvégiennes;Norvégien ou Skogkatt
Ocicat|Ocicat|欧西猫
Ojos azules|Ojos Azules|蓝眼猫
Oriental shorthair|Oriental Shorthair|东方短毛猫|Oriental
Persan|Persian|波斯猫
Peterbald|Peterbald|彼得秃猫
Pixie-bob|Pixie-bob|北美短尾猫
Ragamuffin|RagaMuffin|褴褛猫
Ragdoll|Ragdoll|布偶猫|布偶
Sacré de Birmanie|Birman|伯曼猫|Birman
Safari|Safari|萨法里猫
Savannah|Savannah|萨凡纳猫|薮猫
Scottish fold|Scottish Fold|苏格兰折耳猫|折耳
Scottish straight|Scottish Straight|苏格兰立耳猫
Selkirk rex|Selkirk Rex|塞尔凯克卷毛猫
Serengeti|Serengeti|塞伦盖蒂猫
Seychellois|Seychellois|塞舌尔猫
Siamois|Siamese|暹罗猫|暹罗
Sibérien|Siberian|西伯利亚森林猫|Siberian
Singapura|Singapura|新加坡猫
Skookum|Skookum|斯库库姆猫
Snowshoe|Snowshoe|雪鞋猫
Sokoké|Sokoke|索科克猫
Somali|Somali|索马里猫
Sphynx|Sphynx|斯芬克斯猫|Sphinx;无毛猫
Thaï|Thai|泰国猫
Tiffany|Tiffanie|蒂法尼猫
Tonkinois|Tonkinese|东奇尼猫
Toyger|Toyger|玩具虎猫
Turc de Van|Turkish Van|土耳其梵猫|Van turc
Ural rex|Ural Rex|乌拉尔卷毛猫
York chocolat|York Chocolate|约克巧克力猫
Chat domestique à poil court|Domestic Shorthair|家猫（短毛）|Gouttière;Chat de gouttière;Croisé;Européen de gouttière;DSH;中华田园猫;田园猫
Chat domestique à poil long|Domestic Longhair|家猫（长毛）|Gouttière;Croisé;DLH
"""
