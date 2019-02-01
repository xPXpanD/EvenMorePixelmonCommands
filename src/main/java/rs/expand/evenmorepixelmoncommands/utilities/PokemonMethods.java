package rs.expand.evenmorepixelmoncommands.utilities;

// Remote imports.
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;

// Local imports.
import static rs.expand.evenmorepixelmoncommands.EMPC.*;

// Big ol' list of Pokémon.
public enum PokemonMethods
{
    // Gen 1
    Bulbasaur(1, 0, "Grass", "Poison"),
    Ivysaur(2, 0, "Grass", "Poison"),
    Venusaur(3, 0, "Grass", "Poison"),
    Charmander(4, 0, "Fire"),
    Charmeleon(5, 0, "Fire"),
    Charizard(6, 0, "Fire", "Flying"),
    Squirtle(7, 0, "Water"),
    Wartortle(8, 0, "Water"),
    Blastoise(9, 0, "Water"),
    Caterpie(10, 0, "Bug"),
    Metapod(11, 0, "Bug"),
    Butterfree(12, 0, "Bug", "Flying"),
    Weedle(13, 0, "Bug", "Poison"),
    Kakuna(14, 0, "Bug", "Poison"),
    Beedrill(15, 0, "Bug", "Poison"),
    Pidgey(16, 0, "Normal", "Flying"),
    Pidgeotto(17, 0, "Normal", "Flying"),
    Pidgeot(18, 0, "Normal", "Flying"),
    Rattata(19, 0, "Normal"),
    Raticate(20, 0, "Normal"),
    Spearow(21, 0, "Normal", "Flying"),
    Fearow(22, 0, "Normal", "Flying"),
    Ekans(23, 0, "Poison"),
    Arbok(24, 0, "Poison"),
    Pikachu(25, 0, "Electric"),
    Raichu(26, 0, "Electric"),
    Sandshrew(27, 0, "Ground"),
    Sandslash(28, 0, "Ground"),
    NidoranFemale(29, 0, "Poison"),
    Nidorina(30, 0, "Poison"),
    Nidoqueen(31, 0, "Poison", "Ground"),
    NidoranMale(32, 0, "Poison"),
    Nidorino(33, 0, "Poison"),
    Nidoking(34, 0, "Poison", "Ground"),
    Clefairy(35, 0, "Fairy"),
    Clefable(36, 0, "Fairy"),
    Vulpix(37, 0, "Fire"),
    Ninetales(38, 0, "Fire"),
    Jigglypuff(39, 0, "Normal", "Fairy"),
    Wigglytuff(40, 0, "Normal", "Fairy"),
    Zubat(41, 0, "Poison", "Flying"),
    Golbat(42, 0, "Poison", "Flying"),
    Oddish(43, 0, "Grass", "Poison"),
    Gloom(44, 0, "Grass", "Poison"),
    Vileplume(45, 0, "Grass", "Poison"),
    Paras(46, 0, "Bug", "Grass"),
    Parasect(47, 0, "Bug", "Grass"),
    Venonat(48, 0, "Bug", "Poison"),
    Venomoth(49, 0, "Bug", "Poison"),
    Diglett(50, 0, "Ground"),
    Dugtrio(51, 0, "Ground"),
    Meowth(52, 0, "Normal"),
    Persian(53, 0, "Normal"),
    Psyduck(54, 0, "Water"), // so tempted
    Golduck(55, 0, "Water"),
    Mankey(56, 0, "Fighting"),
    Primeape(57, 0, "Fighting"),
    Growlithe(58, 0, "Fire"),
    Arcanine(59, 0, "Fire"),
    Poliwag(60, 0, "Water"),
    Poliwhirl(61, 0, "Water"),
    Poliwrath(62, 0, "Water", "Fighting"),
    Abra(63, 0, "Psychic"),
    Kadabra(64, 0, "Psychic"),
    Alakazam(65, 0, "Psychic"),
    Machop(66, 0, "Fighting"),
    Machoke(67, 0, "Fighting"),
    Machamp(68, 0, "Fighting"),
    Bellsprout(69, 0, "Grass", "Poison"),
    Weepinbell(70, 0, "Grass", "Poison"),
    Victreebel(71, 0, "Grass", "Poison"),
    Tentacool(72, 0, "Water", "Poison"),
    Tentacruel(73, 0, "Water", "Poison"),
    Geodude(74, 0, "Rock", "Ground"),
    Graveler(75, 0, "Rock", "Ground"),
    Golem(76, 0, "Rock", "Ground"),
    Ponyta(77, 0, "Fire"),
    Rapidash(78, 0, "Fire"),
    Slowpoke(79, 0, "Water", "Psychic"),
    Slowbro(80, 0, "Water", "Psychic"),
    Magnemite(81, 0, "Electric", "Steel"),
    Magneton(82, 0, "Electric", "Steel"),
    Farfetchd(83, 0, "Normal", "Flying"),
    Doduo(84, 0, "Normal", "Flying"),
    Dodrio(85, 0, "Normal", "Flying"),
    Seel(86, 0, "Water"),
    Dewgong(87, 0, "Water", "Ice"),
    Grimer(88, 0, "Poison"),
    Muk(89, 0, "Poison"),
    Shellder(90, 0, "Water"),
    Cloyster(91, 0, "Water", "Ice"),
    Gastly(92, 0, "Ghost", "Poison"),
    Haunter(93, 0, "Ghost", "Poison"),
    Gengar(94, 0, "Ghost", "Poison"),
    Onix(95, 0, "Rock", "Ground"),
    Drowzee(96, 0, "Psychic"),
    Hypno(97, 0, "Psychic"),
    Krabby(98, 0, "Water"),
    Kingler(99, 0, "Water"),
    Voltorb(100, 0, "Electric"),
    Electrode(101, 0, "Electric"),
    Exeggcute(102, 0, "Grass", "Psychic"),
    Exeggutor(103, 0, "Grass", "Psychic"),
    Cubone(104, 0, "Ground"),
    Marowak(105, 0, "Ground"),
    Hitmonlee(106, 0, "Fighting"),
    Hitmonchan(107, 0, "Fighting"),
    Lickitung(108, 0, "Normal"),
    Koffing(109, 0, "Poison"),
    Weezing(110, 0, "Poison"),
    Rhyhorn(111, 0, "Ground", "Rock"),
    Rhydon(112, 0, "Ground", "Rock"),
    Chansey(113, 0, "Normal"),
    Tangela(114, 0, "Grass"),
    Kangaskhan(115, 0, "Normal"),
    Horsea(116, 0, "Water"),
    Seadra(117, 0, "Water"),
    Goldeen(118, 0, "Water"),
    Seaking(119, 0, "Water"),
    Staryu(120, 0, "Water"),
    Starmie(121, 0, "Water", "Psychic"),
    MrMime(122, 0, "Psychic", "Fairy"),
    Scyther(123, 0, "Bug", "Flying"),
    Jynx(124, 0, "Ice", "Psychic"),
    Electabuzz(125, 0, "Electric"),
    Magmar(126, 0, "Fire"),
    Pinsir(127, 0, "Bug"),
    Tauros(128, 0, "Normal"),
    Magikarp(129, 0, "Water"),
    Gyarados(130, 0, "Water", "Flying"),
    Lapras(131, 0, "Water", "Ice"),
    Ditto(132, 0, "Normal"),
    Eevee(133, 0, "Normal"),
    Vaporeon(134, 0, "Water"),
    Jolteon(135, 0, "Electric"),
    Flareon(136, 0, "Fire"),
    Porygon(137, 0, "Normal"),
    Omanyte(138, 0, "Rock", "Water"),
    Omastar(139, 0, "Rock", "Water"),
    Kabuto(140, 0, "Rock", "Water"),
    Kabutops(141, 0, "Rock", "Water"),
    Aerodactyl(142, 0, "Rock", "Flying"),
    Snorlax(143, 0, "Normal"),
    Articuno(144, 0, "Ice", "Flying"),
    Zapdos(145, 0, "Electric", "Flying"),
    Moltres(146, 0, "Fire", "Flying"),
    Dratini(147, 0, "Dragon"),
    Dragonair(148, 0, "Dragon"),
    Dragonite(149, 0, "Dragon", "Flying"),
    Mewtwo(150, 0, "Psychic"),
    Mew(151, 0, "Psychic"),

