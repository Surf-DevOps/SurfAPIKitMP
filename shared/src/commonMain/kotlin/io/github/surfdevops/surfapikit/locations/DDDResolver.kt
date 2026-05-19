package io.github.surfdevops.surfapikit.locations

object DDDResolver {

    fun resolve(city: String?, state: String?): Int? {
        val normCity = city?.let { normalize(it) }
        val normState = state?.let { normalize(it) }

        normCity?.let { cityMap[it]?.let { d -> return d } }
        normState?.let { stateMap[it]?.let { d -> return d } }
        return null
    }

    private fun normalize(s: String): String {
        val lower = s.lowercase()
        val sb = StringBuilder()
        for (c in lower) {
            sb.append(
                when (c) {
                    'á','à','â','ã','ä' -> 'a'
                    'é','è','ê','ë' -> 'e'
                    'í','ì','î','ï' -> 'i'
                    'ó','ò','ô','õ','ö' -> 'o'
                    'ú','ù','û','ü' -> 'u'
                    'ç' -> 'c'
                    'ñ' -> 'n'
                    else -> c
                }
            )
        }
        return sb.toString()
    }

    private val cityMap: Map<String, Int> = mapOf(
        // DDD 11 - São Paulo (Capital e Grande São Paulo)
        "sao paulo" to 11, "guarulhos" to 11, "osasco" to 11, "santo andre" to 11,
        "sao bernardo do campo" to 11, "sao caetano do sul" to 11, "diadema" to 11,
        "maua" to 11, "jundiai" to 11, "mogi das cruzes" to 11, "suzano" to 11,
        "itaquaquecetuba" to 11, "ferraz de vasconcelos" to 11, "franco da rocha" to 11,
        "francisco morato" to 11, "caieiras" to 11, "mairipora" to 11, "aruja" to 11,
        "santa isabel" to 11,
        // DDD 12 - Vale do Paraíba (SP)
        "sao jose dos campos" to 12, "taubate" to 12, "jacarei" to 12, "guaratingueta" to 12,
        "lorena" to 12, "pindamonhangaba" to 12, "cruzeiro" to 12, "cachoeira paulista" to 12,
        "aparecida" to 12, "caraguatatuba" to 12, "ubatuba" to 12, "sao sebastiao" to 12,
        "ilhabela" to 12,
        // DDD 13 - Baixada Santista (SP)
        "santos" to 13, "sao vicente" to 13, "guaruja" to 13, "praia grande" to 13,
        "cubatao" to 13, "itanhaem" to 13, "mongagua" to 13, "peruibe" to 13, "bertioga" to 13,
        // DDD 14 - Bauru e região (SP)
        "bauru" to 14, "marilia" to 14, "botucatu" to 14, "jau" to 14, "lins" to 14,
        "ourinhos" to 14, "assis" to 14, "avare" to 14,
        // DDD 15 - Sorocaba e região (SP)
        "sorocaba" to 15, "itapetininga" to 15, "tatui" to 15, "itu" to 15, "salto" to 15,
        "indaiatuba" to 15, "votorantim" to 15,
        // DDD 16 - Ribeirão Preto e região (SP)
        "ribeirao preto" to 16, "franca" to 16, "sao carlos" to 16, "araraquara" to 16,
        "sertaozinho" to 16, "bebedouro" to 16, "jaboticabal" to 16,
        // DDD 17 - São José do Rio Preto e região (SP)
        "sao jose do rio preto" to 17, "catanduva" to 17, "votuporanga" to 17, "fernandopolis" to 17,
        // DDD 18 - Presidente Prudente e região (SP)
        "presidente prudente" to 18, "aracatuba" to 18, "birigui" to 18, "andradina" to 18,
        // DDD 19 - Campinas e região (SP)
        "campinas" to 19, "piracicaba" to 19, "limeira" to 19, "americana" to 19,
        "santa barbara d'oeste" to 19, "rio claro" to 19, "sumare" to 19, "hortolandia" to 19,
        "paulinia" to 19, "valinhos" to 19, "vinhedo" to 19,
        // DDD 21 - Rio de Janeiro (Capital e região metropolitana)
        "rio de janeiro" to 21, "niteroi" to 21, "nova iguacu" to 21, "duque de caxias" to 21,
        "sao goncalo" to 21, "belford roxo" to 21, "sao joao de meriti" to 21, "mage" to 21,
        "itaborai" to 21, "marica" to 21, "seropedica" to 21,
        // DDD 22 - Campos e região (RJ)
        "campos dos goytacazes" to 22, "nova friburgo" to 22, "cabo frio" to 22, "macae" to 22,
        "buzios" to 22, "casimiro de abreu" to 22,
        // DDD 24 - Petrópolis e região (RJ)
        "petropolis" to 24, "volta redonda" to 24, "barra mansa" to 24, "resende" to 24,
        "teresopolis" to 24,
        // DDD 27 - Espírito Santo
        "vitoria" to 27, "vila velha" to 27, "cariacica" to 27, "serra" to 27,
        "cachoeiro de itapemirim" to 27, "linhares" to 27,
        // DDD 28 - Interior do Espírito Santo
        "colatina" to 28,
        // DDD 31 - Belo Horizonte e região (MG)
        "belo horizonte" to 31, "contagem" to 31, "betim" to 31, "ribeirao das neves" to 31,
        "santa luzia" to 31, "nova lima" to 31, "ipatinga" to 31, "sete lagoas" to 31,
        // DDD 32 - Juiz de Fora e região (MG)
        "juiz de fora" to 32, "barbacena" to 32,
        // DDD 33 - Governador Valadares e região (MG)
        "governador valadares" to 33, "caratinga" to 33, "teofilo otoni" to 33,
        // DDD 34 - Triângulo Mineiro (MG)
        "uberlandia" to 34, "uberaba" to 34, "ituiutaba" to 34,
        // DDD 35 - Sul de Minas (MG)
        "varginha" to 35, "pouso alegre" to 35, "pocos de caldas" to 35, "lavras" to 35,
        // DDD 37 - Divinópolis e região (MG)
        "divinopolis" to 37, "formiga" to 37,
        // DDD 38 - Norte de Minas (MG)
        "montes claros" to 38, "januaria" to 38,
        // DDD 41 - Curitiba e região (PR)
        "curitiba" to 41, "sao jose dos pinhais" to 41, "colombo" to 41, "pinhais" to 41,
        "araucaria" to 41, "fazenda rio grande" to 41, "almirante tamandare" to 41,
        // DDD 42 - Ponta Grossa e região (PR)
        "ponta grossa" to 42, "guarapuava" to 42, "irati" to 42,
        // DDD 43 - Londrina e região (PR)
        "londrina" to 43, "sarandi" to 43, "apucarana" to 43,
        // DDD 44 - Maringá e região (PR)
        "maringa" to 44, "umuarama" to 44, "paranavai" to 44,
        // DDD 45 - Cascavel e região (PR)
        "cascavel" to 45, "foz do iguacu" to 45, "toledo" to 45,
        // DDD 46 - Francisco Beltrão e região (PR)
        "francisco beltrao" to 46, "pato branco" to 46,
        // DDD 47 - Joinville e região (SC)
        "joinville" to 47, "blumenau" to 47, "pomerode" to 47,
        // DDD 48 - Florianópolis e região (SC)
        "florianopolis" to 48, "sao jose" to 48, "palhoca" to 48, "biguacu" to 48,
        // DDD 49 - Chapecó e região (SC)
        "chapeco" to 49, "lages" to 49,
        // DDD 51 - Porto Alegre e região (RS)
        "porto alegre" to 51, "canoas" to 51, "novo hamburgo" to 51, "sao leopoldo" to 51,
        "alvorada" to 51, "gravatai" to 51, "viamao" to 51, "cachoeirinha" to 51,
        // DDD 53 - Pelotas e região (RS)
        "pelotas" to 53, "rio grande" to 53, "bage" to 53,
        // DDD 54 - Caxias do Sul e região (RS)
        "caxias do sul" to 54, "bento goncalves" to 54, "vacaria" to 54, "passo fundo" to 54,
        // DDD 55 - Santa Maria e região (RS)
        "santa maria" to 55, "uruguaiana" to 55,
        // DDD 61 - Distrito Federal e entorno
        "brasilia" to 61, "taguatinga" to 61, "ceilandia" to 61, "gama" to 61,
        "planaltina" to 61, "sobradinho" to 61,
        // DDD 62 - Goiânia e região (GO)
        "goiania" to 62, "aparecida de goiania" to 62, "anapolis" to 62,
        // DDD 63 - Tocantins
        "palmas" to 63,
        // DDD 64 - Rio Verde e região (GO)
        "rio verde" to 64,
        // DDD 65 - Cuiabá e região (MT)
        "cuiaba" to 65, "varzea grande" to 65,
        // DDD 66 - Rondonópolis e região (MT)
        "rondonopolis" to 66,
        // DDD 67 - Campo Grande e região (MS)
        "campo grande" to 67, "dourados" to 67,
        // DDD 68 - Acre
        "rio branco" to 68, "cruzeiro do sul" to 68,
        // DDD 69 - Rondônia
        "porto velho" to 69, "ji-parana" to 69,
        // DDD 71 - Salvador e região (BA)
        "salvador" to 71, "lauro de freitas" to 71, "camacari" to 71, "dias d'avila" to 71,
        "simoes filho" to 71,
        // DDD 73 - Itabuna e região (BA)
        "itabuna" to 73, "ilheus" to 73, "porto seguro" to 73,
        // DDD 74 - Juazeiro e região (BA)
        "juazeiro" to 74,
        // DDD 75 - Feira de Santana e região (BA)
        "feira de santana" to 75, "alagoinhas" to 75,
        // DDD 77 - Vitória da Conquista e região (BA)
        "vitoria da conquista" to 77,
        // DDD 79 - Sergipe
        "aracaju" to 79,
        // DDD 81 - Pernambuco
        "recife" to 81, "jaboatao dos guararapes" to 81, "olinda" to 81, "caruaru" to 81,
        // DDD 82 - Alagoas
        "maceio" to 82,
        // DDD 83 - Paraíba
        "joao pessoa" to 83, "campina grande" to 83,
        // DDD 84 - Rio Grande do Norte
        "natal" to 84, "mossoro" to 84,
        // DDD 85 - Ceará
        "fortaleza" to 85, "caucaia" to 85, "sobral" to 85,
        // DDD 86 - Piauí
        "teresina" to 86,
        // DDD 87 - Petrolina e região (PE)
        "petrolina" to 87,
        // DDD 88 - Interior do Ceará
        "juazeiro do norte" to 88, "crato" to 88,
        // DDD 89 - Picos e região (PI)
        "picos" to 89,
        // DDD 91 - Belém e região (PA)
        "belem" to 91, "ananindeua" to 91,
        // DDD 92 - Manaus e região (AM)
        "manaus" to 92,
        // DDD 93 - Santarém e região (PA)
        "santarem" to 93,
        // DDD 94 - Marabá e região (PA)
        "maraba" to 94,
        // DDD 95 - Roraima
        "boa vista" to 95,
        // DDD 96 - Amapá
        "macapa" to 96,
        // DDD 97 - Coari e região (AM)
        "coari" to 97,
        // DDD 98 - São Luís e região (MA)
        "sao luis" to 98, "imperatriz" to 98,
        // DDD 99 - Interior do Maranhão
        "caxias" to 99, "timon" to 99
    )

    private val stateMap: Map<String, Int> = mapOf(
        "acre" to 68,
        "alagoas" to 82,
        "amapa" to 96,
        "amazonas" to 92,
        "distrito federal" to 61,
        "para" to 91,
        "paraiba" to 83,
        "piaui" to 86,
        "rio grande do norte" to 84,
        "roraima" to 95,
        "rondonia" to 69,
        "sergipe" to 79,
        "tocantins" to 63,
        "mato grosso do sul" to 67
    )
}
