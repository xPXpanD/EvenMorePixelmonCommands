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
    Bulbasaur(1, "Grass, Poison"),
    Ivysaur(2, "Grass, Poison"),
    Venusaur(3, "Grass, Poison"),
    Charmander(4, "Fire"),
    Charmeleon(5, "Fire"),
    Charizard(6, "Fire, Flying"),
    Squirtle(7, "Water"),
    Wartortle(8, "Water"),
    Blastoise(9, "Water"),
    Caterpie(10, "Bug"),
    Metapod(11, "Bug"),
    Butterfree(12, "Bug, Flying"),
    Weedle(13, "Bug, Poison"),
    Kakuna(14, "Bug, Poison"),
    Beedrill(15, "Bug, Poison"),
    Pidgey(16, "Normal, Flying"),
    Pidgeotto(17, "Normal, Flying"),
    Pidgeot(18, "Normal, Flying"),
    Rattata(19, "Normal"),
    Raticate(20, "Normal"),
    Spearow(21, "Normal, Flying"),
    Fearow(22, "Normal, Flying"),
    Ekans(23, "Poison"),
    Arbok(24, "Poison"),
    Pikachu(25, "Electric"),
    Raichu(26, "Electric"),
    Sandshrew(27, "Ground"),
    Sandslash(28, "Ground"),
    NidoranFemale(29, "Poison"),
    Nidorina(30, "Poison"),
    Nidoqueen(31, "Poison, Ground"),
    NidoranMale(32, "Poison"),
    Nidorino(33, "Poison"),
    Nidoking(34, "Poison, Ground"),
    Clefairy(35, "Fairy"),
    Clefable(36, "Fairy"),
    Vulpix(37, "Fire"),
    Ninetales(38, "Fire"),
    Jigglypuff(39, "Normal, Fairy"),
    Wigglytuff(40, "Normal, Fairy"),
    Zubat(41, "Poison, Flying"),
    Golbat(42, "Poison, Flying"),
    Oddish(43, "Grass, Poison"),
    Gloom(44, "Grass, Poison"),
    Vileplume(45, "Grass, Poison"),
    Paras(46, "Bug, Grass"),
    Parasect(47, "Bug, Grass"),
    Venonat(48, "Bug, Poison"),
    Venomoth(49, "Bug, Poison"),
    Diglett(50, "Ground"),
    Dugtrio(51, "Ground"),
    Meowth(52, "Normal"),
    Persian(53, "Normal"),
    Psyduck(54, "Water"), // so tempted
    Golduck(55, "Water"),
    Mankey(56, "Fighting"),
    Primeape(57, "Fighting"),
    Growlithe(58, "Fire"),
    Arcanine(59, "Fire"),
    Poliwag(60, "Water"),
    Poliwhirl(61, "Water"),
    Poliwrath(62, "Water, Fighting"),
    Abra(63, "Psychic"),
    Kadabra(64, "Psychic"),
    Alakazam(65, "Psychic"),
    Machop(66, "Fighting"),
    Machoke(67, "Fighting"),
    Machamp(68, "Fighting"),
    Bellsprout(69, "Grass, Poison"),
    Weepinbell(70, "Grass, Poison"),
    Victreebel(71, "Grass, Poison"),
    Tentacool(72, "Water, Poison"),
    Tentacruel(73, "Water, Poison"),
    Geodude(74, "Rock, Ground"),
    Graveler(75, "Rock, Ground"),
    Golem(76, "Rock, Ground"),
    Ponyta(77, "Fire"),
    Rapidash(78, "Fire"),
    Slowpoke(79, "Water, Psychic"),
    Slowbro(80, "Water, Psychic"),
    Magnemite(81, "Electric, Steel"),
    Magneton(82, "Electric, Steel"),
    Farfetchd(83, "Normal, Flying"),
    Doduo(84, "Normal, Flying"),
    Dodrio(85, "Normal, Flying"),
    Seel(86, "Water"),
    Dewgong(87, "Water, Ice"),
    Grimer(88, "Poison"),
    Muk(89, "Poison"),
    Shellder(90, "Water"),
    Cloyster(91, "Water, Ice"),
    Gastly(92, "Ghost, Poison"),
    Haunter(93, "Ghost, Poison"),
    Gengar(94, "Ghost, Poison"),
    Onix(95, "Rock, Ground"),
    Drowzee(96, "Psychic"),
    Hypno(97, "Psychic"),
    Krabby(98, "Water"),
    Kingler(99, "Water"),
    Voltorb(100, "Electric"),
    Electrode(101, "Electric"),
    Exeggcute(102, "Grass, Psychic"),
    Exeggutor(103, "Grass, Psychic"),
    Cubone(104, "Ground"),
    Marowak(105, "Ground"),
    Hitmonlee(106, "Fighting"),
    Hitmonchan(107, "Fighting"),
    Lickitung(108, "Normal"),
    Koffing(109, "Poison"),
    Weezing(110, "Poison"),
    Rhyhorn(111, "Ground, Rock"),
    Rhydon(112, "Ground, Rock"),
    Chansey(113, "Normal"),
    Tangela(114, "Grass"),
    Kangaskhan(115, "Normal"),
    Horsea(116, "Water"),
    Seadra(117, "Water"),
    Goldeen(118, "Water"),
    Seaking(119, "Water"),
    Staryu(120, "Water"),
    Starmie(121, "Water, Psychic"),
    MrMime(122, "Psychic, Fairy"),
    Scyther(123, "Bug, Flying"),
    Jynx(124, "Ice, Psychic"),
    Electabuzz(125, "Electric"),
    Magmar(126, "Fire"),
    Pinsir(127, "Bug"),
    Tauros(128, "Normal"),
    Magikarp(129, "Water"),
    Gyarados(130, "Water, Flying"),
    Lapras(131, "Water, Ice"),
    Ditto(132, "Normal"),
    Eevee(133, "Normal"),
    Vaporeon(134, "Water"),
    Jolteon(135, "Electric"),
    Flareon(136, "Fire"),
    Porygon(137, "Normal"),
    Omanyte(138, "Rock, Water"),
    Omastar(139, "Rock, Water"),
    Kabuto(140, "Rock, Water"),
    Kabutops(141, "Rock, Water"),
    Aerodactyl(142, "Rock, Flying"),
    Snorlax(143, "Normal"),
    Articuno(144, "Ice, Flying"),
    Zapdos(145, "Electric, Flying"),
    Moltres(146, "Fire, Flying"),
    Dratini(147, "Dragon"),
    Dragonair(148, "Dragon"),
    Dragonite(149, "Dragon, Flying"),
    Mewtwo(150, "Psychic"),
    Mew(151, "Psychic"),

    // Gen 2 (also known as best gen)
    Chikorita(152, "Grass"),
    Bayleef(153, "Grass"),
    Meganium(154, "Grass"),
    Cyndaquil(155, "Fire"),
    Quilava(156, "Fire"),
    Typhlosion(157, "Fire"),
    Totodile(158, "Water"),
    Croconaw(159, "Water"),
    Feraligatr(160, "Water"),
    Sentret(161, "Normal"),
    Furret(162, "Normal"),
    Hoothoot(163, "Normal, Flying"),
    Noctowl(164, "Normal, Flying"),
    Ledyba(165, "Bug, Flying"),
    Ledian(166, "Bug, Flying"),
    Spinarak(167, "Bug, Poison"),
    Ariados(168, "Bug, Poison"),
    Crobat(169, "Poison, Flying"),
    Chinchou(170, "Water, Electric"),
    Lanturn(171, "Water, Electric"),
    Pichu(172, "Electric"),
    Cleffa(173, "Fairy"),
    Igglybuff(174, "Normal, Fairy"),
    Togepi(175, "Fairy"),
    Togetic(176, "Fairy, Flying"),
    Natu(177, "Psychic, Flying"),
    Xatu(178, "Psychic, Flying"),
    Mareep(179, "Electric"),
    Flaaffy(180, "Electric"),
    Ampharos(181, "Electric"),
    Bellossom(182, "Grass"),
    Marill(183, "Water, Fairy"),
    Azumarill(184, "Water, Fairy"),
    Sudowoodo(185, "Rock"),
    Politoed(186, "Water"),
    Hoppip(187, "Grass, Flying"),
    Skiploom(188, "Grass, Flying"),
    Jumpluff(189, "Grass, Flying"),
    Aipom(190, "Normal"),
    Sunkern(191, "Grass"),
    Sunflora(192, "Grass"),
    Yanma(193, "Bug, Flying"),
    Wooper(194, "Water, Ground"),
    Quagsire(195, "Water, Ground"),
    Espeon(196, "Psychic"),
    Umbreon(197, "Dark"),
    Murkrow(198, "Dark, Flying"),
    Slowking(199, "Water, Psychic"),
    Misdreavus(200, "Ghost"),
    Unown(201, "Psychic"),
    Wobbuffet(202, "Psychic"),
    Girafarig(203, "Normal, Psychic"),
    Pineco(204, "Bug"),
    Forretress(205, "Bug, Steel"),
    Dunsparce(206, "Normal"),
    Gligar(207, "Ground, Flying"),
    Steelix(208, "Steel, Ground"),
    Snubbull(209, "Fairy"),
    Granbull(210, "Fairy"),
    Qwilfish(211, "Water, Poison"),
    Scizor(212, "Bug, Steel"),
    Shuckle(213, "Bug, Rock"),
    Heracross(214, "Bug, Fighting"),
    Sneasel(215, "Dark, Ice"),
    Teddiursa(216, "Normal"),
    Ursaring(217, "Normal"),
    Slugma(218, "Fire"),
    Magcargo(219, "Fire, Rock"),
    Swinub(220, "Ice, Ground"),
    Piloswine(221, "Ice, Ground"),
    Corsola(222, "Water, Rock"),
    Remoraid(223, "Water"),
    Octillery(224, "Water"),
    Delibird(225, "Ice, Flying"),
    Mantine(226, "Water, Flying"),
    Skarmory(227, "Steel, Flying"),
    Houndour(228, "Dark, Fire"),
    Houndoom(229, "Dark, Fire"),
    Kingdra(230, "Water, Dragon"),
    Phanpy(231, "Ground"),
    Donphan(232, "Ground"),
    Porygon2(233, "Normal"),
    Stantler(234, "Normal"),
    Smeargle(235, "Normal"),
    Tyrogue(236, "Fighting"),
    Hitmontop(237, "Fighting"),
    Smoochum(238, "Ice, Psychic"),
    Elekid(239, "Electric"),
    Magby(240, "Fire"),
    Miltank(241, "Normal"),
    Blissey(242, "Normal"),
    Raikou(243, "Electric"),
    Entei(244, "Fire"),
    Suicune(245, "Water"),
    Larvitar(246, "Rock, Ground"),
    Pupitar(247, "Rock, Ground"),
    Tyranitar(248, "Rock, Dark"),
    Lugia(249, "Psychic, Flying"),
    HoOh(250, "Fire, Flying"),
    Celebi(251, "Psychic, Grass"),

    // Gen 3
    Treecko(252, "Grass"),
    Grovyle(253, "Grass"),
    Sceptile(254, "Grass"),
    Torchic(255, "Fire"),
    Combusken(256, "Fire, Fighting"),
    Blaziken(257, "Fire, Fighting"),
    Mudkip(258, "Water"),
    Marshtomp(259, "Water, Ground"),
    Swampert(260, "Water, Ground"),
    Poochyena(261, "Dark"),
    Mightyena(262, "Dark"),
    Zigzagoon(263, "Normal"),
    Linoone(264, "Normal"),
    Wurmple(265, "Bug"),
    Silcoon(266, "Bug"),
    Beautifly(267, "Bug, Flying"),
    Cascoon(268, "Bug"),
    Dustox(269, "Bug, Poison"),
    Lotad(270, "Water, Grass"),
    Lombre(271, "Water, Grass"),
    Ludicolo(272, "Water, Grass"),
    Seedot(273, "Grass"),
    Nuzleaf(274, "Grass, Dark"),
    Shiftry(275, "Grass, Dark"),
    Taillow(276, "Normal, Flying"),
    Swellow(277, "Normal, Flying"),
    Wingull(278, "Water, Flying"),
    Pelipper(279, "Water, Flying"),
    Ralts(280, "Psychic, Fairy"),
    Kirlia(281, "Psychic, Fairy"),
    Gardevoir(282, "Psychic, Fairy"),
    Surskit(283, "Bug, Water"),
    Masquerain(284, "Bug, Flying"),
    Shroomish(285, "Grass"),
    Breloom(286, "Grass, Fighting"),
    Slakoth(287, "Normal"),
    Vigoroth(288, "Normal"),
    Slaking(289, "Normal"),
    Nincada(290, "Bug, Ground"),
    Ninjask(291, "Bug, Flying"),
    Shedinja(292, "Bug, Ghost"),
    Whismur(293, "Normal"),
    Loudred(294, "Normal"),
    Exploud(295, "Normal"),
    Makuhita(296, "Fighting"),
    Hariyama(297, "Fighting"),
    Azurill(298, "Normal, Fairy"),
    Nosepass(299, "Rock"),
    Skitty(300, "Normal"),
    Delcatty(301, "Normal"),
    Sableye(302, "Dark, Ghost"),
    Mawile(303, "Steel, Fairy"),
    Aron(304, "Steel, Rock"),
    Lairon(305, "Steel, Rock"),
    Aggron(306, "Steel, Rock"),
    Meditite(307, "Fighting, Psychic"),
    Medicham(308, "Fighting, Psychic"),
    Electrike(309, "Electric"),
    Manectric(310, "Electric"),
    Plusle(311, "Electric"),
    Minun(312, "Electric"),
    Volbeat(313, "Bug"),
    Illumise(314, "Bug"),
    Roselia(315, "Grass, Poison"),
    Gulpin(316, "Poison"),
    Swalot(317, "Poison"),
    Carvanha(318, "Water, Dark"),
    Sharpedo(319, "Water, Dark"),
    Wailmer(320, "Water"),
    Wailord(321, "Water"),
    Numel(322, "Fire, Ground"),
    Camerupt(323, "Fire, Ground"),
    Torkoal(324, "Fire"),
    Spoink(325, "Psychic"),
    Grumpig(326, "Psychic"),
    Spinda(327, "Normal"),
    Trapinch(328, "Ground"),
    Vibrava(329, "Ground, Dragon"),
    Flygon(330, "Ground, Dragon"),
    Cacnea(331, "Grass"),
    Cacturne(332, "Grass, Dark"),
    Swablu(333, "Normal, Flying"),
    Altaria(334, "Dragon, Flying"),
    Zangoose(335, "Normal"),
    Seviper(336, "Poison"),
    Lunatone(337, "Rock, Psychic"),
    Solrock(338, "Rock, Psychic"), // Praise the sun!
    Barboach(339, "Water, Ground"),
    Whiscash(340, "Water, Ground"),
    Corphish(341, "Water"),
    Crawdaunt(342, "Water, Dark"),
    Baltoy(343, "Ground, Psychic"),
    Claydol(344, "Ground, Psychic"),
    Lileep(345, "Rock, Grass"),
    Cradily(346, "Rock, Grass"),
    Anorith(347, "Rock, Bug"),
    Armaldo(348, "Rock, Bug"),
    Feebas(349, "Water"),
    Milotic(350, "Water"),
    Castform(351, "Normal"),
    Kecleon(352, "Normal"),
    Shuppet(353, "Ghost"),
    Banette(354, "Ghost"),
    Duskull(355, "Ghost"),
    Dusclops(356, "Ghost"),
    Tropius(357, "Grass, Flying"),
    Chimecho(358, "Psychic"),
    Absol(359, "Dark"),
    Wynaut(360, "Psychic"), // Why?
    Snorunt(361, "Ice"),
    Glalie(362, "Ice"),
    Spheal(363, "Ice, Water"),
    Sealeo(364, "Ice, Water"),
    Walrein(365, "Ice, Water"),
    Clamperl(366, "Water"),
    Huntail(367, "Water"),
    Gorebyss(368, "Water"),
    Relicanth(369, "Water, Rock"),
    Luvdisc(370, "Water"),
    Bagon(371, "Dragon"),
    Shelgon(372, "Dragon"),
    Salamence(373, "Dragon, Flying"),
    Beldum(374, "Steel, Psychic"),
    Metang(375, "Steel, Psychic"),
    Metagross(376, "Steel, Psychic"),
    Regirock(377, "Rock"),
    Regice(378, "Ice"),
    Registeel(379, "Steel"),
    Latias(380, "Dragon, Psychic"),
    Latios(381, "Dragon, Psychic"),
    Kyogre(382, "Water"),
    Groudon(383, "Ground"),
    Rayquaza(384, "Dragon, Flying"),
    Jirachi(385, "Steel, Psychic"),
    Deoxys(386, "Psychic"),

    // Gen 4
    Turtwig(387, "Grass"),
    Grotle(388, "Grass"),
    Torterra(389, "Grass, Ground"),
    Chimchar(390, "Fire"),
    Monferno(391, "Fire, Fighting"),
    Infernape(392, "Fire, Fighting"),
    Piplup(393, "Water"),
    Prinplup(394, "Water"),
    Empoleon(395, "Water, Steel"),
    Starly(396, "Normal, Flying"),
    Staravia(397, "Normal, Flying"),
    Staraptor(398, "Normal, Flying"),
    Bidoof(399, "Normal"),
    Bibarel(400, "Normal, Water"),
    Kricketot(401, "Bug"),
    Kricketune(402, "Bug"),
    Shinx(403, "Electric"),
    Luxio(404, "Electric"),
    Luxray(405, "Electric"),
    Budew(406, "Grass, Poison"),
    Roserade(407, "Grass, Poison"),
    Cranidos(408, "Rock"),
    Rampardos(409, "Rock"),
    Shieldon(410, "Rock, Steel"),
    Bastiodon(411, "Rock, Steel"),
    Burmy(412, "Bug"),
    Wormadam(413, "Bug, Grass"),
    Mothim(414, "Bug, Flying"),
    Combee(415, "Bug, Flying"),
    Vespiquen(416, "Bug, Flying"),
    Pachirisu(417, "Electric"),
    Buizel(418, "Water"),
    Floatzel(419, "Water"),
    Cherubi(420, "Grass"), // yeah man
    Cherrim(421, "Grass"),
    Shellos(422, "Water"),
    Gastrodon(423, "Water, Ground"),
    Ambipom(424, "Normal"),
    Drifloon(425, "Ghost, Flying"),
    Drifblim(426, "Ghost, Flying"),
    Buneary(427, "Normal"),
    Lopunny(428, "Normal"),
    Mismagius(429, "Ghost"),
    Honchkrow(430, "Dark, Flying"),
    Glameow(431, "Normal"),
    Purugly(432, "Normal"),
    Chingling(433, "Psychic"),
    Stunky(434, "Poison, Dark"),
    Skuntank(435, "Poison, Dark"),
    Bronzor(436, "Steel, Psychic"),
    Bronzong(437, "Steel, Psychic"),
    Bonsly(438, "Rock"),
    MimeJr(439, "Psychic, Fairy"),
    Happiny(440, "Normal"),
    Chatot(441, "Normal, Flying"),
    Spiritomb(442, "Ghost, Dark"),
    Gible(443, "Dragon, Ground"),
    Gabite(444, "Dragon, Ground"),
    Garchomp(445, "Dragon, Ground"),
    Munchlax(446, "Normal"),
    Riolu(447, "Fighting"),
    Lucario(448, "Fighting, Steel"),
    Hippopotas(449, "Ground"),
    Hippowdon(450, "Ground"),
    Skorupi(451, "Poison, Bug"),
    Drapion(452, "Poison, Dark"),
    Croagunk(453, "Poison, Fighting"),
    Toxicroak(454, "Poison, Fighting"),
    Carnivine(455, "Grass"),
    Finneon(456, "Water"),
    Lumineon(457, "Water"),
    Mantyke(458, "Water, Flying"),
    Snover(459, "Grass, Ice"),
    Abomasnow(460, "Grass, Ice"),
    Weavile(461, "Dark, Ice"),
    Magnezone(462, "Electric, Steel"),
    Lickilicky(463, "Normal"),
    Rhyperior(464, "Ground, Rock"),
    Tangrowth(465, "Grass"),
    Electivire(466, "Electric"),
    Magmortar(467, "Fire"),
    Togekiss(468, "Fairy, Flying"),
    Yanmega(469, "Bug, Flying"),
    Leafeon(470, "Grass"),
    Glaceon(471, "Ice"),
    Gliscor(472, "Ground, Flying"),
    Mamoswine(473, "Ice, Ground"),
    PorygonZ(474, "Normal"),
    Gallade(475, "Psychic, Fighting"),
    Probopass(476, "Rock, Steel"),
    Dusknoir(477, "Ghost"),
    Froslass(478, "Ice, Ghost"),
    Rotom(479, "Electric, Ghost"),
    Uxie(480, "Psychic"),
    Mesprit(481, "Psychic"),
    Azelf(482, "Psychic"),
    Dialga(483, "Steel, Dragon"),
    Palkia(484, "Water, Dragon"),
    Heatran(485, "Fire, Steel"),
    Regigigas(486, "Normal"),
    Giratina(487, "Ghost, Dragon"),
    Cresselia(488, "Psychic"),
    Phione(489, "Water"),
    Manaphy(490, "Water"),
    Darkrai(491, "Dark"),
    Shaymin(492, "Grass"),
    Arceus(493, "Normal"),

    // Gen 5
    Victini(494, "Psychic, Fire"),
    Snivy(495, "Grass"),
    Servine(496, "Grass"),
    Serperior(497, "Grass"),
    Tepig(498, "Fire"),
    Pignite(499, "Fire, Fighting"),
    Emboar(500, "Fire, Fighting"),
    Oshawott(501, "Water"),
    Dewott(502, "Water"),
    Samurott(503, "Water"),
    Patrat(504, "Normal"),
    Watchog(505, "Normal"),
    Lillipup(506, "Normal"),
    Herdier(507, "Normal"),
    Stoutland(508, "Normal"),
    Purrloin(509, "Dark"),
    Liepard(510, "Dark"),
    Pansage(511, "Grass"),
    Simisage(512, "Grass"),
    Pansear(513, "Fire"),
    Simisear(514, "Fire"),
    Panpour(515, "Water"),
    Simipour(516, "Water"),
    Munna(517, "Psychic"),
    Musharna(518, "Psychic"),
    Pidove(519, "Normal, Flying"),
    Tranquill(520, "Normal, Flying"),
    Unfezant(521, "Normal, Flying"),
    Blitzle(522, "Electric"),
    Zebstrika(523, "Electric"),
    Roggenrola(524, "Rock"),
    Boldore(525, "Rock"),
    Gigalith(526, "Rock"),
    Woobat(527, "Psychic, Flying"),
    Swoobat(528, "Psychic, Flying"),
    Drilbur(529, "Ground"),
    Excadrill(530, "Ground, Steel"),
    Audino(531, "Normal"),
    Timburr(532, "Fighting"),
    Gurdurr(533, "Fighting"),
    Conkeldurr(534, "Fighting"),
    Tympole(535, "Water"),
    Palpitoad(536, "Water, Ground"),
    Seismitoad(537, "Water, Ground"),
    Throh(538, "Fighting"),
    Sawk(539, "Fighting"),
    Sewaddle(540, "Bug, Grass"),
    Swadloon(541, "Bug, Grass"),
    Leavanny(542, "Bug, Grass"),
    Venipede(543, "Bug, Poison"),
    Whirlipede(544, "Bug, Poison"),
    Scolipede(545, "Bug, Poison"),
    Cottonee(546, "Grass, Fairy"),
    Whimsicott(547, "Grass, Fairy"),
    Petilil(548, "Grass"),
    Lilligant(549, "Grass"),
    Basculin(550, "Water"),
    Sandile(551, "Ground, Dark"),
    Krokorok(552, "Ground, Dark"),
    Krookodile(553, "Ground, Dark"),
    Darumaka(554, "Fire"),
    Darmanitan(555, "Fire"),
    Maractus(556, "Grass"),
    Dwebble(557, "Bug, Rock"),
    Crustle(558, "Bug, Rock"),
    Scraggy(559, "Dark, Fighting"),
    Scrafty(560, "Dark, Fighting"),
    Sigilyph(561, "Psychic, Flying"),
    Yamask(562, "Ghost"),
    Cofagrigus(563, "Ghost"),
    Tirtouga(564, "Water, Rock"),
    Carracosta(565, "Water, Rock"),
    Archen(566, "Rock, Flying"),
    Archeops(567, "Rock, Flying"),
    Trubbish(568, "Poison"),
    Garbodor(569, "Poison"),
    Zorua(570, "Dark"),
    Zoroark(571, "Dark"),
    Minccino(572, "Normal"),
    Cinccino(573, "Normal"),
    Gothita(574, "Psychic"),
    Gothorita(575, "Psychic"),
    Gothitelle(576, "Psychic"),
    Solosis(577, "Psychic"),
    Duosion(578, "Psychic"),
    Reuniclus(579, "Psychic"),
    Ducklett(580, "Water, Flying"),
    Swanna(581, "Water, Flying"),
    Vanillite(582, "Ice"),
    Vanillish(583, "Ice"),
    Vanilluxe(584, "Ice"),
    Deerling(585, "Normal, Grass"),
    Sawsbuck(586, "Normal, Grass"),
    Emolga(587, "Electric, Flying"),
    Karrablast(588, "Bug"),
    Escavalier(589, "Bug, Steel"),
    Foongus(590, "Grass, Poison"),
    Amoonguss(591, "Grass, Poison"),
    Frillish(592, "Water, Ghost"),
    Jellicent(593, "Water, Ghost"),
    Alomomola(594, "Water"),
    Joltik(595, "Bug, Electric"),
    Galvantula(596, "Bug, Electric"),
    Ferroseed(597, "Grass, Steel"),
    Ferrothorn(598, "Grass, Steel"),
    Klink(599, "Steel"),
    Klang(600, "Steel"),
    Klinklang(601, "Steel"),
    Tynamo(602, "Electric"),
    Eelektrik(603, "Electric"),
    Eelektross(604, "Electric"),
    Elgyem(605, "Psychic"),
    Beheeyem(606, "Psychic"),
    Litwick(607, "Ghost, Fire"),
    Lampent(608, "Ghost, Fire"),
    Chandelure(609, "Ghost, Fire"),
    Axew(610, "Dragon"),
    Fraxure(611, "Dragon"),
    Haxorus(612, "Dragon"),
    Cubchoo(613, "Ice"),
    Beartic(614, "Ice"),
    Cryogonal(615, "Ice"),
    Shelmet(616, "Bug"),
    Accelgor(617, "Bug"),
    Stunfisk(618, "Ground, Electric"),
    Mienfoo(619, "Fighting"),
    Mienshao(620, "Fighting"),
    Druddigon(621, "Dragon"),
    Golett(622, "Ground, Ghost"),
    Golurk(623, "Ground, Ghost"),
    Pawniard(624, "Dark, Steel"),
    Bisharp(625, "Dark, Steel"),
    Bouffalant(626, "Normal"),
    Rufflet(627, "Normal, Flying"),
    Braviary(628, "Normal, Flying"),
    Vullaby(629, "Dark, Flying"),
    Mandibuzz(630, "Dark, Flying"),
    Heatmor(631, "Fire"),
    Durant(632, "Bug, Steel"),
    Deino(633, "Dark, Dragon"),
    Zweilous(634, "Dark, Dragon"),
    Hydreigon(635, "Dark, Dragon"),
    Larvesta(636, "Bug, Fire"),
    Volcarona(637, "Bug, Fire"),
    Cobalion(638, "Steel, Fighting"),
    Terrakion(639, "Rock, Fighting"),
    Virizion(640, "Grass, Fighting"),
    Tornadus(641, "Flying"),
    Thundurus(642, "Electric, Flying"),
    Reshiram(643, "Dragon, Fire"),
    Zekrom(644, "Dragon, Electric"),
    Landorus(645, "Ground, Flying"),
    Kyurem(646, "Dragon, Ice"),
    Keldeo(647, "Water, Fighting"),
    Meloetta(648, "Normal, Psychic"),
    Genesect(649, "Bug, Steel"),

    // Gen 6
    Chespin(650, "Grass"),
    Quilladin(651, "Grass"),
    Chesnaught(652, "Grass, Fighting"),
    Fennekin(653, "Fire"),
    Braixen(654, "Fire"),
    Delphox(655, "Fire, Psychic"),
    Froakie(656, "Water"),
    Frogadier(657, "Water"),
    Greninja(658, "Water, Dark"),
    Bunnelby(659, "Normal"),
    Diggersby(660, "Normal, Ground"),
    Fletchling(661, "Normal, Flying"),
    Fletchinder(662, "Fire, Flying"),
    Talonflame(663, "Fire, Flying"),
    Scatterbug(664, "Bug"),
    Spewpa(665, "Bug"),
    Vivillon(666, "Bug, Flying"),
    Litleo(667, "Fire, Normal"),
    Pyroar(668, "Fire, Normal"),
    Flabebe(669, "Fairy"),
    Floette(670, "Fairy"),
    Florges(671, "Fairy"),
    Skiddo(672, "Grass"),
    Gogoat(673, "Grass"),
    Pancham(674, "Fighting"),
    Pangoro(675, "Fighting, Dark"),
    Furfrou(676, "Normal"),
    Espurr(677, "Psychic"),
    Meowstic(678, "Psychic"),
    Honedge(679, "Steel, Ghost"),
    Doublade(680, "Steel, Ghost"),
    Aegislash(681, "Steel, Ghost"),
    Spritzee(682, "Fairy"),
    Aromatisse(683, "Fairy"),
    Swirlix(684, "Fairy"),
    Slurpuff(685, "Fairy"),
    Inkay(686, "Dark, Psychic"),
    Malamar(687, "Dark, Psychic"),
    Binacle(688, "Rock, Water"),
    Barbaracle(689, "Rock, Water"),
    Skrelp(690, "Poison, Water"),
    Dragalge(691, "Poison, Dragon"),
    Clauncher(692, "Water"),
    Clawitzer(693, "Water"),
    Helioptile(694, "Electric, Normal"),
    Heliolisk(695, "Electric, Normal"),
    Tyrunt(696, "Rock, Dragon"),
    Tyrantrum(697, "Rock, Dragon"),
    Amaura(698, "Rock, Ice"),
    Aurorus(699, "Rock, Ice"),
    Sylveon(700, "Fairy"),
    Hawlucha(701, "Fighting, Flying"),
    Dedenne(702, "Electric, Fairy"),
    Carbink(703, "Rock, Fairy"),
    Goomy(704, "Dragon"),
    Sliggoo(705, "Dragon"),
    Goodra(706, "Dragon"),
    Klefki(707, "Steel, Fairy"),
    Phantump(708, "Ghost, Grass"),
    Trevenant(709, "Ghost, Grass"),
    Pumpkaboo(710, "Ghost, Grass"),
    Gourgeist(711, "Ghost, Grass"),
    Bergmite(712, "Ice"),
    Avalugg(713, "Ice"),
    Noibat(714, "Flying, Dragon"),
    Noivern(715, "Flying, Dragon"),
    Xerneas(716, "Fairy"),
    Yveltal(717, "Dark, Flying"),
    Zygarde(718, "Dragon, Ground"),
    Diancie(719, "Rock, Fairy"),
    Hoopa(720, "Psychic, Ghost"),
    Volcanion(721, "Fire, Water"),

    // Gen 7
    Rowlet(722, "Grass, Flying"),
    Dartrix(723, "Grass, Flying"),
    Decidueye(724, "Grass, Ghost"),
    Litten(725, "Fire"),
    Torracat(726, "Fire"),
    Incineroar(727, "Fire, Dark"),
    Popplio(728, "Water"),
    Brionne(729, "Water"),
    Primarina(730, "Water, Fairy"),
    Pikipek(731, "Normal, Flying"),
    Trumbeak(732, "Normal, Flying"),
    Toucannon(733, "Normal, Flying"),
    Yungoos(734, "Normal"),
    Gumshoos(735, "Normal"),
    Grubbin(736, "Bug"),
    Charjabug(737, "Bug, Electric"),
    Vikavolt(738, "Bug, Electric"),
    Crabrawler(739, "Fighting"),
    Crabominable(740, "Fighting, Ice"),
    Oricorio(741, "Fire, Flying"),
    Cutiefly(742, "Bug, Fairy"),
    Ribombee(743, "Bug, Fairy"),
    Rockruff(744, "Rock"),
    Lycanroc(745, "Rock"),
    Wishiwashi(746, "Water"),
    Mareanie(747, "Poison, Water"),
    Toxapex(748, "Poison, Water"),
    Mudbray(749, "Ground"),
    Mudsdale(750, "Ground"),
    Dewpider(751, "Water, Bug"),
    Araquanid(752, "Water, Bug"),
    Fomantis(753, "Grass"),
    Lurantis(754, "Grass"),
    Morelull(755, "Grass, Fairy"),
    Shiinotic(756, "Grass, Fairy"),
    Salandit(757, "Poison, Fire"),
    Salazzle(758, "Poison, Fire"),
    Stufful(759, "Normal, Fighting"),
    Bewear(760, "Normal, Fighting"),
    Bounsweet(761, "Grass"),
    Steenee(762, "Grass"),
    Tsareena(763, "Grass"),
    Comfey(764, "Fairy"),
    Oranguru(765, "Normal, Psychic"),
    Passimian(766, "Fighting"),
    Wimpod(767, "Bug, Water"),
    Golisopod(768, "Bug, Water"),
    Sandygast(769, "Ghost, Ground"),
    Palossand(770, "Ghost, Ground"),
    Pyukumuku(771, "Water"),
    TypeNull(772, "Normal"),
    Silvally(773, "Normal"),
    Minior(774, "Rock, Flying"),
    Komala(775, "Normal"),
    Turtonator(776, "Fire, Dragon"),
    Togedemaru(777, "Electric, Steel"),
    Mimikyu(778, "Ghost, Fairy"),
    Bruxish(779, "Water, Psychic"),
    Drampa(780, "Normal, Dragon"),
    Dhelmise(781, "Ghost, Grass"),
    JangmoO(782, "Dragon"),
    HakamoO(783, "Dragon, Fighting"),
    KommoO(784, "Dragon, Fighting"),
    TapuKoko(785, "Electric, Fairy"),
    TapuLele(786, "Psychic, Fairy"),
    TapuBulu(787, "Grass, Fairy"),
    TapuFini(788, "Water, Fairy"),
    Cosmog(789, "Psychic"),
    Cosmoem(790, "Psychic"),
    Solgaleo(791, "Psychic, Steel"),
    Lunala(792, "Psychic, Ghost"),
    Nihilego(793, "Rock, Poison"),
    Buzzwole(794, "Bug, Fighting"),
    Pheromosa(795, "Bug, Fighting"),
    Xurkitree(796, "Electric"),
    Celesteela(797, "Steel, Flying"),
    Kartana(798, "Grass, Steel"),
    Guzzlord(799, "Dark, Dragon"),
    Necrozma(800, "Psychic"),
    Magearna(801, "Steel, Fairy"),
    Marshadow(802, "Fighting, Ghost"),
    Poipole(803, "Poison"),
    Naganadel(804, "Poison, Dragon"),
    Stakataka(805, "Rock, Steel"),
    Blacephalon(806, "Fire, Ghost"),
    Zeraora(807, "Electric"),

    // Forms. Be careful -- these can only be checked through their name, as /checktypes disallows checking 0.
    CastformSunny(0, "Fire"),
    CastformRainy(0, "Water"),
    CastformSnowy(0, "Ice"),
    WormadamSandy(0, "Bug, Ground"),
    WormadamTrash(0, "Bug, Steel"),
    RotomHeat(0, "Electric, Fire"),
    RotomWash(0, "Electric, Water"),
    RotomFrost(0, "Electric, Ice"),
    RotomFan(0, "Electric, Flying"),
    RotomMow(0, "Electric, Grass"),
    ShayminSky(0, "Grass, Flying"),
    DarmanitanZen(0, "Fire, Psychic"),
    MeloettaPirouette(0, "Normal, Fighting"),
    HoopaUnbound(0, "Psychic, Dark"),
    ArceusFire(0, "Fire"),
    ArceusWater(0, "Water"),
    ArceusElectric(0, "Electric"),
    ArceusGrass(0, "Grass"),
    ArceusIce(0, "Ice"),
    ArceusFighting(0, "Fighting"),
    ArceusPoison(0, "Poison"),
    ArceusGround(0, "Ground"),
    ArceusFlying(0, "Flying"),
    ArceusPsychic(0, "Psychic"),
    ArceusBug(0, "Bug"),
    ArceusRock(0, "Rock"),
    ArceusGhost(0, "Ghost"),
    ArceusDragon(0, "Dragon"),
    ArceusDark(0, "Dark"),
    ArceusSteel(0, "Steel"),
    ArceusFairy(0, "Fairy"),
    SilvallyFire(0, "Fire"),
    SilvallyWater(0, "Water"),
    SilvallyElectric(0, "Electric"),
    SilvallyGrass(0, "Grass"),
    SilvallyIce(0, "Ice"),
    SilvallyFighting(0, "Fighting"),
    SilvallyPoison(0, "Poison"),
    SilvallyGround(0, "Ground"),
    SilvallyFlying(0, "Flying"),
    SilvallyPsychic(0, "Psychic"),
    SilvallyBug(0, "Bug"),
    SilvallyRock(0, "Rock"),
    SilvallyGhost(0, "Ghost"),
    SilvallyDragon(0, "Dragon"),
    SilvallyDark(0, "Dark"),
    SilvallySteel(0, "Steel"),
    SilvallyFairy(0, "Fairy"),
    OricorioBaile(0, "Fire, Flying"),
    OricorioPomPom(0, "Electric, Flying"),
    OricorioPau(0, "Psychic, Flying"),
    OricorioSensu(0, "Ghost, Flying"),
    NecrozmaDuskMane(0, "Psychic, Steel"),
    NecrozmaDawnWings(0, "Psychic, Ghost"),
    NecrozmaUltra(0, "Psychic, Dragon"),

    // Alolan Pokémon variants. Same rules as above.
    RattataAlolan(0, "Dark, Normal"),
    RaticateAlolan(0, "Dark, Normal"),
    RaichuAlolan(0, "Electric, Psychic"),
    SandshrewAlolan(0, "Ice, Steel"),
    SandslashAlolan(0, "Ice, Steel"),
    VulpixAlolan(0, "Ice"),
    NinetalesAlolan(0, "Ice, Fairy"),
    DiglettAlolan(0, "Ground, Steel"),
    DugtrioAlolan(0, "Ground, Steel"),
    MeowthAlolan(0, "Dark"),
    PersianAlolan(0, "Dark"),
    GeodudeAlolan(0, "Rock, Electric"),
    GravelerAlolan(0, "Rock, Electric"),
    GolemAlolan(0, "Rock, Electric"),
    GrimerAlolan(0, "Poison, Dark"),
    MukAlolan(0, "Poison, Dark"),
    ExeggutorAlolan(0, "Grass, Dragon"),
    MarowakAlolan(0, "Fire, Ghost"),
    ;

    // Set up some variables for the Pokémon check.
    public int index;
    public String type1, type2;

    PokemonMethods(final int index, final String types)
    {
        this.index = index;
        final String[] delimitedTypes = types.split(", ");
        final int typeCount = delimitedTypes.length;

        if (typeCount == 2)
        {
            type1 = delimitedTypes[0];
            type2 = delimitedTypes[1];
        }
        else
        {
            type1 = delimitedTypes[0];
            type2 = "EMPTY";
        }
    }

    public static PokemonMethods getPokemonFromID(final int index)
    {
        final PokemonMethods[] values = values();
        final PokemonMethods pokemon = values[index - 1];

        if (pokemon != null)
            return values[index - 1];
        else
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
                return "None"; // Hit when we have a neutral nature.
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