    // Gen 2 (also known as best gen)
    Chikorita(152, 0, "Grass"),
    Bayleef(153, 0, "Grass"),
    Meganium(154, 0, "Grass"),
    Cyndaquil(155, 0, "Fire"),
    Quilava(156, 0, "Fire"),
    Typhlosion(157, 0, "Fire"),
    Totodile(158, 0, "Water"),
    Croconaw(159, 0, "Water"),
    Feraligatr(160, 0, "Water"),
    Sentret(161, 0, "Normal"),
    Furret(162, 0, "Normal"),
    Hoothoot(163, 0, "Normal", "Flying"),
    Noctowl(164, 0, "Normal", "Flying"),
    Ledyba(165, 0, "Bug", "Flying"),
    Ledian(166, 0, "Bug", "Flying"),
    Spinarak(167, 0, "Bug", "Poison"),
    Ariados(168, 0, "Bug", "Poison"),
    Crobat(169, 0, "Poison", "Flying"),
    Chinchou(170, 0, "Water", "Electric"),
    Lanturn(171, 0, "Water", "Electric"),
    Pichu(172, 0, "Electric"),
    Cleffa(173, 0, "Fairy"),
    Igglybuff(174, 0, "Normal", "Fairy"),
    Togepi(175, 0, "Fairy"),
    Togetic(176, 0, "Fairy", "Flying"),
    Natu(177, 0, "Psychic", "Flying"),
    Xatu(178, 0, "Psychic", "Flying"),
    Mareep(179, 0, "Electric"),
    Flaaffy(180, 0, "Electric"),
    Ampharos(181, 0, "Electric"),
    Bellossom(182, 0, "Grass"),
    Marill(183, 0, "Water", "Fairy"),
    Azumarill(184, 0, "Water", "Fairy"),
    Sudowoodo(185, 0, "Rock"),
    Politoed(186, 0, "Water"),
    Hoppip(187, 0, "Grass", "Flying"),
    Skiploom(188, 0, "Grass", "Flying"),
    Jumpluff(189, 0, "Grass", "Flying"),
    Aipom(190, 0, "Normal"),
    Sunkern(191, 0, "Grass"),
    Sunflora(192, 0, "Grass"),
    Yanma(193, 0, "Bug", "Flying"),
    Wooper(194, 0, "Water", "Ground"),
    Quagsire(195, 0, "Water", "Ground"),
    Espeon(196, 0, "Psychic"),
    Umbreon(197, 0, "Dark"),
    Murkrow(198, 0, "Dark", "Flying"),
    Slowking(199, 0, "Water", "Psychic"),
    Misdreavus(200, 0, "Ghost"),
    Unown(201, 0, "Psychic"),
    Wobbuffet(202, 0, "Psychic"),
    Girafarig(203, 0, "Normal", "Psychic"),
    Pineco(204, 0, "Bug"),
    Forretress(205, 0, "Bug", "Steel"),
    Dunsparce(206, 0, "Normal"),
    Gligar(207, 0, "Ground", "Flying"),
    Steelix(208, 0, "Steel", "Ground"),
    Snubbull(209, 0, "Fairy"),
    Granbull(210, 0, "Fairy"),
    Qwilfish(211, 0, "Water", "Poison"),
    Scizor(212, 0, "Bug", "Steel"),
    Shuckle(213, 0, "Bug", "Rock"),
    Heracross(214, 0, "Bug", "Fighting"),
    Sneasel(215, 0, "Dark", "Ice"),
    Teddiursa(216, 0, "Normal"),
    Ursaring(217, 0, "Normal"),
    Slugma(218, 0, "Fire"),
    Magcargo(219, 0, "Fire", "Rock"),
    Swinub(220, 0, "Ice", "Ground"),
    Piloswine(221, 0, "Ice", "Ground"),
    Corsola(222, 0, "Water", "Rock"),
    Remoraid(223, 0, "Water"),
    Octillery(224, 0, "Water"),
    Delibird(225, 0, "Ice", "Flying"),
    Mantine(226, 0, "Water", "Flying"),
    Skarmory(227, 0, "Steel", "Flying"),
    Houndour(228, 0, "Dark", "Fire"),
    Houndoom(229, 0, "Dark", "Fire"),
    Kingdra(230, 0, "Water", "Dragon"),
    Phanpy(231, 0, "Ground"),
    Donphan(232, 0, "Ground"),
    Porygon2(233, 0, "Normal"),
    Stantler(234, 0, "Normal"),
    Smeargle(235, 0, "Normal"),
    Tyrogue(236, 0, "Fighting"),
    Hitmontop(237, 0, "Fighting"),
    Smoochum(238, 0, "Ice", "Psychic"),
    Elekid(239, 0, "Electric"),
    Magby(240, 0, "Fire"),
    Miltank(241, 0, "Normal"),
    Blissey(242, 0, "Normal"),
    Raikou(243, 0, "Electric"),
    Entei(244, 0, "Fire"),
    Suicune(245, 0, "Water"),
    Larvitar(246, 0, "Rock", "Ground"),
    Pupitar(247, 0, "Rock", "Ground"),
    Tyranitar(248, 0, "Rock", "Dark"),
    Lugia(249, 0, "Psychic", "Flying"),
    HoOh(250, 0, "Fire", "Flying"),
    Celebi(251, 0, "Psychic", "Grass"),

    // Gen 3
    Treecko(252, 0, "Grass"),
    Grovyle(253, 0, "Grass"),
    Sceptile(254, 0, "Grass"),
    Torchic(255, 0, "Fire"),
    Combusken(256, 0, "Fire", "Fighting"),
    Blaziken(257, 0, "Fire", "Fighting"),
    Mudkip(258, 0, "Water"),
    Marshtomp(259, 0, "Water", "Ground"),
    Swampert(260, 0, "Water", "Ground"),
    Poochyena(261, 0, "Dark"),
    Mightyena(262, 0, "Dark"),
    Zigzagoon(263, 0, "Normal"),
    Linoone(264, 0, "Normal"),
    Wurmple(265, 0, "Bug"),
    Silcoon(266, 0, "Bug"),
    Beautifly(267, 0, "Bug", "Flying"),
    Cascoon(268, 0, "Bug"),
    Dustox(269, 0, "Bug", "Poison"),
    Lotad(270, 0, "Water", "Grass"),
    Lombre(271, 0, "Water", "Grass"),
    Ludicolo(272, 0, "Water", "Grass"),
    Seedot(273, 0, "Grass"),
    Nuzleaf(274, 0, "Grass", "Dark"),
    Shiftry(275, 0, "Grass", "Dark"),
    Taillow(276, 0, "Normal", "Flying"),
    Swellow(277, 0, "Normal", "Flying"),
    Wingull(278, 0, "Water", "Flying"),
    Pelipper(279, 0, "Water", "Flying"),
    Ralts(280, 0, "Psychic", "Fairy"),
    Kirlia(281, 0, "Psychic", "Fairy"),
    Gardevoir(282, 0, "Psychic", "Fairy"),
    Surskit(283, 0, "Bug", "Water"),
    Masquerain(284, 0, "Bug", "Flying"),
    Shroomish(285, 0, "Grass"),
    Breloom(286, 0, "Grass", "Fighting"),
    Slakoth(287, 0, "Normal"),
    Vigoroth(288, 0, "Normal"),
    Slaking(289, 0, "Normal"),
    Nincada(290, 0, "Bug", "Ground"),
    Ninjask(291, 0, "Bug", "Flying"),
    Shedinja(292, 0, "Bug", "Ghost"),
    Whismur(293, 0, "Normal"),
    Loudred(294, 0, "Normal"),
    Exploud(295, 0, "Normal"),
    Makuhita(296, 0, "Fighting"),
    Hariyama(297, 0, "Fighting"),
    Azurill(298, 0, "Normal", "Fairy"),
    Nosepass(299, 0, "Rock"),
    Skitty(300, 0, "Normal"),
    Delcatty(301, 0, "Normal"),
    Sableye(302, 0, "Dark", "Ghost"),
    Mawile(303, 0, "Steel", "Fairy"),
    Aron(304, 0, "Steel", "Rock"),
    Lairon(305, 0, "Steel", "Rock"),
    Aggron(306, 0, "Steel", "Rock"),
    Meditite(307, 0, "Fighting", "Psychic"),
    Medicham(308, 0, "Fighting", "Psychic"),
    Electrike(309, 0, "Electric"),
    Manectric(310, 0, "Electric"),
    Plusle(311, 0, "Electric"),
    Minun(312, 0, "Electric"),
    Volbeat(313, 0, "Bug"),
    Illumise(314, 0, "Bug"),
    Roselia(315, 0, "Grass", "Poison"),
    Gulpin(316, 0, "Poison"),
    Swalot(317, 0, "Poison"),
    Carvanha(318, 0, "Water", "Dark"),
    Sharpedo(319, 0, "Water", "Dark"),
    Wailmer(320, 0, "Water"),
    Wailord(321, 0, "Water"),
    Numel(322, 0, "Fire", "Ground"),
    Camerupt(323, 0, "Fire", "Ground"),
    Torkoal(324, 0, "Fire"),
    Spoink(325, 0, "Psychic"),
    Grumpig(326, 0, "Psychic"),
    Spinda(327, 0, "Normal"),
    Trapinch(328, 0, "Ground"),
    Vibrava(329, 0, "Ground", "Dragon"),
    Flygon(330, 0, "Ground", "Dragon"),
    Cacnea(331, 0, "Grass"),
    Cacturne(332, 0, "Grass", "Dark"),
    Swablu(333, 0, "Normal", "Flying"),
    Altaria(334, 0, "Dragon", "Flying"),
    Zangoose(335, 0, "Normal"),
    Seviper(336, 0, "Poison"),
    Lunatone(337, 0, "Rock", "Psychic"),
    Solrock(338, 0, "Rock", "Psychic"), // Praise the sun!
    Barboach(339, 0, "Water", "Ground"),
    Whiscash(340, 0, "Water", "Ground"),
    Corphish(341, 0, "Water"),
    Crawdaunt(342, 0, "Water", "Dark"),
    Baltoy(343, 0, "Ground", "Psychic"),
    Claydol(344, 0, "Ground", "Psychic"),
    Lileep(345, 0, "Rock", "Grass"),
    Cradily(346, 0, "Rock", "Grass"),
    Anorith(347, 0, "Rock", "Bug"),
    Armaldo(348, 0, "Rock", "Bug"),
    Feebas(349, 0, "Water"),
    Milotic(350, 0, "Water"),
    Castform(351, 0, "Normal"),
    Kecleon(352, 0, "Normal"),
    Shuppet(353, 0, "Ghost"),
    Banette(354, 0, "Ghost"),
    Duskull(355, 0, "Ghost"),
    Dusclops(356, 0, "Ghost"),
    Tropius(357, 0, "Grass", "Flying"),
    Chimecho(358, 0, "Psychic"),
    Absol(359, 0, "Dark"),
    Wynaut(360, 0, "Psychic"), // Why?
    Snorunt(361, 0, "Ice"),
    Glalie(362, 0, "Ice"),
    Spheal(363, 0, "Ice", "Water"),
    Sealeo(364, 0, "Ice", "Water"),
    Walrein(365, 0, "Ice", "Water"),
    Clamperl(366, 0, "Water"),
    Huntail(367, 0, "Water"),
    Gorebyss(368, 0, "Water"),
    Relicanth(369, 0, "Water", "Rock"),
    Luvdisc(370, 0, "Water"),
    Bagon(371, 0, "Dragon"),
    Shelgon(372, 0, "Dragon"),
    Salamence(373, 0, "Dragon", "Flying"),
    Beldum(374, 0, "Steel", "Psychic"),
    Metang(375, 0, "Steel", "Psychic"),
    Metagross(376, 0, "Steel", "Psychic"),
    Regirock(377, 0, "Rock"),
    Regice(378, 0, "Ice"),
    Registeel(379, 0, "Steel"),
    Latias(380, 0, "Dragon", "Psychic"),
    Latios(381, 0, "Dragon", "Psychic"),
    Kyogre(382, 0, "Water"),
    Groudon(383, 0, "Ground"),
    Rayquaza(384, 0, "Dragon", "Flying"),
    Jirachi(385, 0, "Steel", "Psychic"),
    Deoxys(386, 0, "Psychic"),

    // Gen 4
    Turtwig(387, 0, "Grass"),
    Grotle(388, 0, "Grass"),
    Torterra(389, 0, "Grass", "Ground"),
    Chimchar(390, 0, "Fire"),
    Monferno(391, 0, "Fire", "Fighting"),
    Infernape(392, 0, "Fire", "Fighting"),
    Piplup(393, 0, "Water"),
    Prinplup(394, 0, "Water"),
    Empoleon(395, 0, "Water", "Steel"),
    Starly(396, 0, "Normal", "Flying"),
    Staravia(397, 0, "Normal", "Flying"),
    Staraptor(398, 0, "Normal", "Flying"),
    Bidoof(399, 0, "Normal"),
    Bibarel(400, 0, "Normal", "Water"),
    Kricketot(401, 0, "Bug"),
    Kricketune(402, 0, "Bug"),
    Shinx(403, 0, "Electric"),
    Luxio(404, 0, "Electric"),
    Luxray(405, 0, "Electric"),
    Budew(406, 0, "Grass", "Poison"),
    Roserade(407, 0, "Grass", "Poison"),
    Cranidos(408, 0, "Rock"),
    Rampardos(409, 0, "Rock"),
    Shieldon(410, 0, "Rock", "Steel"),
    Bastiodon(411, 0, "Rock", "Steel"),
    Burmy(412, 0, "Bug"),
    Wormadam(413, 0, "Bug", "Grass"),
    Mothim(414, 0, "Bug", "Flying"),
    Combee(415, 0, "Bug", "Flying"),
    Vespiquen(416, 0, "Bug", "Flying"),
    Pachirisu(417, 0, "Electric"),
    Buizel(418, 0, "Water"),
    Floatzel(419, 0, "Water"),
    Cherubi(420, 0, "Grass"), // yeah man
    Cherrim(421, 0, "Grass"),
    Shellos(422, 0, "Water"),
    Gastrodon(423, 0, "Water", "Ground"),
    Ambipom(424, 0, "Normal"),
    Drifloon(425, 0, "Ghost", "Flying"),
    Drifblim(426, 0, "Ghost", "Flying"),
    Buneary(427, 0, "Normal"),
    Lopunny(428, 0, "Normal"),
    Mismagius(429, 0, "Ghost"),
    Honchkrow(430, 0, "Dark", "Flying"),
    Glameow(431, 0, "Normal"),
    Purugly(432, 0, "Normal"),
    Chingling(433, 0, "Psychic"),
    Stunky(434, 0, "Poison", "Dark"),
    Skuntank(435, 0, "Poison", "Dark"),
    Bronzor(436, 0, "Steel", "Psychic"),
    Bronzong(437, 0, "Steel", "Psychic"),
    Bonsly(438, 0, "Rock"),
    MimeJr(439, 0, "Psychic", "Fairy"),
    Happiny(440, 0, "Normal"),
    Chatot(441, 0, "Normal", "Flying"),
    Spiritomb(442, 0, "Ghost", "Dark"),
    Gible(443, 0, "Dragon", "Ground"),
    Gabite(444, 0, "Dragon", "Ground"),
    Garchomp(445, 0, "Dragon", "Ground"),
    Munchlax(446, 0, "Normal"),
    Riolu(447, 0, "Fighting"),
    Lucario(448, 0, "Fighting", "Steel"),
    Hippopotas(449, 0, "Ground"),
    Hippowdon(450, 0, "Ground"),
    Skorupi(451, 0, "Poison", "Bug"),
    Drapion(452, 0, "Poison", "Dark"),
    Croagunk(453, 0, "Poison", "Fighting"),
    Toxicroak(454, 0, "Poison", "Fighting"),
    Carnivine(455, 0, "Grass"),
    Finneon(456, 0, "Water"),
    Lumineon(457, 0, "Water"),
    Mantyke(458, 0, "Water", "Flying"),
    Snover(459, 0, "Grass", "Ice"),
    Abomasnow(460, 0, "Grass", "Ice"),
    Weavile(461, 0, "Dark", "Ice"),
    Magnezone(462, 0, "Electric", "Steel"),
    Lickilicky(463, 0, "Normal"),
    Rhyperior(464, 0, "Ground", "Rock"),
    Tangrowth(465, 0, "Grass"),
    Electivire(466, 0, "Electric"),
    Magmortar(467, 0, "Fire"),
    Togekiss(468, 0, "Fairy", "Flying"),
    Yanmega(469, 0, "Bug", "Flying"),
    Leafeon(470, 0, "Grass"),
    Glaceon(471, 0, "Ice"),
    Gliscor(472, 0, "Ground", "Flying"),
    Mamoswine(473, 0, "Ice", "Ground"),
    PorygonZ(474, 0, "Normal"),
    Gallade(475, 0, "Psychic", "Fighting"),
    Probopass(476, 0, "Rock", "Steel"),
    Dusknoir(477, 0, "Ghost"),
    Froslass(478, 0, "Ice", "Ghost"),
    Rotom(479, 0, "Electric", "Ghost"),
    Uxie(480, 0, "Psychic"),
    Mesprit(481, 0, "Psychic"),
    Azelf(482, 0, "Psychic"),
    Dialga(483, 0, "Steel", "Dragon"),
    Palkia(484, 0, "Water", "Dragon"),
    Heatran(485, 0, "Fire", "Steel"),
    Regigigas(486, 0, "Normal"),
    Giratina(487, 0, "Ghost", "Dragon"),
    Cresselia(488, 0, "Psychic"),
    Phione(489, 0, "Water"),
    Manaphy(490, 0, "Water"),
    Darkrai(491, 0, "Dark"),
    Shaymin(492, 0, "Grass"),
    Arceus(493, 0, "Normal"),

    // Gen 5
    Victini(494, 0, "Psychic", "Fire"),
    Snivy(495, 0, "Grass"),
    Servine(496, 0, "Grass"),
    Serperior(497, 0, "Grass"),
    Tepig(498, 0, "Fire"),
    Pignite(499, 0, "Fire", "Fighting"),
    Emboar(500, 0, "Fire", "Fighting"),
    Oshawott(501, 0, "Water"),
    Dewott(502, 0, "Water"),
    Samurott(503, 0, "Water"),
    Patrat(504, 0, "Normal"),
    Watchog(505, 0, "Normal"),
    Lillipup(506, 0, "Normal"),
    Herdier(507, 0, "Normal"),
    Stoutland(508, 0, "Normal"),
    Purrloin(509, 0, "Dark"),
    Liepard(510, 0, "Dark"),
    Pansage(511, 0, "Grass"),
    Simisage(512, 0, "Grass"),
    Pansear(513, 0, "Fire"),
    Simisear(514, 0, "Fire"),
    Panpour(515, 0, "Water"),
    Simipour(516, 0, "Water"),
    Munna(517, 0, "Psychic"),
    Musharna(518, 0, "Psychic"),
    Pidove(519, 0, "Normal", "Flying"),
    Tranquill(520, 0, "Normal", "Flying"),
    Unfezant(521, 0, "Normal", "Flying"),
    Blitzle(522, 0, "Electric"),
    Zebstrika(523, 0, "Electric"),
    Roggenrola(524, 0, "Rock"),
    Boldore(525, 0, "Rock"),
    Gigalith(526, 0, "Rock"),
    Woobat(527, 0, "Psychic", "Flying"),
    Swoobat(528, 0, "Psychic", "Flying"),
    Drilbur(529, 0, "Ground"),
    Excadrill(530, 0, "Ground", "Steel"),
    Audino(531, 0, "Normal"),
    Timburr(532, 0, "Fighting"),
    Gurdurr(533, 0, "Fighting"),
    Conkeldurr(534, 0, "Fighting"),
    Tympole(535, 0, "Water"),
    Palpitoad(536, 0, "Water", "Ground"),
    Seismitoad(537, 0, "Water", "Ground"),
    Throh(538, 0, "Fighting"),
    Sawk(539, 0, "Fighting"),
    Sewaddle(540, 0, "Bug", "Grass"),
    Swadloon(541, 0, "Bug", "Grass"),
    Leavanny(542, 0, "Bug", "Grass"),
    Venipede(543, 0, "Bug", "Poison"),
    Whirlipede(544, 0, "Bug", "Poison"),
    Scolipede(545, 0, "Bug", "Poison"),
    Cottonee(546, 0, "Grass", "Fairy"),
    Whimsicott(547, 0, "Grass", "Fairy"),
    Petilil(548, 0, "Grass"),
    Lilligant(549, 0, "Grass"),
    Basculin(550, 0, "Water"),
    Sandile(551, 0, "Ground", "Dark"),
    Krokorok(552, 0, "Ground", "Dark"),
    Krookodile(553, 0, "Ground", "Dark"),
    Darumaka(554, 0, "Fire"),
    Darmanitan(555, 0, "Fire"),
    Maractus(556, 0, "Grass"),
    Dwebble(557, 0, "Bug", "Rock"),
    Crustle(558, 0, "Bug", "Rock"),
    Scraggy(559, 0, "Dark", "Fighting"),
    Scrafty(560, 0, "Dark", "Fighting"),
    Sigilyph(561, 0, "Psychic", "Flying"),
    Yamask(562, 0, "Ghost"),
    Cofagrigus(563, 0, "Ghost"),
    Tirtouga(564, 0, "Water", "Rock"),
    Carracosta(565, 0, "Water", "Rock"),
    Archen(566, 0, "Rock", "Flying"),
    Archeops(567, 0, "Rock", "Flying"),
    Trubbish(568, 0, "Poison"),
    Garbodor(569, 0, "Poison"),
    Zorua(570, 0, "Dark"),
    Zoroark(571, 0, "Dark"),
    Minccino(572, 0, "Normal"),
    Cinccino(573, 0, "Normal"),
    Gothita(574, 0, "Psychic"),
    Gothorita(575, 0, "Psychic"),
    Gothitelle(576, 0, "Psychic"),
    Solosis(577, 0, "Psychic"),
    Duosion(578, 0, "Psychic"),
    Reuniclus(579, 0, "Psychic"),
    Ducklett(580, 0, "Water", "Flying"),
    Swanna(581, 0, "Water", "Flying"),
    Vanillite(582, 0, "Ice"),
    Vanillish(583, 0, "Ice"),
    Vanilluxe(584, 0, "Ice"),
    Deerling(585, 0, "Normal", "Grass"),
    Sawsbuck(586, 0, "Normal", "Grass"),
    Emolga(587, 0, "Electric", "Flying"),
    Karrablast(588, 0, "Bug"),
    Escavalier(589, 0, "Bug", "Steel"),
    Foongus(590, 0, "Grass", "Poison"),
    Amoonguss(591, 0, "Grass", "Poison"),
    Frillish(592, 0, "Water", "Ghost"),
    Jellicent(593, 0, "Water", "Ghost"),
    Alomomola(594, 0, "Water"),
    Joltik(595, 0, "Bug", "Electric"),
    Galvantula(596, 0, "Bug", "Electric"),
    Ferroseed(597, 0, "Grass", "Steel"),
    Ferrothorn(598, 0, "Grass", "Steel"),
    Klink(599, 0, "Steel"),
    Klang(600, 0, "Steel"),
    Klinklang(601, 0, "Steel"),
    Tynamo(602, 0, "Electric"),
    Eelektrik(603, 0, "Electric"),
    Eelektross(604, 0, "Electric"),
    Elgyem(605, 0, "Psychic"),
    Beheeyem(606, 0, "Psychic"),
    Litwick(607, 0, "Ghost", "Fire"),
    Lampent(608, 0, "Ghost", "Fire"),
    Chandelure(609, 0, "Ghost", "Fire"),
    Axew(610, 0, "Dragon"),
    Fraxure(611, 0, "Dragon"),
    Haxorus(612, 0, "Dragon"),
    Cubchoo(613, 0, "Ice"),
    Beartic(614, 0, "Ice"),
    Cryogonal(615, 0, "Ice"),
    Shelmet(616, 0, "Bug"),
    Accelgor(617, 0, "Bug"),
    Stunfisk(618, 0, "Ground", "Electric"),
    Mienfoo(619, 0, "Fighting"),
    Mienshao(620, 0, "Fighting"),
    Druddigon(621, 0, "Dragon"),
    Golett(622, 0, "Ground", "Ghost"),
    Golurk(623, 0, "Ground", "Ghost"),
    Pawniard(624, 0, "Dark", "Steel"),
    Bisharp(625, 0, "Dark", "Steel"),
    Bouffalant(626, 0, "Normal"),
    Rufflet(627, 0, "Normal", "Flying"),
    Braviary(628, 0, "Normal", "Flying"),
    Vullaby(629, 0, "Dark", "Flying"),
    Mandibuzz(630, 0, "Dark", "Flying"),
    Heatmor(631, 0, "Fire"),
    Durant(632, 0, "Bug", "Steel"),
    Deino(633, 0, "Dark", "Dragon"),
    Zweilous(634, 0, "Dark", "Dragon"),
    Hydreigon(635, 0, "Dark", "Dragon"),
    Larvesta(636, 0, "Bug", "Fire"),
    Volcarona(637, 0, "Bug", "Fire"),
    Cobalion(638, 0, "Steel", "Fighting"),
    Terrakion(639, 0, "Rock", "Fighting"),
    Virizion(640, 0, "Grass", "Fighting"),
    Tornadus(641, 0, "Flying"),
    Thundurus(642, 0, "Electric", "Flying"),
    Reshiram(643, 0, "Dragon", "Fire"),
    Zekrom(644, 0, "Dragon", "Electric"),
    Landorus(645, 0, "Ground", "Flying"),
    Kyurem(646, 0, "Dragon", "Ice"),
    Keldeo(647, 0, "Water", "Fighting"),
    Meloetta(648, 0, "Normal", "Psychic"),
    Genesect(649, 0, "Bug", "Steel"),

    // Gen 6
    Chespin(650, 0, "Grass"),
    Quilladin(651, 0, "Grass"),
    Chesnaught(652, 0, "Grass", "Fighting"),
    Fennekin(653, 0, "Fire"),
    Braixen(654, 0, "Fire"),
    Delphox(655, 0, "Fire", "Psychic"),
    Froakie(656, 0, "Water"),
    Frogadier(657, 0, "Water"),
    Greninja(658, 0, "Water", "Dark"),
    Bunnelby(659, 0, "Normal"),
    Diggersby(660, 0, "Normal", "Ground"),
    Fletchling(661, 0, "Normal", "Flying"),
    Fletchinder(662, 0, "Fire", "Flying"),
    Talonflame(663, 0, "Fire", "Flying"),
    Scatterbug(664, 0, "Bug"),
    Spewpa(665, 0, "Bug"),
    Vivillon(666, 0, "Bug", "Flying"),
    Litleo(667, 0, "Fire", "Normal"),
    Pyroar(668, 0, "Fire", "Normal"),
    Flabebe(669, 0, "Fairy"),
    Floette(670, 0, "Fairy"),
    Florges(671, 0, "Fairy"),
    Skiddo(672, 0, "Grass"),
    Gogoat(673, 0, "Grass"),
    Pancham(674, 0, "Fighting"),
    Pangoro(675, 0, "Fighting", "Dark"),
    Furfrou(676, 0, "Normal"),
    Espurr(677, 0, "Psychic"),
    Meowstic(678, 0, "Psychic"),
    Honedge(679, 0, "Steel", "Ghost"),
    Doublade(680, 0, "Steel", "Ghost"),
    Aegislash(681, 0, "Steel", "Ghost"),
    Spritzee(682, 0, "Fairy"),
    Aromatisse(683, 0, "Fairy"),
    Swirlix(684, 0, "Fairy"),
    Slurpuff(685, 0, "Fairy"),
    Inkay(686, 0, "Dark", "Psychic"),
    Malamar(687, 0, "Dark", "Psychic"),
    Binacle(688, 0, "Rock", "Water"),
    Barbaracle(689, 0, "Rock", "Water"),
    Skrelp(690, 0, "Poison", "Water"),
    Dragalge(691, 0, "Poison", "Dragon"),
    Clauncher(692, 0, "Water"),
    Clawitzer(693, 0, "Water"),
    Helioptile(694, 0, "Electric", "Normal"),
    Heliolisk(695, 0, "Electric", "Normal"),
    Tyrunt(696, 0, "Rock", "Dragon"),
    Tyrantrum(697, 0, "Rock", "Dragon"),
    Amaura(698, 0, "Rock", "Ice"),
    Aurorus(699, 0, "Rock", "Ice"),
    Sylveon(700, 0, "Fairy"),
    Hawlucha(701, 0, "Fighting", "Flying"),
    Dedenne(702, 0, "Electric", "Fairy"),
    Carbink(703, 0, "Rock", "Fairy"),
    Goomy(704, 0, "Dragon"),
    Sliggoo(705, 0, "Dragon"),
    Goodra(706, 0, "Dragon"),
    Klefki(707, 0, "Steel", "Fairy"),
    Phantump(708, 0, "Ghost", "Grass"),
    Trevenant(709, 0, "Ghost", "Grass"),
    Pumpkaboo(710, 0, "Ghost", "Grass"),
    Gourgeist(711, 0, "Ghost", "Grass"),
    Bergmite(712, 0, "Ice"),
    Avalugg(713, 0, "Ice"),
    Noibat(714, 0, "Flying", "Dragon"),
    Noivern(715, 0, "Flying", "Dragon"),
    Xerneas(716, 0, "Fairy"),
    Yveltal(717, 0, "Dark", "Flying"),
    Zygarde(718, 0, "Dragon", "Ground"),
    Diancie(719, 0, "Rock", "Fairy"),
    Hoopa(720, 0, "Psychic", "Ghost"),
    Volcanion(721, 0, "Fire", "Water"),

    // Gen 7
    Rowlet(722, 0, "Grass", "Flying"),
    Dartrix(723, 0, "Grass", "Flying"),
    Decidueye(724, 0, "Grass", "Ghost"),
    Litten(725, 0, "Fire"),
    Torracat(726, 0, "Fire"),
    Incineroar(727, 0, "Fire", "Dark"),
    Popplio(728, 0, "Water"),
    Brionne(729, 0, "Water"),
    Primarina(730, 0, "Water", "Fairy"),
    Pikipek(731, 0, "Normal", "Flying"),
    Trumbeak(732, 0, "Normal", "Flying"),
    Toucannon(733, 0, "Normal", "Flying"),
    Yungoos(734, 0, "Normal"),
    Gumshoos(735, 0, "Normal"),
    Grubbin(736, 0, "Bug"),
    Charjabug(737, 0, "Bug", "Electric"),
    Vikavolt(738, 0, "Bug", "Electric"),
    Crabrawler(739, 0, "Fighting"),
    Crabominable(740, 0, "Fighting", "Ice"),
    Oricorio(741, 0, "Fire", "Flying"),
    Cutiefly(742, 0, "Bug", "Fairy"),
    Ribombee(743, 0, "Bug", "Fairy"),
    Rockruff(744, 0, "Rock"),
    Lycanroc(745, 0, "Rock"),
    Wishiwashi(746, 0, "Water"),
    Mareanie(747, 0, "Poison", "Water"),
    Toxapex(748, 0, "Poison", "Water"),
    Mudbray(749, 0, "Ground"),
    Mudsdale(750, 0, "Ground"),
    Dewpider(751, 0, "Water", "Bug"),
    Araquanid(752, 0, "Water", "Bug"),
    Fomantis(753, 0, "Grass"),
    Lurantis(754, 0, "Grass"),
    Morelull(755, 0, "Grass", "Fairy"),
    Shiinotic(756, 0, "Grass", "Fairy"),
    Salandit(757, 0, "Poison", "Fire"),
    Salazzle(758, 0, "Poison", "Fire"),
    Stufful(759, 0, "Normal", "Fighting"),
    Bewear(760, 0, "Normal", "Fighting"),
    Bounsweet(761, 0, "Grass"),
    Steenee(762, 0, "Grass"),
    Tsareena(763, 0, "Grass"),
    Comfey(764, 0, "Fairy"),
    Oranguru(765, 0, "Normal", "Psychic"),
    Passimian(766, 0, "Fighting"),
    Wimpod(767, 0, "Bug", "Water"),
    Golisopod(768, 0, "Bug", "Water"),
    Sandygast(769, 0, "Ghost", "Ground"),
    Palossand(770, 0, "Ghost", "Ground"),
    Pyukumuku(771, 0, "Water"),
    TypeNull(772, 0, "Normal"),
    Silvally(773, 0, "Normal"),
    Minior(774, 0, "Rock", "Flying"),
    Komala(775, 0, "Normal"),
    Turtonator(776, 0, "Fire", "Dragon"),
    Togedemaru(777, 0, "Electric", "Steel"),
    Mimikyu(778, 0, "Ghost", "Fairy"),
    Bruxish(779, 0, "Water", "Psychic"),
    Drampa(780, 0, "Normal", "Dragon"),
    Dhelmise(781, 0, "Ghost", "Grass"),
    JangmoO(782, 0, "Dragon"),
    HakamoO(783, 0, "Dragon", "Fighting"),
    KommoO(784, 0, "Dragon", "Fighting"),
    TapuKoko(785, 0, "Electric", "Fairy"),
    TapuLele(786, 0, "Psychic", "Fairy"),
    TapuBulu(787, 0, "Grass", "Fairy"),
    TapuFini(788, 0, "Water", "Fairy"),
    Cosmog(789, 0, "Psychic"),
    Cosmoem(790, 0, "Psychic"),
    Solgaleo(791, 0, "Psychic", "Steel"),
    Lunala(792, 0, "Psychic", "Ghost"),
    Nihilego(793, 0, "Rock", "Poison"),
    Buzzwole(794, 0, "Bug", "Fighting"),
    Pheromosa(795, 0, "Bug", "Fighting"),
    Xurkitree(796, 0, "Electric"),
    Celesteela(797, 0, "Steel", "Flying"),
    Kartana(798, 0, "Grass", "Steel"),
    Guzzlord(799, 0, "Dark", "Dragon"),
    Necrozma(800, 0, "Psychic"),
    Magearna(801, 0, "Steel", "Fairy"),
    Marshadow(802, 0, "Fighting", "Ghost"),
    Poipole(803, 0, "Poison"),
    Naganadel(804, 0, "Poison", "Dragon"),
    Stakataka(805, 0, "Rock", "Steel"),
    Blacephalon(806, 0, "Fire", "Ghost"),
    Zeraora(807, 0, "Electric"),

    // Forms with different types.
    CastformSnowy(351, 1, "Ice"),
    CastformRainy(351, 2, "Water"),
    CastformSunny(351, 3, "Fire"),
    RotomHeat(479, 1, "Electric", "Fire"),
    RotomWash(479, 2, "Electric", "Water"),
    RotomFrost(479, 3, "Electric", "Ice"),
    RotomFan(479, 4, "Electric", "Flying"),
    RotomMow(479, 5, "Electric", "Grass"),
    ArceusGrass(493, 1, "Grass"),
    ArceusFire(493, 2, "Fire"),
    ArceusWater(493, 3, "Water"),
    ArceusFlying(493, 4, "Flying"),
    ArceusBug(493, 5, "Bug"),
    ArceusPoison(493, 6, "Poison"),
    ArceusElectric(493, 7, "Electric"),
    ArceusPsychic(493, 8, "Psychic"),
    ArceusRock(493, 9, "Rock"),
    ArceusGround(493, 10, "Ground"),
    ArceusDark(493, 11, "Dark"),
    ArceusGhost(493, 12, "Ghost"),
    ArceusSteel(493, 13, "Steel"),
    ArceusFighting(493, 14, "Fighting"),
    ArceusIce(493, 15, "Ice"),
    ArceusDragon(493, 16, "Dragon"),
    ArceusFairy(493, 17, "Fairy"),
    HoopaUnbound(720, 1, "Psychic", "Dark"),
    OricorioBaile(741, 0, "Fire", "Flying"), // Only here for completion's sake.
    OricorioPomPom(741, 1, "Electric", "Flying"),
    OricorioPau(741, 2, "Psychic", "Flying"),
    OricorioSensu(741, 3, "Ghost", "Flying"),
    SilvallyGrass(773, 1, "Grass"),
    SilvallyFire(773, 2, "Fire"),
    SilvallyWater(773, 3, "Water"),
    SilvallyFlying(773, 4, "Flying"),
    SilvallyBug(773, 5, "Bug"),
    SilvallyPoison(773, 6, "Poison"),
    SilvallyElectric(773, 7, "Electric"),
    SilvallyPsychic(773, 8, "Psychic"),
    SilvallyRock(773, 9, "Rock"),
    SilvallyGround(773, 10, "Ground"),
    SilvallyDark(773, 11, "Dark"),
    SilvallyGhost(773, 12, "Ghost"),
    SilvallySteel(773, 13, "Steel"),
    SilvallyFighting(773, 14, "Fighting"),
    SilvallyIce(773, 15, "Ice"),
    SilvallyDragon(773, 16, "Dragon"),
    SilvallyFairy(773, 17, "Fairy"),

    // Forms with different types AND different EV yields.
    WormadamSandy(413, 1, "Bug", "Ground"),
    WormadamTrash(413, 2, "Bug", "Steel"),
    ShayminSky(492, 1, "Grass", "Flying"),
    DarmanitanZen(555, 1, "Fire", "Psychic"),
    MeloettaPirouette(648, 1, "Normal", "Fighting"),
    NecrozmaDuskMane(800, 1, "Psychic", "Steel"),
    NecrozmaDawnWings(800, 2, "Psychic", "Ghost"),
    NecrozmaUltra(800, 3, "Psychic", "Dragon"),

    // Forms with different EV yields.
    DeoxysAttack(386, 1, "Psychic"),
    DeoxysDefense(386, 2, "Psychic"),
    DeoxysSpeed(386, 3, "Psychic"),
    TornadusTherian(641, 1, "Flying"),
    ThundurusTherian(642, 1, "Electric", "Flying"),
    LandorusTherian(645, 1, "Ground", "Flying"),
    KyuremBlack(646, 1, "Dragon", "Ice"),
    KyuremWhite(646, 2, "Dragon", "Ice"),
    AegislashBlade(681, 1, "Steel", "Ghost"),
    MiniorCore(774, 1, "Rock", "Flying"), // Use form 1, everything >0 should be the same for our purposes.

    // Alolan Pokémon variants.
    // TODO: Passing form 1 here is dirty. All current Alolans use it, but a more resilient solution would be nice.
    RattataAlolan(19, 1, "Dark", "Normal"),
    RaticateAlolan(20, 1, "Dark", "Normal"),
    RaichuAlolan(26, 1, "Electric", "Psychic"),
    SandshrewAlolan(27, 1, "Ice", "Steel"),
    SandslashAlolan(28, 1, "Ice", "Steel"),
    VulpixAlolan(37, 1, "Ice"),
    NinetalesAlolan(38, 1, "Ice", "Fairy"),
    DiglettAlolan(50, 1, "Ground", "Steel"),
    DugtrioAlolan(51, 1, "Ground", "Steel"),
    MeowthAlolan(52, 1, "Dark"),
    PersianAlolan(53, 1, "Dark"),
    GeodudeAlolan(74, 1, "Rock", "Electric"),
    GravelerAlolan(75, 1, "Rock", "Electric"),
    GolemAlolan(76, 1, "Rock", "Electric"),
    GrimerAlolan(88, 1, "Poison", "Dark"),
    MukAlolan(89, 1, "Poison", "Dark"),
    ExeggutorAlolan(103, 1, "Grass", "Dragon"),
    MarowakAlolan(105, 1, "Fire", "Ghost"),
    ;

    // Set up some variables for the Pokémon check.
    public int index, form;
    public String type1, type2;

    PokemonMethods(final int index, final int form, final String... types)
    {
        this.index = index;
        this.form = form;
        this.type1 = types[0];
        this.type2 = types.length > 1 ? types[1] : null;
    }

    public static PokemonMethods getPokemonFromID(final int index)
    {
        final PokemonMethods[] values = values();
        final PokemonMethods pokemon = values[index - 1];

        if (pokemon != null)
            return values[index - 1];

        // If we don't find a Pokémon, do this.
        return null;
    }

    public static PokemonMethods getPokemonFromName(final String name)
    {
        final PokemonMethods[] allValues = values();

        for (final PokemonMethods pokemon : allValues)
        {
            if (pokemon.name().equalsIgnoreCase(name))
                return pokemon;
        }

        // If the loop does not find and return a Pokémon, do this.
        return null;
    }

    public static String getTitleWithIDAndFormName(final int id, final String name)
    {
        switch (name)
        {
            // Forms.
            case "CastformSunny":
                return "§1(§9#351§1) §6Sunny Castform";
            case "CastformRainy":
                return "§1(§9#351§1) §6Rainy Castform";
            case "CastformSnowy":
                return "§1(§9#351§1) §6Snowy Castform";
            case "WormadamSandy":
                return "§1(§9#413§1) §6Sandy Wormadam";
            case "WormadamTrash":
                return "§1(§9#413§1) §6Trashy Wormadam";
            case "RotomHeat":
                return "§1(§9#479§1) §6Microwave Rotom";
            case "RotomWash":
                return "§1(§9#479§1) §6Washer Rotom";
            case "RotomFrost":
                return "§1(§9#479§1) §6Fridge Rotom";
            case "RotomFan":
                return "§1(§9#479§1) §6Fan Rotom";
            case "RotomMow":
                return "§1(§9#479§1) §6Mower Rotom";
            case "ShayminSky":
                return "§1(§9#492§1) §6Sky Shaymin";
            case "ArceusDragon":
                return "§1(§9#492§1) §6Draco Plate Arceus";
            case "ArceusDark":
                return "§1(§9#492§1) §6Dread Plate Arceus";
            case "ArceusGround":
                return "§1(§9#492§1) §6Earth Plate Arceus";
            case "ArceusFighting":
                return "§1(§9#492§1) §6Fist Plate Arceus";
            case "ArceusFire":
                return "§1(§9#492§1) §6Flame Plate Arceus";
            case "ArceusIce":
                return "§1(§9#492§1) §6Icicle Plate Arceus";
            case "ArceusBug":
                return "§1(§9#492§1) §6Insect Plate Arceus";
            case "ArceusSteel":
                return "§1(§9#492§1) §6Iron Plate Arceus";
            case "ArceusGrass":
                return "§1(§9#492§1) §6Meadow Plate Arceus";
            case "ArceusPsychic":
                return "§1(§9#492§1) §6Mind Plate Arceus";
            case "ArceusFairy":
                return "§1(§9#492§1) §6Pixie Plate Arceus";
            case "ArceusFlying":
                return "§1(§9#492§1) §6Sky Plate Arceus";
            case "ArceusWater":
                return "§1(§9#492§1) §6Splash Plate Arceus";
            case "ArceusGhost":
                return "§1(§9#492§1) §6Spooky Plate Arceus";
            case "ArceusRock":
                return "§1(§9#492§1) §6Stone Plate Arceus";
            case "ArceusPoison":
                return "§1(§9#492§1) §6Toxic Plate Arceus";
            case "ArceusElectric":
                return "§1(§9#492§1) §6Zap Plate Arceus";
            case "DarmanitanZen":
                return "§1(§9#555§1) §6Zen Darmanitan";
            case "MeloettaPirouette":
                return "§1(§9#648§1) §6Pirouette Meloetta";
            case "HoopaUnbound":
                return "§1(§9#720§1) §6Unbound Hoopa";
            case "OricorioBaile":
                return "§1(§9#741§1) §6Baile Oricorio";
            case "OricorioPomPom":
                return "§1(§9#741§1) §6Pom Pom Oricorio";
            case "OricorioPau":
                return "§1(§9#741§1) §6Pa'u Oricorio";
            case "OricorioSensu":
                return "§1(§9#741§1) §6Sensu Oricorio";
            case "SilvallyBug":
                return "§1(§9#773§1) §6Bug Memory Silvally";
            case "SilvallyDark":
                return "§1(§9#773§1) §6Dark Memory Silvally";
            case "SilvallyDragon":
                return "§1(§9#773§1) §6Dragon Memory Silvally";
            case "SilvallyElectric":
                return "§1(§9#773§1) §6Electric Memory Silvally";
            case "SilvallyFairy":
                return "§1(§9#773§1) §6Fairy Memory Silvally";
            case "SilvallyFighting":
                return "§1(§9#773§1) §6Fighting Memory Silvally";
            case "SilvallyFire":
                return "§1(§9#773§1) §6Fire Memory Silvally";
            case "SilvallyFlying":
                return "§1(§9#773§1) §6Flying Memory Silvally";
            case "SilvallyGhost":
                return "§1(§9#773§1) §6Ghost Memory Silvally";
            case "SilvallyGrass":
                return "§1(§9#773§1) §6Grass Memory Silvally";
            case "SilvallyGround":
                return "§1(§9#773§1) §6Ground Memory Silvally";
            case "SilvallyIce":
                return "§1(§9#773§1) §6Ice Memory Silvally";
            case "SilvallyPoison":
                return "§1(§9#773§1) §6Poison Memory Silvally";
            case "SilvallyPsychic":
                return "§1(§9#773§1) §6Psychic Memory Silvally";
            case "SilvallyRock":
                return "§1(§9#773§1) §6Rock Memory Silvally";
            case "SilvallySteel":
                return "§1(§9#773§1) §6Steel Memory Silvally";
            case "SilvallyWater":
                return "§1(§9#773§1) §6Water Memory Silvally";
            case "NecrozmaDarkMane":
                return "§1(§9#800§1) §6Dark Mane Necrozma";
            case "NecrozmaDawnWings":
                return "§1(§9#800§1) §6Dawn Wings Necrozma";
            case "NecrozmaUltra":
                return "§1(§9#800§1) §6Ultra Necrozma";

            // Alolan variants.
            case "RattataAlolan":
                return "§1(§9#19§1) §6Alolan Rattata";
            case "RaticateAlolan":
                return "§1(§9#20§1) §6Alolan Raticate";
            case "RaichuAlolan":
                return "§1(§9#26§1) §6Alolan Raichu";
            case "SandshrewAlolan":
                return "§1(§9#27§1) §6Alolan Sandshrew";
            case "SandslashAlolan":
                return "§1(§9#28§1) §6Alolan Sandslash";
            case "VulpixAlolan":
                return "§1(§9#37§1) §6Alolan Vulpix";
            case "NinetalesAlolan":
                return "§1(§9#38§1) §6Alolan Ninetales";
            case "DiglettAlolan":
                return "§1(§9#50§1) §6Alolan Diglett";
            case "DugtrioAlolan":
                return "§1(§9#51§1) §6Alolan Dugtrio";
            case "MeowthAlolan":
                return "§1(§9#52§1) §6Alolan Meowth";
            case "PersianAlolan":
                return "§1(§9#53§1) §6Alolan Persian";
            case "GeodudeAlolan":
                return "§1(§9#74§1) §6Alolan Geodude";
            case "GravelerAlolan":
                return "§1(§9#75§1) §6Alolan Graveler";
            case "GolemAlolan":
                return "§1(§9#76§1) §6Alolan Golem";
            case "GrimerAlolan":
                return "§1(§9#88§1) §6Alolan Grimer";
            case "MukAlolan":
                return "§1(§9#89§1) §6Alolan Muk";
            case "ExeggutorAlolan":
                return "§1(§9#103§1) §6Alolan Exeggutor";
            case "MarowakAlolan":
                return "§1(§9#105§1) §6Alolan Marowak";

            // Pokémon with weird internal names due to technical issues.
            case "NidoranFemale":
                return "§1(§9#29§1) §6Nidoran♀";
            case "NidoranMale":
                return "§1(§9#32§1) §6Nidoran♂";
            case "Farfetchd":
                return "§1(§9#83§1) §6Farfetch'd";
            case "MrMime":
                return "§1(§9#122§1) §6Mr. Mime";
            case "HoOh":
                return "§1(§9#250§1) §6Ho-Oh";
            case "MimeJr":
                return "§1(§9#439§1) §6Mime Jr.";
            case "Flabebe":
                return "§1(§9#669§1) §6Flabébé";
            case "TypeNull":
                return "§1(§9#772§1) §6Type: Null";
            case "JangmoO":
                return "§1(§9#782§1) §6Jangmo-O";
            case "HakamoO":
                return "§1(§9#783§1) §6Hakamo-O";
            case "KommoO":
                return "§1(§9#784§1) §6Kommo-O";
            case "TapuKoko":
                return "§1(§9#784§1) §6Tapu Koko";
            case "TapuLele":
                return "§1(§9#784§1) §6Tapu Lele";
            case "TapuBulu":
                return "§1(§9#784§1) §6Tapu Bulu";
            case "TapuFini":
                return "§1(§9#784§1) §6Tapu Fini";

            // Pokémon is not special, print defaults.
            default:
                return "§1(§9#" + id + "§1) §6" + name;
        }
    }

    public static String getShorthand(final StatsType stat)
    {
        switch (stat)
        {
            case HP:
                return (shortenedHP == null) ? "ERROR" : shortenedHP;
            case Attack:
                return (shortenedAttack == null) ? "ERROR" : shortenedAttack;
            case Defence:
                return (shortenedDefense == null) ? "ERROR" : shortenedDefense;
            case SpecialAttack:
                return (shortenedSpecialAttack == null) ? "ERROR" : shortenedSpecialAttack;
            case SpecialDefence:
                return (shortenedSpecialDefense == null) ? "ERROR" : shortenedSpecialDefense;
            case Speed:
                return (shortenedSpeed == null) ? "ERROR" : shortenedSpeed;
            default:
                return "None"; // Hit when we have a stat without a shorthand. Shouldn't happen.
        }
    }

    public static String getGenderCharacter(final CommandSource src, final int genderNum)
    {
        // Console doesn't like these characters, so use letters.
        if (!(src instanceof Player))
        {
            switch (genderNum)
            {
                case 0: return "§3M";
                case 1: return "§5F";
                case 2: return "§7-";
                default: return "§8???";
            }
        }
        else
        {
            switch (genderNum)
            {
                case 0: return "§3♂";
                case 1: return "§5♀";
                case 2: return "§7⚥";
                default: return "§8???";
            }
        }
    }

    /*public static String getGrowthName(final int growthNum)
    {
        switch (growthNum)
        {
            case 0: return "Pygmy";
            case 1: return "Runt";
            case 2: return "Small";
            case 3: return "Ordinary";
            case 4: return "Huge";
            case 5: return "Giant";
            case 6: return "Enormous";
            case 7: return "§nGinormous§r"; // NOW with fancy underlining!
            case 8: return "§oMicroscopic§r"; // NOW with fancy italicization!
            default: return "?";
        }
    }

    public static List<String> getNatureStrings(final int natureNum)
    {
        final String natureName;
        final String plusVal;
        final String minusVal;

        switch (natureNum)
        {
            case 0:
                natureName = "Hardy";
                plusVal = "None";
                minusVal = "None";
                break;
            case 1:
                natureName = "Serious";
                plusVal = "None";
                minusVal = "None";
                break;
            case 2:
                natureName = "Docile";
                plusVal = "None";
                minusVal = "None";
                break;
            case 3:
                natureName = "Bashful";
                plusVal = "None";
                minusVal = "None";
                break;
            case 4:
                natureName = "Quirky";
                plusVal = "None";
                minusVal = "None";
                break;
            case 5:
                natureName = "Lonely";
                plusVal = "Atk";
                minusVal = "Def";
                break;
            case 6:
                natureName = "Brave";
                plusVal = "Atk";
                minusVal = shortenedSpeed;
                break;
            case 7:
                natureName = "Adamant";
                plusVal = "Atk";
                minusVal = shortenedSpecialAttack;
                break;
            case 8:
                natureName = "Naughty";
                plusVal = "Atk";
                minusVal = shortenedSpecialDefense;
                break;
            case 9:
                natureName = "Bold";
                plusVal = "Def";
                minusVal = "Atk";
                break;
            case 10:
                natureName = "Relaxed";
                plusVal = "Def";
                minusVal = shortenedSpeed;
                break;
            case 11:
                natureName = "Impish";
                plusVal = "Def";
                minusVal = shortenedSpecialAttack;
                break;
            case 12:
                natureName = "Lax";
                plusVal = "Def";
                minusVal = shortenedSpecialDefense;
                break;
            case 13:
                natureName = "Timid";
                plusVal = shortenedSpeed;
                minusVal = "Atk";
                break;
            case 14:
                natureName = "Hasty";
                plusVal = shortenedSpeed;
                minusVal = "Def";
                break;
            case 15:
                natureName = "Jolly";
                plusVal = shortenedSpeed;
                minusVal = shortenedSpecialAttack;
                break;
            case 16:
                natureName = "Naive";
                plusVal = shortenedSpeed;
                minusVal = shortenedSpecialDefense;
                break;
            case 17:
                natureName = "Modest";
                plusVal = shortenedSpecialAttack;
                minusVal = "Atk";
                break;
            case 18:
                natureName = "Mild";
                plusVal = shortenedSpecialAttack;
                minusVal = "Def";
                break;
            case 19:
                natureName = "Quiet";
                plusVal = shortenedSpecialAttack;
                minusVal = shortenedSpeed;
                break;
            case 20:
                natureName = "Rash";
                plusVal = shortenedSpecialAttack;
                minusVal = shortenedSpecialDefense;
                break;
            case 21:
                natureName = "Calm";
                plusVal = shortenedSpecialDefense;
                minusVal = "Atk";
                break;
            case 22:
                natureName = "Gentle";
                plusVal = shortenedSpecialDefense;
                minusVal = "Def";
                break;
            case 23:
                natureName = "Sassy";
                plusVal = shortenedSpecialDefense;
                minusVal = shortenedSpeed;
                break;
            case 24:
                natureName = "Careful";
                plusVal = shortenedSpecialDefense;
                minusVal = shortenedSpecialAttack;
                break;
            default:
                natureName = "ERROR! PLEASE REPORT!";
                plusVal = "?";
                minusVal = "?";
                break;
        }

        final List<String> returnString = new ArrayList<>();
        returnString.add(natureName);
        returnString.add(plusVal);
        returnString.add(minusVal);
        return returnString;
    }*/
}
